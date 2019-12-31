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

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.swing.menubar.action.AbstractAction;

public final class ReadEcuCodesAction extends AbstractAction {
    public ReadEcuCodesAction(EcuLogger logger) {
        super(logger);
    }

    public final void actionPerformed(ActionEvent actionEvent) {
        if (logger.getDtcodesEmpty()) {
            showMessageDialog(logger,
                    rb.getString("RECADEFERROR"),
                    rb.getString("RECADEFERRORTITLE"), ERROR_MESSAGE);
        }
        else if (!logger.isEcuInit()) {
            showMessageDialog(logger,MessageFormat.format(
                            rb.getString("RECANOINIT"), logger.getTarget()),
                    rb.getString("RECANOINITTITLE"), ERROR_MESSAGE);
        }
        else {
            final boolean logging = logger.isLogging();
            if (showConfirmation() == OK_OPTION) {
                if (logging) logger.stopLogging();
                readEcu();
                if (logging) logger.startLogging();
            }
        }
    }

    private final int showConfirmation() {
        return showConfirmDialog(
                logger, 
                MessageFormat.format(
                        rb.getString("RECACONFIRM"), logger.getTarget()), 
                MessageFormat.format(
                        rb.getString("RECACONFIRMTITLE"), logger.getTarget()), 
                YES_NO_OPTION, WARNING_MESSAGE);
    }

    private final void readEcu() {
        final int result = doRead();
        if (result == -1) {
            showMessageDialog(
                    logger, 
                    rb.getString("RECANOCODES"),
                    rb.getString("RECANOCODESTILTE"),
                    INFORMATION_MESSAGE);
        }
        else if (result == 0) {
            showMessageDialog(
                    logger, 
                    MessageFormat.format(
                            rb.getString("RECAREADERROR"), logger.getTarget()), 
                    MessageFormat.format(
                            rb.getString("RECAREADERRORTITLE"), logger.getTarget()),
                    ERROR_MESSAGE);
        }
    }

    private int doRead() {
        try {
            return logger.readEcuCodes();
        } catch (Exception e) {
            logger.reportError(
                    MessageFormat.format(
                            rb.getString("RECAREPORTERROR"),
                            logger.getTarget()),
                    e);
            return 0;
        }
    }
}
