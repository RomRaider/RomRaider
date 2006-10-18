// Parses attributes from ROM XML

package enginuity.xml;

import static enginuity.maps.Scale.INVERSE;
import static enginuity.maps.Scale.LINEAR;
import static enginuity.maps.Table.ENDIAN_BIG;
import static enginuity.maps.Table.ENDIAN_LITTLE;
import static enginuity.maps.Table.STORAGE_TYPE_FLOAT;
import static enginuity.maps.Table.TABLE_1D;
import static enginuity.maps.Table.TABLE_2D;
import static enginuity.maps.Table.TABLE_3D;
import static enginuity.maps.Table.TABLE_X_AXIS;
import static enginuity.maps.Table.TABLE_Y_AXIS;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RomAttributeParser {

    private RomAttributeParser() {
    }

    public static int parseEndian(String input) {
        if (input.equalsIgnoreCase("big") || input.equalsIgnoreCase(String.valueOf(ENDIAN_BIG))) {
            return ENDIAN_BIG;
        } else if (input.equalsIgnoreCase("little") || input.equalsIgnoreCase(String.valueOf(ENDIAN_LITTLE))) {
            return ENDIAN_LITTLE;
        } else {
            return ENDIAN_LITTLE;
        }
    }

    public static int parseHexString(String input) {
        if (input.equals("0")) {
            return 0;
        } else if (input.length() > 2 && input.substring(0, 2).equalsIgnoreCase("0x")) {
            return Integer.parseInt(input.substring(2), 16);
        } else {
            return Integer.parseInt(input, 16);
        }
    }

    public static int parseStorageType(String input) {
        if (input.equalsIgnoreCase("float")) {
            return STORAGE_TYPE_FLOAT;
        } else if (input.length() > 4 && input.substring(0, 4).equalsIgnoreCase("uint")) {
            return Integer.parseInt(input.substring(4)) / 8;
        } else {
            return Integer.parseInt(input);
        }
    }

    public static int parseScaleType(String input) {
        if (input.equalsIgnoreCase("inverse")) {
            return INVERSE;
        } else {
            return LINEAR;
        }
    }

    public static int parseTableType(String input) {
        if (input.equalsIgnoreCase("3D") || input.equalsIgnoreCase(String.valueOf(TABLE_3D))) {
            return TABLE_3D;
        } else if (input.equalsIgnoreCase("2D") || input.equalsIgnoreCase(String.valueOf(TABLE_2D))) {
            return TABLE_2D;
        } else if (input.equalsIgnoreCase("X Axis") || input.equalsIgnoreCase("Static X Axis") || input.equalsIgnoreCase(String.valueOf(TABLE_X_AXIS))) {
            return TABLE_X_AXIS;
        } else if (input.equalsIgnoreCase("Y Axis") || input.equalsIgnoreCase("Static Y Axis") || input.equalsIgnoreCase(String.valueOf(TABLE_Y_AXIS))) {
            return TABLE_Y_AXIS;
        } else {
            return TABLE_1D;
        }
    }

    public static int parseByteValue(byte[] input, int endian, int address, int length) throws ArrayIndexOutOfBoundsException {
        try {
            int output = 0;
            if (endian == ENDIAN_BIG) {
                for (int i = 0; i < length; i++) {
                    output += (input[address + i] & 0xff) << 8 * (length - i - 1);
                }
            } else { // little endian
                for (int i = 0; i < length; i++) {
                    output += (input[address + length - i - 1] & 0xff) << 8 * (length - i - 1);
                }
            }
            return output;
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public static byte[] parseIntegerValue(int input, int endian, int length) {
        byte[] byteArray = new byte[4];

        byteArray[0] = (byte) ((input >> 24) & 0x000000FF);
        byteArray[1] = (byte) ((input >> 16) & 0x0000FF);
        byteArray[2] = (byte) ((input >> 8) & 0x00FF);
        byteArray[3] = (byte) (input & 0xFF);

        byte[] output = new byte[length];

        for (int i = 0; i < length; i++) {
            if (endian == ENDIAN_BIG) {
                //output[i] = byteArray[i + length];
                output[i] = byteArray[4 - length + i];
            } else { // little endian
                output[length - 1 - i] = byteArray[4 - length + i];
            }
        }
        return output;
    }

    public static int parseFileSize(String input) throws NumberFormatException {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            if (input.substring(input.length() - 2).equalsIgnoreCase("kb")) {
                return Integer.parseInt(input.substring(0, input.length() - 2)) * 1024;
            } else if (input.substring(input.length() - 2).equalsIgnoreCase("mb")) {
                return Integer.parseInt(input.substring(0, input.length() - 2)) * 1024 * 1024;
            }
            throw new NumberFormatException();
        }
    }

    public static byte[] floatToByte(float input, int endian) {
        byte[] output = new byte[4];
        ByteBuffer bb = ByteBuffer.wrap(output, 0, 4);
        if (endian == ENDIAN_LITTLE) {
            bb.order(ByteOrder.BIG_ENDIAN);
        }
        bb.putFloat(input);
        return bb.array();
    }

    public static float byteToFloat(byte[] input, int endian) {
        ByteBuffer bb = ByteBuffer.wrap(input, 0, 4);
        if (endian == ENDIAN_LITTLE) {
            bb.order(ByteOrder.BIG_ENDIAN);
        }
        return bb.getFloat();
    }
}