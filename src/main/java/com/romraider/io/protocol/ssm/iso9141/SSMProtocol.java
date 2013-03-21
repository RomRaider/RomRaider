/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.protocol.Protocol;

import static com.romraider.io.protocol.ssm.iso9141.SSMChecksumCalculator.calculateChecksum;

import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.manager.PollingStateImpl;
import com.romraider.logger.ecu.comms.query.EcuInit;
import com.romraider.logger.ecu.comms.query.SSMEcuInit;
import com.romraider.logger.ecu.exception.InvalidResponseException;
import static com.romraider.util.ByteUtil.asByte;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkGreaterThanZero;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

public final class SSMProtocol implements Protocol {
    public static final byte HEADER = (byte) 0x80;
    public static byte ECU_ID = (byte) 0x10;
    public static final byte DIAGNOSTIC_TOOL_ID = (byte) 0xF0;
    public static final byte READ_ADDRESS_ONCE = (byte) 0x00;
    public static final byte READ_ADDRESS_CONTINUOUS = (byte) 0x01;
    public static final byte READ_MEMORY_PADDING = (byte) 0x00;
    public static final byte READ_MEMORY_COMMAND = (byte) 0xA0;
    public static final byte READ_MEMORY_RESPONSE = (byte) 0xE0;
    public static final byte READ_ADDRESS_COMMAND = (byte) 0xA8;
    public static final byte READ_ADDRESS_RESPONSE = (byte) 0xE8;
    public static final byte WRITE_MEMORY_COMMAND = (byte) 0xB0;
    public static final byte WRITE_MEMORY_RESPONSE = (byte) 0xF0;
    public static final byte WRITE_ADDRESS_COMMAND = (byte) 0xB8;
    public static final byte WRITE_ADDRESS_RESPONSE = (byte) 0xF8;
    public static final byte ECU_INIT_COMMAND = (byte) 0xBF;
    public static final byte ECU_INIT_RESPONSE = (byte) 0xFF;
    public static final int ADDRESS_SIZE = 3;
    public static final int DATA_SIZE = 1;
    public static final int RESPONSE_NON_DATA_BYTES = 6;
    public static final int REQUEST_NON_DATA_BYTES = 7;
    private final PollingState pollState = new PollingStateImpl();
    
    public byte[] constructEcuInitRequest(byte id) {
        checkGreaterThanZero(id, "ECU_ID");
        SSMProtocol.ECU_ID = id;
        // 0x80 0x10 0xF0 0x01 0xBF 0x40
        return buildRequest(ECU_INIT_COMMAND, false, new byte[0]);
    }

    public byte[] constructWriteMemoryRequest(byte id, byte[] address, byte[] values) {
        checkGreaterThanZero(id, "ECU_ID");
        checkNotNullOrEmpty(address, "address");
        checkNotNullOrEmpty(values, "values");
        SSMProtocol.ECU_ID = id;
        // 0x80 0x10 0xF0 data_length 0xB0 from_address value1 value2 ... valueN checksum
        return buildRequest(WRITE_MEMORY_COMMAND, false, address, values);
    }

    public byte[] constructWriteAddressRequest(byte[] address, byte value) {
        checkNotNullOrEmpty(address, "address");
        checkNotNull(value, "value");
        // 0x80 0x10 0xF0 data_length 0xB8 from_address value checksum
        return buildRequest(WRITE_ADDRESS_COMMAND, false, address, new byte[]{value});
    }

    public byte[] constructReadMemoryRequest(byte id, byte[] address, int numBytes) {
        checkGreaterThanZero(id, "ECU_ID");
        checkNotNullOrEmpty(address, "address");
        checkGreaterThanZero(numBytes, "numBytes");
        SSMProtocol.ECU_ID = id;
        // 0x80 0x10 0xF0 data_length 0xA0 padding from_address num_bytes-1 checksum
        return buildRequest(READ_MEMORY_COMMAND, true, address, new byte[]{asByte(numBytes - 1)});
    }

    public byte[] constructReadAddressRequest(byte id, byte[][] addresses) {
        checkGreaterThanZero(id, "ECU_ID");
        checkNotNullOrEmpty(addresses, "addresses");
        SSMProtocol.ECU_ID = id;
        // 0x80 0x10 0xF0 data_length 0xA8 padding address1 address2 ... addressN checksum
        return buildRequest(READ_ADDRESS_COMMAND, true, addresses);
    }

    public byte[] preprocessResponse(byte[] request, byte[] response, PollingState pollState) {
        return SSMResponseProcessor.filterRequestFromResponse(request, response, pollState);
    }

    public byte[] parseResponseData(byte[] processedResponse) {
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        return SSMResponseProcessor.extractResponseData(processedResponse);
    }

    public void checkValidEcuInitResponse(byte[] processedResponse) {
        // response_header 3_unknown_bytes 5_ecu_id_bytes readable_params_switches... checksum
        // 80F01039FF A21011315258400673FACB842B83FEA800000060CED4FDB060000F200000000000DC0000551E30C0F222000040FB00E10000000000000000 59
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        SSMResponseProcessor.validateResponse(processedResponse);
        byte responseType = processedResponse[4];
        if (responseType != ECU_INIT_RESPONSE) {
            throw new InvalidResponseException("Unexpected ECU Init response type: " + asHex(new byte[]{responseType}));
        }
    }

    public EcuInit parseEcuInitResponse(byte[] processedResponse) {
        return new SSMEcuInit(parseResponseData(processedResponse));
    }

    public final byte[] constructEcuResetRequest(byte id) {
        //  80 10 F0 05 B8 00 00 60 40 DD
        checkGreaterThanZero(id, "ECU_ID");
        SSMProtocol.ECU_ID = id;
        final byte[] resetAddress = new byte[]{
                (byte) 0x00, (byte) 0x00, (byte) 0x60};
        final byte reset = 0x40;
        return constructWriteAddressRequest(resetAddress, reset);
    }

    public void checkValidEcuResetResponse(byte[] processedResponse) {
        // 80 F0 10 02 F8 40 BA
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        SSMResponseProcessor.validateResponse(processedResponse);
        byte responseType = processedResponse[4];
        if (responseType != WRITE_ADDRESS_RESPONSE || processedResponse[5] != (byte) 0x40) {
            throw new InvalidResponseException("Unexpected ECU Reset response: " + asHex(processedResponse));
        }
    }

    public ConnectionProperties getDefaultConnectionProperties() {
        return new ConnectionProperties() {

            public int getBaudRate() {
                return 4800;
            }

            public void setBaudRate(int b) {

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

    private final byte[] buildRequest(byte command, boolean padContent,
            byte[]... content) {
    
        int length = 0;
        for (byte[] tmp : content) {
            length += tmp.length;
        }
        final ByteArrayOutputStream bb = new ByteArrayOutputStream(length);
        bb.write(HEADER);
        bb.write(ECU_ID);
        bb.write(DIAGNOSTIC_TOOL_ID);
        bb.write(Integer.valueOf(length + (padContent ? 2 : 1)).byteValue());
        bb.write(command);
        if (padContent) {
            bb.write((pollState.isFastPoll() ? 
                    READ_ADDRESS_CONTINUOUS : READ_ADDRESS_ONCE));
        }
        for (byte[] tmp : content) {
            try {
                bb.write(tmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bb.write((byte) 0x00);
        final byte[] request = bb.toByteArray();
        final byte cs = calculateChecksum(request);
        request[request.length - 1] = cs;
        return request;
    }
}
