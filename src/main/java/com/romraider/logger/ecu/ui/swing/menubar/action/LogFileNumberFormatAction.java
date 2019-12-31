/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2019 RomRaider.com
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

import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.swing.menubar.action.AbstractAction;

public final class LogFileNumberFormatAction extends AbstractAction {
    private static final String EN_US = "en_US";
    private static final String SYSTEM_NUMFORMAT = "system";

    public LogFileNumberFormatAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            if ((Boolean) getValue(SELECTED_KEY)) {
                logger.getSettings().setLocale(EN_US);
            }
            else {
                logger.getSettings().setLocale(SYSTEM_NUMFORMAT);
            }
            showMessageDialog(logger, MessageFormat.format(
                    rb.getString("LFNFAMSG"), logger.getSettings().getLocale()),
                    rb.getString("LFNFATITLE"), INFORMATION_MESSAGE);
        } catch (Exception e) {
            logger.reportError(e);
        }
    }
}
