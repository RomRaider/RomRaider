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

package com.romraider.io.j2534.api;

import java.lang.reflect.Constructor;

import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.connection.ConnectionProperties;

public final class J2534TransportFactory {
    private J2534TransportFactory() {
    }

    public static ConnectionManager getManager(
            String protocolName,
            ConnectionProperties connectionProperties,
            String library) {

        try {
            final Class<?> cls = Class.forName(
                    J2534TransportFactory.class.getPackage().getName() + 
                    ".J2534Connection" + protocolName.toUpperCase());

            final Constructor<?> cnstrtr = cls.getConstructor(
                    ConnectionProperties.class, String.class);

            return (ConnectionManager) cnstrtr.newInstance(
                            connectionProperties, library);
        }
        catch (Exception e) {
            throw new J2534Exception("J2534 initialization: " +
                    e.getCause().getMessage(), e);
        }
    }
}
