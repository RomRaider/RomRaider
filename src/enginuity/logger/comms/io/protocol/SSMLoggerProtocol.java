package enginuity.logger.comms.io.protocol;

import enginuity.io.connection.ConnectionProperties;
import enginuity.io.protocol.Protocol;
import enginuity.io.protocol.SSMProtocol;
import static enginuity.io.protocol.SSMProtocol.ADDRESS_SIZE;
import static enginuity.io.protocol.SSMProtocol.DATA_SIZE;
import static enginuity.io.protocol.SSMProtocol.DIAGNOSTIC_TOOL_ID;
import static enginuity.io.protocol.SSMProtocol.ECU_ID;
import static enginuity.io.protocol.SSMProtocol.ECU_INIT_RESPONSE;
import static enginuity.io.protocol.SSMProtocol.HEADER;
import static enginuity.io.protocol.SSMProtocol.READ_ADDRESS_RESPONSE;
import static enginuity.io.protocol.SSMProtocol.READ_MEMORY_RESPONSE;
import static enginuity.io.protocol.SSMProtocol.REQUEST_NON_DATA_BYTES;
import static enginuity.io.protocol.SSMProtocol.RESPONSE_NON_DATA_BYTES;
import enginuity.logger.comms.query.RegisteredQuery;
import enginuity.logger.exception.InvalidResponseException;
import static enginuity.util.ByteUtil.asByte;
import static enginuity.util.HexUtil.asHex;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class SSMLoggerProtocol implements LoggerProtocol {
    private final Protocol protocol;

    public SSMLoggerProtocol() {
        protocol = new SSMProtocol();
    }

    public byte[] constructReadAddressRequest(Collection<RegisteredQuery> queries) {
        Collection<RegisteredQuery> filteredQueries = filterDuplicates(queries);
        return protocol.constructReadAddressRequest(convertToByteAddresses(filteredQueries));
    }

    @SuppressWarnings({"PointlessArithmeticExpression"})
    public byte[] constructReadAddressResponse(Collection<RegisteredQuery> queries) {
        checkNotNullOrEmpty(queries, "queries");
        // 0x80 0xF0 0x10 data_length 0xE8 value1 value2 ... valueN checksum
        Collection<RegisteredQuery> filteredQueries = filterDuplicates(queries);
        int numAddresses = 0;
        for (RegisteredQuery registeredQuery : filteredQueries) {
            numAddresses += (registeredQuery.getBytes().length / ADDRESS_SIZE);
        }
        return new byte[(numAddresses * DATA_SIZE + RESPONSE_NON_DATA_BYTES) + (numAddresses * ADDRESS_SIZE + REQUEST_NON_DATA_BYTES)];
    }

    // processes the response bytes and sets individual responses on corresponding query objects
    @SuppressWarnings({"PointlessArithmeticExpression"})
    public void processReadAddressResponses(Collection<RegisteredQuery> queries, byte[] response) {
        checkNotNullOrEmpty(queries, "queries");
        checkNotNullOrEmpty(response, "response");
        byte[] responseData = extractResponseData(response);
        Collection<RegisteredQuery> filteredQueries = filterDuplicates(queries);
        Map<String, byte[]> addressResults = new HashMap<String, byte[]>();
        int i = 0;
        for (RegisteredQuery filteredQuery : filteredQueries) {
            byte[] bytes = new byte[DATA_SIZE * (filteredQuery.getBytes().length / ADDRESS_SIZE)];
            System.arraycopy(responseData, i, bytes, 0, bytes.length);
            addressResults.put(filteredQuery.getHex(), bytes);
            i += bytes.length;
        }
        for (RegisteredQuery query : queries) {
            query.setResponse(addressResults.get(query.getHex()));
        }
    }

    public ConnectionProperties getConnectionProperties() {
        return protocol.getConnectionProperties();
    }

    private Collection<RegisteredQuery> filterDuplicates(Collection<RegisteredQuery> queries) {
//        System.out.println("queries         = " + queries);
        Collection<RegisteredQuery> filteredQueries = new ArrayList<RegisteredQuery>();
        for (RegisteredQuery query : queries) {
            if (!filteredQueries.contains(query)) {
                filteredQueries.add(query);
            }
        }
//        System.out.println("filteredQueries = " + filteredQueries);
        return filteredQueries;
    }

    private byte[][] convertToByteAddresses(Collection<RegisteredQuery> queries) {
        int byteCount = 0;
        for (RegisteredQuery query : queries) {
            byteCount += query.getAddresses().length;
        }
        byte[][] addresses = new byte[byteCount][ADDRESS_SIZE];
        int i = 0;
        for (RegisteredQuery query : queries) {
            byte[] bytes = query.getBytes();
            for (int j = 0; j < bytes.length / ADDRESS_SIZE; j++) {
                System.arraycopy(bytes, j * ADDRESS_SIZE, addresses[i++], 0, ADDRESS_SIZE);
            }
        }
        return addresses;
    }

    private byte[] extractResponseData(byte[] response) {
        checkNotNullOrEmpty(response, "response");
        // 0x80 0xF0 0x10 data_length 0xE8 response_data checksum
        validateResponse(response);
        byte[] data = new byte[response.length - RESPONSE_NON_DATA_BYTES];
        System.arraycopy(response, (RESPONSE_NON_DATA_BYTES - 1), data, 0, data.length);
        return data;
    }


    private void validateResponse(byte[] response) {
        int i = 0;
        assertEquals(HEADER, response[i++], "Invalid header");
        assertEquals(DIAGNOSTIC_TOOL_ID, response[i++], "Invalid diagnostic tool id");
        assertEquals(ECU_ID, response[i++], "Invalid ECU id");
        assertEquals(asByte(response.length - RESPONSE_NON_DATA_BYTES + 1), response[i++], "Invalid response data length");
        assertOneOf(new byte[]{READ_ADDRESS_RESPONSE, READ_MEMORY_RESPONSE, ECU_INIT_RESPONSE}, response[i], "Invalid response code");
        assertEquals(protocol.calculateChecksum(response), response[response.length - 1], "Invalid checksum");
    }

    private void assertEquals(byte expected, byte actual, String msg) {
        if (actual != expected) {
            throw new InvalidResponseException(msg + ". Expected: " + asHex(new byte[]{expected}) + ". Actual: " + asHex(new byte[]{actual}) + ".");
        }
    }

    private void assertOneOf(byte[] validOptions, byte actual, String msg) {
        for (byte option : validOptions) {
            if (option == actual) {
                return;
            }
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < validOptions.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(asHex(new byte[]{validOptions[i]}));
        }
        throw new InvalidResponseException(msg + ". Expected one of [" + builder.toString() + "]. Actual: " + asHex(new byte[]{actual}) + ".");
    }

}
