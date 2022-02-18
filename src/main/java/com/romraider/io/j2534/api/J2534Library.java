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

/**
 * Each class instance holds the Vendor and Library details for a J2534
 * installed device.  On Windows these are discovered on the local computer
 * from keys and value settings in the Windows registry.
 * On Linux, from a static list.
 * @see J2534LibraryLocator
 */
public class J2534Library {
    private String vendor;
    private String library;

    /**
     * Create a new instance of a J2534 library detail.
     * @param vendor    - J2534 vendor string
     * @param library    - J2534 library path
     */
    public J2534Library(String vendor, String library) {
        this.vendor = vendor;
        this.library = library;
    }

    /**
     * Get the vendor of this library detail instance.
     * @return the vendor
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Get the fully qualified library path of this library detail instance.
     * @return the library
     */
    public String getLibrary() {
        return library;
    }
}
