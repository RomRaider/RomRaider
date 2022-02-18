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
import static com.romraider.util.ThreadUtil.sleep;
import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;
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
import com.romraider.logger.ecu.comms.query.EcuQueryData;
import com.romraider.logger.ecu.comms.query.EcuQueryRangeTest;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.exception.SerialCommunicationException;

public final class NCSLoggerConnection implements LoggerConnection {
    private static final Logger LOGGER = getLogger(NCSLoggerConnection.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            NCSLoggerConnection.class.getName());
    private final LoggerProtocolNCS protocol;
    private final ConnectionManager manager;
    private int queryCount;
    private final Settings settings = SettingsManager.getSettings();
    private final Collection<EcuQuery> scQuery = new ArrayList<EcuQuery>();
    private final Collection<EcuQuery> ramQuery = new ArrayList<EcuQuery>();
    private boolean commsStarted;
    private boolean elevatedDiag;


    public NCSLoggerConnection(ConnectionManager manager) {
        checkNotNull(manager, "manager");
        this.manager = manager;
        this.protocol = (LoggerProtocolNCS) ProtocolFactory.getProtocol(
                settings.getLoggerProtocol(),
                settings.getTransportProtocol());
        commsStarted = false;
        elevatedDiag = false;
    }

    @Override
    //TODO: not yet implemented
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
    // can reference supported parameters using ecubyte/bit attributes.
    public void ecuInit(EcuInitCallback callback, Module module) {
        // ConnectionManger must have completed a fastInit to start comms
        if (!commsStarted) open(module);
        final byte[] initResponse = new byte[422];
        byte[] request;
        byte[] response;
        request = protocol.constructEcuIdRequest(module);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("%s ID Request  ---> %s",
                module, asHex(request)));
        response = manager.send(request);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("%s ID Raw Response <--- %s",
                module, asHex(response)));
        response = protocol.processEcuIdResponse(response);
        if (LOGGER.isDebugEnabled())
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
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(String.format("%s SID %02X, PID Group %02X Request  ---> %s",
                        module, sid, pid, asHex(request)));
                response = manager.send(request);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(String.format("%s SID %02X, PID Group %02X Raw Response <--- %s",
                        module, sid ,pid, asHex(response)));
                // Validate response
                response = protocol.processReadSidPidResponse(response);
                if (LOGGER.isDebugEnabled())
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
                (byte) 0x11, (byte) 0x12, (byte) 0x13,
                (byte) 0x15};
        for (byte hb : highBytes) {
            if (hb == (byte) 0x13) {    // Supported Switch PIDs
                test_grp = true;
                for (byte pid : supportedPidsPid) {
                    if (test_grp) {
                        request = protocol.constructReadSidPidRequest(
                                module, sid, new byte[]{hb, pid});
                        if (LOGGER.isDebugEnabled())
                            LOGGER.debug(String.format("%s SID %02X, PID Group %02X%02X Request  ---> %s",
                                module, sid, hb, pid, asHex(request)));
                        response = manager.send(request);
                        if (LOGGER.isDebugEnabled())
                            LOGGER.debug(String.format("%s SID %02X, PID Group %02X%02X Raw Response <--- %s",
                                module, sid, hb, pid, asHex(response)));
                        // Validate response
                        response = protocol.processReadSidPidResponse(response);
                        if (LOGGER.isDebugEnabled())
                            LOGGER.debug(String.format("%s SID %02X, PID Group %02X%02X Processed Response <--- %s",
                                module, sid, hb, pid, asHex(response)));
                        // Check lsb to see if next PID group is supported
                        if ((response[response.length-1] & 0x01) == 0) {
                            test_grp = false;
                        }
                        final short[] supported = new short[2];
                        for (int j = 0; j < 2; j++) {
                            supported[j] = (short) ((short)(response[j*2] << 8) + (response[j*2+1] & 0x00FF));
                        }
                        for (int k = 0; k < supported.length; k++) {
                            // ex: 7FFC2000
                            for (int shift = 15; shift > -1; shift--) {
                                if (((1 << shift) & supported[k]) > 0) {
                                    byte cid = (byte) ((16 - shift) + (k * 16));
                                    request = protocol.constructReadSidPidRequest(
                                            module, sid, new byte[]{hb, cid});
                                    if (LOGGER.isDebugEnabled())
                                        LOGGER.debug(String.format("%s SID %02X, PID %02X%02X Request  ---> %s",
                                            module, sid, hb, cid, asHex(request)));
                                    response = manager.send(request);
                                    if (LOGGER.isDebugEnabled())
                                        LOGGER.debug(String.format("%s SID %02X, PID %02X%02X Raw Response <--- %s",
                                            module, sid ,hb, cid, asHex(response)));
                                    // Validate response
                                    response = protocol.processReadSidPidResponse(response);
                                    if (LOGGER.isDebugEnabled())
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
                        if (LOGGER.isDebugEnabled())
                            LOGGER.debug(String.format("%s SID %02X, PID Group %02X%02X Request  ---> %s",
                                module, sid, hb, pid, asHex(request)));
                        response = manager.send(request);
                        if (LOGGER.isDebugEnabled())
                            LOGGER.debug(String.format("%s SID %02X, PID Group %02X%02X Raw Response <--- %s",
                                module, sid ,hb, pid, asHex(response)));
                        // Validate response
                        response = protocol.processReadSidPidResponse(response);
                        System.arraycopy(response, 0, initResponse, i, 4);
                        if (LOGGER.isDebugEnabled())
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
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("%s Init Response <--- %s",
                module, asHex(initResponse)));  // contains ECUID
        protocol.processEcuInitResponse(callback, initResponse);
    }

    @Override
    public final void sendAddressReads(
            Collection<EcuQuery> queries,
            Module module,
            PollingState pollState) {

        // ConnectionManger must have completed a fastInit to start comms
        if (!commsStarted) open(module);

        // CAN Slow poll, read each parameter in a separate query, inefficient
        if (settings.isCanBus() && !pollState.isFastPoll()) {
            doSlowCanQueries(queries, module, pollState);
        }
        // Use CAN UDS 2C to load parameters and then request to read all loaded
        else if (settings.isCanBus() && pollState.isFastPoll()) {
            doFastCanQueries(queries, module, pollState);
        }
        // if not CAN do k-line queries
        else {
            doKlineQueries(queries, module, pollState);
        }
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
    public void open(Module module) {
        // For k-line, get the fast init sequence and stop command, then open
        // the connection via the ConnectionManager
        byte[] start = protocol.constructEcuFastInitRequest(module);
        byte[] stop = protocol.constructEcuStopRequest(module);
        // For CAN, start a standard diagnostics session
        if (settings.isCanBus()) {
            start = protocol.constructStartDiagRequest(module);
        }
        manager.open(start, stop);
        commsStarted = true;
    }

    @Override
    public void close() {
        commsStarted = false;
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

    private void doKlineQueries(
            Collection<EcuQuery> queries,
            Module module,
            PollingState pollState) {

        // k-line max data bytes is 63 when length encoded into format byte
        if (queries.size() != queryCount
                || pollState.isNewQuery()) {
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
        }

        if (queries.size() != queryCount
                || pollState.isNewQuery()) {
            final byte[] request = protocol.constructLoadAddressRequest(queries);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format("Mode:%s %s Load address request  ---> %s",
                    pollState.getCurrentState(), module, asHex(request)));

            byte[] response = new byte[4];  // short header response
            if ((request[0] & (byte)0x80) == (byte)0x80) {
                response = new byte[6];     // long header response
            }
            protocol.validateLoadAddressResponse(
                    sendRcv(module, request, response, pollState));
            queryCount = queries.size();
        }
        final byte[] request = protocol.constructReadAddressRequest(
                module, queries, pollState);
        if (pollState.getCurrentState() == PollingState.State.STATE_0) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format("Mode:%s %s Read request  ---> %s",
                    pollState.getCurrentState(), module, asHex(request)));
            pollState.setLastState(PollingState.State.STATE_0);
        }
        final byte[] response = protocol.constructReadAddressResponse(
                queries, pollState);
        protocol.processReadAddressResponses(
                queries,
                sendRcv(module, request, response, pollState),
                pollState);
    }

    /**
     * SID/CID can't be combined with reading memory addresses direct,
     * so we need to load and read each group separately.
     * The queries are divided into and added to the class attributes scQuery
     * and ramQuery collections.
     * @param queries - the collection of queries to evaluate and split
    */
    private void splitSidFromRamQueries(Collection<EcuQuery> queries) {
        scQuery.clear();
        ramQuery.clear();
        for (EcuQuery query : queries) {
            final String[] addresses = query.getAddresses();
            for (final String address : addresses) {
                if (address.startsWith("0x22")) {   // SID&CID
                    scQuery.add(query);
                    break;
                }
                else {
                    ramQuery.add(query);
                    break;
                }
            }
        }
    }

    private void doSlowCanQueries(
            Collection<EcuQuery> queries,
            Module module,
            PollingState pollState) {

        splitSidFromRamQueries(queries);
        byte[] request;
        byte[] response;
        if (!scQuery.isEmpty()) {
            final Collection<EcuQuery> sidQuery = new ArrayList<EcuQuery>();
            for (EcuQuery query : scQuery) {
                // for each query in the collection create a new collection with one item
                sidQuery.clear();
                sidQuery.add(query);
                if (elevatedDiag) {
                    request = protocol.constructStartDiagRequest(module);
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug(String.format("%s Standard Diagnostics Request  ---> %s",
                            module, asHex(request)));
                    response = manager.send(request);
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug(String.format("%s Standard Diagnostics Response <--- %s",
                            module, asHex(response)));
                    elevatedDiag = false;
                }
                request = protocol.constructReadAddressRequest(
                        module, sidQuery);
                response = new byte[0];
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(module + " CAN Request  ---> " + asHex(request));
                response = protocol.constructReadAddressResponse(
                        sidQuery, pollState);
                protocol.processReadAddressResponses(
                        sidQuery,
                        sendRcv(module, request, response, pollState),
                        pollState);
            }
        }
        // if query address is not an SID, elevate diag session and
        // switch to SID 23 using readMemoryRequest
        if (!ramQuery.isEmpty()) {
            if (!elevatedDiag) {
                request = protocol.constructElevatedDiagRequest(module);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(String.format("%s Elevated Diagnostics Request  ---> %s",
                        module, asHex(request)));
                response = manager.send(request);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(String.format("%s Elevated Diagnostics Response <--- %s",
                        module, asHex(response)));
                elevatedDiag = true;
            }
            // Inspect the address of each query to determine if a single query
            // with a start address and byte length can be substituted as opposed
            // to querying each address separately.
            final EcuQueryRangeTest range = new EcuQueryRangeTest(ramQuery, 63);
            final Collection<EcuQuery> newQuery = range.validate();
            int length = range.getLength();
            if (newQuery != null && length > 0) {
                request = protocol.constructReadMemoryRequest(module, newQuery, length);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(module + " CAN $23 Request  ---> " + asHex(request));
                response = protocol.constructReadMemoryResponse(1, length);
                protocol.processReadMemoryResponses(
                        ramQuery,
                        sendRcv(module, request, response, pollState));
            }
            else {
                // for each query in the collection create a new collection with one item
                for (EcuQuery query : ramQuery) {
                    newQuery.clear();
                    newQuery.add(query);
                    request = protocol.constructReadMemoryRequest(
                            module, newQuery, EcuQueryData.getDataLength(query));
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug(String.format("Mode:%s %s Memory request  ---> %s",
                            pollState.getCurrentState(), module, asHex(request)));
                    response = protocol.constructReadMemoryResponse(1,
                            EcuQueryData.getDataLength(query));
                    protocol.processReadMemoryResponses(
                            newQuery,
                            sendRcv(module, request, response, pollState));
                }
            }
        }
    }

    private void doFastCanQueries (
            Collection<EcuQuery> queries,
            Module module,
            PollingState pollState) {

        // When parameter selection changes or there are RAM parameters present
        // load and read the SID/CID parameters separate from the RAM parameters
        if (queries.size() != queryCount
                || pollState.isNewQuery()) {

            splitSidFromRamQueries(queries);
            queryCount = queries.size();
            pollState.setNewQuery(true);
        }

        byte[] request;
        byte[] response;
        if (!scQuery.isEmpty()) {   // SID/CID queries
            if (pollState.isNewQuery() || !ramQuery.isEmpty()) {
                request = protocol.constructLoadAddressRequest(scQuery);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(String.format("Mode:%s %s Load address request  ---> %s",
                        pollState.getCurrentState(), module, asHex(request)));
                // CAN max is 99 bytes
                if (request.length > 99) {
                    throw new SerialCommunicationException(
                            rb.getString("TOOLARGE"));
                }
                response = manager.send(request);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(String.format("Mode:%s %s Load address response  <--- %s",
                        pollState.getCurrentState(), module, asHex(response)));
                protocol.validateLoadAddressResponse(response);
            }
            request = protocol.constructReadAddressRequest(
                    module, scQuery, pollState);
            response = protocol.constructReadAddressResponse(
                    scQuery, pollState);
            protocol.processReadAddressResponses(
                    scQuery,
                    sendRcv(module, request, response, pollState),
                    pollState);
        }
        // When parameter selection changes or there are SID CID parameters present
        // load and read the RAM parameters separate from the SID CID parameters
        if (!ramQuery.isEmpty()) {  // RAM queries
            if (pollState.isNewQuery() || !scQuery.isEmpty()) {
                request = protocol.constructLoadAddressRequest(ramQuery);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(String.format("Mode:%s %s Load address request  ---> %s",
                        pollState.getCurrentState(), module, asHex(request)));
                // CAN max is 99 bytes
                if (request.length > 99) {
                    throw new SerialCommunicationException(
                            rb.getString("TOOLARGE"));
                }
                response = manager.send(request);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(String.format("Mode:%s %s Load address response  <--- %s",
                        pollState.getCurrentState(), module, asHex(response)));
                protocol.validateLoadAddressResponse(response);
                pollState.setFastPoll(true);
            }
            request = protocol.constructReadAddressRequest(
                    module, ramQuery, pollState);
            response = protocol.constructReadAddressResponse(
                    ramQuery, pollState);
            protocol.processReadAddressResponses(
                    ramQuery,
                    sendRcv(module, request, response, pollState),
                    pollState);
        }
    }

    private byte[] sendRcv(
            Module module, byte[] request,
            byte[] response, PollingState pollState) {

        manager.send(request, response, pollState);
        if (LOGGER.isTraceEnabled())
            LOGGER.trace(module + " Read Raw Response <--- " + asHex(response));
        final byte[] processedResponse = protocol.preprocessResponse(
                request, response, pollState);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Mode:" + pollState.getCurrentState() + " " +
                module + " Response <--- " + asHex(processedResponse));
        return processedResponse;
    }

}
