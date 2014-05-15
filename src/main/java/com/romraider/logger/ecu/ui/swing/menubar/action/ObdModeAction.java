/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2014 RomRaider.com
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

import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;

import java.awt.event.ActionEvent;

import com.romraider.Settings;
import com.romraider.logger.ecu.EcuLogger;
import com.romraider.swing.menubar.action.AbstractAction;
import com.romraider.util.SettingsManager;

public final class ObdModeAction extends AbstractAction {

    public ObdModeAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        final Settings settings = SettingsManager.getSettings();
            try {
                logger.stopLogging();
                if ((Boolean) getValue(SELECTED_KEY) &&
                        !settings.isObdProtocol() && 
                        showConfirmation() == OK_OPTION) {
                    settings.setTransportProtocol("ISO15765");
                    settings.setLoggerProtocol("OBD");
                }
                else {
                    settings.setLoggerProtocol("SSM");
                }
                logger.loadLoggerParams();
                logger.startLogging();
            }
            catch (Exception e) {
                logger.reportError(e);
            }
    }

    private final int showConfirmation() {
        return showConfirmDialog(logger,
                "Confirm switching to the OBD communications protocol.\n" +
                "This mode is only supported for CAN enabled ECUs using " +
                "a J2534 compatible cable.",
                "OBD Comminucations Mode",
                YES_NO_OPTION,
                QUESTION_MESSAGE);
    }
}
