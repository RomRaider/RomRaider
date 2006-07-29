package enginuity.logger.protocol;

import static enginuity.util.HexUtil.asBytes;
import static enginuity.util.HexUtil.asHex;
import static enginuity.util.ParamChecker.checkGreaterThanZero;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class SSMProtocol implements Protocol {
    private static final byte HEADER = (byte) 0x80;
    private static final byte ECU_ID = (byte) 0x10;
    private static final byte DIAGNOSTIC_TOOL_ID = (byte) 0xF0;
    private static final byte READ_PADDING = (byte) 0x00;
    private static final byte READ_MEMORY_COMMAND = (byte) 0xA0;
    private static final byte READ_ADDRESS_COMMAND = (byte) 0xA8;
    private static final byte ECU_INIT_COMMAND = (byte) 0xBF;


    public byte[] constructReadMemoryRequest(byte[] fromAddress, int numBytes) {
        checkNotNullOrEmpty(fromAddress, "fromAddress");
        checkGreaterThanZero(numBytes, "numBytes");
        // 0x80 0x10 0xF0 data_length 0xA0 padding from_address num_bytes-1 checksum
        return buildRequest(READ_MEMORY_COMMAND, true, fromAddress, new byte[]{asByte(numBytes - 1)});
    }

    public byte[] constructReadAddressRequest(byte[]... addresses) {
        checkNotNullOrEmpty(addresses, "addresses");
        // 0x80 0x10 0xF0 data_length 0xA8 padding address1 address2 ... addressN checksum
        return buildRequest(READ_ADDRESS_COMMAND, true, addresses);
    }

    public byte[] constructEcuInitRequest() {
        // 0x80 0x10 0xF0 0x01 0xBF 0x40
        return buildRequest(ECU_INIT_COMMAND, false, new byte[0]);
    }

    public byte[] extractResponseData(byte[] response, byte[] request) {
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


    private void validateResponse(byte[] response) {
        int i = 0;
        assertEquals(HEADER, response[i++]);
        assertEquals(DIAGNOSTIC_TOOL_ID, response[i++]);
        assertEquals(ECU_ID, response[i++]);
        assertEquals(asByte(response.length - 5), response[i]);
        assertEquals(calculateChecksum(response), response[response.length - 1]);
    }

    private void assertEquals(byte expected, byte actual) {
        if (actual != expected) {
            throw new InvalidResponseException();
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

        bytes = protocol.constructReadAddressRequest(asBytes("0x000008"), asBytes("0x00001C"));
        System.out.println("Read Address (0x000008 and 0x00001C)   = " + asHex(bytes));

        bytes = protocol.constructReadMemoryRequest(asBytes("0x200000"), 128);
        System.out.println("Read Memory (from 0x200000, 128 bytes) = " + asHex(bytes));

        bytes = protocol.extractResponseData(asBytes("0x80F01003E87DB199"), asBytes("0x8010F008A80000000800001C54"));
        System.out.println("Extract Response (0x80F01003E87DB199)  = " + asHex(bytes));
    }

}
