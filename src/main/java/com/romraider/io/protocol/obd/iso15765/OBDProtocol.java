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

package com.romraider.io.protocol.obd.iso15765;

import static com.romraider.util.HexUtil.asBytes;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static java.lang.System.arraycopy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import com.romraider.Settings;
import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.protocol.Protocol;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.query.EcuInit;
import com.romraider.logger.ecu.comms.query.SSMEcuInit;
import com.romraider.logger.ecu.definition.EcuDefinition;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.exception.InvalidResponseException;
import com.romraider.logger.ecu.exception.UnsupportedProtocolException;
import com.romraider.util.SettingsManager;

public final class OBDProtocol implements Protocol {
    private final ByteArrayOutputStream bb = new ByteArrayOutputStream(255);
    public static final byte OBD_INIT_COMMAND = (byte) 0x01;
    public static final byte OBD_INIT_RESPONSE = (byte) 0x41;
    public static final byte OBD_INFO_COMMAND = (byte) 0x09;
    public static final byte OBD_INFO_RESPONSE = (byte) 0x49;
    public static final byte OBD_RESET_COMMAND = (byte) 0x04;
    public static final byte OBD_RESET_RESPONSE = (byte) 0x44;
    public static final byte OBD_NRC = (byte) 0x7F;
    public static final int RESPONSE_NON_DATA_BYTES = 5;
    public static Module module;
    
    @Override
    public byte[] constructEcuInitRequest(Module module) {
        checkNotNull(module, "module");
        OBDProtocol.module = module;
        final byte[] request = buildRequest(
                    OBD_INFO_COMMAND, true, new byte[]{4});
        return request;
    }

    @Override
    public byte[] constructWriteMemoryRequest(
            Module module, byte[] address, byte[] values) {

        throw new UnsupportedProtocolException(
                "Write memory command is not supported on OBD for address: " + 
                asHex(address));
    }

    @Override
    public byte[] constructWriteAddressRequest(
            Module module, byte[] address, byte value) {

        throw new UnsupportedProtocolException(
                "Write Address command is not supported on OBD for address: " + 
                asHex(address));
    }

    @Override
    public byte[] constructReadMemoryRequest(
            Module module, byte[] address, int numBytes) {

        throw new UnsupportedProtocolException(
                "Read memory command is not supported on OBD for address: " + 
                asHex(address));
    }

    @Override
    public byte[] constructReadAddressRequest(Module module, byte[][] addresses) {
        checkNotNull(module, "module");
        checkNotNullOrEmpty(addresses, "addresses");
        OBDProtocol.module = module;
        return buildRequest(OBD_INIT_COMMAND, true, addresses);
    }

    @Override
    public byte[] preprocessResponse(
            byte[] request, byte[] response, PollingState pollState) {
        return OBDResponseProcessor.filterRequestFromResponse(
                request, response, pollState);
    }

    @Override
    public byte[] parseResponseData(byte[] processedResponse) {
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        return OBDResponseProcessor.extractResponseData(processedResponse);
    }

    @Override
    public void checkValidEcuInitResponse(byte[] processedResponse) {
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        OBDResponseProcessor.validateResponse(processedResponse);
        // four byte - CAN ID
        // one byte  - Response mode
        // one byte  - Response pid
        // null terminated CAL ID string
        // 000007E8 49 .....
        byte responseType = processedResponse[4];
        if (responseType != OBD_INFO_RESPONSE) {
            throw new InvalidResponseException(
                    "Unexpected " + module.getName() + " Info response type: " + 
                    asHex(new byte[]{responseType}));
        }
    }

    @Override
    public EcuInit parseEcuInitResponse(byte[] processedResponse) {
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        final byte[] ecuInitBytes = parseResponseData(processedResponse);
        final byte[] calIdBytes = Arrays.copyOf(ecuInitBytes, 8);
        final String calIdStr = new String(calIdBytes);
        final Settings settings = SettingsManager.getSettings();

        final Map<String, EcuDefinition> defMap =
                settings.getLoggerEcuDefinitionMap();
        byte[] ecuIdBytes = new byte[] {0,0,0,0,0};
        // convert CALID to ECUID based on defined ECU defs
        for (EcuDefinition ecuDef : defMap.values()) {
            if (ecuDef.getCalId().equals(calIdStr)) {
                ecuIdBytes = asBytes(ecuDef.getEcuId());
                break;
            }
        }

        arraycopy(ecuIdBytes, 0, ecuInitBytes, 3, 5);
        return new SSMEcuInit(ecuInitBytes);
    }

    @Override
    public byte[] constructEcuResetRequest(Module module, int resetCode) {
        checkNotNull(module, "module");
        OBDProtocol.module = module;
        //  000007E0 04
        return buildRequest((byte) 0, false, new byte[]{OBD_RESET_COMMAND});
    }

    @Override
    public void checkValidEcuResetResponse(byte[] processedResponse) {
        checkNotNullOrEmpty(processedResponse, "processedResponse");
        // 000007E8 44
        byte responseType = processedResponse[4];
        if (responseType != OBD_RESET_RESPONSE) {
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
            boolean addCommand, 
            byte[]... content) {

        bb.reset();
        try {
            bb.write(module.getTester());
            if (addCommand) {
                bb.write(command);
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
