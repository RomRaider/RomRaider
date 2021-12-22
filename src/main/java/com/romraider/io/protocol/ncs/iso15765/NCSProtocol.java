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

package com.romraider.io.protocol.ncs.iso15765;

import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkGreaterThanZero;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.connection.KwpConnectionProperties;
import com.romraider.io.protocol.ProtocolNCS;
import com.romraider.io.protocol.ncs.iso15765.NCSResponseProcessor;
import com.romraider.io.protocol.ncs.iso15765.NCSProtocol;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.query.EcuInit;
import com.romraider.logger.ecu.comms.query.NCSEcuInit;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.exception.InvalidResponseException;
import com.romraider.logger.ecu.exception.UnsupportedProtocolException;

public final class NCSProtocol implements ProtocolNCS {
    private final ByteArrayOutputStream bb = new ByteArrayOutputStream(255);
    public static final byte READ_MEMORY_PADDING = (byte) 0x00;
    public static final byte READ_MEMORY_COMMAND = (byte) 0x23;
    public static final byte READ_MEMORY_RESPONSE = (byte) 0x63;
    public static final byte LOAD_ADDRESS_COMMAND = (byte) 0x2C;
    public static final byte DDLOCID = (byte) 0xE0;
    public static final byte LOAD_ADDRESS_RESPONSE = (byte) 0x6C;
    public static final byte READ_LOAD_COMMAND = (byte) 0x21;
    public static final byte READ_LOAD_RESPONSE = (byte) 0x61;
    public static final byte ECU_INIT_COMMAND = (byte) 0x10;
    public static final byte ECU_INIT_RESPONSE = (byte) 0x50;
    public static final byte ECU_ID_SID = (byte) 0x21;
    public static final byte ECU_ID_CMD = (byte) 0x10;
    public static final byte FIELD_TYPE_01 = (byte) 0x01;
    public static final byte FIELD_TYPE_02 = (byte) 0x02;
    public static final byte FIELD_TYPE_03 = (byte) 0x03;
    public static final byte SID_21 = (byte) 0x21;
    public static final byte SID_22 = (byte) 0x22;
    public static final byte ECU_ID_SID_RESPONSE = (byte) 0x50;
    public static final byte READ_SID_21_RESPONSE = (byte) 0x61;
    public static final byte READ_SID_GRP_RESPONSE = (byte) 0x62;
    public static final byte ECU_RESET_COMMAND = (byte) 0x04;
    public static final byte ECU_RESET_RESPONSE = (byte) 0x44;
    public static final byte NCS_NRC = (byte) 0x7F;
    public static final int RESPONSE_NON_DATA_BYTES = 4;
    public static final int ADDRESS_SIZE = 4;
    public static Module module;

    // not implemented
    @Override
    public byte[] constructEcuFastInitRequest(Module module) {
        return null;
    }

    @Override
    public byte[] constructEcuStopRequest(Module module) {
        checkNotNull(module, "module");
        NCSProtocol.module = module;
        // 000007E01081
        final byte[] request = buildRequest(
                ECU_INIT_COMMAND, false, new byte[]{(byte) 0x81});
        return request;
    }

    @Override
    public byte[] constructEcuInitRequest(Module module) {
        checkNotNull(module, "module");
        NCSProtocol.module = module;
        // 000007E010C0
        final byte[] request = buildRequest(
                ECU_INIT_COMMAND, false, new byte[]{(byte) 0xC0});
        return request;
    }

    @Override
    public byte[] constructStartDiagRequest(Module module) {
        checkNotNull(module, "module");
        NCSProtocol.module = module;
        // 000007E010C0
        final byte[] request = buildRequest(
                ECU_INIT_COMMAND, false, new byte[]{(byte) 0xC0});
        return request;
    }

    @Override
    public byte[] constructElevatedDiagRequest(Module module) {
        checkNotNull(module, "module");
        NCSProtocol.module = module;
        // 000007E010FB
        final byte[] request = buildRequest(
                ECU_INIT_COMMAND, false, new byte[]{(byte) 0xFB});
        return request;
    }

    @Override
    public byte[] constructEcuIdRequest(Module module) {
        checkNotNull(module, "module");
        NCSProtocol.module = module;
        // 000007E02110
        final byte[] request = buildRequest(
                ECU_ID_SID, false, new byte[]{ECU_ID_CMD});
        return request;
    }

