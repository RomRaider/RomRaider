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

package com.romraider.util;

import java.nio.ByteBuffer;
import java.util.List;

public final class ByteUtil {

    private ByteUtil() {
        throw new UnsupportedOperationException();
    }

    public static int asUnsignedInt(byte b) {
        return asUnsignedInt(new byte[]{b});
    }

    public static int asSignedInt(byte[] bytes) {
        int i = 0;
        for (int j = 0; j < bytes.length; j++) {
            if (j == 0) {
                i |= bytes[j];
            }
            else {
                i <<= 8;
                i |= bytes[j] & 0xFF;
            }
        }
        return i;
    }

    public static int asUnsignedInt(byte[] bytes) {
        int i = 0;
        for (int j = 0; j < bytes.length; j++) {
            if (j > 0) {
                i <<= 8;
            }
            i |= bytes[j] & 0xFF;
        }
        return i;
    }

    public static byte asByte(int i) {
        return Integer.valueOf(i).byteValue();
    }

    public static float asFloat(byte[] b, int offset, int length) {
        final ByteBuffer buf = ByteBuffer.wrap(b, offset, length);
        return buf.getFloat();
    }

    public static int asInt(byte b) {
        return Byte.valueOf(b).intValue();
    }

    public static boolean matchOnes(byte b, int mask) {
        return (b & mask) == mask;
    }

    public static boolean matchZeroes(byte b, int mask) {
        return (b & mask) == 0;
    }

    public static void byteListToBytes(List<Byte> buffer, byte[] response) {
        for (int i = 0; i < buffer.size(); i++) {
            response[i] = buffer.get(i);
        }
    }

    public static int indexOfBytes(byte[] bytes, byte[] pattern) {
        int[] failure = computeFailure(pattern);
        int j = 0;
        for (int i = 0; i < bytes.length; i++) {
            while (j > 0 && pattern[j] != bytes[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == bytes[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }
    public static boolean isBitSet(byte b, int position) {
    	return (b & 1 << position) != 0;
    }

    public static boolean[] byteToBoolArr(byte b) {
        boolean boolArr[] = new boolean[8];
        for(int i=0;i<8;i++) boolArr[i] = (b & (byte)(128 / Math.pow(2, i))) != 0;
        return boolArr;
    }
    
    public static byte booleanArrayToBit(boolean[] arr){
      byte val = 0;
      for (boolean b: arr) {
        val <<= 1;
        if (b) val |= 1;
      }
      return val;
    }
    
    public static byte firstOneOfMask(int mask) {
    	byte index = (byte) 0xFF;
    	
    	for(byte i=0; i < 32; i++) {
    		if(((mask >> i) & 1) == 1) {
    			index = i;
    			break;
    		}
    	}
    	
    	return index;
    }
    
    public static byte lengthOfMask(int mask) {
    	byte counter = 0;
    	
    	for(byte i=0; i < 32; i++) {
    		if(((mask >> i) & 1) == 1) {
    			counter++;
    		}
    	}
    	
    	return counter;
    }
    
    public static int bitToMask(int bit) {
    	return  1 << bit;
    }

    private static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];
        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j>0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }
        return failure;
    }
    
	//Java 9 Method
	public static int parseUnsignedInt(String s, int radix) throws NumberFormatException {
	    if (s == null)  {
	        throw new NumberFormatException("null");
	    }
	
	    int len = s.length();
	    if (len > 0) {
	        char firstChar = s.charAt(0);
	        if (firstChar == '-') {
	            throw new
	                NumberFormatException(String.format("Illegal leading minus sign " +
	                                                   "on unsigned string %s.", s));
	        } else {
	            if (len <= 5 ||(radix == 10 && len <= 9) ) {
	                return Integer.parseInt(s, radix);
	            } else {
	                long ell = Long.parseLong(s, radix);
	                if ((ell & 0xffffffff00000000L) == 0) {
	                    return (int) ell;
	                } else {
	                    throw new
	                        NumberFormatException(String.format("String value %s exceeds " + "range of unsigned int.", s));
	                }
	            }
	        }
	    } else {
	    	throw new NumberFormatException(s);
	    }
	}  
}
