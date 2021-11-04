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

import static com.romraider.io.protocol.ncs.iso15765.NCSProtocol.module;
import static com.romraider.io.protocol.ncs.iso15765.NCSProtocol.ECU_INIT_RESPONSE;
import static com.romraider.io.protocol.ncs.iso15765.NCSProtocol.ECU_ID_SID_RESPONSE;
import static com.romraider.io.protocol.ncs.iso15765.NCSProtocol.READ_SID_21_RESPONSE;
import static com.romraider.io.protocol.ncs.iso15765.NCSProtocol.NCS_NRC;
import static com.romraider.io.protocol.ncs.iso15765.NCSProtocol.READ_SID_GRP_RESPONSE;
import static com.romraider.io.protocol.ncs.iso15765.NCSProtocol.READ_MEMORY_RESPONSE;
import static com.romraider.io.protocol.ncs.iso15765.NCSProtocol.RESPONSE_NON_DATA_BYTES;
import static com.romraider.io.protocol.ncs.iso15765.NCSProtocol.LOAD_ADDRESS_RESPONSE;
import static com.romraider.io.protocol.ncs.iso15765.NCSProtocol.READ_LOAD_RESPONSE;
import static com.romraider.util.ByteUtil.asUnsignedInt;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.exception.InvalidResponseException;

public final class NCSResponseProcessor {

    private NCSResponseProcessor() {
        throw new UnsupportedOperationException();
    }

    public final static byte[] filterRequestFromResponse(
            byte[] request, byte[] response, PollingState pollState) {
        checkNotNullOrEmpty(response, "response");
        // If J2534 device Loopback is off, the request is filtered out by J2534 device
        // and only the response is present
        return response;
    }

    public static void validateResponse(byte[] response) {
        assertTrue(response.length > RESPONSE_NON_DATA_BYTES, "Invalid response length");
        assertEquals(module.getAddress(), response, "Invalid " +
                module.getName() + " id");
        if (response[4] == NCS_NRC) {
            assertNrc(NCS_NRC, response[4], response[5], response[6], "Request type not supported");
        }
        assertOneOf(new byte[]{ECU_INIT_RESPONSE, ECU_ID_SID_RESPONSE,
                READ_SID_21_RESPONSE, READ_SID_GRP_RESPONSE,
                READ_MEMORY_RESPONSE, LOAD_ADDRESS_RESPONSE,
                READ_LOAD_RESPONSE}, response[4], "Invalid response code");
    }

    public static byte[] extractResponseData(byte[] response) {
        checkNotNullOrEmpty(response, "response");
        // 0x00 0x00 0x07 0xe0 response_command response_data
        validateResponse(response);
        int nonDataLength = 0;
        byte[] data = new byte[]{};
        if (response[4] == ECU_ID_SID_RESPONSE) {
            nonDataLength = RESPONSE_NON_DATA_BYTES;
        }
        else if (response[4] == READ_SID_21_RESPONSE) {
            nonDataLength = RESPONSE_NON_DATA_BYTES + 2;
        }
        else if (response[4] == READ_SID_GRP_RESPONSE) {
            nonDataLength = RESPONSE_NON_DATA_BYTES + 3;
        }
        else if (response[4] == READ_MEMORY_RESPONSE) {
            nonDataLength = RESPONSE_NON_DATA_BYTES + 1;
        }
        else if (response[4] == LOAD_ADDRESS_RESPONSE) {
            nonDataLength = RESPONSE_NON_DATA_BYTES + 2;
        }
        else if (response[4] == READ_LOAD_RESPONSE) {
            nonDataLength = RESPONSE_NON_DATA_BYTES + 2;
        }
        data = new byte[response.length - nonDataLength];
        System.arraycopy(response, nonDataLength, data, 0, data.length);
        return data;
    }

    private final static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new InvalidResponseException(msg);
        }
    }

    private final static void assertNrc(
            byte expected, byte actual, byte command, byte code, String msg) {

        if (actual == expected) {
            String ec = "unsupported.";
            if (code == 0x10) {
                ec = "general reject no specific reason.";
            }
            if (code == 0x11) {
                ec = "mode not supported.";
            }
            if (code == 0x12) {
                ec = "request sub-function is not supported or invalid format.";
            }
            if (code == 0x13) {
                ec = "invalid format or length.";
            }
            if (code == 0x21) {
                ec = "busy, repeat request.";
            }
            if (code == 0x22) {
                ec = "conditions not correct or request sequence error.";
            }
            throw new InvalidResponseException(String.format(
                    "%s. Command: %s, %s",
                    msg, asHex(command), ec));
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

    private final static void assertOneOf(
            byte[] validOptions, byte actual, String msg) {

        for (byte option : validOptions) {
            if (option == actual) {
                return;
            }
        }
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < validOptions.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(asHex(validOptions[i]));
        }
        throw new InvalidResponseException(String.format(
                "%s. Expected one of [%s]. Actual: %s.",
                msg, builder.toString(), asHex(actual)));
    }
}
