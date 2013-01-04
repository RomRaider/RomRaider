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

package com.romraider.logger.external.phidget.interfacekit.plugin;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.phidget.interfacekit.io.IntfKitManager;
import com.romraider.swing.menubar.action.AbstractAction;

/**
 * IntfKitPluginMenuAction is used to populate the Phidgets Plugins menu 
 * of the Logger. It will report the device type and serial number of each
 * PhidgetInterfaceKit found connected to the system.
 * This is informational only. 
 */
public final class IntfKitPluginMenuAction extends AbstractAction {

    /**
     * Initialise the Phidgets Plugins menu item.
     * @param logger - the parent frame to bind the dialog message to
     */
    public IntfKitPluginMenuAction(final EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        JOptionPane.showMessageDialog(
                logger,
                getKits(),
                "Interface Kits found",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Build a list of device types with serial numbers attached to the system.
     * @return    a formated string to be displayed in the message box
     * @see IntfKitManager
     */
    private String getKits() {
        final Integer[] kits = IntfKitManager.findIntfkits();
        final StringBuilder sb = new StringBuilder();
        if (kits.length < 1) {
            sb.append("No Interface Kits attached");
        }
        else {
            IntfKitManager.loadIk();
            for (int serial : kits) {
                final String result = IntfKitManager.getIkName(serial);
                if (result != null) {
                    sb.append(result);
                }
                else {
                    sb.append("Unable to read properties of serial # " + serial +
                            ", it may be in use");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
