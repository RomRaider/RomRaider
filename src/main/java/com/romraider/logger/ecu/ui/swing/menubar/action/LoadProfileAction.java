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

import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.JFileChooser;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.swing.menubar.action.AbstractAction;
import com.romraider.util.SettingsManager;

public final class LoadProfileAction extends AbstractAction {

    public LoadProfileAction(EcuLogger logger) {
        super(logger);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        try {
            loadProfileDialog();
        } catch (Exception e) {
            logger.reportError(e);
        }
    }

    private void loadProfileDialog() throws Exception {
        final File lastProfileFile = getFile(SettingsManager.getSettings().getLoggerProfileFilePath());
        JFileChooser fc = getProfileFileChooser(lastProfileFile);
        if (fc.showOpenDialog(logger) == JFileChooser.APPROVE_OPTION) {
            final String profileFilePath = fc.getSelectedFile().getAbsolutePath();
            if (logger.loadUserProfile(profileFilePath)) {
                logger.getSettings().setLoggerProfileFilePath(profileFilePath);
                logger.restartLogging();
                logger.reportMessage(MessageFormat.format(
                        rb.getString("LPASUCCESS"), profileFilePath));
            }
        }
    }
}
