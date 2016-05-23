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

package com.romraider.logger.ecu.comms.query;

import com.romraider.logger.ecu.definition.LoggerData;

/**
 * @return the length of the data based on the data storage type
 * value set in the selected converter.
 */
public final class EcuQueryData {

    public final static int getDataLength(EcuQuery ecuQuery) {
        return getDataLength(ecuQuery.getLoggerData());
    }

    public final static int getDataLength(LoggerData loggerData) {
        final String dataType =
                loggerData.getSelectedConvertor().getDataType().toLowerCase();
        return getDataLength(dataType);
    }

    public final static int getDataLength(String dataType) {
        int dataLength = 1;
        if (dataType.contains("int16")) {
            dataLength = 2;
        }
        else if (dataType.contains("int32") ||
                 dataType.contains("float")) {
            dataLength = 4;
        }
        return dataLength;
    }

    private EcuQueryData() {}
}
