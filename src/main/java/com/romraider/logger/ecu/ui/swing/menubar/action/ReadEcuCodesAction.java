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

package com.romraider.logger.ecu.ui.swing.menubar.action;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.event.ActionEvent;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.swing.menubar.action.AbstractAction;

public final class ReadEcuCodesAction extends AbstractAction {
    public ReadEcuCodesAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (showConfirmation() == OK_OPTION) {
            boolean logging = logger.isLogging();
            if (logging) logger.stopLogging();
            readEcu();
//            if (logging) logger.startLogging();
        }
    }

    private int showConfirmation() {
        return showConfirmDialog(logger, "Do you want to read the " + logger.getTarget() + "codes?", "Read " + logger.getTarget(), YES_NO_OPTION, WARNING_MESSAGE);
    }

    private void readEcu() {
        if (doRead()) {
            showMessageDialog(logger, "Read Successful!",
                    "Reset " + logger.getTarget(), INFORMATION_MESSAGE);
        } else {
            showMessageDialog(logger, "Error reading " + logger.getTarget() + " codes.\nCheck the following:\n* Correct COM port selected\n" +
                    "* Cable is connected properly\n* Ignition is ON\n* Logger is stopped", "Read " + logger.getTarget(), ERROR_MESSAGE);
        }
    }

    private boolean doRead() {
        try {
            return logger.readEcuCodes();
        } catch (Exception e) {
            logger.reportError("Error performing " + logger.getTarget() + " read", e);
            return false;
        }
    }
}
