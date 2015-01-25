/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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

package com.romraider.io.protocol.ds2.iso9141;

import static com.romraider.io.protocol.ds2.iso9141.DS2Protocol.RESPONSE_NON_DATA_BYTES;
import static com.romraider.io.protocol.ds2.iso9141.DS2ResponseProcessor.extractResponseData;
import static com.romraider.io.protocol.ds2.iso9141.DS2ResponseProcessor.filterRequestFromResponse;
import static com.romraider.util.HexUtil.asBytes;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static java.lang.System.arraycopy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.romraider.io.protocol.Protocol;
import com.romraider.io.protocol.ProtocolDS2;
import com.romraider.logger.ecu.comms.io.protocol.LoggerProtocolDS2;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.query.EcuInit;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.definition.EcuData;
import com.romraider.logger.ecu.definition.Module;

public final class DS2LoggerProtocol implements LoggerProtocolDS2 {
    private final ProtocolDS2 protocol = new DS2Protocol();

    @Override
    public byte[] constructEcuInitRequest(Module module) {
        return protocol.constructEcuInitRequest(module);
    }

    @Override
    public byte[] constructEcuResetRequest(Module module) {
        return protocol.constructEcuResetRequest(module);
    }

    @Override
    public byte[] constructReadAddressRequest(
            Module module, Collection<EcuQuery> queries) {

        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        return protocol.constructReadAddressRequest(
                module, convertToByteAddresses(filteredQueries));
    }

    @Override
    public byte[] constructReadGroupRequest(
            Module module, String group) {

        return protocol.constructReadGroupRequest(
                module, new byte[][]{asBytes(group)});
    }

    @Override
    public byte[] constructReadMemoryRequest(
            Module module, Collection<EcuQuery> queries) {

        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        return protocol.constructReadMemoryRequest(
                module, convertToByteAddresses(filteredQueries), getDataLength(filteredQueries));
    }

    @Override
    public byte[] constructReadAddressResponse(Collection<EcuQuery> queries,
            PollingState pollState) {
        return null;
    }

    @Override
    public byte[] constructReadAddressResponse(
            Collection<EcuQuery> queries, int requestSize) {

        checkNotNullOrEmpty(queries, "queries");
        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        int numAddresses = 0;
        for (EcuQuery ecuQuery : filteredQueries) {
            numAddresses += getDataLength(ecuQuery); 
        }
        return new byte[requestSize + RESPONSE_NON_DATA_BYTES + numAddresses];
    }

    @Override
    public byte[] constructReadGroupResponse(
            Collection<EcuQuery> queries, int requestSize) {

        checkNotNullOrEmpty(queries, "queries");
        int size = 0;
        for (EcuQuery ecuQuery : queries) {
            size = ((EcuData) ecuQuery.getLoggerData()).getGroupSize();
            break;
        }
        return new byte[requestSize + RESPONSE_NON_DATA_BYTES + size];
    }

    @Override
    public byte[] preprocessResponse(byte[] request, byte[] response, PollingState pollState) {
        return filterRequestFromResponse(request, response, pollState);
    }

    @Override
    public void processEcuInitResponse(EcuInitCallback callback, byte[] response) {
        checkNotNull(callback, "callback");
        checkNotNullOrEmpty(response, "response");
        protocol.checkValidEcuInitResponse(response);
        EcuInit ecuInit = protocol.parseEcuInitResponse(response);
        callback.callback(ecuInit);
    }

    @Override
    public void processEcuResetResponse(byte[] response) {
        checkNotNullOrEmpty(response, "response");
        protocol.checkValidEcuResetResponse(response);
    }

    // processes the response bytes and sets individual responses on corresponding query objects
    @Override
    public void processReadAddressResponses(Collection<EcuQuery> queries, byte[] response, PollingState pollState) {
        checkNotNullOrEmpty(queries, "queries");
        checkNotNullOrEmpty(response, "response");
        byte[] responseData = extractResponseData(response);
        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        Map<String, byte[]> addressResults = new HashMap<String, byte[]>();

        int srcPos = 0;
        for (EcuQuery filteredQuery : filteredQueries) {
            byte[] bytes = new byte[getDataLength(filteredQuery)];
            if (((EcuData) filteredQuery.getLoggerData()).getGroupSize() > 0) {
                srcPos = filteredQuery.getBytes()[0];
            }
            arraycopy(responseData, srcPos, bytes, 0, bytes.length);
            addressResults.put(filteredQuery.getHex(), bytes);
            srcPos = 0;
        }
        for (EcuQuery query : queries) {
            query.setResponse(addressResults.get(query.getHex()));
        }
    }

    @Override
    public Protocol getProtocol() {
        return protocol;
    }

    @Override
    public byte[] constructWriteAddressRequest(
            Module module, byte[] writeAddress, byte value) {

        return protocol.constructWriteAddressRequest(module, writeAddress, value);
    }

    @Override
    public void processWriteResponse(byte[] data, byte[] response) {
        checkNotNullOrEmpty(data, "data");
        checkNotNullOrEmpty(response, "response");
        protocol.checkValidWriteResponse(data, response);
    }

    private Collection<EcuQuery> filterDuplicates(Collection<EcuQuery> queries) {
        Collection<EcuQuery> filteredQueries = new ArrayList<EcuQuery>();
        for (EcuQuery query : queries) {
            if (!filteredQueries.contains(query)) {
                filteredQueries.add(query);
            }
        }
        return filteredQueries;
    }

    private byte[][] convertToByteAddresses(Collection<EcuQuery> queries) {
        int byteCount = 0;
        int addressSize = 0;
        for (EcuQuery query : queries) {
            byteCount += query.getAddresses().length;
            addressSize = query.getBytes().length;
        }
        final byte[][] addresses = new byte[byteCount][addressSize];
        int i = 0;
        for (EcuQuery query : queries) {
            final int addrLength = query.getBytes().length;
            final byte[] bytes = query.getBytes();
            for (int j = 0; j < bytes.length / addrLength; j++) {
                arraycopy(bytes, j * addrLength, addresses[i++], 0, addrLength);
            }
        }
        return addresses;
    }

    private int getDataLength(EcuQuery ecuQuery) {
        int dataLength = 1;
        final String dataType =
                ecuQuery.getLoggerData().getSelectedConvertor().getDataType().toLowerCase();
        if (dataType.contains("int16")) {
            dataLength = 2;
        }
        else if (dataType.contains("int32") ||
                 dataType.contains("float")) {
            dataLength = 4;
        }
        return dataLength;
    }

    private int getDataLength(Collection<EcuQuery> queries) {
        int dataLength = 0;
        for (EcuQuery query : queries) {
            final String dataType =
                    query.getLoggerData().getSelectedConvertor().getDataType().toLowerCase();
            if (dataType.contains("int16")) {
                dataLength += 2;
            }
            else if (dataType.contains("int32") ||
                     dataType.contains("float")) {
                dataLength += 4;
            }
            else {
                dataLength += 1;
            }
        }
        return dataLength;
    }
}
