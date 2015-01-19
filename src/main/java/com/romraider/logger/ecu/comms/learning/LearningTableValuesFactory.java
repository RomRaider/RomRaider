/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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

package com.romraider.logger.ecu.comms.learning;

import com.romraider.logger.ecu.exception.UnsupportedProtocolException;

public final class LearningTableValuesFactory {
    private LearningTableValuesFactory() {
    }

    public static LearningTableValues getManager(String protocol) {
        final String className = getClassName(protocol);
        try {
            final Class<?> cls = Class.forName(className);
            return (LearningTableValues) cls.newInstance();
        } catch (Exception e) {
            throw new UnsupportedProtocolException(String.format(
                    "LearningTableValues class for %s not found: %s",
                    protocol, className));
        }
    }

    private static String getClassName(String protocol) {
        final Package pkg = LearningTableValuesFactory.class.getPackage();
        return pkg.getName() + "." + protocol.toUpperCase() + "LearningTableValues";
    }
}
