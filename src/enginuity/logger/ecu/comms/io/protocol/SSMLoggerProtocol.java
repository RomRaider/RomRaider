/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.logger.ecu.comms.io.protocol;

import enginuity.io.protocol.Protocol;
import enginuity.io.protocol.SSMProtocol;
import static enginuity.io.protocol.SSMProtocol.ADDRESS_SIZE;
import static enginuity.io.protocol.SSMProtocol.DATA_SIZE;
import static enginuity.io.protocol.SSMProtocol.REQUEST_NON_DATA_BYTES;
import static enginuity.io.protocol.SSMProtocol.RESPONSE_NON_DATA_BYTES;
import static enginuity.io.protocol.SSMResponseProcessor.extractResponseData;
import static enginuity.io.protocol.SSMResponseProcessor.filterRequestFromResponse;
import enginuity.logger.ecu.comms.query.EcuQuery;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class SSMLoggerProtocol implements LoggerProtocol {
    private final Protocol protocol = new SSMProtocol();

    public byte[] constructReadAddressRequest(Collection<EcuQuery> queries) {
        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        return protocol.constructReadAddressRequest(convertToByteAddresses(filteredQueries));
    }

    @SuppressWarnings({"PointlessArithmeticExpression"})
    public byte[] constructReadAddressResponse(Collection<EcuQuery> queries) {
        checkNotNullOrEmpty(queries, "queries");
        // 0x80 0xF0 0x10 data_length 0xE8 value1 value2 ... valueN checksum
        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        int numAddresses = 0;
        for (EcuQuery ecuQuery : filteredQueries) {
            numAddresses += (ecuQuery.getBytes().length / ADDRESS_SIZE);
        }
        return new byte[(numAddresses * DATA_SIZE + RESPONSE_NON_DATA_BYTES) + (numAddresses * ADDRESS_SIZE + REQUEST_NON_DATA_BYTES)];
    }

    public byte[] preprocessResponse(byte[] request, byte[] response) {
        return filterRequestFromResponse(request, response);
    }

    // processes the response bytes and sets individual responses on corresponding query objects
    @SuppressWarnings({"PointlessArithmeticExpression"})
    public void processReadAddressResponses(Collection<EcuQuery> queries, byte[] response) {
        checkNotNullOrEmpty(queries, "queries");
        checkNotNullOrEmpty(response, "response");
        byte[] responseData = extractResponseData(response);
        Collection<EcuQuery> filteredQueries = filterDuplicates(queries);
        Map<String, byte[]> addressResults = new HashMap<String, byte[]>();
        int i = 0;
        for (EcuQuery filteredQuery : filteredQueries) {
            byte[] bytes = new byte[DATA_SIZE * (filteredQuery.getBytes().length / ADDRESS_SIZE)];
            System.arraycopy(responseData, i, bytes, 0, bytes.length);
            addressResults.put(filteredQuery.getHex(), bytes);
            i += bytes.length;
        }
        for (EcuQuery query : queries) {
            query.setResponse(addressResults.get(query.getHex()));
        }
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
                System.arraycopy(bytes, j * ADDRESS_SIZE, addresses[i++], 0, ADDRESS_SIZE);
            }
        }
        return addresses;
    }

}
