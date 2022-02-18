/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

package com.romraider.logger.external.plx.io;

import static com.romraider.logger.external.plx.io.PlxParserImpl.ParserState.EXPECTING_FIRST_HALF_OF_SENSOR_TYPE;
import static com.romraider.logger.external.plx.io.PlxParserImpl.ParserState.EXPECTING_FIRST_HALF_OF_VALUE;
import static com.romraider.logger.external.plx.io.PlxParserImpl.ParserState.EXPECTING_INSTANCE;
import static com.romraider.logger.external.plx.io.PlxParserImpl.ParserState.EXPECTING_SECOND_HALF_OF_SENSOR_TYPE;
import static com.romraider.logger.external.plx.io.PlxParserImpl.ParserState.EXPECTING_SECOND_HALF_OF_VALUE;
import static com.romraider.logger.external.plx.io.PlxParserImpl.ParserState.EXPECTING_START;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.valueOf;
import static org.apache.log4j.Logger.getLogger;

import org.apache.log4j.Logger;

import com.romraider.logger.external.plx.plugin.PlxSensorType;


public final class PlxParserImpl implements PlxParser {
    private static final Logger LOGGER = getLogger(PlxParserImpl.class);
    private ParserState state = EXPECTING_START;
    private PlxSensorType sensorType;
    private int partialValue;
    private byte instance;

    @Override
    public PlxResponse pushByte(byte b) {
        if (b == (byte) 0x80) {
            state = EXPECTING_FIRST_HALF_OF_SENSOR_TYPE;
            return null;
        }

        if (b == 0x40) {
            state = EXPECTING_START;
            return null;
        }

        switch (state) {
            case EXPECTING_START:
                break;
            case EXPECTING_FIRST_HALF_OF_SENSOR_TYPE:
                state = EXPECTING_SECOND_HALF_OF_SENSOR_TYPE;
                partialValue = b;
                break;

            case EXPECTING_SECOND_HALF_OF_SENSOR_TYPE:
                state = EXPECTING_INSTANCE;
                int value = (partialValue << 6) | b;
                sensorType = valueOf(value);
                if (PlxSensorType.UNKNOWN == sensorType) {
                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace(String.format(
                            "PLX sensor address: %d, unknown sensor type", value));
                }
                break;

            case EXPECTING_INSTANCE:
                state = EXPECTING_FIRST_HALF_OF_VALUE;
                instance = b;
                break;

            case EXPECTING_FIRST_HALF_OF_VALUE:
                state = EXPECTING_SECOND_HALF_OF_VALUE;
                partialValue = b;
                break;

            case EXPECTING_SECOND_HALF_OF_VALUE:
                state = EXPECTING_FIRST_HALF_OF_SENSOR_TYPE;
                int rawValue = (partialValue << 6) | b;
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace(String.format(
                        "PLX sensor: %s instance: %d, value: %d",
                        sensorType, instance, rawValue));
                return new PlxResponse(sensorType, instance, rawValue);
        }
        return null;
    }

    enum ParserState {
        EXPECTING_START,
        EXPECTING_FIRST_HALF_OF_SENSOR_TYPE,
        EXPECTING_SECOND_HALF_OF_SENSOR_TYPE,
        EXPECTING_INSTANCE,
        EXPECTING_FIRST_HALF_OF_VALUE,
        EXPECTING_SECOND_HALF_OF_VALUE,
    }
}
