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

import com.romraider.logger.ecu.EcuLogger;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import java.awt.event.ActionEvent;

public final class ResetEcuAction extends AbstractAction {
    public ResetEcuAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (showConfirmation() == OK_OPTION) {
            boolean logging = logger.isLogging();
            if (logging) logger.stopLogging();
            resetEcu();
            if (logging) logger.startLogging();
        }
    }

    private int showConfirmation() {
        return showConfirmDialog(logger, "Do you want to reset the ECU?", "Reset ECU", YES_NO_OPTION, WARNING_MESSAGE);
    }

    private void resetEcu() {
        if (doReset()) {
            showMessageDialog(logger, "Reset Successful!\nTurn your ignition OFF and then\nback ON to complete the process.",
                    "Reset ECU", INFORMATION_MESSAGE);
        } else {
            showMessageDialog(logger, "Error performing ECU reset.\nCheck the following:\n* Correct COM port selected\n" +
                    "* Cable is connected properly\n* Ignition is ON\n* Logger is stopped", "Reset ECU", ERROR_MESSAGE);
        }
    }

    private boolean doReset() {
        try {
            return logger.resetEcu();
        } catch (Exception e) {
            logger.reportError("Error performing ECU reset", e);
            return false;
        }
    }
}
