/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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

package com.romraider.io.protocol.ncs.iso14230;

import static com.romraider.io.protocol.ncs.iso14230.NCSChecksumCalculator.calculateChecksum;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.connection.KwpConnectionProperties;
import com.romraider.io.protocol.ProtocolNCS;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.query.EcuInit;
import com.romraider.logger.ecu.comms.query.NCSEcuInit;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.exception.InvalidResponseException;
import com.romraider.logger.ecu.exception.UnsupportedProtocolException;

public final class NCSProtocol implements ProtocolNCS {
    private final ByteArrayOutputStream bb = new ByteArrayOutputStream(255);
    public static final byte PHY_ADDR = (byte) 0x80;
    public static final byte READ_MEMORY_PADDING = (byte) 0x00;
    public static final byte READ_MEMORY_COMMAND = (byte) 0xA0;
    public static final byte READ_MEMORY_RESPONSE = (byte) 0xE0;
    public static final byte LOAD_ADDRESS_COMMAND = (byte) 0xAC;
    public static final byte LOAD_ADDRESS_RESPONSE = (byte) 0xEC;
    public static final byte READ_LOAD_COMMAND = (byte) 0x21;
    public static final byte READ_LOAD_RESPONSE = (byte) 0x61;
    public static final byte WRITE_MEMORY_COMMAND = (byte) 0xB0;
    public static final byte WRITE_MEMORY_RESPONSE = (byte) 0xF0;
    public static final byte WRITE_ADDRESS_COMMAND = (byte) 0xB8;
    public static final byte WRITE_ADDRESS_RESPONSE = (byte) 0xF8;
    public static final byte FASTINIT_COMMAND = (byte) 0x81;
    public static final byte FASTINIT_RESPONSE = (byte) 0xC1;
    public static final byte STOP_COMMAND = (byte) 0x82;
    public static final byte STOP_RESPONSE = (byte) 0xC2;
    public static final byte ECU_ID_SID = (byte) 0x1A;
    public static final byte OPTION_81 = (byte) 0x81;
    public static final byte FIELD_TYPE_01 = (byte) 0x01;
    public static final byte FIELD_TYPE_02 = (byte) 0x02;
    public static final byte FIELD_TYPE_83 = (byte) 0x83;
    public static final byte SID_21 = (byte) 0x21;
    public static final byte SID_22 = (byte) 0x22;
    public static final byte ECU_ID_SID_RESPONSE = (byte) 0x5A;
    public static final byte READ_SID_GRP_RESPONSE = (byte) 0x62;
    public static final byte ECU_RESET_COMMAND = (byte) 0x04;
    public static final byte ECU_RESET_RESPONSE = (byte) 0x44;
    public static final byte NCS_NRC = (byte) 0x7F;
    public static final int RESPONSE_NON_DATA_BYTES = 3;
    public static final int ADDRESS_SIZE = 3;
    public static Module module;

    @Override
    public byte[] constructEcuFastInitRequest(Module module) {
        checkNotNull(module, "module");
        NCSProtocol.module = module;
        final byte[] request = buildRequest(
                FASTINIT_COMMAND, false, new byte[]{});
        return request;
    }

    @Override
    public byte[] constructEcuStopRequest(Module module) {
        checkNotNull(module, "module");
        NCSProtocol.module = module;
        final byte[] request = buildRequest(
                STOP_COMMAND, false, new byte[]{});
        return request;
    }

    // not implemented
    @Override
    public byte[] constructStartDiagRequest(Module module) {
        return null;
    }

    // not implemented
    @Override
    public byte[] constructElevatedDiagRequest(Module module) {
        return null;
    }

    // not implemented
    @Override
    public byte[] constructEcuInitRequest(Module module) {
        return null;
    }

    @Override
    public byte[] constructEcuIdRequest(Module module) {
        checkNotNull(module, "module");
        NCSProtocol.module = module;
        // len  SID  opt  chk
        // 0x02 0x1A 0x81 0x9D
        final byte[] request = buildRequest(
                ECU_ID_SID, true, new byte[]{OPTION_81});
        return request;
    }

    @Override
    public byte[] constructReadSidPidRequest(Module module, byte sid, byte[][] pid) {
        checkNotNull(module, "module");
        NCSProtocol.module = module;
        final byte[] request = buildSidPidRequest(sid, true, pid);
        return request;
    }

