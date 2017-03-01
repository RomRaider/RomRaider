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

package com.romraider.io.protocol.ds2.iso9141;

import static com.romraider.io.protocol.ds2.iso9141.DS2ChecksumCalculator.calculateChecksum;
import static com.romraider.io.protocol.ds2.iso9141.DS2Protocol.RESPONSE_NON_DATA_BYTES;
import static com.romraider.io.protocol.ds2.iso9141.DS2Protocol.VALID_RESPONSE;
import static com.romraider.io.protocol.ds2.iso9141.DS2Protocol.module;
import static com.romraider.util.ByteUtil.asByte;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.exception.InvalidResponseException;

public final class DS2ResponseProcessor {

    private DS2ResponseProcessor() {
        throw new UnsupportedOperationException();
    }

    public static byte[] filterRequestFromResponse(byte[] request, byte[] response, PollingState pollState) {
        checkNotNull(request, "request");
        checkNotNullOrEmpty(response, "response");
        checkNotNull(pollState, "pollState");
        byte[] filteredResponse = new byte[0];
        if (pollState.getCurrentState() == PollingState.State.STATE_0) {
            filteredResponse = new byte[response.length - request.length];
            System.arraycopy(response, request.length, filteredResponse, 0, filteredResponse.length);
        }
        if (pollState.getCurrentState() == PollingState.State.STATE_1) {
            filteredResponse = new byte[response.length];
            System.arraycopy(response, 0, filteredResponse, 0, filteredResponse.length);
        }
        return filteredResponse;
    }

    public static void validateResponse(byte[] response) {
        int i = 0;
        assertTrue(response.length >= RESPONSE_NON_DATA_BYTES, "Invalid response length");
        assertEquals(module.getAddress()[0], response[i++], "Invalid " + module.getName() + " id");
        assertEquals(asByte(response.length), response[i++], "Invalid response packet length");
        assertEquals(VALID_RESPONSE, response[i], "Request not supported");
        assertEquals(calculateChecksum(response), response[response.length - 1], "Invalid checksum");
    }

    public static byte[] extractResponseData(byte[] response) {
        checkNotNullOrEmpty(response, "response");
        // 12 <length> a0 <bytes> <checksum>
        validateResponse(response);
        byte[] data = new byte[response.length - RESPONSE_NON_DATA_BYTES];
        System.arraycopy(response, (RESPONSE_NON_DATA_BYTES - 1), data, 0, data.length);
        return data;
    }


    private static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new InvalidResponseException(msg);
        }
    }


    private static void assertEquals(byte expected, byte actual, String msg) {
        if (actual != expected) {
            throw new InvalidResponseException(msg + ". Expected: " + asHex(new byte[]{expected}) + ". Actual: " + asHex(new byte[]{actual}) + ".");
        }
    }
}
