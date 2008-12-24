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
import static com.romraider.logger.ecu.ui.swing.menubar.util.FileHelper.getFile;
import static com.romraider.logger.ecu.ui.swing.menubar.util.FileHelper.getProfileFileChooser;
import static com.romraider.logger.ecu.ui.swing.menubar.util.FileHelper.saveProfileToFile;
import javax.swing.JFileChooser;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import java.awt.event.ActionEvent;
import java.io.File;

public final class SaveProfileAsAction extends AbstractAction {

    public SaveProfileAsAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            saveProfileAs();
        } catch (Exception e) {
            logger.reportError(e);
        }
    }

    private void saveProfileAs() throws Exception {
        File lastProfileFile = getFile(logger.getSettings().getLoggerProfileFilePath());
        JFileChooser fc = getProfileFileChooser(lastProfileFile);
        if (fc.showSaveDialog(logger) == APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            if (!selectedFile.exists() || showConfirmDialog(logger, selectedFile.getName() + " already exists! Overwrite?") == OK_OPTION) {
                String profileFilePath = saveProfileToFile(logger.getCurrentProfile(), selectedFile);
                logger.getSettings().setLoggerProfileFilePath(profileFilePath);
                logger.reportMessageInTitleBar("Profile: " + profileFilePath);
                logger.reportMessage("Profile succesfully saved as: " + profileFilePath);
            }
        }
    }
}
