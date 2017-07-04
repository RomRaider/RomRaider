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

package com.romraider.maps.checksum;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.util.Map;

/**
 * Instantiate a ChecksumManager class.
 */
public final class ChecksumFactory {
    private static final String TYPE = "type";
    private static final String MISSING =
            "Error in checksum definition, 'type' attribute missing.";
    private static final String NO_CLASS =
            "Error loading Checksum Manager of type: %s";
    private ChecksumFactory() {
    }

    /**
     * Instantiate the specific ChecksumManager class based on
     * the "type" requested.
     * @param attrs    - the configuration for the checksum manager, must
     *                   contain a "type" K,V pair
     * @return    a configured instance of the requested ChecksumManager
     * @throws     ClassNotFoundException if the class    based on "type"
     *             does not exist
     */
    public static ChecksumManager getManager(
            Map<String, String> attrs) {
           
        ChecksumManager cm = null;
        Class<?> cls;
        final String type = attrs.get(TYPE);
        try {
            cls = Class.forName(
                    ChecksumFactory.class.getPackage().getName() + 
                    ".Checksum" + type.toUpperCase());
            cm = (ChecksumManager) cls.newInstance();
            cm.configure(attrs);
        } catch (Exception e) {
            String message = null;
            if (type == null) {
                message = MISSING;
            }
            else {
                message = String.format(NO_CLASS, type.toUpperCase());
            }
            showMessageDialog(null,
                    message,
                    "Error Loading Checksum Manager", ERROR_MESSAGE);
        }
        return cm;
    }
}
