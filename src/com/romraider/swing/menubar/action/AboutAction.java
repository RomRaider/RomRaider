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

package com.romraider.swing.menubar.action;

import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.event.ActionEvent;

import com.romraider.Version;
import com.romraider.logger.ecu.EcuLogger;

public final class AboutAction extends AbstractAction {

    public AboutAction(final EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(final ActionEvent actionEvent) {
        final String message = String.format(
                "%s - ECU Logger%nVersion: %s%nBuild #: %s%n%s%nLogger Def Version: %s",
                Version.PRODUCT_NAME,
                Version.VERSION,
                Version.BUILDNUMBER,
                Version.SUPPORT_URL,
                logger.getDefVersion());
        final String title = "About " + Version.PRODUCT_NAME;
        showMessageDialog(logger, message, title,
                INFORMATION_MESSAGE, Version.ABOUT_ICON);
    }
}
