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

package enginuity.io.protocol;

import enginuity.io.connection.ConnectionProperties;
import static enginuity.util.ByteUtil.asByte;
import static enginuity.util.ByteUtil.asInt;
import static enginuity.util.ParamChecker.checkGreaterThanZero;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class SSMProtocol implements Protocol {
    public static final byte HEADER = (byte) 0x80;
    public static final byte ECU_ID = (byte) 0x10;
    public static final byte DIAGNOSTIC_TOOL_ID = (byte) 0xF0;
    public static final byte READ_PADDING = (byte) 0x00;
    public static final byte READ_MEMORY_COMMAND = (byte) 0xA0;
    public static final byte READ_MEMORY_RESPONSE = (byte) 0xE0;
    public static final byte READ_ADDRESS_COMMAND = (byte) 0xA8;
    public static final byte READ_ADDRESS_RESPONSE = (byte) 0xE8;
    public static final byte ECU_INIT_COMMAND = (byte) 0xBF;
    public static final byte ECU_INIT_RESPONSE = (byte) 0xFF;
    public static final int ADDRESS_SIZE = 3;
    public static final int DATA_SIZE = 1;
    public static final int RESPONSE_NON_DATA_BYTES = 6;
    public static final int REQUEST_NON_DATA_BYTES = 7;

    public byte[] constructEcuInitRequest() {
        // 0x80 0x10 0xF0 0x01 0xBF 0x40
        return buildRequest(ECU_INIT_COMMAND, false, new byte[0]);
    }

    public byte[] constructReadMemoryRequest(byte[] address, int numBytes) {
        checkNotNullOrEmpty(address, "address");
        checkGreaterThanZero(numBytes, "numBytes");
        // 0x80 0x10 0xF0 data_length 0xA0 padding from_address num_bytes-1 checksum
        return buildRequest(READ_MEMORY_COMMAND, true, address, new byte[]{asByte(numBytes - 1)});
    }

    public byte[] constructReadAddressRequest(byte[][] addresses) {
        checkNotNullOrEmpty(addresses, "addresses");
        // 0x80 0x10 0xF0 data_length 0xA8 padding address1 address2 ... addressN checksum
        return buildRequest(READ_ADDRESS_COMMAND, true, addresses);
    }

    public byte calculateChecksum(byte[] bytes) {
        int total = 0;
        for (int i = 0; i < (bytes.length - 1); i++) {
            byte b = bytes[i];
            total += asInt(b);
        }
        return asByte(total - ((total >>> 16) << 16));
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

            public int getConnectTimeout() {
                return 2000;
            }

            public int getSendTimeout() {
                return 55;
            }
        };
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
        byte[] request = new byte[data.length + (padContent ? REQUEST_NON_DATA_BYTES : RESPONSE_NON_DATA_BYTES)];
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

}
