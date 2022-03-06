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

package com.romraider.io.j2534.api;

import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static com.romraider.util.ParamChecker.isNullOrEmpty;
import static com.romraider.util.Platform.WINDOWS;
import static com.romraider.util.Platform.isPlatform;
import static com.sun.jna.platform.win32.WinError.ERROR_FILE_NOT_FOUND;
import static com.sun.jna.platform.win32.WinError.ERROR_SUCCESS;
import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.romraider.util.ParamChecker;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.WinBase.FILETIME;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.platform.win32.WinReg.HKEYByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * Discover all the J2534 device installations on the local computer from
 * keys and value settings in the Windows registry.
 * On Linux, from a static list.
 * Return a List of J2534Library instances.
 * @see J2534Library
 */
public class J2534LibraryLocator {
    private static final Logger LOGGER = Logger.getLogger(J2534LibraryLocator.class);
    private static final String FUNCTIONLIBRARY = "FunctionLibrary";
    private static final int KEY_READ = 0x20019;
    private static final int ERROR_NO_MORE_ITEMS = 0x103;
    private static final Set<J2534Library> libraries = new HashSet<J2534Library>();
    private static List<String> WIN_LIBRARIES = new ArrayList<String>();
    private static List<String> LINUX_LIBRARIES = new ArrayList<String>();
    private static String j2534Protocol = "";

    public static Set<J2534Library> getLibraries(final String protocol) {
        checkNotNullOrEmpty(protocol, "protocol");
        if (WIN_LIBRARIES.isEmpty() && LINUX_LIBRARIES.isEmpty()) {
            loadProperties();
        }

        if (isNullOrEmpty(j2534Protocol)) {
            j2534Protocol = protocol;
        }
        if (!j2534Protocol.equalsIgnoreCase(protocol)) {
            libraries.clear();
        }
        if (libraries.isEmpty()) {
            File libFile = null;
            if (isPlatform(WINDOWS)) {
                try {
                    listLibraries(j2534Protocol);
                }
                catch (Exception e) {
                    LOGGER.info("No J2534 Registry libraries found that support protocol " +
                            j2534Protocol);
                }

                if (!WIN_LIBRARIES.isEmpty()) {
                    for (String lib : WIN_LIBRARIES) {
                        if (lib.contains("\\") || lib.contains("/")) {
                            libFile = new File(lib);
                        }
                        else {
                            libFile = new File(System.getProperty("java.library.path"), lib);
                        }
                        if (libFile.exists()) {
                            libraries.add(new J2534Library(
                                    "Windows J2534 OP2 WinUSB",
                                    libFile.getAbsolutePath()));
                        }
                        else {
                            LOGGER.info("Windows Openport 2.0 library not found at: " +
                                    libFile.getAbsolutePath());
                        }
                    }
                }
            }
            else {
                if (!LINUX_LIBRARIES.isEmpty()) {
                    for (String lib : LINUX_LIBRARIES) {
                        if (lib.contains("/")) {
                            libFile = new File(lib);
                        }
                        else {
                            libFile = new File(System.getProperty("java.library.path"), lib);
                        }
                        if (libFile.exists()) {
                            libraries.add(new J2534Library(
                                    "Linux J2534 OP2",
                                    libFile.getAbsolutePath()));
                        }
                        else {
                            LOGGER.info("Linux Openport 2.0 library not found at: " +
                                    libFile.getAbsolutePath());
                        }
                    }
                }
            }
        }
        return libraries;
    }

    private static void listLibraries(final String protocol) throws Exception {
        final Advapi32 advapi32 = Advapi32.INSTANCE;
        final HKEY hklm = HKEY_LOCAL_MACHINE;
        final String passThru = "SOFTWARE\\PassThruSupport.04.04";
        final HKEYByReference passThruHandle = getHandle(advapi32, hklm, passThru);

        if(passThruHandle != null) {
	        List<String> vendors = getKeys(advapi32, passThruHandle.getValue());
	        for (String vendor : vendors) {
	            final HKEYByReference vendorKey =
	                    getHandle(advapi32, passThruHandle.getValue(), vendor);
	            final int supported = getDWord(advapi32, vendorKey.getValue(), protocol);
	            if (supported == 0 ) continue;
	            final String library = getSZ(advapi32, vendorKey.getValue(), FUNCTIONLIBRARY);
	            if (LOGGER.isDebugEnabled())
                    LOGGER.debug(String.format("Found J2534 Vendor:%s | Library:%s",
	                    vendor, library));
	            if (ParamChecker.isNullOrEmpty(library)) continue;
	            libraries.add(new J2534Library(vendor, library));
	            advapi32.RegCloseKey(vendorKey.getValue());
	        }
	        advapi32.RegCloseKey(passThruHandle.getValue());
        }
    }

