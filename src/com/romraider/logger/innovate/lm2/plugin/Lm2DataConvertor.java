/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
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

package com.romraider.logger.innovate.lm2.plugin;

import com.romraider.logger.innovate.generic.plugin.DataConvertor;
import static com.romraider.util.ByteUtil.matchOnes;
import static com.romraider.util.ByteUtil.matchZeroes;
import static com.romraider.util.HexUtil.asBytes;
import static com.romraider.util.HexUtil.asHex;
import org.apache.log4j.Logger;

//TODO: Remove dupe with Lc1DataConvertor
public final class Lm2DataConvertor implements DataConvertor {
    private static final Logger LOGGER = Logger.getLogger(Lm2DataConvertor.class);
    private static final double MAX_AFR = 20.33;

    public double convert(byte[] bytes) {
        LOGGER.trace("Converting LM-2 bytes: " + asHex(bytes));
        if (isLm2(bytes) && isHeaderValid(bytes)) {
            if (isError(bytes)) {
                int error = -1 * getLambda(bytes);
                LOGGER.error("LM-2 error: " + asHex(bytes) + " --> " + error);
                return error;
            }
            if (isOk(bytes)) {
                double afr = getAfr(bytes);
                LOGGER.trace("LM-2 AFR: " + afr);
                return afr > MAX_AFR ? MAX_AFR : afr;
            }
            // out of range value seen on overrun...
            LOGGER.trace("LM-2 response out of range (overrun?): " + asHex(bytes));
            return MAX_AFR;
        }
        LOGGER.error("LM-2 unrecognized response: " + asHex(bytes));
        return 0;
    }

    private double getAfr(byte[] bytes) {
        return (getLambda(bytes) + 500) * getAf(bytes) / 10000.0;
    }

    private int getAf(byte[] bytes) {
        return ((bytes[2] & 1) << 7) | bytes[3];
    }

    // 1x0xxx0x
    private boolean isLm2(byte[] bytes) {
        return bytes.length >= 6 && matchOnes(bytes[2], 128) && matchZeroes(bytes[2], 34);
    }

    // 1x11xx1x 1xxxxxxx
    private boolean isHeaderValid(byte[] bytes) {
        return matchOnes(bytes[0], 178) && matchOnes(bytes[1], 128);
    }

    // 1x00000x
    private boolean isOk(byte[] bytes) {
        return matchOnes(bytes[2], 128) && matchZeroes(bytes[2], 62);
    }

    // 1x01100x
    private boolean isError(byte[] bytes) {
        return matchOnes(bytes[2], 152) && matchZeroes(bytes[2], 38);
    }

    // 00xxxxxx 0xxxxxxx
    private int getLambda(byte[] bytes) {
        return (bytes[4] << 7) | bytes[5];
    }

    public static void main(String[] args) {
        byte[] bytes = asBytes("0xB2808113036F1E650124007000470039003A");
        DataConvertor convertor = new Lm2DataConvertor();
        double result = convertor.convert(bytes);
        System.out.println("result = " + result);
    }
}