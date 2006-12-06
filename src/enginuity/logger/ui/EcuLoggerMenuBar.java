package enginuity.logger.ui;

import enginuity.logger.EcuLogger;

import javax.swing.*;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EcuLoggerMenuBar extends JMenuBar implements ActionListener {

    private JMenu fileMenu = new JMenu("File");
    private JMenuItem openProfile = new JMenuItem("Open Profile...");
    private JMenuItem saveProfile = new JMenuItem("Save Profile");
    private JMenuItem saveProfileAs = new JMenuItem("Save Profile As...");
    private JMenuItem exit = new JMenuItem("Exit");

    private JMenu editMenu = new JMenu("Edit");
    private JMenuItem profileManager = new JMenuItem("Profile Manager");
    private JMenuItem settings = new JMenuItem("Settings");

    private JMenu loggerMenu = new JMenu("Logger");
    private JMenuItem startLogging = new JMenuItem("Start Logging");
    private JMenuItem stopLogging = new JMenuItem("Stop Logging");

    private JMenu helpMenu = new JMenu("Help");
    private JMenuItem about = new JMenuItem("About Enginuity ECU Logger");

    private EcuLogger parent;

    public EcuLoggerMenuBar(EcuLogger parent) {
        this.parent = parent;

        // file menu items
        add(fileMenu);
        fileMenu.setMnemonic('F');
        openProfile.setMnemonic('O');
        saveProfile.setMnemonic('S');
        saveProfileAs.setMnemonic('A');
        exit.setMnemonic('X');
        fileMenu.add(openProfile);
        fileMenu.add(saveProfile);
        fileMenu.add(saveProfileAs);
        fileMenu.add(new JSeparator());
        fileMenu.add(exit);
        openProfile.addActionListener(this);
        saveProfile.addActionListener(this);
        saveProfileAs.addActionListener(this);
        exit.addActionListener(this);

        // edit menu items
        add(editMenu);
        editMenu.setMnemonic('E');
        profileManager.setMnemonic('P');
        settings.setMnemonic('S');
        editMenu.add(profileManager);
        editMenu.add(settings);
        profileManager.addActionListener(this);
        settings.addActionListener(this);

        // logger menu stuff
        add(loggerMenu);
        loggerMenu.setMnemonic('L');
        startLogging.setMnemonic('A');
        stopLogging.setMnemonic('O');
        loggerMenu.add(startLogging);
        loggerMenu.add(stopLogging);
        startLogging.addActionListener(this);
        stopLogging.addActionListener(this);

        // help menu stuff
        add(helpMenu);
        helpMenu.setMnemonic('H');
        about.setMnemonic('A');
        helpMenu.add(about);
        about.addActionListener(this);

        // disable unimplemented buttons!
        about.setEnabled(false);
        profileManager.setEnabled(false);
        settings.setEnabled(false);
        about.setEnabled(false);

    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == openProfile) {
            try {
                openProfileDialog();
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
            //System.exit(0);

        } else if (evt.getSource() == profileManager) {
            try {
                openProfileManager();
            } catch (Exception e) {
                parent.reportError(e);
            }

        } else if (evt.getSource() == settings) {
            try {
                openSettingsEditor();
            } catch (Exception e) {
                parent.reportError(e);
            }

        } else if (evt.getSource() == startLogging) {
            parent.startLogging();

        } else if (evt.getSource() == stopLogging) {
            parent.stopLogging();

        }
    }

    private void openProfileManager() {
        //TODO: Finish profile manager!!
//        DefinitionManager form = new DefinitionManager(parent);
//        form.setLocationRelativeTo(parent);
//        form.setVisible(true);
    }

    private void openSettingsEditor() {
        //TODO: Finish settings editor!!
//        DefinitionManager form = new DefinitionManager(parent);
//        form.setLocationRelativeTo(parent);
//        form.setVisible(true);
    }

    private void openProfileDialog() throws Exception {
        File lastProfileFile = new File(parent.getSettings().getLoggerProfileFilePath());
        JFileChooser fc = getProfileFileChooser(lastProfileFile);
        if (fc.showOpenDialog(parent) == APPROVE_OPTION) {
            String profileFilePath = fc.getSelectedFile().getAbsolutePath();
            parent.reloadUserProfile(profileFilePath);
            parent.getSettings().setLoggerProfileFilePath(profileFilePath);
            parent.reportMessage("Profile succesfully loaded: " + profileFilePath);
        }
    }

    private void saveProfile() throws Exception {
        File lastProfileFile = new File(parent.getSettings().getLoggerProfileFilePath());
        saveProfileToFile(lastProfileFile);
    }

    private void saveProfileAs() throws Exception {
        File lastProfileFile = new File(parent.getSettings().getLoggerProfileFilePath());
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
        parent.reportMessage("Profile succesfully saved: " + profileFilePath);
    }

    private JFileChooser getProfileFileChooser(File lastProfileFile) {
        JFileChooser fc;
        if (lastProfileFile.exists() && lastProfileFile.isFile() && lastProfileFile.getParentFile() != null) {
            fc = new JFileChooser(lastProfileFile.getParentFile().getAbsolutePath());
        } else {
            fc = new JFileChooser();
        }
        fc.setFileFilter(new LoggerProfileFileFilter());
        return fc;
    }

}