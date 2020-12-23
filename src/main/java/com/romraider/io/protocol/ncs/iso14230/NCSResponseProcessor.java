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

package com.romraider.io.protocol.ncs.iso14230;

import static com.romraider.io.protocol.ncs.iso14230.NCSChecksumCalculator.calculateChecksum;
import static com.romraider.io.protocol.ncs.iso14230.NCSProtocol.ECU_ID_SID_RESPONSE;
import static com.romraider.io.protocol.ncs.iso14230.NCSProtocol.LOAD_ADDRESS_RESPONSE;
import static com.romraider.io.protocol.ncs.iso14230.NCSProtocol.NCS_NRC;
import static com.romraider.io.protocol.ncs.iso14230.NCSProtocol.READ_LOAD_RESPONSE;
import static com.romraider.io.protocol.ncs.iso14230.NCSProtocol.READ_SID_GRP_RESPONSE;
import static com.romraider.io.protocol.ncs.iso14230.NCSProtocol.RESPONSE_NON_DATA_BYTES;
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

    public final static void validateResponse(byte[] response) {
        checkNotNullOrEmpty(response, "response");
        assertTrue(response.length > RESPONSE_NON_DATA_BYTES,
                "Invalid response length");
        validateChecksum(response);
        if (response[1] == NCS_NRC) {
            assertNrc((byte) (response[2] + 0x40), response[1], response[2], response[3],
                    "Request type not supported");
        }
        if ((response[0] & (byte)0x80) == (byte)0x80) { // long header
            assertOneOf(new byte[]{ECU_ID_SID_RESPONSE, LOAD_ADDRESS_RESPONSE,
                    READ_LOAD_RESPONSE, READ_SID_GRP_RESPONSE},
                    response[3], "Invalid response code");
        }
        else {  // short header
            assertOneOf(new byte[]{ECU_ID_SID_RESPONSE, LOAD_ADDRESS_RESPONSE,
                    READ_LOAD_RESPONSE, READ_SID_GRP_RESPONSE},
                    response[1], "Invalid response code");
        }
    }

    public final static byte[] extractResponseData(byte[] response) {
        checkNotNullOrEmpty(response, "response");
        // len response_sid option response_data1 ... [response_dataN] CS
        validateResponse(response);
        byte[] data = new byte[]{};
        // Strip headers and CS returning only the payload
        if (response[1] == ECU_ID_SID_RESPONSE) {
            data = new byte[response.length - RESPONSE_NON_DATA_BYTES];
            System.arraycopy(response, RESPONSE_NON_DATA_BYTES-1, data, 0, data.length);
        }
        if (response[1] == READ_LOAD_RESPONSE) {
            data = new byte[response.length - 4];
            System.arraycopy(response, RESPONSE_NON_DATA_BYTES, data, 0, data.length);
        }
        if (response[1] == READ_SID_GRP_RESPONSE) {
            data = new byte[response.length - 5];
            System.arraycopy(response, RESPONSE_NON_DATA_BYTES+1, data, 0, data.length);
        }
        return data;
    }

    private final static void validateChecksum(byte[] response) {
        final byte calc_chk = calculateChecksum(response);
        final byte pkt_cs = response[response.length - 1];
        assertTrue(calc_chk == pkt_cs, String.format(
                "Response checksum match failure. Expected: %s, Actual: %s",
                asHex(calc_chk), asHex(pkt_cs)));
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
