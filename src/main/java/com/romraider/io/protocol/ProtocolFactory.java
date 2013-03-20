/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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

package com.romraider.io.protocol;

import com.romraider.logger.ecu.comms.io.protocol.LoggerProtocol;
import com.romraider.logger.ecu.exception.UnsupportedProtocolException;

public final class ProtocolFactory {
    private ProtocolFactory() {
    }

    public static LoggerProtocol getProtocol(String protocol, String transport) {
        String className = getClassName(protocol, transport);
        try {
            Class<?> cls = Class.forName(className);
            return (LoggerProtocol) cls.newInstance();
        } catch (Exception e) {
            throw new UnsupportedProtocolException("Protocol class for " +
                    protocol + "/" + transport + " not found: " + className);
        }
    }

    private static String getClassName(String protocol, String transport) {
        Package pkg = ProtocolFactory.class.getPackage();
        return pkg.getName() + "." + protocol.toLowerCase() + "." +
        transport.toLowerCase() + "." + protocol.toUpperCase() + "LoggerProtocol";
    }
}
