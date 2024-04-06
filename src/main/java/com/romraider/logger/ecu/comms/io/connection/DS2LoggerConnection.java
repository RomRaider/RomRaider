/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2024 RomRaider.com
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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.protocol.ProtocolFactory;
import com.romraider.logger.ecu.comms.io.protocol.LoggerProtocolDS2;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.manager.PollingStateImpl;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.comms.query.EcuQueryRangeTest;
import com.romraider.logger.ecu.definition.EcuData;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.exception.SerialCommunicationException;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;

public final class DS2LoggerConnection implements LoggerConnection {
    private static final Logger LOGGER = getLogger(DS2LoggerConnection.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            DS2LoggerConnection.class.getName());
    private final LoggerProtocolDS2 protocol;
    private final ConnectionManager manager;
    private final Settings settings = SettingsManager.getSettings();
    private int queryCount;

    public DS2LoggerConnection(ConnectionManager manager) {
        checkNotNull(manager, "manager");
        this.manager = manager;

        this.protocol = (LoggerProtocolDS2) ProtocolFactory.getProtocol(
                settings.getLoggerProtocol(),
                settings.getTransportProtocol());
    }

    @Override
    public void open(Module module) {
    }

    @Override
    public void ecuReset(Module module, int resetCode) {
        byte[] request = protocol.constructEcuResetRequest(module, resetCode);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(module + " Reset Request  ---> " + asHex(request));
        byte[] response = manager.send(request);
        byte[] processedResponse = protocol.preprocessResponse(request, response, new PollingStateImpl());
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(module + " Reset Response <--- " + asHex(processedResponse));
        protocol.processEcuResetResponse(processedResponse);
    }

    @Override
    public void ecuInit(EcuInitCallback callback, Module module) {
        byte[] request = protocol.constructEcuInitRequest(module);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(module + " Init Request  ---> " + asHex(request));
        byte[] response = manager.send(request);
        if (LOGGER.isTraceEnabled())
            LOGGER.trace(module + " Init Raw Response <--- " + asHex(response));
        byte[] processedResponse = protocol.preprocessResponse(request, response, new PollingStateImpl());
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(module + " Init Response <--- " + asHex(processedResponse));
        protocol.processEcuInitResponse(callback, processedResponse);
    }

