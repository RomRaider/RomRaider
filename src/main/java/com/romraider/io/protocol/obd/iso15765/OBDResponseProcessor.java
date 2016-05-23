/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2014 RomRaider.com
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

import static com.romraider.io.protocol.obd.iso15765.OBDProtocol.OBD_INFO_RESPONSE;
import static com.romraider.io.protocol.obd.iso15765.OBDProtocol.OBD_INIT_RESPONSE;
import static com.romraider.io.protocol.obd.iso15765.OBDProtocol.OBD_NRC;
import static com.romraider.io.protocol.obd.iso15765.OBDProtocol.OBD_RESET_RESPONSE;
import static com.romraider.io.protocol.obd.iso15765.OBDProtocol.RESPONSE_NON_DATA_BYTES;
import static com.romraider.io.protocol.obd.iso15765.OBDProtocol.module;
import static com.romraider.util.ByteUtil.asUnsignedInt;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

import java.util.Arrays;

import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.exception.InvalidResponseException;

public final class OBDResponseProcessor {

    private OBDResponseProcessor() {
        throw new UnsupportedOperationException();
    }

    public final static byte[] filterRequestFromResponse(
            byte[] request, byte[] response, PollingState pollState) {

        checkNotNullOrEmpty(response, "response");
        return response;
    }

    public final static void validateResponse(byte[] response) {
        checkNotNullOrEmpty(response, "response");
        assertTrue(response.length > RESPONSE_NON_DATA_BYTES,
                "Invalid response length");
        assertEquals(module.getAddress(), response, "Invalid " +
                module.getName() + " id");
        if (response[4] == OBD_NRC) {
            assertNrc(OBD_NRC, response[4], response[5], response[6],
                    "Request type not supported");
        }
        assertOneOf(new byte[]{
                OBD_INIT_RESPONSE, OBD_INFO_RESPONSE, OBD_RESET_RESPONSE},
                response[4], "Invalid response code");
    }

    public final static byte[] extractResponseData(byte[] response) {
        checkNotNullOrEmpty(response, "response");
        // ECU_addr response_mode pid1 response_data1 ... [pid6 response_data6]
        validateResponse(response);
        final byte[] data = new byte[response.length - RESPONSE_NON_DATA_BYTES];
        System.arraycopy(response, RESPONSE_NON_DATA_BYTES, data, 0, data.length);
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
            if (code == 0x12) {
                ec = "request sub-function is not supported.";
            }
            if (code == 0x13) {
                ec = "invalid format or length.";
            }
            if (code == 0x22) {
                ec = "is supported but data is currently not available.";
            }
            throw new InvalidResponseException(String.format(
                    "%s. Command: %s, %s",
                    msg, asHex(command), ec));
        }
    }

    private final static void assertEquals(
            byte[] expected, byte[] actual, String msg) {

        final byte[] idBytes = Arrays.copyOf(actual, 4);
        final int idExpected = asUnsignedInt(expected);
        final int idActual = asUnsignedInt(idBytes);
        if (idActual != idExpected) {
            throw new InvalidResponseException(String.format(
                    "%s. Expected: %s. Actual: %s.",
                    msg, asHex(expected), asHex(idBytes)));
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
