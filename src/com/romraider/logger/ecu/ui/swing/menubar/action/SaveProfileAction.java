/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2010 RomRaider.com
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
import static com.romraider.logger.ecu.ui.swing.menubar.util.FileHelper.saveProfileToFile;
import com.romraider.swing.menubar.action.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;

public final class SaveProfileAction extends AbstractAction {

    public SaveProfileAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            saveProfile();
        } catch (Exception e) {
            logger.reportError(e);
        }
    }

    private void saveProfile() throws Exception {
        File lastProfileFile = new File(logger.getSettings().getLoggerProfileFilePath());
        String profileFilePath = saveProfileToFile(logger.getCurrentProfile(), lastProfileFile);
        logger.getSettings().setLoggerProfileFilePath(profileFilePath);
        logger.reportMessageInTitleBar("Profile: " + profileFilePath);
        logger.reportMessage("Profile succesfully saved: " + profileFilePath);
    }
}
