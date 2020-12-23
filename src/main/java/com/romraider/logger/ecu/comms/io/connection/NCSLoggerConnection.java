/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2020 RomRaider.com
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
import static com.romraider.util.ThreadUtil.sleep;
import static org.apache.log4j.Logger.getLogger;

import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;
import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.protocol.ProtocolFactory;
import com.romraider.logger.ecu.comms.io.protocol.LoggerProtocolNCS;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.manager.PollingStateImpl;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.exception.SerialCommunicationException;

public final class NCSLoggerConnection implements LoggerConnection {
    private static final Logger LOGGER = getLogger(NCSLoggerConnection.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            NCSLoggerConnection.class.getName());
    private final LoggerProtocolNCS protocol;
    private final ConnectionManager manager;
    private int queryCount;

    public NCSLoggerConnection(ConnectionManager manager) {
        checkNotNull(manager, "manager");
        this.manager = manager;
        final Settings settings = SettingsManager.getSettings();
        this.protocol = (LoggerProtocolNCS) ProtocolFactory.getProtocol(
                settings.getLoggerProtocol(),
                settings.getTransportProtocol());
    }

    @Override
    //TODO: not yet implemented
    public void ecuReset(Module module, int resetCode) {
        byte[] request = protocol.constructEcuResetRequest(module, resetCode);
        LOGGER.debug(String.format("%s Reset Request  ---> %s",
                module, asHex(request)));
        byte[] response = manager.send(request);
        byte[] processedResponse = protocol.preprocessResponse(
                request, response, new PollingStateImpl());
        LOGGER.debug(String.format("%s Reset Response <--- %s",
                module, asHex(processedResponse)));
        protocol.processEcuResetResponse(processedResponse);
    }

