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

import static com.romraider.io.protocol.ssm.iso15765.SSMProtocol.module;
import static com.romraider.io.protocol.ssm.iso15765.SSMProtocol.ECU_INIT_RESPONSE;
import static com.romraider.io.protocol.ssm.iso15765.SSMProtocol.ECU_NRC;
import static com.romraider.io.protocol.ssm.iso15765.SSMProtocol.READ_ADDRESS_RESPONSE;
import static com.romraider.io.protocol.ssm.iso15765.SSMProtocol.READ_MEMORY_RESPONSE;
import static com.romraider.io.protocol.ssm.iso15765.SSMProtocol.RESPONSE_NON_DATA_BYTES;
import static com.romraider.io.protocol.ssm.iso15765.SSMProtocol.WRITE_ADDRESS_RESPONSE;
import static com.romraider.io.protocol.ssm.iso15765.SSMProtocol.WRITE_MEMORY_RESPONSE;
import static com.romraider.util.ByteUtil.asUnsignedInt;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.exception.InvalidResponseException;

public final class SSMResponseProcessor {

    private SSMResponseProcessor() {
        throw new UnsupportedOperationException();
    }

    public static byte[] filterRequestFromResponse(byte[] request, byte[] response, PollingState pollState) {
        checkNotNullOrEmpty(response, "response");
        return response;
    }

    public static void validateResponse(byte[] response) {
        assertTrue(response.length > RESPONSE_NON_DATA_BYTES, "Invalid response length");
        assertEquals(module.getAddress(), response, "Invalid " +
                module.getName() + " id");
        if (response[4] == ECU_NRC) {
            assertNrc(ECU_NRC, response[4], response[5], response[6],"Request type not supported");
        }
        assertOneOf(new byte[]{ECU_INIT_RESPONSE, READ_ADDRESS_RESPONSE, READ_MEMORY_RESPONSE, WRITE_ADDRESS_RESPONSE, WRITE_MEMORY_RESPONSE}, response[4], "Invalid response code");
    }

    public static byte[] extractResponseData(byte[] response) {
        checkNotNullOrEmpty(response, "response");
        // 0x00 0x00 0x07 0xe0 response_command response_data
        validateResponse(response);
        final byte[] data = new byte[response.length - RESPONSE_NON_DATA_BYTES];
        System.arraycopy(response, (RESPONSE_NON_DATA_BYTES), data, 0, data.length);
        return data;
    }


    private static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new InvalidResponseException(msg);
        }
    }

    private static void assertNrc(byte expected, byte actual, byte command, byte code, String msg) {
        if (actual == expected) {
            String ec = " unsupported parameter.";
            if (code == 0x13) {
                ec = " invalid format or length.";
            }
            if (code == 0x22) {
                ec = " address not allowed.";
            }
            throw new InvalidResponseException(
                    msg + ". Command: " + asHex(new byte[]{command}) + ec);
        }
    }

    private static void assertEquals(byte[] expected, byte[] actual, String msg) {
        final byte[] idBytes = new byte[4];
        System.arraycopy(actual, 0, idBytes, 0, 4);
        final int idExpected = asUnsignedInt(expected);
        final int idActual = asUnsignedInt(idBytes);
        if (idActual != idExpected) {
            throw new InvalidResponseException(msg + ". Expected: " + asHex(expected) + ". Actual: " + asHex(idBytes) + ".");
        }
    }

    private static void assertOneOf(byte[] validOptions, byte actual, String msg) {
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
