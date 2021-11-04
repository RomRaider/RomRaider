/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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

package com.romraider.io.protocol.ncs.iso15765;

import static com.romraider.io.protocol.ncs.iso15765.NCSProtocol.RESPONSE_NON_DATA_BYTES;
import static com.romraider.io.protocol.ncs.iso15765.NCSResponseProcessor.extractResponseData;
import static com.romraider.io.protocol.ncs.iso15765.NCSResponseProcessor.filterRequestFromResponse;
import static com.romraider.util.HexUtil.hexToInt;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static java.lang.System.arraycopy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.romraider.io.protocol.Protocol;
import com.romraider.io.protocol.ProtocolNCS;
import com.romraider.logger.ecu.comms.io.protocol.LoggerProtocolNCS;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.query.EcuInit;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.comms.query.EcuQueryData;
import com.romraider.logger.ecu.definition.Module;

public final class NCSLoggerProtocol implements LoggerProtocolNCS {
    private final ProtocolNCS protocol = new NCSProtocol();

    @Override
    public byte[] constructEcuFastInitRequest(Module module) {
        return protocol.constructEcuFastInitRequest(module);
    }

    @Override
    public byte[] constructStartDiagRequest(Module module) {
        return protocol.constructStartDiagRequest(module);
    }

    @Override
    public byte[] constructElevatedDiagRequest(Module module) {
        return protocol.constructElevatedDiagRequest(module);
    }

    @Override
    public byte[] constructEcuInitRequest(Module module) {
        return protocol.constructEcuInitRequest(module);
    }

    @Override
    public byte[] constructEcuStopRequest(Module module) {
        return protocol.constructEcuStopRequest(module);
    }

    @Override
    public byte[] constructEcuIdRequest(Module module) {
        return protocol.constructEcuIdRequest(module);
    }

    @Override
    public byte[] constructEcuResetRequest(Module module, int resetCode) {
        return protocol.constructEcuResetRequest(module, resetCode);
    }

    @Override
    public byte[] constructReadAddressRequest(Module module,
            Collection<EcuQuery> queries) {
    return protocol.constructReadAddressRequest(
            module, convertToByteAddresses(queries));
    }

    @Override
    public byte[] constructReadAddressRequest(
            Module module, Collection<EcuQuery> queries, PollingState pollState) {
        return protocol.constructReadAddressRequest(
                module, new byte[0][0], pollState);
    }

    @Override
    public byte[] constructReadSidPidRequest(Module module, byte sid, byte[] pid) {
        final byte[][] request = new byte[1][pid.length];
        arraycopy(pid, 0, request[0], 0, pid.length);
        return protocol.constructReadSidPidRequest(module, sid, request);
    }

    @Override
    public byte[] constructLoadAddressRequest(Collection<EcuQuery> queries) {
        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        // convert to address and data length
        return protocol.constructLoadAddressRequest(
                convertToByteAddressAndLen(filteredQueries));
    }

    @Override
    public byte[] constructReadMemoryRequest(Module module,
            Collection<EcuQuery> queries, int length) {

        return protocol.constructReadMemoryRequest(
                module, convertToByteAddresses(queries), length);
    }

    @Override
    public byte[] constructReadMemoryResponse(int requestSize, int length) {
        return new byte[RESPONSE_NON_DATA_BYTES + requestSize + length];
    }

    @Override
    public void validateLoadAddressResponse(byte[] response) {
        protocol.validateLoadAddressResponse(response);
    }

    @Override
    public byte[] constructReadAddressResponse(
            Collection<EcuQuery> queries, PollingState pollState) {

        checkNotNullOrEmpty(queries, "queries");
        int numBytes = 7;
        if (pollState.isFastPoll()) {
            numBytes = 6;
        }
        // CAN addr
        // one byte  - Response sid
        // one byte  - Response pid
        // one byte  - option
        // variable bytes of data defined for pid
        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        for (EcuQuery ecuQuery : filteredQueries) {
            //numBytes += ecuQuery.getBytes().length;
            numBytes += EcuQueryData.getDataLength(ecuQuery); 
        }
        return new byte[(numBytes)];
    }

    @Override
    public byte[] preprocessResponse(
            byte[] request, byte[] response, PollingState pollState) {

        return filterRequestFromResponse(request, response, pollState);
    }

