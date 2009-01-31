/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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

package com.romraider.io.j2534.op20;

import static com.ice.jni.registry.Registry.HKEY_LOCAL_MACHINE;
import static com.ice.jni.registry.Registry.openSubkey;
import com.ice.jni.registry.RegistryException;
import com.ice.jni.registry.RegistryKey;
import static com.ice.jni.registry.RegistryKey.ACCESS_READ;
import com.ice.jni.registry.RegistryValue;
import static com.ice.jni.registry.RegistryValue.REG_DWORD;
import static com.ice.jni.registry.RegistryValue.REG_SZ;
import static com.romraider.util.ByteUtil.asUnsignedInt;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Enumeration;

public final class OpenPort20Manager {
    private static final Logger LOGGER = getLogger(OpenPort20Manager.class);
    private static final String OP20PT32_DLL_LOCATION = "lib/windows/j2534.dll";
    private static boolean SUPPORTED;

    private OpenPort20Manager() {
    }

    public static void init() {
        SUPPORTED = init2();
    }

    private static boolean init2() {
        File dll = findLibrary();
        if (dll == null) return false;
        try {
            copy(dll, new File(OP20PT32_DLL_LOCATION));
            return true;
        } catch (Exception e) {
            LOGGER.warn("Error initialising OP20 dll", e);
            return false;
        }
    }

    public static boolean isSupported() {
        return SUPPORTED;
    }

    private static File findLibrary() {
        try {
            RegistryKey software = openSubkey(HKEY_LOCAL_MACHINE, "SOFTWARE", ACCESS_READ);
            RegistryKey passThruSupport = software.openSubKey("PassThruSupport.04.04");
            Enumeration vendors = passThruSupport.keyElements();
            while (vendors.hasMoreElements()) {
                String vendor = (String) vendors.nextElement();
                RegistryKey vendorKey = passThruSupport.openSubKey(vendor);
                if (!valueExists(vendorKey, "ISO9141", REG_DWORD)) continue;
                if (dwordValue(vendorKey, "ISO9141") != 1) continue;
                if (!valueExists(vendorKey, "FunctionLibrary", REG_SZ)) continue;
                File f = new File(stringValue(vendorKey, "FunctionLibrary"));
                if (f.exists()) return f;
            }
            return null;
        } catch (Exception e) {
            LOGGER.info("Error determining J2534 library location: " + e.getMessage());
            return null;
        }
    }

    private static String stringValue(RegistryKey key, String name) throws RegistryException {
        byte[] bytes = valueOf(key, name);
        return new String(bytes);
    }

    private static int dwordValue(RegistryKey key, String name) throws RegistryException {
        byte[] bytes = valueOf(key, name);
        return asUnsignedInt(bytes);
    }

    private static byte[] valueOf(RegistryKey key, String name) throws RegistryException {
        RegistryValue value = key.getValue(name);
        return value.getByteData();
    }

    public static boolean valueExists(RegistryKey key, String name, int type) {
        try {
            Enumeration elements = key.valueElements();
            while (elements.hasMoreElements()) {
                String element = (String) elements.nextElement();
                if (!element.equals(name)) continue;
                RegistryValue value = key.getValue(element);
                return value.getType() == type;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static void copy(File from, File to) {
        LOGGER.info("Copying: " + from.getAbsolutePath() + " => " + to.getAbsolutePath());
        try {
            FileReader in = new FileReader(from);
            try {
                FileWriter out = new FileWriter(to);
                try {
                    int c;
                    while ((c = in.read()) != -1) out.write(c);
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
