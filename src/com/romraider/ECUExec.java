/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
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

package com.romraider;

import static com.romraider.Version.PRODUCT_NAME;
import com.romraider.editor.ecu.ECUEditor;
import static com.romraider.editor.ecu.ECUEditorManager.getECUEditor;
import static com.romraider.logger.ecu.EcuLogger.startLogger;
import static com.romraider.swing.LookAndFeelManager.initLookAndFeel;
import static com.romraider.util.LogManager.initDebugLogging;
import static com.romraider.util.RomServer.isRunning;
import static com.romraider.util.RomServer.sendRomToOpenInstance;
import static com.romraider.util.RomServer.waitForRom;
import com.romraider.util.SettingsManager;
import com.romraider.util.SettingsManagerImpl;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import java.io.File;

public class ECUExec {
    private static final Logger LOGGER = getLogger(ECUExec.class);
    private static final String START_LOGGER_ARG = "-logger";

    private ECUExec() {
        throw new UnsupportedOperationException();
    }

    public static void main(String args[]) {
        // init debug logging
        initDebugLogging();

        // check for dodgy threading - dev only
//        RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager(true));

        // set look and feel
        initLookAndFeel();

        // check if already running
        if (isRunning()) {
            if (args.length == 0 || containsLoggerArg(args)) {
                showAlreadyRunningMessage();
            } else {
                sendRomToOpenInstance(args[0]);
            }
        } else {
            // open editor or logger
            if (containsLoggerArg(args)) {
                openLogger(args);
            } else {
                openEditor(args);
            }
        }
    }

    private static void showAlreadyRunningMessage() {
        showMessageDialog(null, PRODUCT_NAME + " is already running.", PRODUCT_NAME, INFORMATION_MESSAGE);
    }

    private static boolean containsLoggerArg(String[] args) {
        for (String arg : args) {
            if (arg.equals(START_LOGGER_ARG)) {
                return true;
            }
        }
        return false;
    }

    private static void openLogger(String[] args) {
        SettingsManager manager = new SettingsManagerImpl();
        Settings settings = manager.load();
        startLogger(EXIT_ON_CLOSE, settings, args);
    }

    private static void openRom(final ECUEditor editor, final String rom) {
        invokeLater(new Runnable() {
            public void run() {
                try {
                    File file = new File(rom);
                    editor.openImage(file);
                } catch (Exception ex) {
                    LOGGER.error("Error opening rom", ex);
                }
            }
        });
    }

    private static void openEditor(String[] args) {
        ECUEditor editor = getECUEditor();
        if (args.length > 0) {
            openRom(editor, args[0]);
        }
        startRomListener(editor);
    }

    private static void startRomListener(ECUEditor editor) {
        try {
            while (true) {
                String rom = waitForRom();
                openRom(editor, rom);
            }
        } catch (Throwable e) {
            LOGGER.error("Error occurred", e);
        }
    }
}
