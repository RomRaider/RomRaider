/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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
import com.romraider.logger.ecu.ui.swing.tools.DS2ResetPanel;
import com.romraider.swing.menubar.action.AbstractAction;
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
            if (logger.getSettings().getLoggerProtocol().equals("DS2")) {
                selectResetItems();
            }
            else {
                resetEcu(0x40);
            }
            if (logging) logger.startLogging();
        }
    }

    private int showConfirmation() {
        return showConfirmDialog(logger, "Do you want to reset the " + logger.getTarget() + "?", "Reset " + logger.getTarget(), YES_NO_OPTION, WARNING_MESSAGE);
    }

    private void resetEcu(int resetCode) {
        if (doReset(resetCode)) {
            showMessageDialog(logger, "Reset Successful!\nTurn your ignition OFF and then\nback ON to complete the process.",
                    "Reset " + logger.getTarget(), INFORMATION_MESSAGE);
        } else {
            showMessageDialog(logger, "Error performing " + logger.getTarget() + " reset.\nCheck the following:\n* Correct COM port selected\n" +
                    "* Cable is connected properly\n* Ignition is ON\n* Logger is stopped", "Reset " + logger.getTarget(), ERROR_MESSAGE);
        }
    }

    private boolean doReset(int resetCode) {
        try {
            return logger.resetEcu(resetCode);
        } catch (Exception e) {
            logger.reportError("Error performing " + logger.getTarget() + " reset", e);
            return false;
        }
    }

    private void selectResetItems() {
        final DS2ResetPanel resetPanel = new DS2ResetPanel(logger);
        resetPanel.showResetPanel();
        final int result = resetPanel.getResults();
        if (result > 0) {
            resetEcu(result);
        }
    }

}
