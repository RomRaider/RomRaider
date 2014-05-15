/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2014 RomRaider.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.romraider.logger.ecu.comms.io.connection;

import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.util.SettingsManager;
import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.protocol.ProtocolFactory;
import com.romraider.logger.ecu.comms.io.protocol.LoggerProtocolOBD;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.manager.PollingStateImpl;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.query.EcuQuery;

public final class OBDLoggerConnection implements LoggerConnection {
    private static final Logger LOGGER = getLogger(OBDLoggerConnection.class);
    private final LoggerProtocolOBD protocol;
    private final ConnectionManager manager;
    private Collection<EcuQuery> obdQueries = new ArrayList<EcuQuery>();

    public OBDLoggerConnection(ConnectionManager manager) {
        checkNotNull(manager, "manager");
        this.manager = manager;
        final Settings settings = SettingsManager.getSettings();
        this.protocol = (LoggerProtocolOBD) ProtocolFactory.getProtocol(
                settings.getLoggerProtocol(),
                settings.getTransportProtocol());
    }

    @Override
    public void ecuReset(byte id) {
        byte[] request = protocol.constructEcuResetRequest(id);
        LOGGER.debug(String.format("OBD Reset Request  ---> %s",
                asHex(request)));
        byte[] response = manager.send(request);
        byte[] processedResponse = protocol.preprocessResponse(
                request, response, new PollingStateImpl());
        LOGGER.debug(String.format("OBD Reset Response <--- %s",
                asHex(processedResponse)));
        protocol.processEcuResetResponse(processedResponse);
    }

    @Override
    // Build an init string similar to the SSM version so the logger definition
    // can reference supported parameters with ecubyte/bit attributes. 
    public void ecuInit(EcuInitCallback callback, byte id) {
        final byte[] processedResponse = new byte[46];
        final byte[] request = protocol.constructEcuInitRequest(id);
        LOGGER.debug(String.format("OBD Calibration ID Request  ---> %s",
                asHex(request)));
        final byte[] tmp = manager.send(request);
        final byte[] response = protocol.preprocessResponse(
                request, tmp, new PollingStateImpl());
        LOGGER.debug(String.format("OBD Calibration ID Response <--- %s",
                asHex(response)));
        System.arraycopy(response, 0, processedResponse, 0, response.length);
        int j = 7;
        while (response[j] != 0 && j < response.length) { j++; }
        final byte[] calIdStr = new byte[j - 7];
        System.arraycopy(response, 7, calIdStr, 0, j - 7);
        System.arraycopy(calIdStr, 0, processedResponse, 5, 8);
        LOGGER.info(String.format("OBD Calibration ID: %s", new String(calIdStr)));

        final byte[] supportedPidsPid = {
            (byte) 0x00, (byte) 0x20, (byte) 0x40, (byte) 0x60,
            (byte) 0x80, (byte) 0xA0, (byte) 0xC0, (byte) 0xE0};
        int i = 13;
        for (byte pid : supportedPidsPid) {
            final byte[] pidRequest = protocol.constructReadPidRequest(
                    id, new byte[]{pid});
            LOGGER.debug(String.format("OBD PID Group %02X Request  ---> %s",
                    pid, asHex(pidRequest)));
            final byte[] pidtmp = manager.send(pidRequest);
            final byte[] pidPpResponse = protocol.preprocessResponse(
                    pidRequest, pidtmp, new PollingStateImpl());
            LOGGER.debug(String.format("OBD PID Group %02X Response <--- %s",
                    pid, asHex(pidPpResponse)));
            System.arraycopy(pidPpResponse, 6, processedResponse, i, 4);
            i = i + 4;
            if ((pidPpResponse[pidPpResponse.length - 1] & 0x01) == 0) break;
        }

        // Check if PID 0x65 is supported and if so read it to obtain the Aux
        // Input support bits.  Map the first byte into the init string.  This
        // byte can be referenced as byte 40 by the ecubyte/bit attributes in
        // the logger definition to indicate supported switches.
        if ((processedResponse[25] & 0x08) > 0) {
            final byte[] aiRequest = protocol.constructReadPidRequest(
                    id, new byte[]{0x65});
            LOGGER.debug(String.format(
                    "OBD Auxiliary Inputs Support Request  ---> %s",
                    asHex(aiRequest)));
            final byte[] aiResponse = manager.send(aiRequest);
            final byte[] aiPpResponse = protocol.preprocessResponse(
                    aiRequest, aiResponse, new PollingStateImpl());
            LOGGER.debug(String.format(
                    "OBD Auxiliary Inputs Support Response <--- %s",
                    asHex(aiPpResponse)));
            System.arraycopy(aiPpResponse, 6, processedResponse, 45, 1);
        }
        LOGGER.debug(String.format("OBD Init Response <--- %s",
                asHex(processedResponse)));  // contains CALID not ECUID
        protocol.processEcuInitResponse(callback, processedResponse);
    }

    @Override
    public final void sendAddressReads(
            Collection<EcuQuery> queries,
            byte id, 
            PollingState pollState) {

        final int obdQueryListLength = queries.size();
        for (int i = 0; i < obdQueryListLength; i += 6) {
            for (int j = i; (j < i + 6) && (j < obdQueryListLength); j++) {
                obdQueries.add(((ArrayList<EcuQuery>) queries).get(j));
            }
            final byte[] request = protocol.constructReadAddressRequest(
                    id, obdQueries);
            LOGGER.debug(String.format("Mode:%d OBD Request  ---> %s",
                    pollState.getCurrentState(), asHex(request)));

            final byte[] response = protocol.constructReadAddressResponse(
                    obdQueries, pollState);
            manager.send(request, response, pollState);
            final byte[] processedResponse = protocol.preprocessResponse(
                    request, response, pollState);
            LOGGER.debug(String.format("Mode:%d OBD Response <--- %s",
                    pollState.getCurrentState(), asHex(processedResponse)));
            protocol.processReadAddressResponses(
                    obdQueries, processedResponse, pollState);
            obdQueries.clear();
        }
    }

    @Override
    public void clearLine() {
        manager.clearLine();
    }

    @Override
    public void close() {
        manager.close();
    }

    @Override
    public void sendAddressWrites(Map<EcuQuery, byte[]> writeQueries, byte id) {
        throw new UnsupportedOperationException();
    }
}