    private static HKEYByReference getHandle(
            final Advapi32 advapi32,
            final HKEY hKey, String lpSubKey) {

        HKEYByReference phkResult = new HKEYByReference();
        int ret = advapi32.RegOpenKeyEx(
            hKey,
            lpSubKey,
            0,
            KEY_READ,
            phkResult);

        if(ret != ERROR_SUCCESS) {
        	return null;
        }
        return phkResult;
    }

    private static int reverse(final byte[] bytes, final int size) {
        ByteBuffer b = ByteBuffer.wrap(bytes, 0, size);
        return b.order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private static void handleError(final String operation,
            final int status)
            throws Exception {

        String errString = String.format("%s error [%d]%n",
                operation, status);
        throw new Exception(errString);
    }

    private static List<String> getKeys(
            final Advapi32 advapi32,
            final HKEY hkey)
            throws Exception {

        int dwIndex = 0;
        List<String> vendors = new ArrayList<String>();
        int ret = 0;
        do {
            char[] lpName = new char[255];
            IntByReference lpcName = new IntByReference(-1);
            IntByReference lpReserved = null;
            char[] lpClass = new char[255];
            IntByReference lpcClass = new IntByReference(-1);
            FILETIME lpftLastWriteTime = new FILETIME();
            ret = advapi32.RegEnumKeyEx(
                    hkey,
                    dwIndex,
                    lpName,
                    lpcName,
                    lpReserved,
                    lpClass,
                    lpcClass,
                    lpftLastWriteTime);

            switch (ret) {
                case ERROR_SUCCESS:
                    dwIndex++;
                    vendors.add(Native.toString(lpName));
                    break;
                case ERROR_NO_MORE_ITEMS:
                    break;
                default:
                    handleError("RegEnumKeyEx", ret);
            }
        } while(ret == ERROR_SUCCESS);
        return vendors;
    }

    private static int getDWord(
            final Advapi32 advapi32,
            final HKEY hkey, final String valueName)
            throws Exception {

        IntByReference lpType = new IntByReference(-1);
        byte[] lpData = new byte[16];
        IntByReference lpcbData = new IntByReference(-1);
        int ret = advapi32.RegQueryValueEx(
                hkey,
                valueName,
                0,
                lpType,
                lpData,
                lpcbData);

        int dword = -1;
        switch (ret) {
            case ERROR_SUCCESS:
                dword = reverse(lpData, lpcbData.getValue());
                break;
            case ERROR_FILE_NOT_FOUND:
                dword = 0;
                break;
            default:
                String errString = String.format("DWORD_RegQueryValueEx(%s)",
                        valueName);
                handleError(errString, ret);
        }
        return dword;
    }

    private static String getSZ(
            final Advapi32 advapi32,
            final HKEY hkey, final String valueName)
            throws Exception {

        final IntByReference lpType = new IntByReference(-1);
        final char[] lpData = new char[1024];
        final IntByReference lpcbData = new IntByReference(-1);
        final int ret = advapi32.RegQueryValueEx(
                hkey,
                valueName,
                0,
                lpType,
                lpData,
                lpcbData);

        String szValue = null;
        switch (ret) {
            case ERROR_SUCCESS:
                szValue = Native.toString(lpData);
                break;
            case ERROR_FILE_NOT_FOUND:
                break;
            default:
                final String errString = String.format("SZ_RegQueryValueEx(%s)",
                        valueName);
                handleError(errString, ret);
        }
        return szValue;
    }

    /**
     * Load J2534 library names from a user customized properties file.
     * The file will populate the library lists if it is present.
     * Library names in the file should be separated by the ; character
     * @exception    FileNotFoundException if the directory or file is not present
     * @exception    IOException if there's some kind of IO error
     */
    private static void loadProperties() {
        final Properties libraries = new Properties();
        FileInputStream propFile;
        try {
            propFile = new FileInputStream("./customize/j2534Libraries.properties");
            libraries.load(propFile);
            final String win_names = libraries.getProperty("windows");
            if (!isNullOrEmpty(win_names)) {
                final String[] names = win_names.split(";", 0);
                for (String name : names) {
                    if (isNullOrEmpty(name)) continue;
                    WIN_LIBRARIES.add(name.trim());
                }
            }
            final String linux_names = libraries.getProperty("linux");
            if (!isNullOrEmpty(linux_names)) {
                final String[] names = linux_names.split(";", 0);
                for (String name : names) {
                    if (isNullOrEmpty(name)) continue;
                    LINUX_LIBRARIES.add(name.trim());
                }
            }
            propFile.close();
            LOGGER.info("J2534 Library names loaded from file: ./customize/j2534Libraries.properties");
        } catch (FileNotFoundException e) {
            LOGGER.error("j2534Libraries.properties file: " + e.getLocalizedMessage());
        } catch (IOException e) {
            LOGGER.error("j2534Libraries.properties IOException: " + e.getLocalizedMessage());
        }
    }
}
