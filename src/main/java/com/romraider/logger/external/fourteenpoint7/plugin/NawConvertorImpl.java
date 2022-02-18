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

package com.romraider.logger.external.fourteenpoint7.plugin;

import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ByteUtil.asFloat;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;


/*
* PC sends "7" to NAW_7S to request 1 packet.
*
* Each packet consists of:
*
* Byte 0-3: IEEE formatted 32 bit floating number representing the free air claibrated pump current
* Byte 4-7: IEEE formatted 32 bit floating number representing the uncalibrated Pump_Current
* Byte 8: 8 bit number representing sensor temperature
*
* Once you get the pump current you have to convert that to lambda:
* if (Pump_Current>128) //lean
* {
*
* Temp_Double=-0.00000359*Pump_Current*Pump_Current+0.003894*Pump_Current-0.4398; //O2 conc
* if (Temp_Double>0.209)
*    {
*       Temp_Double=0.209;
*    }
*    Lambda = (Temp_Double/3+1)/(1-4.76*Temp_Double);
* }
* else //rich
* {
*    Lambda=0.00003453*Pump_Current*Pump_Current-0.00159*Pump_Current+0.6368;
* }
*
*
*
* The conversion of Byte 8 to temperature in % is:
* Temperature = 1500 / (Byte) // range 12 - 18 or (0x0C - 0x12)
*
* http://14point7.com/
*/
public final class NawConvertorImpl implements NawConvertor {
    private static final Logger LOGGER = getLogger(NawConvertorImpl.class);

    @Override
    public double convert(byte[] bytes) {
        int temp = 0;
        double lambda = 0.0;
        double unCalIp = 0.0;
//        float facpc = asFloat(bytes, 0, 4);
        float upc = asFloat(bytes, 4, 4);
        unCalIp = upc;    // uncalibrated pump current cast to double
        if (unCalIp > 128) { // Lean
            double o2conc = (-0.00000359 * unCalIp * unCalIp) + (0.003894 * unCalIp) - 0.4398;
            if (o2conc > 0.210) { // O2 concentration 20.9% this is equal to 207 lambda
                o2conc = 0.210;
            }
            lambda = ((o2conc / 3) + 1) / (1 - (4.76 * o2conc));
            if (lambda >= 1.43) {
                lambda = 1.43;
            }
        } else { // Rich
            lambda = ((0.00003453 * unCalIp * unCalIp) - (0.00159 * unCalIp) + 0.6368);
        }
        temp = (0x000000FF & (bytes[8])); // convert signed byte to unsigned byte
        if (temp < 11) { // check sensor temperature range for valid reading
            lambda = 99.99;    // sensor too hot
        } else if (temp > 19) {
            lambda = -99.99;    // sensor too cold
        }
        temp = 1500 / temp;  // temperature in percent, 100% is best
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Converting NAW_7S response: " + asHex(bytes) + " --> Ip:" + unCalIp + " --> temp:" + temp + "% --> lambda:" + lambda);
        return lambda;
    }
}
