package enginuity.logger.protocol;

import enginuity.logger.query.DefaultRegisteredQuery;
import enginuity.logger.query.LoggerCallback;
import enginuity.logger.query.RegisteredQuery;
import static enginuity.util.HexUtil.asBytes;
import static enginuity.util.HexUtil.asHex;
import static enginuity.util.ParamChecker.checkGreaterThanZero;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

import java.util.ArrayList;
import java.util.Collection;

public final class SSMProtocol implements Protocol {
    private static final byte HEADER = (byte) 0x80;
    private static final byte ECU_ID = (byte) 0x10;
    private static final byte DIAGNOSTIC_TOOL_ID = (byte) 0xF0;
    private static final byte READ_PADDING = (byte) 0x00;
    private static final byte READ_MEMORY_COMMAND = (byte) 0xA0;
    private static final byte READ_ADDRESS_COMMAND = (byte) 0xA8;
    private static final byte ECU_INIT_COMMAND = (byte) 0xBF;
    private static final int ADDRESS_SIZE = 3;
    private static final int DATA_SIZE = 1;


    public byte[] constructReadMemoryRequest(RegisteredQuery query, int numBytes) {
        checkNotNull(query, "query");
        checkGreaterThanZero(numBytes, "numBytes");
        // 0x80 0x10 0xF0 data_length 0xA0 padding from_address num_bytes-1 checksum
        return buildRequest(READ_MEMORY_COMMAND, true, query.getBytes(), new byte[]{asByte(numBytes - 1)});
    }

    public byte[] constructReadAddressRequest(Collection<RegisteredQuery> queries) {
        checkNotNullOrEmpty(queries, "queries");
        // 0x80 0x10 0xF0 data_length 0xA8 padding address1 address2 ... addressN checksum
        return buildRequest(READ_ADDRESS_COMMAND, true, convertToByteAddresses(queries));
    }

    //TODO: Query responses are variable length!!
    public byte[] constructReadAddressResponse(Collection<RegisteredQuery> queries) {
        checkNotNullOrEmpty(queries, "queries");
        return new byte[DATA_SIZE * queries.size() + 5];
    }

    public void setResponse(Collection<RegisteredQuery> queries, byte[] response) {
        checkNotNullOrEmpty(queries, "queries");
        checkNotNullOrEmpty(response, "response");
        byte[] responseData = extractResponseData(response);
        int i = 0;
        for (RegisteredQuery query : queries) {
            byte[] bytes = new byte[DATA_SIZE];
            System.arraycopy(responseData, i, bytes, 0, DATA_SIZE);
            query.setResponse(bytes);
            i += DATA_SIZE;
        }
    }

    public byte[] constructEcuInitRequest() {
        // 0x80 0x10 0xF0 0x01 0xBF 0x40
        return buildRequest(ECU_INIT_COMMAND, false, new byte[0]);
    }

    public byte[] extractResponseData(byte[] response) {
        checkNotNullOrEmpty(response, "response");
        // 0x80 0xF0 0x10 data_length response_data checksum
        //TODO: Take possible echoed request into account when extracting response!!
        validateResponse(response);
        byte[] data = new byte[response.length - 5];
        System.arraycopy(response, 4, data, 0, data.length);
        return data;
    }

    public ConnectionProperties getConnectionProperties() {
        return new ConnectionProperties() {

            public int getBaudRate() {
                return 4800;
            }

            public int getDataBits() {
                return 8;
            }

            public int getStopBits() {
                return 1;
            }

            public int getParity() {
                return 0;
            }
        };
    }

    private byte[][] convertToByteAddresses(Collection<RegisteredQuery> queries) {
        byte[][] addresses = new byte[queries.size()][ADDRESS_SIZE];
        int i = 0;
        for (RegisteredQuery query : queries) {
            byte[] bytes = query.getBytes();
            System.arraycopy(bytes, 0, addresses[i++], 0, ADDRESS_SIZE);
        }
        return addresses;
    }


    private void validateResponse(byte[] response) {
        int i = 0;
        assertEquals(HEADER, response[i++], "Invalid header");
        assertEquals(DIAGNOSTIC_TOOL_ID, response[i++], "Invalid diagnostic tool id");
        assertEquals(ECU_ID, response[i++], "Invalid ECU id");
        assertEquals(asByte(response.length - 5), response[i], "Invalid response data length");
        assertEquals(calculateChecksum(response), response[response.length - 1], "Invalid checksum");
    }

    private void assertEquals(byte expected, byte actual, String msg) {
        if (actual != expected) {
            throw new InvalidResponseException(msg + ". Expected: " + asHex(new byte[]{expected}) + ". Actual: " + asHex(new byte[]{actual}) + ".");
        }
    }

    //TODO: Clean up SSM request building... pretty ugly at the moment..
    private byte[] buildRequest(byte command, boolean padContent, byte[]... content) {
        byte[] data = new byte[0];
        for (byte[] tmp : content) {
            byte[] tmp2 = new byte[data.length + tmp.length];
            System.arraycopy(data, 0, tmp2, 0, data.length);
            System.arraycopy(tmp, 0, tmp2, data.length, tmp.length);
            data = tmp2;
        }
        byte[] request = new byte[data.length + (padContent ? 7 : 6)];
        int i = 0;
        request[i++] = HEADER;
        request[i++] = ECU_ID;
        request[i++] = DIAGNOSTIC_TOOL_ID;
        request[i++] = asByte(data.length + (padContent ? 2 : 1));
        request[i++] = command;
        if (padContent) {
            request[i++] = READ_PADDING;
        }
        System.arraycopy(data, 0, request, i, data.length);
        request[request.length - 1] = calculateChecksum(request);
        return request;
    }

    private byte calculateChecksum(byte[] request) {
        int total = 0;
        for (int i = 0; i < (request.length - 1); i++) {
            byte b = request[i];
            total += asInt(b);
        }
        return asByte(total - ((total >>> 16) << 16));
    }

    //TODO: Move these utility methods out to another class
    @SuppressWarnings({"UnnecessaryBoxing"})
    private byte asByte(int i) {
        return Integer.valueOf(i).byteValue();
    }

    @SuppressWarnings({"UnnecessaryBoxing"})
    private int asInt(byte b) {
        return Byte.valueOf(b).intValue();
    }

    public byte[] asByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }

    //****************** Test stuff ***********************//

    public static void main(String... args) {
        Protocol protocol = ProtocolFactory.getInstance().getProtocol("SSM");

        byte[] bytes = protocol.constructEcuInitRequest();
        System.out.println("Ecu Init                               = " + asHex(bytes));

        bytes = protocol.constructReadAddressRequest(constructRegisteredQueries("0x000008", "0x00001C"));
        System.out.println("Read Address (0x000008 and 0x00001C)   = " + asHex(bytes));

        bytes = protocol.extractResponseData(asBytes("0x80F01003E87DB199"));
        System.out.println("Extract Response (0x80F01003E87DB199)  = " + asHex(bytes));
    }

    private static Collection<RegisteredQuery> constructRegisteredQueries(String... addresses) {
        Collection<RegisteredQuery> queries = new ArrayList<RegisteredQuery>();
        for (String address : addresses) {
            queries.add(new DefaultRegisteredQuery(address, new LoggerCallback() {
                public void callback(byte[] value) {
                }
            }));
        }
        return queries;
    }

}
