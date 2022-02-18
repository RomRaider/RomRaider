/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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
import java.util.Arrays;
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
import com.romraider.logger.ecu.definition.Module;

public final class OBDLoggerConnection implements LoggerConnection {
    private static final Logger LOGGER = getLogger(OBDLoggerConnection.class);
    private final LoggerProtocolOBD protocol;
    private final ConnectionManager manager;
    private Collection<EcuQuery> obdQueries = new ArrayList<EcuQuery>();

    public OBDLoggerConnection(ConnectionManager manager) {
        checkNotNull(manager, "manager");
        this.manager = manager;
        final Settings settings = SettingsManager.getSettings();
        this.protocol = (LoggerProtocolOBD) ProtocolFactory.getProtocol(settings.getLoggerProtocol(), settings.getTransportProtocol());
    }

    @Override
    public void open(Module module) {
    }

    @Override
    public void ecuReset(Module module, int resetCode) {
        byte[] request = protocol.constructEcuResetRequest(module, resetCode);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("%s Reset Request  ---> %s",
                module, asHex(request)));
        byte[] response = manager.send(request);
        byte[] processedResponse = protocol.preprocessResponse(
                request, response, new PollingStateImpl());
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("%s Reset Response <--- %s",
                module, asHex(processedResponse)));
        protocol.processEcuResetResponse(processedResponse);
    }

    @Override
    // Build an init string similar to the SSM version so the logger definition
    // can reference supported parameters with ecubyte/bit attributes.
    public void ecuInit(EcuInitCallback callback, Module module) {
        final byte[] processedResponse = new byte[46];
        final byte[] request = protocol.constructEcuInitRequest(module);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("%s Calibration ID Request  ---> %s",
                module, asHex(request)));
        final byte[] response = manager.send(request);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("%s Calibration ID Response <--- %s",
                module, asHex(response)));
        System.arraycopy(response, 0, processedResponse, 0, response.length);
        int j = 7;
        // try to find the string termination character 0x00 in the response
        while (j < response.length && response[j] != 0) { j++; }
        byte[] calIdStr = new byte[j - 7];
        System.arraycopy(response, 7, calIdStr, 0, calIdStr.length);
        LOGGER.info(String.format("%s Calibration ID: %s", module, new String(calIdStr)));
        calIdStr = Arrays.copyOf(calIdStr, 8);  // extend to 8 bytes maximum
        System.arraycopy(calIdStr, 0, processedResponse, 5, calIdStr.length);

        final byte[] supportedPidsPid = {
            (byte) 0x00, (byte) 0x20, (byte) 0x40, (byte) 0x60,
            (byte) 0x80, (byte) 0xA0, (byte) 0xC0, (byte) 0xE0};
        int i = 13;
        for (byte pid : supportedPidsPid) {
            final byte[] pidRequest = protocol.constructReadPidRequest(
                    module, new byte[]{pid});
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format("%s PID Group %02X Request  ---> %s",
                    module, pid, asHex(pidRequest)));
            final byte[] pidtmp = manager.send(pidRequest);
            final byte[] pidPpResponse = protocol.preprocessResponse(
                    pidRequest, pidtmp, new PollingStateImpl());
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format("%s PID Group %02X Response <--- %s",
                    module, pid, asHex(pidPpResponse)));
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
                    module, new byte[]{0x65});
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format(
                    "%s Auxiliary Inputs Support Request  ---> %s",
                    module, asHex(aiRequest)));
            final byte[] aiResponse = manager.send(aiRequest);
            final byte[] aiPpResponse = protocol.preprocessResponse(
                    aiRequest, aiResponse, new PollingStateImpl());
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format(
                    "%s Auxiliary Inputs Support Response <--- %s",
                    module, asHex(aiPpResponse)));
            System.arraycopy(aiPpResponse, 6, processedResponse, 45, 1);
        }
        // contains CAL ID not ECU ID
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("%s Init Response <--- %s",
                module, asHex(processedResponse)));
        protocol.processEcuInitResponse(callback, processedResponse);
    }

    @Override
    public final void sendAddressReads(
            Collection<EcuQuery> queries,
            Module module,
            PollingState pollState) {

        final int obdQueryListLength = queries.size();
        for (int i = 0; i < obdQueryListLength; i += 6) {
            for (int j = i; (j < i + 6) && (j < obdQueryListLength); j++) {
                obdQueries.add(((ArrayList<EcuQuery>) queries).get(j));
            }
            final byte[] request = protocol.constructReadAddressRequest(
                    module, obdQueries);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format("Mode:%s %s Request  ---> %s",
                    pollState.getCurrentState(), module, asHex(request)));

            final byte[] response = protocol.constructReadAddressResponse(
                    obdQueries, pollState);
            manager.send(request, response, pollState);
            final byte[] processedResponse = protocol.preprocessResponse(
                    request, response, pollState);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format("Mode:%s %s Response <--- %s",
                    pollState.getCurrentState(), module, asHex(processedResponse)));
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
    public void sendAddressWrites(Map<EcuQuery, byte[]> writeQueries, Module module) {
        throw new UnsupportedOperationException();
    }
}