    @Override
    public byte[] constructReadSidPidRequest(Module module, byte sid, byte[][] pid) {
        checkNotNull(module, "module");
        NCSProtocol.module = module;
        final byte[] request = buildRequest(sid, false, pid);
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
    public byte[] constructWriteAddressRequest(
            Module module, byte[] address, byte value) {

        throw new UnsupportedProtocolException(
                "Write Address command is not supported on for address: " +
                asHex(address));
    }

    @Override
    public byte[] constructReadMemoryRequest(Module module, byte[] address,
            int numBytes) {
        NCSProtocol.module = module;
        // 000007E023 4-byte_address 2-byte_numBytes
        final byte[] frame = new byte[6];
        if (address.length == 3 && (address[0] & 0x80) == 0x80) {
            // expand a 6-byte RAM address to 8-bytes
            frame[0] = (byte) 0xFF;
        }
        System.arraycopy(address, 0, frame, 4-address.length, address.length);
        frame[5] = (byte) numBytes;
        return buildRequest(READ_MEMORY_COMMAND, false, frame, new byte[]{});
    }

    @Override
    public byte[] constructReadMemoryRequest(
            Module module, byte[][] address, int numBytes) {
        checkNotNull(module, "module");
        checkNotNullOrEmpty(address, "address");
        checkGreaterThanZero(numBytes, "numBytes");
        return constructReadMemoryRequest(module, address[0],
                numBytes);
    }

    @Override
    public byte[] constructLoadAddressRequest(Map<byte[], Integer> queryMap) {
        checkNotNullOrEmpty(queryMap, "queryMap");
        // ID 0x2C 0xE0 DEFMODE ... DEFMODE ... DEFMODE ...
        return buildLoadAddrRequest(queryMap);
    }

    @Override
    public byte[] constructReadAddressRequest(Module module, byte[][] addresses) {
        // read addresses
        // addr sid pid
        return buildSidPidRequest(addresses);
    }

    @Override
    public byte[] constructReadAddressRequest(Module module, byte[][] bs,
            PollingState pollState) {
        return buildRequest(
                READ_LOAD_COMMAND, false, new byte[]{(byte) 0xE0});
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
        //final byte[] ecuInitBytes = parseResponseData(processedResponse);
        return new NCSEcuInit(processedResponse);
    }

    @Override
    public void validateLoadAddressResponse(byte[] response) {
        checkNotNullOrEmpty(response, "addressLoadResponse");
        NCSResponseProcessor.validateResponse(response);
    }

    @Override
    //TODO: not yet implemented
    // maybe use: Service $11 with 0x80 - module reset
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
                return 500000;
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

    private final byte[] buildSidPidRequest(byte[]... content) {

        bb.reset();
        try {
            bb.write(module.getTester());
            for (byte[] tmp : content) {
                    bb.write(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bb.toByteArray();
    }

    private final byte[] buildLoadAddrRequest(Map<byte[], Integer> queryMap) {

        int PIDYDLID = 1;
        byte[] request = new byte[0];
        try {
            bb.reset();
            bb.write(module.getTester());
            bb.write(LOAD_ADDRESS_COMMAND);
            bb.write(DDLOCID);
            for (byte[] tmp : queryMap.keySet()) {
                if (tmp[0] == SID_22) {
                    bb.write(FIELD_TYPE_02);    //definitionMode
                    bb.write(PIDYDLID++);       //positionIn
                    bb.write(queryMap.get(tmp)); //size
                    bb.write(tmp, 1, tmp.length - 1);//CID
                    bb.write(1);                //positionInRecord
                    continue;
                }
                else if (tmp.length == 3 && (tmp[0] & 0x80) == 0x80) {
                    bb.write(FIELD_TYPE_03);    //definitionMode
                    bb.write(PIDYDLID++);       //positionIn
                    bb.write(queryMap.get(tmp));//size (could be 1, 2 or 4)
                    bb.write((byte) 0xFF);      //RAM addr high byte
                    bb.write(tmp, 0, 3);        //3-byte RAM addr
                }
            }
            request = bb.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return request;
    }
}