    @Override
    //TODO: not yet implemented
    public byte[] constructWriteMemoryRequest(
            Module module, byte[] address, byte[] values) {

        throw new UnsupportedProtocolException(
                "Write memory command is not supported on for address: " +
                asHex(address));
    }

    @Override
    //TODO: not yet implemented
    public byte[] constructWriteAddressRequest(
            Module module, byte[] address, byte value) {

        throw new UnsupportedProtocolException(
                "Write Address command is not supported on for address: " +
                asHex(address));
    }

    @Override
    //TODO: not yet implemented
    public byte[] constructReadMemoryRequest(
            Module module, byte[] address, int numBytes) {

        throw new UnsupportedProtocolException(
                "Read memory command is not supported on for address: " +
                asHex(address));
    }

    @Override
    //TODO: not yet implemented
    public byte[] constructReadMemoryRequest(Module module, byte[][] address,
            int numBytes) {

        throw new UnsupportedProtocolException(
                "Read memory command is not supported on for address: " +
                asHex(address[0]));
    }

    @Override
    public byte[] constructLoadAddressRequest(Map<byte[], Integer> queries) {
        checkNotNullOrEmpty(queries, "queries");
        // short header - false, length encoded into lower 5 bits of first byte
        // 0x8Len 0x10 0xFC 0xac 0x81 fld_typ address1 [[fld_typ address2] ... [fld_typ addressN]] checksum
        // short header - true
        // Len 0xac 0x81 fld_typ address1 [[fld_typ address2] ... [fld_typ addressN]] checksum
        byte [][] addresses = new byte[queries.size()][];
        int i = 0;
        for (byte [] query : queries.keySet()) {
                addresses[i++] = query;
        }
        return buildLoadAddrRequest(true, addresses);
    }

    @Override
    public byte[] constructReadAddressRequest(Module module, byte[][] addresses) {
        // read previously loaded addresses
        // len 0x21 0x81 0x04 0x01 checksum
        return buildRequest(
                READ_LOAD_COMMAND, true, new byte[]{(byte) 0x81, (byte) 0x04, (byte) 0x01});
    }

    @Override
    public byte[] constructReadAddressRequest(Module module, byte[][] bs,
            PollingState pollState) {
        byte opt_byte3;
        if (pollState.isFastPoll()) {
            // continuously read response of previously loaded addresses
            // len 0x21 0x81 0x06 0x01 checksum
            opt_byte3 = (byte) 0x06;
        }
        else {
            // read one response of previously loaded addresses
            // len 0x21 0x81 0x04 0x01 checksum
            opt_byte3 = (byte) 0x04;
        }
        return buildRequest(
                READ_LOAD_COMMAND, true, new byte[]{(byte) 0x81, opt_byte3, (byte) 0x01});
    }

    @Override
    public byte[] preprocessResponse(
            byte[] request, byte[] response, PollingState pollState) {
        return NCSResponseProcessor.filterRequestFromResponse(
                request, response, pollState);
    }

    @Override
    public byte[] parseResponseData(byte[] processedResponse) {
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        return NCSResponseProcessor.extractResponseData(processedResponse);
    }

    @Override
    public void checkValidEcuInitResponse(byte[] processedResponse) {
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        NCSResponseProcessor.validateResponse(processedResponse);
    }

    @Override
    public EcuInit parseEcuInitResponse(byte[] processedResponse) {
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        return new NCSEcuInit(processedResponse);
    }

    @Override
    public void validateLoadAddressResponse(byte[] response) {
        checkNotNullOrEmpty(response, "addressLoadResponse");
        NCSResponseProcessor.validateResponse(response);
    }

    @Override
    //TODO: not yet implemented
    // maybe use: Service $11 - Module reset
    public byte[] constructEcuResetRequest(Module module, int resetCode) {
        checkNotNull(module, "module");
        NCSProtocol.module = module;
        return buildRequest((byte) 0, false, new byte[]{ECU_RESET_COMMAND});
    }

    @Override
    public byte[] checkValidSidPidResponse(byte[] response) {
        checkNotNullOrEmpty(response, "SidPidResponse");
        return NCSResponseProcessor.extractResponseData(response);
    }

    @Override
    //TODO: not yet implemented
    public void checkValidEcuResetResponse(byte[] processedResponse) {
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        byte responseType = processedResponse[4];
        if (responseType != ECU_RESET_RESPONSE) {
            throw new InvalidResponseException(
                    "Unexpected OBD Reset response: " +
                    asHex(processedResponse));
        }
    }

