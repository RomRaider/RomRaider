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

package com.romraider.io.protocol.ds2.iso9141;

import static com.romraider.io.protocol.ds2.iso9141.DS2Protocol.RESPONSE_NON_DATA_BYTES;
import static com.romraider.io.protocol.ds2.iso9141.DS2ResponseProcessor.extractResponseData;
import static com.romraider.io.protocol.ds2.iso9141.DS2ResponseProcessor.filterRequestFromResponse;
import static com.romraider.util.ByteUtil.asUnsignedInt;
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
import com.romraider.logger.ecu.comms.query.EcuQueryData;
import com.romraider.logger.ecu.definition.EcuData;
import com.romraider.logger.ecu.definition.Module;

public final class DS2LoggerProtocol implements LoggerProtocolDS2 {
    private final ProtocolDS2 protocol = new DS2Protocol();

    @Override
    public byte[] constructEcuInitRequest(Module module) {
        return protocol.constructEcuInitRequest(module);
    }

    @Override
    public byte[] constructEcuResetRequest(Module module, int resetCode) {
        return protocol.constructEcuResetRequest(module, resetCode);
    }

    @Override
    public byte[] constructReadAddressRequest(
            Module module, Collection<EcuQuery> queries) {

        return protocol.constructReadAddressRequest(
                module, new byte[][]{});
    }

    @Override
    public byte[] constructReadProcedureRequest(
            Module module, Collection<EcuQuery> queries) {

        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        return protocol.constructReadProcedureRequest(
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
    public byte[] constructReadMemoryRange(
            Module module, Collection<EcuQuery> queries, int length) {

        return protocol.constructReadMemoryRequest(
                module, convertToByteAddresses(queries), length);
    }

    /**
     * Convert address for the list of queries into the hex format the
     * ECU expects.
     **/
    @Override
    public byte[] constructSetAddressRequest(Module module,
            Collection<EcuQuery> queries) {
        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        return protocol.constructSetAddressRequest(
                module, convertListToByteAddresses(filteredQueries));
    }

    /**
     * An Address set response is a 4 byte ACK sequence
     **/
    @Override
    public byte[] constructSetAddressResponse(int requestSize) {
        return new byte[requestSize + RESPONSE_NON_DATA_BYTES];
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
        int responseBytes = requestSize + RESPONSE_NON_DATA_BYTES;
        for (EcuQuery ecuQuery : filteredQueries) {
            responseBytes += EcuQueryData.getDataLength(ecuQuery); 
        }
        return new byte[responseBytes];
    }

    @Override
    public byte[] constructReadMemoryRangeResponse(int requestSize, int length) {

        return new byte[requestSize + RESPONSE_NON_DATA_BYTES + length];
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

    /**
     * Processes the response bytes and set individual response on corresponding
     * query objects.
     * If EcuData has a group size value greater than 0 then the response is
     * the result of a group read and the address is the index into the response
     * array. 
     **/
    @Override
    public void processReadAddressResponse(Collection<EcuQuery> queries, byte[] response, PollingState pollState) {
        checkNotNullOrEmpty(queries, "queries");
        checkNotNullOrEmpty(response, "response");
        byte[] responseData = extractResponseData(response);
        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        Map<String, byte[]> addressResults = new HashMap<String, byte[]>();

        int srcPos = 0;
        for (EcuQuery filteredQuery : filteredQueries) {
            byte[] bytes = new byte[EcuQueryData.getDataLength(filteredQuery)];
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

    /**
     * Processes the response bytes and set individual response on corresponding
     * query objects.
     * The response data is based on the lowest EcuData address and the length
     * is the result of the difference between the highest and lowest address.
     * The index into the response array is based in the lowest address. 
     **/
    public void processReadMemoryRangeResponse(
            Collection<EcuQuery> queries, byte[] response) {
        
        checkNotNullOrEmpty(queries, "queries");
        checkNotNullOrEmpty(response, "response");
        final byte[] responseData = extractResponseData(response);
        final Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        final Map<String, byte[]> addressResults = new HashMap<String, byte[]>();

        int lowestAddress = Integer.MAX_VALUE;
        for (EcuQuery filteredQuery : filteredQueries) {
            final int address = Integer.parseInt(filteredQuery.getHex(), 16);
            if (address < lowestAddress) {
                lowestAddress = address;
            }
        }

        int srcPos = 0;
        for (EcuQuery filteredQuery : filteredQueries) {
            final byte[] bytes = new byte[EcuQueryData.getDataLength(filteredQuery)];
            srcPos = Integer.parseInt(filteredQuery.getHex(), 16) - lowestAddress;
            arraycopy(responseData, srcPos, bytes, 0, bytes.length);
            addressResults.put(filteredQuery.getHex(), bytes);
        }

        for (EcuQuery query : queries) {
            query.setResponse(addressResults.get(query.getHex()));
        }
    }

    @Override
    public void processReadAddressResponses(Collection<EcuQuery> queries,
            byte[] response, PollingState pollState) {
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
     * Validate response headers, length and checksum 
     **/
    @Override
    public void validateSetAddressResponse(byte[] response) {
        checkNotNullOrEmpty(response, "response");
        protocol.validateSetAddressResponse(response);
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

    private byte[][] convertListToByteAddresses(Collection<EcuQuery> queries) {
        /*
         * Address format is five bytes. First byte is data type
         * (byte, word, procedure) followed by the four byte address.
         */
        final byte[][] addresses = new byte[queries.size()][5];
        int i = 0;
        for (EcuQuery query : queries) {
            final int addrLength = query.getBytes().length;
            final byte[] bytes = query.getBytes();
            for (int j = 0; j < bytes.length / addrLength; j++) {
                arraycopy(bytes, j * addrLength, addresses[i], 1, addrLength);
            }
            // Update the first byte to the appropriate data type
            // 0 = byte, 1 = word and 2 = procedure
            // procedure
            if (asUnsignedInt(bytes) < 0x1C) {
                addresses[i][0] = 2;
            }
            // word or byte
            else {
                addresses[i][0] = (byte) (EcuQueryData.getDataLength(query) - 1);
            }
            i++;
        }
        return addresses;
    }

    private int getDataLength(Collection<EcuQuery> queries) {
        int dataLength = 0;
        for (EcuQuery query : queries) {
            dataLength += EcuQueryData.getDataLength(query);
        }
        return dataLength;
    }
}
