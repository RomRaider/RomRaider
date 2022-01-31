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

package com.romraider.logger.ecu.comms.query;

import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static java.lang.System.arraycopy;

public final class SSMEcuInit implements EcuInit {
    private byte[] ecuInitBytes;
    private String ecuId;

    /**
     * Create a SSM ECU init class for the module and determine the ECU ID from
     * within the init byte sequence.
     * @param ecuInitBytes - init sequence from the module
     */
    public SSMEcuInit(byte[] ecuInitBytes) {
        checkNotNullOrEmpty(ecuInitBytes, "ecuInitBytes");
        this.ecuInitBytes = ecuInitBytes;
        byte[] ecuIdBytes = new byte[5];
        arraycopy(ecuInitBytes, 3, ecuIdBytes, 0, 5);
        ecuId = asHex(ecuIdBytes);
    }

    /**
     * Create a SSM ECU init class for the module and set the ECU ID to the value
     * provided.  Use this when the ECU ID contains characters that can't be
     * encoded in the init byte sequence.
     * @param ecuInitBytes - init sequence from the module
     * @param ecuIdString - ECU ID string
     */
    public SSMEcuInit(byte[] ecuInitBytes, String ecuIdString) {
        checkNotNullOrEmpty(ecuInitBytes, "ecuInitBytes");
        checkNotNullOrEmpty(ecuIdString, "ecuIdString");
        this.ecuInitBytes = ecuInitBytes;
        this.ecuId = ecuIdString;
    }

    @Override
    public String getEcuId() {
        return ecuId;
    }

    @Override
    public byte[] getEcuInitBytes() {
        return ecuInitBytes;
    }

}
