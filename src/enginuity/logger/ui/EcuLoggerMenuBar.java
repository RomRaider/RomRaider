/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.logger.ui;

import enginuity.logger.EcuLogger;
import enginuity.logger.profile.UserProfileFileFilter;

import javax.swing.*;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EcuLoggerMenuBar extends JMenuBar implements ActionListener {
    private static final String USER_HOME_DIR = System.getProperty("user.home");

    private JMenu fileMenu = new JMenu("File");
    private JMenuItem loadProfile = new JMenuItem("Load Profile...");
    private JMenuItem reloadProfile = new JMenuItem("Reload Profile");
    private JMenuItem saveProfile = new JMenuItem("Save Profile");
    private JMenuItem saveProfileAs = new JMenuItem("Save Profile As...");
    private JMenuItem exit = new JMenuItem("Exit");

    private JMenu settingsMenu = new JMenu("Settings");
    private JMenuItem logFileLocation = new JMenuItem("Log File Output Location...");
    private JMenuItem logFileControllerSwitch = new JRadioButtonMenuItem("Control File Logging With Defogger Switch");
    private JMenuItem logFileAbsoluteTimestamp = new JRadioButtonMenuItem("Use Absolute Timestamp In Log File");

    private JMenu connectionMenu = new JMenu("Connection");
    private JMenuItem resetConnection = new JMenuItem("Reset");
    private JMenuItem disconnect = new JMenuItem("Disconnect");

    private JMenu helpMenu = new JMenu("Help");
    private JMenuItem about = new JMenuItem("About Enginuity ECU Logger");

    private EcuLogger parent;

    public EcuLoggerMenuBar(EcuLogger parent) {
        this.parent = parent;

        // file menu items
        add(fileMenu);
        fileMenu.setMnemonic('F');
        loadProfile.setMnemonic('O');
        reloadProfile.setMnemonic('R');
        saveProfile.setMnemonic('S');
        saveProfileAs.setMnemonic('A');
        exit.setMnemonic('X');
        fileMenu.add(loadProfile);
        fileMenu.add(reloadProfile);
        fileMenu.add(saveProfile);
        fileMenu.add(saveProfileAs);
        fileMenu.add(new JSeparator());
        fileMenu.add(exit);
        loadProfile.addActionListener(this);
        reloadProfile.addActionListener(this);
        saveProfile.addActionListener(this);
        saveProfileAs.addActionListener(this);
        exit.addActionListener(this);

        // settings menu items
        add(settingsMenu);
        settingsMenu.setMnemonic('E');
        logFileLocation.setMnemonic('F');
        logFileControllerSwitch.setMnemonic('C');
        logFileAbsoluteTimestamp.setMnemonic('A');
        settingsMenu.add(logFileLocation);
        settingsMenu.add(new JSeparator());
        settingsMenu.add(logFileControllerSwitch);
        settingsMenu.add(logFileAbsoluteTimestamp);
        logFileLocation.addActionListener(this);
        logFileControllerSwitch.addActionListener(this);
        logFileAbsoluteTimestamp.addActionListener(this);
        logFileControllerSwitch.setSelected(parent.getSettings().isFileLoggingControllerSwitchActive());
        logFileAbsoluteTimestamp.setSelected(parent.getSettings().isFileLoggingAbsoluteTimestamp());

        // connection menu items
        add(connectionMenu);
        connectionMenu.setMnemonic('C');
        resetConnection.setMnemonic('R');
        disconnect.setMnemonic('D');
        connectionMenu.add(resetConnection);
        connectionMenu.add(disconnect);
        resetConnection.addActionListener(this);
        disconnect.addActionListener(this);

        // help menu stuff
        add(helpMenu);
        helpMenu.setMnemonic('H');
        about.setMnemonic('A');
        helpMenu.add(about);
        about.addActionListener(this);

        // disable unimplemented buttons!
        about.setEnabled(false);

    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == loadProfile) {
            try {
                loadProfileDialog();
            } catch (Exception e) {
                parent.reportError(e);
            }

        } else if (evt.getSource() == reloadProfile) {
            try {
                parent.loadUserProfile(parent.getSettings().getLoggerProfileFilePath());
            } catch (Exception e) {
                parent.reportError(e);
            }

        } else if (evt.getSource() == saveProfile) {
            try {
                saveProfile();
            } catch (Exception e) {
                parent.reportError(e);
            }

        } else if (evt.getSource() == saveProfileAs) {
            try {
                saveProfileAs();
            } catch (Exception e) {
                parent.reportError(e);
            }

        } else if (evt.getSource() == exit) {
            parent.handleExit();
            parent.dispose();
            parent.setVisible(false);

        } else if (evt.getSource() == logFileLocation) {
            try {
                setLogFileLocationDialog();
            } catch (Exception e) {
                parent.reportError(e);
            }

        } else if (evt.getSource() == logFileControllerSwitch) {
            try {
                parent.getSettings().setFileLoggingControllerSwitchActive(logFileControllerSwitch.isSelected());
            } catch (Exception e) {
                parent.reportError(e);
            }

        } else if (evt.getSource() == logFileAbsoluteTimestamp) {
            try {
                parent.getSettings().setFileLoggingAbsoluteTimestamp(logFileAbsoluteTimestamp.isSelected());
            } catch (Exception e) {
                parent.reportError(e);
            }

        } else if (evt.getSource() == resetConnection) {
            try {
                parent.restartLogging();
            } catch (Exception e) {
                parent.reportError(e);
            }

        } else if (evt.getSource() == disconnect) {
            try {
                parent.stopLogging();
            } catch (Exception e) {
                parent.reportError(e);
            }

        }
    }

    private void setLogFileLocationDialog() throws Exception {
        File lastLoggerOutputDir = getFile(parent.getSettings().getLoggerOutputDirPath());
        JFileChooser fc = getLoggerOutputDirFileChooser(lastLoggerOutputDir);
        if (fc.showOpenDialog(parent) == APPROVE_OPTION) {
            String loggerOutputDirPath = fc.getSelectedFile().getAbsolutePath();
            parent.getSettings().setLoggerOutputDirPath(loggerOutputDirPath);
            parent.reportMessage("Log file output location successfully updated: " + loggerOutputDirPath);
        }
    }

    private void loadProfileDialog() throws Exception {
        File lastProfileFile = getFile(parent.getSettings().getLoggerProfileFilePath());
        JFileChooser fc = getProfileFileChooser(lastProfileFile);
        if (fc.showOpenDialog(parent) == APPROVE_OPTION) {
            String profileFilePath = fc.getSelectedFile().getAbsolutePath();
            parent.loadUserProfile(profileFilePath);
            parent.getSettings().setLoggerProfileFilePath(profileFilePath);
            parent.reportMessageInTitleBar("Profile: " + profileFilePath);
            parent.reportMessage("Profile succesfully loaded: " + profileFilePath);
        }
    }

    private void saveProfile() throws Exception {
        File lastProfileFile = new File(parent.getSettings().getLoggerProfileFilePath());
        saveProfileToFile(lastProfileFile);
    }

    private void saveProfileAs() throws Exception {
        File lastProfileFile = getFile(parent.getSettings().getLoggerProfileFilePath());
        JFileChooser fc = getProfileFileChooser(lastProfileFile);
        if (fc.showSaveDialog(parent) == APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            if (!selectedFile.exists()
                    || showConfirmDialog(parent, selectedFile.getName() + " already exists! Overwrite?") == OK_OPTION) {
                saveProfileToFile(selectedFile);
            }
        }
    }

    private void saveProfileToFile(File destinationFile) throws IOException {
        String profileFilePath = destinationFile.getAbsolutePath();
        if (!profileFilePath.endsWith(".xml")) {
            profileFilePath += ".xml";
            destinationFile = new File(profileFilePath);
        }
        FileOutputStream fos = new FileOutputStream(destinationFile);
        try {
            fos.write(parent.getCurrentProfile().getBytes());
        } finally {
            fos.close();
        }
        parent.getSettings().setLoggerProfileFilePath(profileFilePath);
        parent.reportMessageInTitleBar("Profile: " + profileFilePath);
        parent.reportMessage("Profile succesfully saved: " + profileFilePath);
    }

    private JFileChooser getProfileFileChooser(File lastProfileFile) {
        JFileChooser fc;
        if (lastProfileFile.exists() && lastProfileFile.isFile() && lastProfileFile.getParentFile() != null) {
            fc = new JFileChooser(lastProfileFile.getParentFile().getAbsolutePath());
        } else {
            fc = new JFileChooser();
        }
        fc.setFileFilter(new UserProfileFileFilter());
        return fc;
    }

    private JFileChooser getLoggerOutputDirFileChooser(File lastLoggerOutputDir) {
        JFileChooser fc;
        if (lastLoggerOutputDir.exists() && lastLoggerOutputDir.isDirectory()) {
            fc = new JFileChooser(lastLoggerOutputDir.getAbsolutePath());
        } else {
            fc = new JFileChooser();
        }
        fc.setFileSelectionMode(DIRECTORIES_ONLY);
        return fc;
    }

    private File getFile(String filePath) {
        return filePath == null ? new File(USER_HOME_DIR) : new File(filePath);
    }

}