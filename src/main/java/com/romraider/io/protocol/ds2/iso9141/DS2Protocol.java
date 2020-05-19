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

import static com.romraider.io.protocol.ds2.iso9141.DS2ChecksumCalculator.calculateChecksum;
import static com.romraider.util.ByteUtil.asByte;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkGreaterThanZero;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.protocol.ProtocolDS2;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.query.DS2EcuInit;
import com.romraider.logger.ecu.comms.query.EcuInit;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.exception.UnsupportedProtocolException;

public final class DS2Protocol implements ProtocolDS2 {
    private static final byte[] READ_MEMORY_COMMAND = new byte[]{0x06, 0x00};
    private static final byte[] READ_PROCEDURE_COMMAND = new byte[]{0x0B, 0x02};
    private static final byte[] ECU_INIT_COMMAND = new byte[]{0x00};
    private static final byte[] ECU_RESET_COMMAND = new byte[]{0x43};
    private static final byte[] SET_ADDRESS_COMMAND = new byte[]{0x0B, 0x01};
    private static final byte[] READ_ADDRESS_COMMAND = new byte[]{0x0B, 0x00};
    public static final byte VALID_RESPONSE = (byte) 0xA0;
    public static final int RESPONSE_NON_DATA_BYTES = 4;
    public static Module module;
    private final ByteArrayOutputStream bb = new ByteArrayOutputStream(255);
    
    @Override
    public byte[] constructEcuInitRequest(Module module) {
        checkNotNull(module, "module");
        DS2Protocol.module = module;
        // 0x12 0x04 0x00 0x16
        return buildRequest(ECU_INIT_COMMAND, new byte[0], new byte[0]);
    }

    @Override
    public byte[] constructWriteMemoryRequest(
            Module module, byte[] address, byte[] values) {

        return buildRequest(new byte[0], new byte[0], values);
    }

    @Override
    public byte[] constructWriteAddressRequest(
            Module module, byte[] address, byte value) {

        throw new UnsupportedProtocolException(
                "Write Address command is not supported by DS2 for address: " + 
                asHex(address));
    }

    @Override
    public byte[] constructReadMemoryRequest(
            Module module, byte[] address, int numBytes) {

        return constructReadMemoryRequest(
                module, new byte[][]{address}, numBytes);
    }

    @Override
    public byte[] constructReadMemoryRequest(
            Module module, byte[][] address, int numBytes) {
        checkNotNull(module, "module");
        checkNotNullOrEmpty(address, "address");
        checkGreaterThanZero(numBytes, "numBytes");
        // 0x12 0x09 0x06 <seg> <from_address> <num_bytes>
        return buildRequest(
                READ_MEMORY_COMMAND, new byte[]{asByte(numBytes)}, address);
    }

    @Override
    public byte[] constructReadAddressRequest(
            Module module, byte[][] addresses) {
        checkNotNull(module, "module");
        // 0x12 0x05 0x0B 0x00
        return buildRequest(
                READ_ADDRESS_COMMAND, new byte[0], new byte[]{0});
    }

    @Override
    public byte[] constructReadProcedureRequest(
            Module module, byte[][] addresses) {
        checkNotNull(module, "module");
        checkNotNullOrEmpty(addresses, "addresses");
        // 0x12 data_length group subgroup [procedure] checksum
        return buildRequest(READ_PROCEDURE_COMMAND, new byte[0], addresses);
    }

    public byte[] constructReadGroupRequest(
            Module module, byte[][] addresses) {
        checkNotNull(module, "module");
        checkNotNullOrEmpty(addresses, "addresses");
        // 0x12 data_length group subgroup checksum
        return buildRequest(new byte[0], new byte[0], addresses);
    }

    @Override
    public byte[] constructSetAddressRequest(Module module,
            byte[][] addresses) {
        checkNotNull(module, "module");
        checkNotNullOrEmpty(addresses, "address");
        return buildRequest(SET_ADDRESS_COMMAND, new byte[0], addresses);
    }

    @Override
    public byte[] preprocessResponse(byte[] request, byte[] response, PollingState pollState) {
        return DS2ResponseProcessor.filterRequestFromResponse(request, response, pollState);
    }

    @Override
    public byte[] parseResponseData(byte[] processedResponse) {
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        return DS2ResponseProcessor.extractResponseData(processedResponse);
    }

    @Override
    public void checkValidEcuInitResponse(byte[] processedResponse) {
        // 12 2e a0 31343337383036 3131303133303231323239363030303031313538353236303030393632313432353634 9c
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        DS2ResponseProcessor.validateResponse(processedResponse);
    }

    @Override
    public void validateSetAddressResponse(byte[] processedResponse) {
        // Valid response is an ACK: 12 04 A0 B6
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        DS2ResponseProcessor.validateResponse(processedResponse);
    }

    @Override
    public EcuInit parseEcuInitResponse(byte[] processedResponse) {
        return new DS2EcuInit(parseResponseData(processedResponse));
    }

    @Override
    public final byte[] constructEcuResetRequest(Module module, int resetCode) {
        //  0x12 data_length cmd byte1 byte2 checksum
        checkNotNull(module, "module");
        final byte[] resetBytes = ByteBuffer.allocate(4).putInt(resetCode).array();
        final byte[] reset = new byte[2];
        System.arraycopy(resetBytes, 2, reset, 0, 2);
        return buildRequest(ECU_RESET_COMMAND, new byte[0], reset);
    }

    @Override
    public void checkValidEcuResetResponse(byte[] processedResponse) {
        // 80 F0 10 02 F8 40 BA
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        DS2ResponseProcessor.validateResponse(processedResponse);
    }

    @Override
    public void checkValidWriteResponse(byte[] data, byte[] processedResponse) {
    }

    @Override
    public ConnectionProperties getDefaultConnectionProperties() {
        return new ConnectionProperties() {

            public int getBaudRate() {
                return 9600;
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
                return 2;
            }

            public int getConnectTimeout() {
                return 2000;
            }

            public int getSendTimeout() {
                return 55;
            }
        };
    }

    private final byte[] buildRequest(
            byte[] command, byte[] readLen, byte[]... content) {
    
        byte[] request = new byte[0];
        try {
            int length = 3;
            length += command.length;
            length += readLen.length;
            bb.reset();
            bb.write(module.getAddress());
            bb.write((byte) length);
            bb.write(command);
            if (command == SET_ADDRESS_COMMAND) {
                length++;
                bb.write(content.length);   // number of parameters
            }
            for (byte[] tmp : content) {    // address payload
                if (command == READ_PROCEDURE_COMMAND
                        && tmp.length == 3) {// legacy ADC def 0x0B020E support
                    length++;
                    bb.write((byte) 0x00);
                }
                else if (command == READ_ADDRESS_COMMAND) {
                    break;  // no content
                }
                length += tmp.length;
                bb.write(tmp);
            }
            if (readLen.length > 0) {   // readMemory, # of bytes to reads
                bb.write(readLen);
            }
            bb.write((byte) 0x00);      // chksum placeholder
            request = bb.toByteArray();
            request[1] = (byte) length; // update length value
            final byte cs = calculateChecksum(request);
            request[request.length - 1] = cs;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return request;
    }
}
