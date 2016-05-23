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

package com.romraider.io.protocol.ssm.iso15765;

import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkGreaterThanZero;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.protocol.Protocol;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.query.EcuInit;
import com.romraider.logger.ecu.comms.query.SSMEcuInit;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.exception.InvalidResponseException;
import com.romraider.logger.ecu.exception.UnsupportedProtocolException;

public final class SSMProtocol implements Protocol {
    private static final byte[] resetAddress = 
            new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x60};
    public static final byte READ_MEMORY_PADDING = (byte) 0x00;
    public static final byte READ_MEMORY_COMMAND = (byte) 0xA0;
    public static final byte READ_MEMORY_RESPONSE = (byte) 0xE0;
    public static final byte READ_ADDRESS_COMMAND = (byte) 0xA8;
    public static final byte READ_ADDRESS_RESPONSE = (byte) 0xE8;
    public static final byte WRITE_MEMORY_COMMAND = (byte) 0xB0;
    public static final byte WRITE_MEMORY_RESPONSE = (byte) 0xF0;
    public static final byte WRITE_ADDRESS_COMMAND = (byte) 0xB8;
    public static final byte WRITE_ADDRESS_RESPONSE = (byte) 0xF8;
    public static final byte ECU_INIT_COMMAND = (byte) 0xAA;
    public static final byte ECU_INIT_RESPONSE = (byte) 0xEA;
    public static final byte ECU_NRC = (byte) 0x7F;
    public static final int ADDRESS_SIZE = 3;
    public static final int DATA_SIZE = 1;
    public static final int RESPONSE_NON_DATA_BYTES = 5;
    public static Module module;
    private final ByteArrayOutputStream bb = new ByteArrayOutputStream(255);
    
    public byte[] constructEcuInitRequest(Module module) {
        checkNotNull(module, "module");
        SSMProtocol.module = module;
        // 000007E0 AA
        return buildRequest(ECU_INIT_COMMAND, false, new byte[0]);
    }

    public byte[] constructWriteMemoryRequest(
            Module module, byte[] address, byte[] values) {
        checkNotNull(module, "module");
        checkNotNullOrEmpty(address, "address");
        checkNotNullOrEmpty(values, "values");
        SSMProtocol.module = module;
        // 000007E0 B0 from_address value1 value2 ... valueN
        throw new UnsupportedProtocolException(
                "Write memory command is not supported on CAN for address: " + 
                asHex(address));
    }

    public byte[] constructWriteAddressRequest(
            Module module, byte[] address, byte value) {

        checkNotNull(module, "module");
        checkNotNullOrEmpty(address, "address");
        checkNotNull(value, "value");
        SSMProtocol.module = module;
        // 000007E0 B8 address value
        return buildRequest(
                WRITE_ADDRESS_COMMAND, false, address, new byte[]{value});
    }

    public byte[] constructReadMemoryRequest(
            Module module, byte[] address, int numBytes) {
        checkNotNull(module, "module");
        checkNotNullOrEmpty(address, "address");
        checkGreaterThanZero(numBytes, "numBytes");
        SSMProtocol.module = module;
        // 000007E0 A0 padding from_address num_bytes-1
        throw new UnsupportedProtocolException(
                "Read memory command is not supported on CAN for address: " + 
                asHex(address));
    }

    public byte[] constructReadAddressRequest(Module module, byte[][] addresses) {
        checkNotNull(module, "module");
        checkNotNullOrEmpty(addresses, "addresses");
        SSMProtocol.module = module;
        // 000007E0 A8 padding [address1] [address2] ... [addressN]
        return buildRequest(READ_ADDRESS_COMMAND, true, addresses);
    }

    public byte[] preprocessResponse(
            byte[] request, byte[] response, PollingState pollState) {
        return SSMResponseProcessor.filterRequestFromResponse(
                request, response, pollState);
    }

    public byte[] parseResponseData(byte[] processedResponse) {
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        return SSMResponseProcessor.extractResponseData(processedResponse);
    }

    public void checkValidEcuInitResponse(byte[] processedResponse) {
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        SSMResponseProcessor.validateResponse(processedResponse);
        // four byte - CAN ID
        // one byte  - Response Code
        // 3_unknown_bytes 5_ecu_id_bytes readable_params_switches...
        // 000007E8 EA A21011 5B125A4007 F3FAC98E0B81FEAC00820046CE54F...
        byte responseType = processedResponse[4];
        if (responseType != ECU_INIT_RESPONSE) {
            throw new InvalidResponseException(
                    "Unexpected " + module.getName() + " Init response type: " + 
                    asHex(new byte[]{responseType}));
        }
    }

    public EcuInit parseEcuInitResponse(byte[] processedResponse) {
        return new SSMEcuInit(parseResponseData(processedResponse));
    }

    public byte[] constructEcuResetRequest(Module module, int resetCode) {
        //  000007E0 B8 000060 40
        return constructWriteAddressRequest(module, resetAddress, (byte) resetCode);
    }

    public void checkValidEcuResetResponse(byte[] processedResponse) {
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        // 000007E8 F8 40
        byte responseType = processedResponse[4];
        if (responseType != WRITE_ADDRESS_RESPONSE || 
                processedResponse[5] != (byte) 0x40) {
            throw new InvalidResponseException(
                    "Unexpected " + module.getName() + " Reset response: " + 
                    asHex(processedResponse));
        }
    }

    public void checkValidWriteResponse(byte[] data, byte[] processedResponse) {
        checkNotNullOrEmpty(data, "data");
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        // 000007E8 F8 data
        byte responseType = processedResponse[4];
        if (responseType != WRITE_ADDRESS_RESPONSE || 
                processedResponse[5] != (byte) data[0]) {
            throw new InvalidResponseException(
                    "Unexpected " + module.getName() + " Write response: " + 
                    asHex(processedResponse));
        }
    }

    public ConnectionProperties getDefaultConnectionProperties() {
        return new ConnectionProperties() {

            public int getBaudRate() {
                return 500000;
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

    private byte[] buildRequest(
            byte command, 
            boolean padContent, 
            byte[]... content) {

        bb.reset();
        try {
            bb.write(module.getTester());
            bb.write(command);
            if (padContent) {
                bb.write(READ_MEMORY_PADDING);
            }
            for (byte[] tmp : content) {
                    bb.write(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bb.toByteArray();
    }
}
