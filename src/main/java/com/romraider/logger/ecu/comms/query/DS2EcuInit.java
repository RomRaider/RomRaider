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

package com.romraider.logger.ecu.comms.query;

import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static java.lang.System.arraycopy;

public final class DS2EcuInit implements EcuInit {
    private byte[] ecuInitBytes;
    private String ecuId;

    public DS2EcuInit(byte[] ecuInitBytes) {
        checkNotNullOrEmpty(ecuInitBytes, "ecuInitBytes");
        this.ecuInitBytes = ecuInitBytes;
        final byte[] ecuIdBytes = new byte[7];
        arraycopy(ecuInitBytes, 0, ecuIdBytes, 0, 7);
        ecuId = new String(ecuIdBytes);
    }

    public String getEcuId() {
        return ecuId;
    }

    public byte[] getEcuInitBytes() {
        return ecuInitBytes;
    }

}
