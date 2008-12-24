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

package com.romraider.logger.ecu.ui.swing.menubar.action;

import static com.romraider.Version.ABOUT_ICON;
import static com.romraider.Version.BUILDNUMBER;
import static com.romraider.Version.PRODUCT_NAME;
import static com.romraider.Version.SUPPORT_URL;
import static com.romraider.Version.VERSION;
import com.romraider.logger.ecu.EcuLogger;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import java.awt.event.ActionEvent;

public final class AboutAction extends AbstractAction {

    public AboutAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        String message = PRODUCT_NAME + " - ECU Logger\n"
                + "Version: " + VERSION + "\n"
                + "Build #: " + BUILDNUMBER + "\n"
                + SUPPORT_URL;
        String title = "About " + PRODUCT_NAME;
        showMessageDialog(logger, message, title, INFORMATION_MESSAGE, ABOUT_ICON);
    }
}
