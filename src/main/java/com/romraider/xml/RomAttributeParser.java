/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
<<<<<<< HEAD
<<<<<<< HEAD
 * Copyright (C) 2006-2020 RomRaider.com
=======
 * Copyright (C) 2006-2019 RomRaider.com
>>>>>>> Added XOR for single byte checksums. Added possibility of multiple checksums in file
=======
 * Copyright (C) 2006-2020 RomRaider.com
>>>>>>> Updated copyright. Switched to checkboxes for presets. Allowed multiple selection. Fixed saving bug. CChanged table name to 2DMaskedSwitchable
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

// Parses attributes from ROM XML

package com.romraider.xml;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.romraider.Settings;
import com.romraider.maps.Table;
import com.romraider.util.ByteUtil;

public final class RomAttributeParser {

    private RomAttributeParser() {
    }

    public static Settings.Endian parseEndian(String input) {
        if (input.equalsIgnoreCase("big") || input.equalsIgnoreCase(Settings.Endian.BIG.getMarshallingString())) {
            return Settings.Endian.BIG;
        }
        else if (input.equalsIgnoreCase("little") || input.equalsIgnoreCase(Settings.Endian.LITTLE.getMarshallingString())) {
            return Settings.Endian.LITTLE;
        }
        else {
            return Settings.Endian.LITTLE;
        }
    }

    public static int parseHexString(String input) {
        if (input.equals("0")) {
            return 0;
        }
        else if (input.length() > 2 && input.substring(0, 2).equalsIgnoreCase("0x")) {
            return Integer.parseInt(input.substring(2), 16);
        }
        else {
            return Integer.parseInt(input, 16);
        }
    }

    public static int parseStorageType(String input) {
    	if (input.equalsIgnoreCase("movi20")) {	// when data is in MOVI20 instruction
    		return Settings.STORAGE_TYPE_MOVI20;
    	}
    	else if (input.equalsIgnoreCase("movi20s")) {	// when data is in MOVI20 instruction
    		return Settings.STORAGE_TYPE_MOVI20S;
    	}
    	else if (input.equalsIgnoreCase("float")) {
            return Settings.STORAGE_TYPE_FLOAT;
        }
        else if (input.startsWith("uint")) {
            return Integer.parseInt(input.substring(4)) / 8;
        }
        else if (input.startsWith("int")) {
            return Integer.parseInt(input.substring(3)) / 8;
        }
        else {
            return Integer.parseInt(input);
        }
    }

    public static boolean parseStorageDataSign(String input) {
        if (input.toLowerCase().startsWith("int") ||
        		input.toLowerCase().startsWith("movi20")) { // when data is in MOVI20 instruction
            return true;
        }
        else {
            return false;
        }
    }

    public static int parseScaleType(String input) {
        if (input.equalsIgnoreCase("inverse")) {
            return Settings.INVERSE;
        }
        else {
            return Settings.LINEAR;
        }
    }

    public static Table.TableType parseTableType(String input) {
        if (input.equalsIgnoreCase("3D") || input.equalsIgnoreCase(Table.TableType.TABLE_3D.getMarshallingString())) {
            return Table.TableType.TABLE_3D;
        }
        else if (input.equalsIgnoreCase("2D") || input.equalsIgnoreCase(Table.TableType.TABLE_2D.getMarshallingString())) {
            return Table.TableType.TABLE_2D;
        }
        else if (input.equalsIgnoreCase("X Axis") || input.equalsIgnoreCase("Static X Axis") || input.equalsIgnoreCase(Table.TableType.X_AXIS.getMarshallingString())) {
            return Table.TableType.X_AXIS;
        }
        else if (input.equalsIgnoreCase("Y Axis") || input.equalsIgnoreCase("Static Y Axis") || input.equalsIgnoreCase(Table.TableType.Y_AXIS.getMarshallingString())) {
            return Table.TableType.Y_AXIS;
        }
        else {
            return Table.TableType.TABLE_1D;
        }
    }
    
    //This assumes the bits inside the mask aren't spread. OK = 11110000, Not OK = 11001100

    public static long parseByteValueMasked(byte[] input, Settings.Endian endian, int address, int length, boolean signed, int mask) throws ArrayIndexOutOfBoundsException, IndexOutOfBoundsException { 	 	
    	long tempValue = parseByteValue(input,endian,address,length,signed) & mask;
    	
    	byte index = ByteUtil.firstOneOfMask(mask);
    	
    	return tempValue >> index;
    }
    