    @Override
    public void checkValidWriteResponse(byte[] data, byte[] processedResponse) {
    }

    @Override
    public ConnectionProperties getDefaultConnectionProperties() {
        return new KwpConnectionProperties() {

            @Override
            public int getBaudRate() {
                return 10400;
            }

            @Override
            public void setBaudRate(int b) {

            }

            @Override
            public int getDataBits() {
                return 8;
            }

            @Override
            public int getStopBits() {
                return 1;
            }

            @Override
            public int getParity() {
                return 0;
            }

            @Override
            public int getConnectTimeout() {
                return 2000;
            }

            @Override
            public int getSendTimeout() {
                return 255;
            }


            @Override
            public int getP1Max() {
                return 0;
            }

            @Override
            public int getP3Min() {
                return 5;
            }

            @Override
            public int getP4Min() {
                return 0;
            }
        };
    }

    private final byte[] buildRequest(byte command, boolean shortHeader,
            byte[]... content) {

        int length = 1;
        for (byte[] tmp : content) {
            length += tmp.length;
        }
        byte[] request = new byte[0];
        try {
            bb.reset();
            if (shortHeader) {
                bb.write(length);
            }
            else {
                bb.write(PHY_ADDR + length);
                bb.write(module.getAddress());
                bb.write(module.getTester());
            }
            bb.write(command);
            for (byte[] tmp : content) {
                bb.write(tmp);
            }
            bb.write((byte) 0x00);
            request = bb.toByteArray();
            final byte cs = calculateChecksum(request);
            request[request.length - 1] = cs;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return request;
    }

    private final byte[] buildSidPidRequest(byte command, boolean shortHeader,
            byte[]... content) {

        int length = 3;
        for (byte[] tmp : content) {
            length += tmp.length;
        }
        byte[] request = new byte[0];
        try {
            bb.reset();
            if (shortHeader) {
                bb.write(length);
            }
            else {
                bb.write(PHY_ADDR + length);
                bb.write(module.getAddress());
                bb.write(module.getTester());
            }
            bb.write(command);
            for (byte[] tmp : content) {
                bb.write(tmp);
            }
            bb.write((byte) 0x04);
            bb.write((byte) 0x01);
            bb.write((byte) 0x00);
            request = bb.toByteArray();
            final byte cs = calculateChecksum(request);
            request[request.length - 1] = cs;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return request;
    }

    private final byte[] buildLoadAddrRequest(boolean shortHeader,
            byte[]... content) {

        int length = 2;
        byte[] request = new byte[0];
        try {
            bb.reset();
            if (shortHeader) {
                bb.write(length);
            }
            else {
                bb.write(PHY_ADDR);
                bb.write(module.getAddress());
                bb.write(module.getTester());
            }
            bb.write(LOAD_ADDRESS_COMMAND);
            bb.write(OPTION_81);
            for (byte[] tmp : content) {
                if (tmp[0] == SID_21) {
                    bb.write(FIELD_TYPE_01);
                    bb.write(tmp, 1, tmp.length - 1);
                    continue;
                }
                if (tmp[0] == SID_22) {
                    bb.write(FIELD_TYPE_02);
                    bb.write(tmp, 1, tmp.length - 1);
                    continue;
                }
                if (tmp.length == 3 && (tmp[0] & 0x80) == 0x80) {
                    bb.write(FIELD_TYPE_83);
                    bb.write((byte) 0xFF);
                    bb.write(tmp, 0, 3);
                }
                else {  //assume a short length ROM address
                    bb.write(FIELD_TYPE_83);
                    bb.write((byte) 0x00);
                    switch (tmp.length) {
                        case 1:
                            bb.write((byte) 0x00);
                            bb.write((byte) 0x00);
                            break;
                        case 2:
                            bb.write((byte) 0x00);
                            break;
                        case 3:
                            break;
                    }
                    bb.write(tmp);
                }
            }
            bb.write(0);    // reserve last byte for checksum
            request = bb.toByteArray();
            if (shortHeader) {
                request[0] = (byte)(request.length - 2);
            }
            else {
                request[0] = (byte)(PHY_ADDR + request.length - 4);
            }
            final byte cs = calculateChecksum(request);
            request[request.length - 1] = cs;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return request;
    }
}
