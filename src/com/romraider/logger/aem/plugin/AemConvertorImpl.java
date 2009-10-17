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


package com.romraider.logger.aem.plugin;

import static com.romraider.util.HexUtil.asHex;
import org.apache.log4j.Logger;
import static java.lang.Double.parseDouble;
import java.nio.charset.Charset;

public final class AemConvertorImpl implements AemConvertor {
    private static final Logger LOGGER = Logger.getLogger(AemConvertorImpl.class);
    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    public double convert(byte[] bytes) {
        String value = new String(bytes, CHARSET_UTF8);
        double result = convert(value);
        LOGGER.trace("Converting AEM response: " + asHex(bytes) + " --> \"" + value + "\" --> " + result);
        return result;
    }

    private double convert(String value) {
        try {
            return parseDouble(value);
        } catch (NumberFormatException e) {
            LOGGER.error("Error converting AEM response to double: \"" + value + "\"", e);
            return -1.0;
        }
    }
}
