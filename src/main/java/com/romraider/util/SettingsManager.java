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

package com.romraider.util;

import static com.romraider.Version.VERSION;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.romraider.Settings;
import com.romraider.swing.JProgressPane;
import com.romraider.xml.DOMSettingsBuilder;
import com.romraider.xml.DOMSettingsUnmarshaller;

public class SettingsManager {
    private static final Logger LOGGER =
            Logger.getLogger(SettingsManager.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            SettingsManager.class.getName());
    private static final String SETTINGS_FILE = "/settings.xml";
    private static final String USER_HOME =
            System.getProperty("user.home") + "/.RomRaider";
    private static final String START_DIR = System.getProperty("user.dir");
    private static String settingsDir = USER_HOME;

    private static Settings settings = null;
    private static boolean testing = false;

    public static Settings getSettings() {
        if(null == settings) {
            settings = load();
        }
        return settings;
    }

    public static void setTesting(boolean b) {
    	testing = b;
    }

    public static boolean getTesting() {
    	return testing;
    }
    
    private static Settings load() {
        Settings loadedSettings;
        try {
            FileInputStream settingsFileIn = null;
            File sf = new File(START_DIR + SETTINGS_FILE);
            if (sf.exists()) {
                settingsFileIn = new FileInputStream(sf);
                settingsDir = START_DIR;
            }
            else {
                sf = new File(USER_HOME + SETTINGS_FILE);
                settingsFileIn = new FileInputStream(sf);
            }
            LOGGER.info("Loaded settings from file: " + settingsDir.replace("\\", "/") + SETTINGS_FILE);

            if (sf.length() > 0) {
                final InputSource src = new InputSource(settingsFileIn);
                final DOMSettingsUnmarshaller domUms = new DOMSettingsUnmarshaller();
                final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                final DocumentBuilder builder = dbf.newDocumentBuilder();
                final Document doc = builder.parse(src);
                loadedSettings = domUms.unmarshallSettings(doc.getDocumentElement());
                settingsFileIn.close();
            }
            else {
                if (settingsFileIn != null)
                    settingsFileIn.close();
                throw new FileNotFoundException("file length is 0");
            }
        } catch (FileNotFoundException e) {
            showMessageDialog(null,
                    rb.getString("FNF"),
                    rb.getString("ERROR"), INFORMATION_MESSAGE);
            loadedSettings = new Settings();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return loadedSettings;
    }

    public static void save(Settings newSettings) {
        save(newSettings, new JProgressPane());
    }

    public static void save(Settings newSettings, JProgressPane progress) {
    	if(testing) return;

        final DOMSettingsBuilder builder = new DOMSettingsBuilder();
        try {
            final File newDir = new File(settingsDir);
            newDir.mkdir();     // Creates directory if it does not exist
            final File sf = new File(settingsDir + SETTINGS_FILE);
            builder.buildSettings(newSettings, sf, progress, VERSION);
            settings = newSettings;
            if (sf.length() == 0)
                throw new RuntimeException("Settings file write failed");
        } catch (Exception e) {
            settings = newSettings;
            throw new RuntimeException(e);
        }
    }
}
