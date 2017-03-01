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

package com.romraider.io.protocol.ssm.iso9141;

import static com.romraider.io.protocol.ssm.iso9141.SSMProtocol.ADDRESS_SIZE;
import static com.romraider.io.protocol.ssm.iso9141.SSMProtocol.DATA_SIZE;
import static com.romraider.io.protocol.ssm.iso9141.SSMProtocol.REQUEST_NON_DATA_BYTES;
import static com.romraider.io.protocol.ssm.iso9141.SSMProtocol.RESPONSE_NON_DATA_BYTES;
import static com.romraider.io.protocol.ssm.iso9141.SSMResponseProcessor.extractResponseData;
import static com.romraider.io.protocol.ssm.iso9141.SSMResponseProcessor.filterRequestFromResponse;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static java.lang.System.arraycopy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.romraider.io.protocol.Protocol;
import com.romraider.logger.ecu.comms.io.protocol.LoggerProtocol;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.query.EcuInit;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.definition.Module;

public final class SSMLoggerProtocol implements LoggerProtocol {
    private final Protocol protocol = new SSMProtocol();

    public byte[] constructEcuInitRequest(Module module) {
        return protocol.constructEcuInitRequest(module);
    }

    public byte[] constructEcuResetRequest(Module module, int resetCode) {
        return protocol.constructEcuResetRequest(module, resetCode);
    }

    public byte[] constructReadAddressRequest(Module module, Collection<EcuQuery> queries) {
        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        return protocol.constructReadAddressRequest(module, convertToByteAddresses(filteredQueries));
    }

    public byte[] constructReadAddressResponse(Collection<EcuQuery> queries, PollingState pollState) {
        checkNotNullOrEmpty(queries, "queries");
        checkNotNull(pollState, "pollState");
        // 0x80 0xF0 0x10 data_length 0xE8 value1 value2 ... valueN checksum
        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        int numAddresses = 0;
        for (EcuQuery ecuQuery : filteredQueries) {
            numAddresses += (ecuQuery.getBytes().length / ADDRESS_SIZE);
        }
        switch (pollState.getCurrentState()) {
            case STATE_0:
            return new byte[(numAddresses * DATA_SIZE + RESPONSE_NON_DATA_BYTES) + (numAddresses * ADDRESS_SIZE + REQUEST_NON_DATA_BYTES)];
            case STATE_1:
            return new byte[(numAddresses * DATA_SIZE + RESPONSE_NON_DATA_BYTES)];
        default:
            throw new UnsupportedOperationException("Poll mode not supported:" + pollState.getCurrentState());
        }
    }

    public byte[] preprocessResponse(byte[] request, byte[] response, PollingState pollState) {
        return filterRequestFromResponse(request, response, pollState);
    }

    public void processEcuInitResponse(EcuInitCallback callback, byte[] response) {
        checkNotNull(callback, "callback");
        checkNotNullOrEmpty(response, "response");
        protocol.checkValidEcuInitResponse(response);
        EcuInit ecuInit = protocol.parseEcuInitResponse(response);
        callback.callback(ecuInit);
    }

    public void processEcuResetResponse(byte[] response) {
        checkNotNullOrEmpty(response, "response");
        protocol.checkValidEcuResetResponse(response);
    }

    // processes the response bytes and sets individual responses on corresponding query objects
    public void processReadAddressResponses(Collection<EcuQuery> queries, byte[] response, PollingState pollState) {
        checkNotNullOrEmpty(queries, "queries");
        checkNotNullOrEmpty(response, "response");
        byte[] responseData = extractResponseData(response);
        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        Map<String, byte[]> addressResults = new HashMap<String, byte[]>();
        int i = 0;
        for (EcuQuery filteredQuery : filteredQueries) {
            byte[] bytes = new byte[DATA_SIZE * (filteredQuery.getBytes().length / ADDRESS_SIZE)];
            arraycopy(responseData, i, bytes, 0, bytes.length);
            addressResults.put(filteredQuery.getHex(), bytes);
            i += bytes.length;
        }
        for (EcuQuery query : queries) {
            query.setResponse(addressResults.get(query.getHex()));
        }
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public byte[] constructWriteAddressRequest(
            Module module, byte[] writeAddress, byte value) {

        return protocol.constructWriteAddressRequest(module, writeAddress, value);
    }

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
        byte[][] addresses = new byte[byteCount][ADDRESS_SIZE];
        int i = 0;
        for (EcuQuery query : queries) {
            byte[] bytes = query.getBytes();
            for (int j = 0; j < bytes.length / ADDRESS_SIZE; j++) {
                arraycopy(bytes, j * ADDRESS_SIZE, addresses[i++], 0, ADDRESS_SIZE);
            }
        }
        return addresses;
    }
}
