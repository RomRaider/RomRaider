package com.romraider.logger.plx.io;

import static com.romraider.logger.plx.io.PlxParserImpl.ParserState.EXPECTING_FIRST_HALF_OF_SENSOR_TYPE;
import static com.romraider.logger.plx.io.PlxParserImpl.ParserState.EXPECTING_FIRST_HALF_OF_VALUE;
import static com.romraider.logger.plx.io.PlxParserImpl.ParserState.EXPECTING_INSTANCE;
import static com.romraider.logger.plx.io.PlxParserImpl.ParserState.EXPECTING_SECOND_HALF_OF_SENSOR_TYPE;
import static com.romraider.logger.plx.io.PlxParserImpl.ParserState.EXPECTING_SECOND_HALF_OF_VALUE;
import static com.romraider.logger.plx.io.PlxParserImpl.ParserState.EXPECTING_START;
import static com.romraider.logger.plx.io.PlxSensorType.valueOf;

public final class PlxParserImpl implements PlxParser {
    private ParserState state = EXPECTING_START;
    private PlxSensorType sensorType;
    private int partialValue;

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
            case EXPECTING_FIRST_HALF_OF_SENSOR_TYPE:
                state = EXPECTING_SECOND_HALF_OF_SENSOR_TYPE;
                partialValue = b;
                break;

            case EXPECTING_SECOND_HALF_OF_SENSOR_TYPE:
                state = EXPECTING_INSTANCE;
                int value = (partialValue << 6) | b;
                sensorType = valueOf(value);
                break;

            case EXPECTING_INSTANCE:
                state = EXPECTING_FIRST_HALF_OF_VALUE;
                break;

            case EXPECTING_FIRST_HALF_OF_VALUE:
                state = EXPECTING_SECOND_HALF_OF_VALUE;
                partialValue = b;
                break;

            case EXPECTING_SECOND_HALF_OF_VALUE:
                state = EXPECTING_FIRST_HALF_OF_SENSOR_TYPE;
                int rawValue = (partialValue << 6) | b;
                return new PlxResponse(sensorType, rawValue);
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
