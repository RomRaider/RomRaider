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

import static com.sun.jna.platform.win32.WinError.ERROR_SUCCESS;
import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;
import static org.apache.log4j.Logger.getLogger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.WinBase.FILETIME;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.platform.win32.WinReg.HKEYByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * Discover all the J2534 device installations on the local computer from
 * keys and value settings in the Windows registry.  Return a List of
 * J2534Library instances.
 * @see J2534Library
 */
public class J2534DllLocator {
	private static final Logger LOGGER = getLogger(J2534DllLocator.class);
	private static final String FUNCTIONLIBRARY = "FunctionLibrary";
	private static final int KEY_READ = 0x20019;
    private static final int ERROR_NO_MORE_ITEMS = 0x103;
    private static  Advapi32 advapi32 = Advapi32.INSTANCE;

    public static Set<J2534Library> listLibraries(String protocol) throws Exception {
    	Set<J2534Library> libraries = new HashSet<J2534Library>();
        HKEY hklm = HKEY_LOCAL_MACHINE;
        String passThru = "SOFTWARE\\PassThruSupport.04.04";
        HKEYByReference passThruHandle = getHandle(hklm, passThru);
 
        List<String> vendors = getKeys(passThruHandle.getValue());
        for (String vendor : vendors) {
            HKEYByReference vendorKey =
            		getHandle(passThruHandle.getValue(), vendor);
            int supported = getDWord(vendorKey.getValue(), protocol);
			if (supported == 0 ) continue;
			String library = getSZ(vendorKey.getValue(), FUNCTIONLIBRARY);
			LOGGER.debug(String.format("Found J2534 Vendor:%s | Library:%s",
					vendor, library));
			libraries.add(new J2534Library(vendor, library));
			advapi32.RegCloseKey(vendorKey.getValue());
        }
        advapi32.RegCloseKey(passThruHandle.getValue());
        return libraries;
    }
 
    private static HKEYByReference getHandle(HKEY hKey, String lpSubKey)
    		throws Exception {

    	HKEYByReference phkResult = new HKEYByReference();
	    int ret = advapi32.RegOpenKeyEx(
	        hKey,
		    lpSubKey,
		    0,
		    KEY_READ,
		    phkResult);
	
	    if(ret != ERROR_SUCCESS) {
	    	handleError("RegOpenKeyEx", ret);
	    }
	    return phkResult;
    }

    private static int reverse(byte[] bytes, int size) {
    	ByteBuffer b = ByteBuffer.wrap(bytes, 0, size);
    	return b.order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
 
	private static void handleError(String operation, int status)
			throws Exception {
		
    	String errString = String.format("%s error [%d]%n",
    			operation, status);
    	throw new Exception(errString);
    }

	private static List<String> getKeys(HKEY hkey)
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

	private static int getDWord(HKEY hkey, String valueName)
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
	    if(ret != ERROR_SUCCESS) {
	    	String errString = String.format("DWORD_RegQueryValueEx(%s)",
	    			valueName);
	    	handleError(errString, ret);
	    }
	    int dword = reverse(lpData, lpcbData.getValue());
		return dword;
	}

	private static String getSZ(HKEY hkey, String valueName)
			throws Exception {
		
        IntByReference lpType = new IntByReference(-1);
        char[] lpData = new char[1024];
        IntByReference lpcbData = new IntByReference(-1);
        int ret = advapi32.RegQueryValueEx(
        		hkey,
        		valueName,
                0,
                lpType,
                lpData,
                lpcbData);
	    if(ret != ERROR_SUCCESS) {
	    	String errString = String.format("SZ_RegQueryValueEx(%s)",
	    			valueName);
	    	handleError(errString, ret);
	    }
	    return Native.toString(lpData);
	}
}
