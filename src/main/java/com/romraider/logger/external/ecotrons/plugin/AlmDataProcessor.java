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

package com.romraider.logger.external.ecotrons.plugin;

import static com.romraider.util.ByteUtil.asSignedInt;
import static com.romraider.util.ByteUtil.asUnsignedInt;
import static com.romraider.util.ByteUtil.byteListToBytes;

import java.util.List;
import java.util.Map;


public final class AlmDataProcessor {

    private AlmDataProcessor() {}

    public final static void parseResponse(
            Map<AlmSensorType, AlmDataItem> dataItems,
            List<Byte> buffer) {

        final byte[] response = new byte[buffer.size()];
        byteListToBytes(buffer, response);
        for (AlmSensorType sensor : dataItems.keySet()) {
            int value = 0;
            byte[] bytes = new byte[2];
            switch (sensor) {
                case AFR1:
                    System.arraycopy(response, 6, bytes, 0, bytes.length);
                    value = asUnsignedInt(bytes);
                    dataItems.get(sensor).setData(value * 0.001);
                    break;
                case AFR2:
                    System.arraycopy(response, 8, bytes, 0, bytes.length);
                    value = asUnsignedInt(bytes);
                    dataItems.get(sensor).setData(value * 0.001);
                    break;
                case RPM:
                    System.arraycopy(response, 10, bytes, 0, bytes.length);
                    value = asUnsignedInt(bytes);
                    dataItems.get(sensor).setData(value * 40);
                    break;
                case VDC1:
                    System.arraycopy(response, 12, bytes, 0, bytes.length);
                    value = asUnsignedInt(bytes);
                    dataItems.get(sensor).setData(value * 5 / 1024);
                    break;
                case VDC2:
                    System.arraycopy(response, 14, bytes, 0, bytes.length);
                    value = asUnsignedInt(bytes);
                    dataItems.get(sensor).setData(value * 5 / 1024);
                    break;
                case TEMP1:
                    System.arraycopy(response, 16, bytes, 0, bytes.length);
                    value = asUnsignedInt(bytes);
                    dataItems.get(sensor).setData(value * 0.023438 - 273);
                    break;
                case TEMP2:
                    System.arraycopy(response, 18, bytes, 0, bytes.length);
                    value = asUnsignedInt(bytes);
                    dataItems.get(sensor).setData(value * 0.023438 - 273);
                    break;
                case O21:
                    System.arraycopy(response, 20, bytes, 0, bytes.length);
                    value = asSignedInt(bytes);
                    dataItems.get(sensor).setData(value * 0.001);
                    break;
                case O22:
                    System.arraycopy(response, 22, bytes, 0, bytes.length);
                    value = asSignedInt(bytes);
                    dataItems.get(sensor).setData(value * 0.001);
                    break;
                default:
                    break;
            }
        }
    }
}