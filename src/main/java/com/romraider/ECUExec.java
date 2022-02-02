/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

import static com.romraider.Settings.COMMA;
import static com.romraider.Version.BUILDNUMBER;
import static com.romraider.Version.PRODUCT_NAME;
import static com.romraider.Version.SUPPORT_URL;
import static com.romraider.Version.VERSION;
import static com.romraider.editor.ecu.ECUEditorManager.*;
import static com.romraider.logger.ecu.EcuLogger.startLogger;
import static com.romraider.swing.LookAndFeelManager.initLookAndFeel;
import static com.romraider.util.LogManager.initDebugLogging;
import static com.romraider.EditorLoggerCommunication.*;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import static org.apache.log4j.Logger.getLogger;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.romraider.editor.ecu.ECUEditor;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.logger.ecu.EcuLogger;
import com.romraider.util.ResourceUtil;

public class ECUExec {
    private static final Logger LOGGER = getLogger(ECUExec.class);
    private static final String START_LOGGER_ARG = "-logger";
    private static final String START_LOGGER_FULLSCREEN_ARG = "-logger.fullscreen";
    private static final String LOGGER_TOUCH_ARG = "-logger.touch";
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            ECUExec.class.getName());

    private ECUExec() {
        throw new UnsupportedOperationException();
    }

    public static void main(String args[]) {
        // init i18n resources
        if (rb == null) return;
        // init debug logging
        initDebugLogging();
        // dump the system properties to the log file as early as practical to
        // help debugging/support
        LOGGER.info(PRODUCT_NAME + " " + VERSION + " Build: " + BUILDNUMBER);
        LOGGER.info(MessageFormat.format(rb.getString("SUPPORT"), SUPPORT_URL));
        LOGGER.info(DateFormat.getDateTimeInstance(
                DateFormat.FULL,
                DateFormat.LONG).format(System.currentTimeMillis()));
        LOGGER.info("System Properties: \n\t"
                + System.getProperties().toString().replace(COMMA, "\n\t"));

        /**
         * Bitness of supporting libraries must match the bitness of RomRaider
         * and the running JRE.  Notify if mixed bitness is detected.
         */
        if (!System.getProperty("sun.arch.data.model").equals(Version.BUILD_ARCH) &&
                !containsLoggerArg(args)) {
            showMessageDialog(null,
                    MessageFormat.format(
                    		rb.getString("COMPATJRE"), PRODUCT_NAME, Version.BUILD_ARCH),
                    rb.getString("JREWARN"),
                    WARNING_MESSAGE);
        }

        // check for dodgy threading - dev only
        //RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager(true));

        // set look and feel
        initLookAndFeel();
        setExecType(args);

        // check if already running
        if (isRunning()) {
        	// The other executable will open us, close this app
        	EditorLoggerCommunication.sendTypeToOtherExec(args);
        } else {
            // open editor or logger
            if (containsLoggerArg(args)) {
                openLogger(DISPOSE_ON_CLOSE, args);
            } else {
                openEditor(DISPOSE_ON_CLOSE, args);
            }

            startExecCommunication();
        }
    }

    private static void setExecType(String[] args) {
    	Exec_type execType = containsLoggerArg(args) ? Exec_type.LOGGER : Exec_type.EDITOR;
    	EditorLoggerCommunication.setExectable(execType, args);
    }

    public static void showAlreadyRunningMessage() {
        showMessageDialog(null,
                MessageFormat.format(rb.getString("ISRUNNING"), PRODUCT_NAME),
                PRODUCT_NAME, INFORMATION_MESSAGE);
    }

    private static boolean containsLoggerArg(String[] args) {
        for (String arg : args) {
            if (	arg.equalsIgnoreCase(START_LOGGER_ARG) ||
            		arg.equalsIgnoreCase(START_LOGGER_FULLSCREEN_ARG) ||
            		arg.equalsIgnoreCase(LOGGER_TOUCH_ARG)) {
                return true;
            }
        }
        return false;
    }

    public static void openLogger(int defaultCloseOperation, String[] args) {
        startLogger(defaultCloseOperation, getECUEditorWithoutCreation(), args);
    }

    private static void openEditor(int defaultCloseOperation, String[] args) {
        ECUEditor editor = getECUEditor();
        editor.setDefaultCloseOperation(defaultCloseOperation);
        editor.initializeEditorUI();
        editor.checkDefinitions();

        if (args.length > 0) {
            editor.openImage(args[0]);
        }
    }

    private static void startExecCommunication() {
	    while (true) {
	    	try {
	    		ExecutableInstance instance = EditorLoggerCommunication.waitForOtherExec();

	    		if(instance.execType == Exec_type.LOGGER) {
	    			if(EditorLoggerCommunication.getExecutableType() == Exec_type.LOGGER ||
	    					EcuLogger.getEcuLoggerWithoutCreation() != null) {
	    				showAlreadyRunningMessage();
	    				continue;
	    			}

	    			openLogger(DISPOSE_ON_CLOSE, instance.currentArgs);
	    			LOGGER.info("Opening Logger with args: " +  Arrays.toString(instance.currentArgs));
	    		}
	    		else if(instance.execType == Exec_type.EDITOR) {
	    			openEditor(DISPOSE_ON_CLOSE, instance.currentArgs);

		    		if(EditorLoggerCommunication.getExecutableType() == Exec_type.LOGGER) {
		    			EcuLogger.getEcuLoggerWithoutCreation().setEcuEditor(
		    					ECUEditorManager.getECUEditorWithoutCreation());
		    		}

	    			LOGGER.info("Opening Editor with args: " +  Arrays.toString(instance.currentArgs));
	    		}
	    		else {
	    			LOGGER.error("Unknown type in Editor/Logger communication with args: " +  Arrays.toString(instance.currentArgs));
	    		}

			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
    }
}
