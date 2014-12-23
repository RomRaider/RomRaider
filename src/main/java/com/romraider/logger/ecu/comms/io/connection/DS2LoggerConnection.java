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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.protocol.ProtocolFactory;
import com.romraider.logger.ecu.comms.io.protocol.LoggerProtocolDS2;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.manager.PollingStateImpl;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.definition.EcuData;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.util.SettingsManager;

public final class DS2LoggerConnection implements LoggerConnection {
    private static final Logger LOGGER = getLogger(DS2LoggerConnection.class);
    private final LoggerProtocolDS2 protocol;
    private final ConnectionManager manager;
    Settings settings = SettingsManager.getSettings();

    public DS2LoggerConnection(ConnectionManager manager) {
        checkNotNull(manager, "manager");
        this.manager = manager;

        this.protocol = (LoggerProtocolDS2) ProtocolFactory.getProtocol(
                settings.getLoggerProtocol(),
                settings.getTransportProtocol());
    }

    @Override
    public void ecuReset(Module module) {
        byte[] request = protocol.constructEcuResetRequest(module);
        LOGGER.debug("ECU Reset Request  ---> " + asHex(request));
        byte[] response = manager.send(request);
        byte[] processedResponse = protocol.preprocessResponse(request, response, new PollingStateImpl());
        LOGGER.debug("ECU Reset Response <--- " + asHex(processedResponse));
        protocol.processEcuResetResponse(processedResponse);
    }

    @Override
    public void ecuInit(EcuInitCallback callback, Module module) {
        byte[] request = protocol.constructEcuInitRequest(module);
        LOGGER.debug("ECU Init Request  ---> " + asHex(request));
        byte[] response = manager.send(request);
        LOGGER.trace("ECU Init Raw Response <--- " + asHex(response));
        byte[] processedResponse = protocol.preprocessResponse(request, response, new PollingStateImpl());
        LOGGER.debug("ECU Init Response <--- " + asHex(processedResponse));
        protocol.processEcuInitResponse(callback, processedResponse);
    }

    @Override
    public final void sendAddressReads(
            Collection<EcuQuery> queries,
            Module module,
            PollingState pollState) {

        final Map<String, Collection<EcuQuery>> groupList = getGroupList(queries);
        for (String group : groupList.keySet().toArray(new String[0])) {
            final Collection<EcuQuery> querySet = groupList.get(group);
            byte[] request = new byte[0];
            byte[] response = new byte[0];
            if (group.equalsIgnoreCase("0x0b0x020e")) {
                for (EcuQuery query : querySet) {
                    final Collection<EcuQuery> queryList = new ArrayList<EcuQuery>();
                    queryList.add(query);
                    request = protocol.constructReadAddressRequest(
                            module, queryList);
                    LOGGER.debug("Mode:" + pollState.getCurrentState() +
                            " ECU Request  ---> " + asHex(request));
                    response = protocol.constructReadAddressResponse(
                            queryList, request.length);
                    protocol.processReadAddressResponses(
                            queryList,
                            sendRcv(request, response, pollState),
                            pollState);
                }
            }
            else if (group.equalsIgnoreCase("0x060x00")) {
                for (EcuQuery query : querySet) {
                    final Collection<EcuQuery> queryList = new ArrayList<EcuQuery>();
                    queryList.add(query);
                    request = protocol.constructReadMemoryRequest(
                            module, queryList);
                    LOGGER.debug("Mode:" + pollState.getCurrentState() +
                            " ECU Request  ---> " + asHex(request));
                    response = protocol.constructReadAddressResponse(
                            queryList, request.length);
                    protocol.processReadAddressResponses(
                            queryList,
                            sendRcv(request, response, pollState),
                            pollState);
                }
            }
            else {
                request = protocol.constructReadGroupRequest(
                        module, group);
                LOGGER.debug("Mode:" + pollState.getCurrentState() +
                        " ECU Request  ---> " + asHex(request));
                response = protocol.constructReadGroupResponse(
                        querySet, request.length);
                protocol.processReadAddressResponses(
                        querySet,
                        sendRcv(request, response, pollState),
                        pollState);
            }
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
    public final void sendAddressWrites(
            Map<EcuQuery, byte[]> writeQueries, Module module) {

        for (EcuQuery writeKey : writeQueries.keySet()) {
            if (writeKey.getBytes().length == 3) {
                final byte[] request =
                        protocol.constructWriteAddressRequest(
                                module,
                                writeKey.getBytes(),
                                writeQueries.get(writeKey)[0]);

                LOGGER.debug("ECU Write Request  ---> " + asHex(request));
                final byte[] response = manager.send(request);
                byte[] processedResponse =
                        protocol.preprocessResponse(
                                request,
                                response,
                                new PollingStateImpl());
                LOGGER.debug("ECU Write Response <--- " + asHex(processedResponse));
                protocol.processWriteResponse(
                        writeQueries.get(writeKey), processedResponse);
            }
        }
    }

    // Create a map of groups each with a value of a list of queries having the same group
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

    private byte[] sendRcv(byte[] request, byte[] response, PollingState pollState) {
        manager.send(request, response, pollState);
        LOGGER.trace("ECU Read Raw Response <--- " + asHex(response));
        final byte[] processedResponse = protocol.preprocessResponse(
                request, response, pollState);
        LOGGER.debug("Mode:" + pollState.getCurrentState() +
                " ECU Response <--- " + asHex(processedResponse));
        return processedResponse;
    }
}