    @Override
    public final void sendAddressReads(
            Collection<EcuQuery> queries,
            Module module,
            PollingState pollState) {

        // Group the queries into common command groups
        final Map<String, Collection<EcuQuery>> groupList = getGroupList(queries);

        // for each group populate the queries with results
        for (String group : groupList.keySet().toArray(new String[0])) {

            final Collection<EcuQuery> querySet = groupList.get(group);
            byte[] request = new byte[0];
            byte[] response = new byte[0];
            final String groupTest = group.toLowerCase();

            // read from procedure [02 XX XX XX PN] PN - Procedure Number<28 (0x1C)
            if (groupTest.startsWith("0x0b0x02")) {
                for (EcuQuery query : querySet) {
                    final Collection<EcuQuery> queryList = new ArrayList<EcuQuery>();
                    queryList.add(query);
                    request = protocol.constructReadProcedureRequest(
                            module, queryList);
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug(String.format("Mode:%s %s Procedure request  ---> %s",
                            pollState.getCurrentState(), module, asHex(request)));
                    response = protocol.constructReadAddressResponse(
                            queryList, request.length);
                    protocol.processReadAddressResponse(
                            queryList,
                            sendRcv(module, request, response, pollState),
                            pollState);
                }
            }
            // read data starting at address [00 SG HI LO NN] NN - number of bytes<249
            else if (groupTest.startsWith("0x060x00")) {
                final EcuQueryRangeTest range = new EcuQueryRangeTest(querySet, 128);
                final Collection<EcuQuery> newQuery = range.validate();
                int length = range.getLength();
                if (newQuery != null && length > 0) {
                    request = protocol.constructReadMemoryRange(
                            module, newQuery, length);
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug(String.format("Mode:%s %s Range request  ---> %s",
                            pollState.getCurrentState(), module, asHex(request)));
                    response = protocol.constructReadMemoryRangeResponse(
                            request.length, length);
                    protocol.processReadMemoryRangeResponse(
                            querySet,
                            sendRcv(module, request, response, pollState));
                }
                else {
                    for (EcuQuery query : querySet) {
                        final Collection<EcuQuery> queryList = new ArrayList<EcuQuery>();
                        queryList.add(query);
                        request = protocol.constructReadMemoryRequest(
                                module, queryList);
                        if (LOGGER.isDebugEnabled())
                            LOGGER.debug(String.format("Mode:%s %s Memory request  ---> %s",
                                pollState.getCurrentState(), module, asHex(request)));
                        response = protocol.constructReadAddressResponse(
                                queryList, request.length);
                        protocol.processReadAddressResponse(
                                queryList,
                                sendRcv(module, request, response, pollState),
                                pollState);
                    }
                }
            }
            //  Pre-defined Group parameter calls
            // #03 Engine Parameters
            // #04 Switch Parameters
            // #91h Lambda Adaptations
            // #92h Other Adaptations
            // #93h Timing Correction/Fuel Compensation
            else if (groupTest.startsWith("0x0b0x03")
                || groupTest.startsWith("0x0b0x04")
                || groupTest.startsWith("0x0b0x90")
                || groupTest.startsWith("0x0b0x91")
                || groupTest.startsWith("0x0b0x92")
                || groupTest.startsWith("0x0b0x93")
                || groupTest.startsWith("0x0b0x94")
                || groupTest.startsWith("0x0b0x95")) {

                request = protocol.constructReadGroupRequest(
                        module, group);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(String.format("Mode:%s %s Group request  ---> %s",
                        pollState.getCurrentState(), module, asHex(request)));
                response = protocol.constructReadGroupResponse(
                        querySet, request.length);
                protocol.processReadAddressResponse(
                        querySet,
                        sendRcv(module, request, response, pollState),
                        pollState);
            }
            // user selected parameter list
            // [01 NN B4 B3 B2 B1 B0 ... B4n B3n B2n B1n B0n] NN<33h
            else if (groupTest.startsWith("0x0b0x01")) {
                // update address list if size or new query changes
                if (querySet.size() != queryCount
                        || pollState.isNewQuery()) {

                    // Set new address list
                    request = protocol.constructSetAddressRequest(
                            module, querySet);
                    // if too many parameters selected then notify user
                    if (request.length > 256) {
                        throw new SerialCommunicationException(
                                rb.getString("TOOLARGE"));
                    }
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug(String.format("Mode:%s %s Load address request  ---> %s",
                            pollState.getCurrentState(), module, asHex(request)));
                    pollState.setLastState(PollingState.State.STATE_0);
                    // Response to address list set is just an ACK
                    response = protocol.constructSetAddressResponse(
                            request.length);
                    protocol.validateSetAddressResponse(
                            sendRcv(module, request, response, pollState));
                    queryCount = querySet.size();
                }
                // Read set addresses
                request = protocol.constructReadAddressRequest(
                module, querySet);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(String.format("Mode:%s %s Read addresses request  ---> %s",
                        pollState.getCurrentState(), module, asHex(request)));
                pollState.setLastState(PollingState.State.STATE_0);
                // Response to address list read is the parameter bytes
                response = protocol.constructReadAddressResponse(
                        querySet, request.length);
                protocol.processReadAddressResponses(
                        querySet,
                        sendRcv(module, request, response, pollState),
                        pollState);
            }
        }
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
    public final void sendAddressWrites(
            Map<EcuQuery, byte[]> writeQueries, Module module) {

        for (EcuQuery writeKey : writeQueries.keySet()) {
            if (writeKey.getBytes().length == 3) {
                final byte[] request =
                        protocol.constructWriteAddressRequest(
                                module,
                                writeKey.getBytes(),
                                writeQueries.get(writeKey)[0]);

                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(module + " Write Request  ---> " + asHex(request));
                final byte[] response = manager.send(request);
                byte[] processedResponse =
                        protocol.preprocessResponse(
                                request,
                                response,
                                new PollingStateImpl());
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(module + " Write Response <--- " + asHex(processedResponse));
                protocol.processWriteResponse(
                        writeQueries.get(writeKey), processedResponse);
            }
        }
    }

    /**
     *  Return a map of groups each with a value of a list of queries having the same group
     * @param queries
     * @return Map(Group, Collection(EcuQuery))
     */
    private Map<String, Collection<EcuQuery>> getGroupList(Collection<EcuQuery> queries) {
        final Map<String, Collection<EcuQuery>> groups = new HashMap<String, Collection<EcuQuery>>();
        String group;
        String subGroup;
        for (EcuQuery query : queries) {
            group = ((EcuData) query.getLoggerData()).getGroup();
            subGroup = ((EcuData) query.getLoggerData()).getSubgroup();
            group = group + (subGroup == null ? "" : subGroup);
            if (!groups.containsKey(group)) {
                final Collection<EcuQuery> queryList = new ArrayList<EcuQuery>();
                queryList.add(query);
                groups.put(group, queryList);
            }
            else {
                groups.get(group).add(query);
            }
        }
        return groups;
    }

    private byte[] sendRcv(Module module, byte[] request, byte[] response, PollingState pollState) {
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

    public void clearQueryCount() {
        queryCount = -1;
    }
}