    @Override
    // Build an init string similar to the SSM version so the logger definition
    // can reference supported parameters with ecubyte/bit attributes. 
    public void ecuInit(EcuInitCallback callback, Module module) {
        final byte[] initResponse = new byte[422];
        byte[] request = protocol.constructEcuIdRequest(module);
        LOGGER.debug(String.format("%s ID Request  ---> %s",
                module, asHex(request)));
        byte[] response = manager.send(request);
        LOGGER.debug(String.format("%s ID Raw Response <--- %s",
                module, asHex(response)));
        response = protocol.processEcuIdResponse(response);
        LOGGER.debug(String.format("%s ID Processed Response <--- %s",
                module, asHex(response)));
        System.arraycopy(response, 0, initResponse, 2, response.length);
        sleep(55L);

        final byte[] supportedPidsPid = {
            (byte) 0x00, (byte) 0x20, (byte) 0x40, (byte) 0x60,
            (byte) 0x80, (byte) 0xA0, (byte) 0xC0, (byte) 0xE0};
        int i = 8;
        byte sid = (byte) 0x21;
        boolean test_grp = true;
        for (byte pid : supportedPidsPid) {
            if (test_grp) {
                request = protocol.constructReadSidPidRequest(
                        module, sid, new byte[]{pid});
                LOGGER.debug(String.format("%s SID %02X, PID Group %02X Request  ---> %s",
                        module, sid, pid, asHex(request)));
                response = manager.send(request);
                LOGGER.debug(String.format("%s SID %02X, PID Group %02X Raw Response <--- %s",
                        module, sid ,pid, asHex(response)));
                // Validate response
                response = protocol.processReadSidPidResponse(response);
                LOGGER.debug(String.format("%s SID %02X, PID Group %02X Processed Response <--- %s",
                        module, sid ,pid, asHex(response)));
                System.arraycopy(response, 0, initResponse, i, 4);
                // Check lsb to see if next PID group is supported
                if ((response[response.length-1] & 0x01) == 0) {
                    test_grp = false;
                }
            }
            i = i + 4;
        }
        sid = (byte) 0x22;
        final byte[] highBytes = {
                (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14,
                (byte) 0x15};
        for (byte hb : highBytes) {
            if (hb == (byte) 0x13) {    // Supported Switch PIDs
                test_grp = true;
                for (byte pid : supportedPidsPid) {
                    if (test_grp) {
                        request = protocol.constructReadSidPidRequest(
                                module, sid, new byte[]{hb, pid});
                        LOGGER.debug(String.format("%s SID %02X, PID Group %02X%02X Request  ---> %s",
                                module, sid, hb, pid, asHex(request)));
                        response = manager.send(request);
                        LOGGER.debug(String.format("%s SID %02X, PID Group %02X%02X Raw Response <--- %s",
                                module, sid, hb, pid, asHex(response)));
                        // Validate response
                        response = protocol.processReadSidPidResponse(response);
                        LOGGER.debug(String.format("%s SID %02X, PID Group %02X%02X Processed Response <--- %s",
                                module, sid, hb, pid, asHex(response)));
                        // Check lsb to see if next PID group is supported
                        if ((response[response.length-1] & 0x01) == 0) {
                            test_grp = false;
                        }
                        final short[] supported = new short[2];
                        for (int j = 0; j < 2; j++) {
                            supported[j] = (short) ((short)(response[j*2] << 8) + ((short)response[j*2+1] & 0x00FF));
                        }
                        for (int k = 0; k < supported.length; k++) {
                            // ex: 7FFC2000
                            for (int shift = 15; shift > -1; shift--) {
                                if (((1 << shift) & supported[k]) > 0) {
                                    byte cid = (byte) ((16 - shift) + (k * 16));
                                    request = protocol.constructReadSidPidRequest(
                                            module, sid, new byte[]{hb, cid});
                                    LOGGER.debug(String.format("%s SID %02X, PID %02X%02X Request  ---> %s",
                                            module, sid, hb, cid, asHex(request)));
                                    response = manager.send(request);
                                    LOGGER.debug(String.format("%s SID %02X, PID %02X%02X Raw Response <--- %s",
                                            module, sid ,hb, cid, asHex(response)));
                                    // Validate response
                                    response = protocol.processReadSidPidResponse(response);
                                    LOGGER.debug(String.format("%s SID %02X, PID Group %02X%02X Processed Response <--- %s",
                                            module, sid, hb, pid, asHex(response)));
                                    // 2 bytes returned, we only need the second byte
                                    System.arraycopy(response, 1, initResponse, i, 1);
                                }
                                i++;
                            }
                        }
                    }
                    else {
                        i = i + 32;
                    }
                }
                i--;    // move back one unused index byte position
            }
            else {
                test_grp = true;
                for (byte pid : supportedPidsPid) {
                    if (test_grp) {
                        request = protocol.constructReadSidPidRequest(
                                module, sid, new byte[]{hb, pid});
                        LOGGER.debug(String.format("%s SID %02X, PID Group %02X%02X Request  ---> %s",
                                module, sid, hb, pid, asHex(request)));
                        response = manager.send(request);
                        LOGGER.debug(String.format("%s SID %02X, PID Group %02X%02X Raw Response <--- %s",
                                module, sid ,hb, pid, asHex(response)));
                        // Validate response
                        response = protocol.processReadSidPidResponse(response);
                        System.arraycopy(response, 0, initResponse, i, 4);
                        LOGGER.debug(String.format("%s SID %02X, PID Group %02X%02X Processed Response <--- %s",
                                module, sid, hb, pid, asHex(response)));
                        // Check lsb to see if next PID group is supported
                        if ((response[response.length-1] & 0x01) == 0) {
                            test_grp = false;
                        }
                    }
                    i = i + 4;
                }
            }
        }
        LOGGER.debug(String.format("%s Init Response <--- %s",
                module, asHex(initResponse)));  // contains ECUID
        protocol.processEcuInitResponse(callback, initResponse);
    }

    @Override
    public final void sendAddressReads(
            Collection<EcuQuery> queries,
            Module module, 
            PollingState pollState) {

        if (queries.size() != queryCount
                || pollState.isNewQuery()) {
            // max data bytes 63 when length encoded into format byte
            int dataLength = 0;
            for (EcuQuery query : queries) {
                for (final String address : query.getAddresses()) {
                    dataLength += calcLength(address);
                }
            }
            // if length is too big then notify user to un-select some parameters
            if (dataLength > 61) {
                throw new SerialCommunicationException(
                        rb.getString("TOOLARGE"));
            }
            final byte[] request = protocol.constructLoadAddressRequest(queries);
            LOGGER.debug(String.format("Mode:%s %s Load address request  ---> %s",
                    pollState.getCurrentState(), module, asHex(request)));

            byte[] response = new byte[4];  // short header response
            if ((request[0] & (byte)0x80) == (byte)0x80) {
                response = new byte[6];     // long header response
            }
            manager.send(request, response, pollState);
            LOGGER.debug(String.format("Mode:%s %s Load address response  <--- %s",
                    pollState.getCurrentState(), module, asHex(response)));
            protocol.validateLoadAddressResponse(response);
            queryCount = queries.size();
        }
        final byte[] request = protocol.constructReadAddressRequest(
                module, queries, pollState);
        if (pollState.getCurrentState() == PollingState.State.STATE_0) {
            LOGGER.debug(String.format("Mode:%s %s Read request  ---> %s",
                    pollState.getCurrentState(), module, asHex(request)));
            pollState.setLastState(PollingState.State.STATE_0);
        }
        final byte[] response = protocol.constructReadAddressResponse(
                queries, pollState);
        manager.send(request, response, pollState);
        LOGGER.debug(String.format("Mode:%s %s Read response <--- %s",
                pollState.getCurrentState(), module, asHex(response)));
        protocol.processReadAddressResponses(
                queries, response, pollState);
    }

    public void clearQueryCount() {
        queryCount = -1;
    }

    @Override
    public void clearLine() {
        clearQueryCount();
        manager.clearLine();
    }

    @Override
    public void close() {
        clearQueryCount();
        manager.close();
    }

    @Override
    public void sendAddressWrites(Map<EcuQuery, byte[]> writeQueries, Module module) {
        throw new UnsupportedOperationException();
    }

    private int calcLength(String address) {
        if (address.toLowerCase().startsWith("0x2")) {
            return 3;
        }
        else {
            return 5;
        }
    }
}