    @Override
    public void processEcuInitResponse(EcuInitCallback callback, byte[] response) {
        checkNotNull(callback, "callback");
        checkNotNullOrEmpty(response, "response");
        EcuInit ecuInit = protocol.parseEcuInitResponse(response);
        callback.callback(ecuInit);
    }

    @Override
    public byte[] processEcuIdResponse(byte[] response) {
        checkNotNullOrEmpty(response, "response");
        return protocol.parseResponseData(response);
    }

    @Override
    public byte[] processReadSidPidResponse(byte[] response) {
        checkNotNullOrEmpty(response, "response");
        return protocol.checkValidSidPidResponse(response);
    }

    @Override
    public void processEcuResetResponse(byte[] response) {
        checkNotNullOrEmpty(response, "response");
        protocol.checkValidEcuResetResponse(response);
    }

    // processes the response bytes and sets individual responses on corresponding query objects
    @Override
    public void processReadAddressResponses(
            Collection<EcuQuery> queries, byte[] response, PollingState pollState) {

        checkNotNullOrEmpty(queries, "queries");
        checkNotNullOrEmpty(response, "response");
        final byte[] responseData = extractResponseData(response);
        final Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        final Map<String, byte[]> addressResults = new HashMap<String, byte[]>();
        int i = 0;
        for (EcuQuery filteredQuery : filteredQueries) {
            final int dataLength = EcuQueryData.getDataLength(filteredQuery);
            final byte[] data = new byte[dataLength];
            arraycopy(responseData, i, data, 0, dataLength);
            addressResults.put(filteredQuery.getHex(), data);
            i += dataLength;
        }
        for (EcuQuery query : queries) {
            query.setResponse(addressResults.get(query.getHex()));
        }
    }

    /**
     * Processes the response bytes and set individual response on corresponding
     * query objects.
     * The response data is based on the lowest EcuData address and the length
     * is the result of the difference between the highest and lowest address.
     * The index into the response array is based in the lowest address. 
     **/
    public void processReadMemoryResponses(
            Collection<EcuQuery> queries, byte[] response) {
        
        checkNotNullOrEmpty(queries, "queries");
        checkNotNullOrEmpty(response, "response");
        final byte[] responseData = extractResponseData(response);
        final Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        final Map<String, byte[]> addressResults = new HashMap<String, byte[]>();

        int lowestAddress = Integer.MAX_VALUE;
        for (EcuQuery filteredQuery : filteredQueries) {
            final int address = hexToInt(filteredQuery.getAddresses()[0]);
            if (address < lowestAddress) {
                lowestAddress = address;
            }
        }

        int srcPos = 0;
        for (EcuQuery filteredQuery : filteredQueries) {
            int dataTypeLength = EcuQueryData.getDataLength(filteredQuery);
            final byte[] bytes = new byte[dataTypeLength];
            final int address = hexToInt(filteredQuery.getAddresses()[0]);
            srcPos = address - lowestAddress;
            arraycopy(responseData, srcPos, bytes, 0, bytes.length);
            addressResults.put(filteredQuery.getHex(), bytes);
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

    @Override
    public Collection<EcuQuery> filterDuplicates(Collection<EcuQuery> queries) {
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
        for (EcuQuery query : queries) {
            byteCount += query.getAddresses().length;
        }
        byte[][] addresses = new byte[byteCount][];
        int i = 0;
        for (EcuQuery query : queries) {
            byte[] bytes = query.getBytes();
            int addrCount = query.getAddresses().length;
            int addrLen = bytes.length / addrCount;
            for (int j = 0; j < addrCount; j++) {
                final byte[] addr = new byte[addrLen];
                arraycopy(bytes, j * addrLen, addr, 0, addr.length);
                addresses[i++] = addr;
            }
        }
        return addresses;
    }

    private Map<byte[], Integer> convertToByteAddressAndLen(Collection<EcuQuery> queries) {
        final Map<byte[], Integer> queryMap = new LinkedHashMap<byte[], Integer>();
        for (EcuQuery query : queries) {
            queryMap.put(query.getBytes(), EcuQueryData.getDataLength(query));
        }
        return queryMap;
    }
}
