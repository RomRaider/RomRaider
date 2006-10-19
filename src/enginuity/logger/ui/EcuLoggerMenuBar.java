package enginuity.logger.ui;

import enginuity.logger.EcuLogger;

import javax.management.modelmbean.XMLParseException;
import javax.swing.*;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class EcuLoggerMenuBar extends JMenuBar implements ActionListener {

    private JMenu fileMenu = new JMenu("File");
    private JMenuItem openProfile = new JMenuItem("Open Profile");
    private JMenuItem saveProfile = new JMenuItem("Save Profile");
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
        exit.setMnemonic('X');
        fileMenu.add(openProfile);
        fileMenu.add(saveProfile);
        fileMenu.add(new JSeparator());
        fileMenu.add(exit);
        openProfile.addActionListener(this);
        saveProfile.addActionListener(this);
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
        saveProfile.setEnabled(false);
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

        } else if (evt.getSource() == exit) {
            parent.handleExit();
            System.exit(0);

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

    public void openProfileDialog() throws XMLParseException, Exception {
        File lastProfileFile = new File(parent.getSettings().getLoggerProfileFilePath());
        JFileChooser fc;
        if (lastProfileFile.exists() && lastProfileFile.isFile()) {
            fc = new JFileChooser(lastProfileFile.getParentFile().getAbsolutePath());
        } else {
            fc = new JFileChooser();
        }
        fc.setFileFilter(new LoggerProfileFileFilter());
        if (fc.showOpenDialog(parent) == APPROVE_OPTION) {
            String profileFilePath = fc.getSelectedFile().getAbsolutePath();
            parent.reloadUserProfile(profileFilePath);
            parent.getSettings().setLoggerProfileFilePath(profileFilePath);
        }
    }

    public void saveProfile() {
        //TODO: Finish profile saving option!
    }
}