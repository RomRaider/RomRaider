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

import static com.romraider.logger.ecu.ui.swing.menubar.util.FileHelper.getFile;
import static com.romraider.logger.ecu.ui.swing.menubar.util.FileHelper.getProfileFileChooser;
import static com.romraider.logger.ecu.ui.swing.menubar.util.FileHelper.saveProfileToFile;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;

import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.JFileChooser;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.swing.menubar.action.AbstractAction;
import com.romraider.util.SettingsManager;

public final class SaveProfileAsAction extends AbstractAction {

    public SaveProfileAsAction(EcuLogger logger) {
        super(logger);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        try {
            saveProfileAs();
        } catch (Exception e) {
            logger.reportError(e);
        }
    }

    private void saveProfileAs() throws Exception {
        logger.getSettings();
        File lastProfileFile = getFile(SettingsManager.getSettings().getLoggerProfileFilePath());
        JFileChooser fc = getProfileFileChooser(lastProfileFile);
        if (fc.showSaveDialog(logger) == APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            if (!selectedFile.exists() ||
                    showConfirmDialog(logger,
                            MessageFormat.format(
                                    rb.getString("SPAACONFIRM"),
                                    selectedFile.getName())) == OK_OPTION) {
                String profileFilePath = saveProfileToFile(logger.getCurrentProfile(), selectedFile);
                logger.getSettings().setLoggerProfileFilePath(profileFilePath);
                logger.reportMessageInTitleBar(MessageFormat.format(
                        rb.getString("SPATITLE"), profileFilePath));
                logger.reportMessage(MessageFormat.format(
                        rb.getString("SPAMSG"), profileFilePath));
            }
        }
    }
}
