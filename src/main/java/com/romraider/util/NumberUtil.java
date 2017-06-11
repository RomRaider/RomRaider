/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2017 RomRaider.com
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * This class provides Number conversion utilities based on the default Locale. 
 */
public final class NumberUtil {
    private static final NumberFormat NUM_FORMATTER = NumberFormat.getInstance(Locale.getDefault());
    private static final String NAN = "NaN";
    static {
        ((DecimalFormat) NUM_FORMATTER).applyPattern("0.0#################");
    }

    private NumberUtil() {
    }
    
    /**
     * Returns the separator based on the current locale (. or ,)
     * @return  The separation character
     */
    public static char getSeperator(){
    	return ((DecimalFormat) NUM_FORMATTER).getDecimalFormatSymbols().getDecimalSeparator();
    }
    
    /**
     * Returns the value of the specified number in the default locale as a double.
     * @param   str - string to be converted.
     * @return  the numeric value represented by this object after conversion 
     * to type double.
     * @exception ParseException is thrown when parse errors are encountered.
     */
    public static double doubleValue(String str) throws ParseException {
       	return NUM_FORMATTER.parse(str).doubleValue();
    }

    /**
     * Format a double using the current Locale
     * @param  value to format
     * @return the number as a String
     */
    public static String stringValue(double value) {
        return Double.isNaN(value) ? NAN : NUM_FORMATTER.format(value);
    }
}
