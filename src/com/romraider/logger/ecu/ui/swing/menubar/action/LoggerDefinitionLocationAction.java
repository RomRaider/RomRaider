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
import static com.romraider.logger.ecu.ui.swing.menubar.util.FileHelper.getDefinitionFileChooser;
import static com.romraider.logger.ecu.ui.swing.menubar.util.FileHelper.getFile;
import com.romraider.swing.menubar.action.AbstractAction;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.io.File;

public final class LoggerDefinitionLocationAction extends AbstractAction {

    public LoggerDefinitionLocationAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            setDefinitionLocationDialog();
            logger.loadLoggerParams();
        } catch (Exception e) {
            logger.reportError(e);
        }
    }

    private void setDefinitionLocationDialog() throws Exception {
        File lastConfigPath = getFile(logger.getSettings().getLoggerDefinitionFilePath());
        JFileChooser fc = getDefinitionFileChooser(lastConfigPath);
        if (fc.showOpenDialog(logger) == APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            logger.getSettings().setLoggerDefinitionFilePath(path);
            logger.reportMessage("Logger definition location successfully updated: " + path);
        }
    }
}