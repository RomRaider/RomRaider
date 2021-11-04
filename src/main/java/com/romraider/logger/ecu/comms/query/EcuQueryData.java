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

import com.romraider.logger.ecu.definition.LoggerData;

/**
 * @return the length of the data based on the data storage type
 * value set in the selected converter.
 */
public final class EcuQueryData {

    /**
     * Return the length for an ECU Query item. The larger of either the
     * number of addresses or the length defined by the data storagetype.
     * @param ecuQuery - the ECU query to evaluate
     * @return the length of the data type
     */
    public final static int getDataLength(EcuQuery ecuQuery) {
        // A query has its data length encoded in the definition using either
        // the length="#" attribute of an address element or, by the
        // storagetype="?" attribute in a conversion element.
        final int addressLength = ecuQuery.getAddresses().length;
        int dataTypeLength = getDataLength(ecuQuery.getLoggerData());
        if (addressLength > dataTypeLength) {
            dataTypeLength = addressLength;
        }
        return dataTypeLength;
    }

    /**
     * Return the length for an Logger Data item.
     * @param loggerData - the Logger Data to evaluate
     * @return the length of the data type
     */
    public final static int getDataLength(LoggerData loggerData) {
        final String dataType =
                loggerData.getSelectedConvertor().getDataType().toLowerCase();
        return getDataLength(dataType);
    }

    /**
     * Return the length for a Data type, int8, uint8, int16, etc.
     * @param dataType - the Data type string to evaluate
     * @return the length of the data type
     */
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
}