    public static long parseByteValue(byte[] input, Settings.Endian endian, int address, int length, boolean signed) throws ArrayIndexOutOfBoundsException, IndexOutOfBoundsException {
        try {
            long output = 0L;
            int llength = length;
            if (length == Settings.STORAGE_TYPE_MOVI20 ||
            		length == Settings.STORAGE_TYPE_MOVI20S) {
            	llength = 3;
            }
            final ByteBuffer bb = ByteBuffer.wrap(input, address, llength);
            if (endian == Settings.Endian.LITTLE) {
                bb.order(ByteOrder.LITTLE_ENDIAN);
            }
            switch (length) {
            case 1:
                output = bb.get();
                break;
            case 2:
                output = bb.getShort();
                break;
            case 4:
                output = bb.getInt();
                break;
            case Settings.STORAGE_TYPE_MOVI20:
            case Settings.STORAGE_TYPE_MOVI20S:
            	bb.position(bb.position()-1);
                output = getMovi20(bb.getInt());
                break;
            }
            if (!signed) {
                switch (length) {
                case 1:
                    output = output & 0xff;
                    break;
                case 2:
                    output = output & 0xffff;
                    break;
                case 4:
                    output = output & 0xffffffffL;
                    break;
                }
            }
            return output;
        } catch (IndexOutOfBoundsException ex) {
            throw new IndexOutOfBoundsException();
        }
    }

    // when data is in MOVI20 instruction
    private static int getMovi20(int value) {
    	final int shift = value & 0x00010000;
    	value = ((value & 0x00f00000) >>> 4) + (value & 0x0000FFFF);
    	if ((value & 0x00080000) > 0) {
    		value = (value | 0xfff00000);
    	}
		if (shift > 0) { //MOVI20S
			return (value << 8);
		}
    	return value;
	}

	public static byte[] parseIntegerValue(int input, Settings.Endian endian, int length) {
        try {
        	int llength = length;
        	if (length == Settings.STORAGE_TYPE_MOVI20 ||
        			length == Settings.STORAGE_TYPE_MOVI20S) {
        		llength = 4;
        	}
            final ByteBuffer bb = ByteBuffer.allocate(llength);
            if (endian == Settings.Endian.LITTLE) {
                bb.order(ByteOrder.LITTLE_ENDIAN);
            }
            switch (length) {
            case 1:
                bb.put((byte) input);
                break;
            case 2:
                bb.putShort((short) input);
                break;
            case 4:
                bb.putInt(input);
                break;
            case Settings.STORAGE_TYPE_MOVI20:
            	return parseMovi20(bb.putInt(input).array(), length);
            case Settings.STORAGE_TYPE_MOVI20S:
            	return parseMovi20(bb.putInt(input>>8).array(), length);
            }
            return bb.array();
        }
        catch (BufferOverflowException ex) {
            throw new BufferOverflowException();
        }
    }

    // when data is in MOVI20 instruction
    private static byte[] parseMovi20(byte[] bytes, int length) {
    	final byte[] output = {0,0,0};
   		output[0] = (byte) (bytes[1] << 4);
    	if (length == Settings.STORAGE_TYPE_MOVI20S) { //MOVI20S
    		output[0] = (byte) ((bytes[1] << 4) | 0x01);
    	}
    	output[1] = bytes[2];
    	output[2] = bytes[3];
    	return output;
	}

    public static int parseFileSize(String input) throws NumberFormatException {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            if (input.substring(input.length() - 2).equalsIgnoreCase("kb")) {
                return Integer.parseInt(input.substring(0, input.length() - 2)) * 1024;
            }
            else if (input.substring(input.length() - 2).equalsIgnoreCase("mb")) {
                return Integer.parseInt(input.substring(0, input.length() - 2)) * 1024 * 1024;
            }
            else if (input.substring(input.length() - 1).equalsIgnoreCase("b")) {
                return Integer.parseInt(input.substring(0, input.length() - 1));
            }
            throw new NumberFormatException();
        }
    }

    public static byte[] floatToByte(float input, Settings.Endian endian, Settings.Endian memModelEndian) {
        byte[] output = new byte[4];
        ByteBuffer bb = ByteBuffer.wrap(output, 0, 4);
        if (memModelEndian == Settings.Endian.LITTLE) {
            bb.order(ByteOrder.LITTLE_ENDIAN);
        }
        else if (memModelEndian == Settings.Endian.BIG) {
            bb.order(ByteOrder.BIG_ENDIAN);
        }
        else {
            // this case corrects improperly defined float table endian in legacy definition files
            if (endian == Settings.Endian.LITTLE) {
                bb.order(ByteOrder.BIG_ENDIAN);
            }
        }
        bb.putFloat(input);
        return bb.array();
    }

    public static float byteToFloat(byte[] input, Settings.Endian endian, Settings.Endian memModelEndian) {
        ByteBuffer bb = ByteBuffer.wrap(input, 0, 4);
        if (memModelEndian == Settings.Endian.LITTLE) {
            bb.order(ByteOrder.LITTLE_ENDIAN);
        }
        else if (memModelEndian == Settings.Endian.BIG) {
            bb.order(ByteOrder.BIG_ENDIAN);
        }
        else {
            // this case corrects improperly defined float table endian in legacy definition files
            if (endian == Settings.Endian.LITTLE) {
                bb.order(ByteOrder.BIG_ENDIAN);
            }
        }
        return bb.getFloat();
    }
}