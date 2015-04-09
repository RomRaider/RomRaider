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

package com.romraider.io.protocol.obd.iso15765;

import static com.romraider.io.protocol.obd.iso15765.OBDProtocol.RESPONSE_NON_DATA_BYTES;
import static com.romraider.io.protocol.obd.iso15765.OBDResponseProcessor.extractResponseData;
import static com.romraider.io.protocol.obd.iso15765.OBDResponseProcessor.filterRequestFromResponse;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static java.lang.System.arraycopy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.romraider.io.protocol.Protocol;
import com.romraider.logger.ecu.comms.io.protocol.LoggerProtocolOBD;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.query.EcuInit;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.comms.query.EcuQueryData;
import com.romraider.logger.ecu.definition.Module;

public final class OBDLoggerProtocol implements LoggerProtocolOBD {
    private final Protocol protocol = new OBDProtocol();

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

        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        return protocol.constructReadAddressRequest(
                module, convertToByteAddresses(filteredQueries));
    }

    @Override
    public byte[] constructReadPidRequest(Module module, byte[] pid) {
        final byte[][] request = new byte[1][pid.length];
        arraycopy(pid, 0, request[0], 0, pid.length);
        return protocol.constructReadAddressRequest(module, request);
    }

    @Override
    public byte[] constructReadAddressResponse(
            Collection<EcuQuery> queries, PollingState pollState) {

        checkNotNullOrEmpty(queries, "queries");
        // four byte - CAN ID
        // one byte  - Response mode
        // one byte  - Response pid
        // variable bytes of data defined for pid
        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        int numAddresses = 0;
        for (EcuQuery ecuQuery : filteredQueries) {
            numAddresses += ecuQuery.getBytes().length;
            numAddresses += EcuQueryData.getDataLength(ecuQuery); 
        }
        return new byte[(numAddresses + RESPONSE_NON_DATA_BYTES)];
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
    public void processReadAddressResponses(
            Collection<EcuQuery> queries, byte[] response, PollingState pollState) {

        checkNotNullOrEmpty(queries, "queries");
        checkNotNullOrEmpty(response, "response");
        final byte[] responseData = extractResponseData(response);
        final Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        final Map<String, byte[]> addressResults = new HashMap<String, byte[]>();
        int i = 0;
        for (EcuQuery filteredQuery : filteredQueries) {
            final int addrLength = filteredQuery.getBytes().length;
            final int dataLength = EcuQueryData.getDataLength(filteredQuery);
            final byte[] addr = new byte[addrLength];
            final byte[] data = new byte[dataLength];
            arraycopy(responseData, i, addr, 0, addrLength);
            arraycopy(responseData, i + addrLength, data, 0, dataLength);
            addressResults.put(asHex(addr), data);
            i += addrLength + dataLength;
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
        for (EcuQuery query : queries) {
            byteCount += query.getAddresses().length;
        }
        final int ADDRESS_SIZE = 1;
        // TODO how do we handle variable address lengths ?
        final byte[][] addresses = new byte[byteCount][ADDRESS_SIZE];
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
}
