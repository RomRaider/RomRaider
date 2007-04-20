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

package enginuity.swing;

import com.centerkey.utils.BareBonesBrowserLaunch;
import enginuity.ECUEditor;
import enginuity.logger.ecu.EcuLogger;
import enginuity.logger.utec.gui.JutecGUI;
import enginuity.maps.Rom;
import enginuity.ramtune.test.RamTuneTestApp;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import static javax.swing.JFrame.DISPOSE_ON_CLOSE;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import static javax.swing.JOptionPane.CANCEL_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;

public class ECUEditorMenuBar extends JMenuBar implements ActionListener {

    private JMenu fileMenu = new JMenu("File");
    private JMenuItem openImage = new JMenuItem("Open Image...");
    private JMenuItem saveImage = new JMenuItem("Save Image...");
    private JMenuItem refreshImage = new JMenuItem("Refresh Image");
    private JMenuItem closeImage = new JMenuItem("Close Image");
    private JMenuItem closeAll = new JMenuItem("Close All Images");
    private JMenuItem exit = new JMenuItem("Exit");

    private JMenu definitionMenu = new JMenu("ECU Definitions");
    private JMenuItem defManager = new JMenuItem("ECU Definition Manager...");
    private JMenuItem editDefinition = new JMenuItem("Edit ECU Definitions...");
    private JMenuItem updateDefinition = new JMenuItem("Update ECU Definitions...");

    private JMenu editMenu = new JMenu("Edit");
    private JMenuItem settings = new JMenuItem("Enginuity Settings...");

    private JMenu viewMenu = new JMenu("View");
    private JMenuItem romProperties = new JMenuItem("ECU Image Properties");
    private ButtonGroup levelGroup = new ButtonGroup();
    private JMenu levelMenu = new JMenu("User Level");
    private JRadioButtonMenuItem level1 = new JRadioButtonMenuItem("1 Beginner");
    private JRadioButtonMenuItem level2 = new JRadioButtonMenuItem("2 Intermediate");
    private JRadioButtonMenuItem level3 = new JRadioButtonMenuItem("3 Advanced");
    private JRadioButtonMenuItem level4 = new JRadioButtonMenuItem("4 Highest");
    private JRadioButtonMenuItem level5 = new JRadioButtonMenuItem("5 Debug Mode");

    private JMenu loggerMenu = new JMenu("Logger");
    private JMenuItem openLogger = new JMenuItem("Launch ECU Logger...");
    private JMenuItem utecLogger = new JMenuItem("Launch UTEC Logger...");

    private JMenu ramTuneMenu = new JMenu("RAMTune");
    private JMenuItem launchRamTuneTestApp = new JMenuItem("Launch Test App...");

    private JMenu helpMenu = new JMenu("Help");
    private JMenuItem about = new JMenuItem("About Enginuity");

    private ECUEditor parent;

    public ECUEditorMenuBar(ECUEditor parent) {
        this.parent = parent;

        // file menu items
        add(fileMenu);
        fileMenu.setMnemonic('F');
        openImage.setMnemonic('O');
        saveImage.setMnemonic('S');
        refreshImage.setMnemonic('R');
        closeImage.setMnemonic('C');
        closeAll.setMnemonic('A');
        exit.setMnemonic('X');
        fileMenu.add(openImage);
        fileMenu.add(saveImage);
        fileMenu.add(refreshImage);
        fileMenu.add(new JSeparator());
        fileMenu.add(closeImage);
        fileMenu.add(closeAll);
        fileMenu.add(new JSeparator());
        fileMenu.add(exit);
        openImage.addActionListener(this);
        saveImage.addActionListener(this);
        refreshImage.addActionListener(this);
        closeImage.addActionListener(this);
        closeAll.addActionListener(this);
        exit.addActionListener(this);

        // edit menu items
        add(editMenu);
        editMenu.setMnemonic('E');
        editMenu.add(settings);
        settings.addActionListener(this);

        // ecu def menu items
        add(definitionMenu);
        definitionMenu.setMnemonic('D');
        defManager.setMnemonic('D');
        editDefinition.setMnemonic('E');
        updateDefinition.setMnemonic('U');
        settings.setMnemonic('S');
        definitionMenu.add(defManager);
        definitionMenu.add(editDefinition);
        definitionMenu.add(updateDefinition);
        defManager.addActionListener(this);
        editDefinition.addActionListener(this);
        updateDefinition.addActionListener(this);

        // view menu items
        add(viewMenu);
        viewMenu.setMnemonic('V');
        romProperties.setMnemonic('P');
        levelMenu.setMnemonic('U');
        level1.setMnemonic('1');
        level2.setMnemonic('2');
        level3.setMnemonic('3');
        level4.setMnemonic('4');
        level5.setMnemonic('5');
        viewMenu.add(romProperties);
        viewMenu.add(levelMenu);
        levelMenu.add(level1);
        levelMenu.add(level2);
        levelMenu.add(level3);
        levelMenu.add(level4);
        levelMenu.add(level5);
        romProperties.addActionListener(this);
        level1.addActionListener(this);
        level2.addActionListener(this);
        level3.addActionListener(this);
        level4.addActionListener(this);
        level5.addActionListener(this);
        levelGroup.add(level1);
        levelGroup.add(level2);
        levelGroup.add(level3);
        levelGroup.add(level4);
        levelGroup.add(level5);
        // select correct userlevel button
        if (parent.getSettings().getUserLevel() == 1) {
            level1.setSelected(true);
        } else if (parent.getSettings().getUserLevel() == 2) {
            level2.setSelected(true);
        } else if (parent.getSettings().getUserLevel() == 3) {
            level3.setSelected(true);
        } else if (parent.getSettings().getUserLevel() == 4) {
            level4.setSelected(true);
        } else if (parent.getSettings().getUserLevel() == 5) {
            level5.setSelected(true);
        }

        // logger menu stuff
        add(loggerMenu);
        loggerMenu.setMnemonic('L');
        openLogger.setMnemonic('O');
        loggerMenu.add(openLogger);
        loggerMenu.add(utecLogger);
        openLogger.addActionListener(this);
        utecLogger.addActionListener(this);

        // ramtune menu stuff
        add(ramTuneMenu);
        ramTuneMenu.setMnemonic('R');
        launchRamTuneTestApp.setMnemonic('L');
        ramTuneMenu.add(launchRamTuneTestApp);
        launchRamTuneTestApp.addActionListener(this);
        
        // help menu stuff
        add(helpMenu);
        helpMenu.setMnemonic('H');
        about.setMnemonic('A');
        helpMenu.add(about);
        about.addActionListener(this);

        // disable unused buttons! 0.3.1
        about.setEnabled(false);
        editDefinition.setEnabled(false);
        this.updateMenu();
    }

    public void updateMenu() {
        String file = getLastSelectedRomFileName();
        if ("".equals(file)) {
            saveImage.setEnabled(false);
            closeImage.setEnabled(false);
            closeAll.setEnabled(false);
            romProperties.setEnabled(false);
            saveImage.setText("Save...");
        } else {
            saveImage.setEnabled(true);
            closeImage.setEnabled(true);
            closeAll.setEnabled(true);
            romProperties.setEnabled(true);
            saveImage.setText("Save " + file + "...");
        }
        refreshImage.setText("Refresh " + file);
        closeImage.setText("Close " + file);
        romProperties.setText(file + "Properties");
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openImage) {
            try {
                openImageDialog();
            } catch (Exception ex) {
                showMessageDialog(parent,
                        new DebugPanel(ex, parent.getSettings().getSupportURL()), "Exception", ERROR_MESSAGE);
            }

        } else if (e.getSource() == saveImage) {
            try {
                this.saveImage(parent.getLastSelectedRom());
            } catch (Exception ex) {
                showMessageDialog(parent,
                        new DebugPanel(ex, parent.getSettings().getSupportURL()), "Exception", ERROR_MESSAGE);
            }

        } else if (e.getSource() == closeImage) {
            this.closeImage();

        } else if (e.getSource() == closeAll) {
            this.closeAllImages();

        } else if (e.getSource() == exit) {
            parent.handleExit();
            System.exit(0);

        } else if (e.getSource() == romProperties) {
            showMessageDialog(parent, new RomPropertyPanel(parent.getLastSelectedRom()),
                    parent.getLastSelectedRom().getRomIDString() + " Properties", INFORMATION_MESSAGE);

        } else if (e.getSource() == refreshImage) {
            try {
                refreshImage();

            } catch (Exception ex) {
                showMessageDialog(parent, new DebugPanel(ex,
                        parent.getSettings().getSupportURL()), "Exception", ERROR_MESSAGE);
            }

        } else if (e.getSource() == settings) {
            SettingsForm form = new SettingsForm(parent);
            form.setLocationRelativeTo(parent);
            form.setVisible(true);

        } else if (e.getSource() == defManager) {
            DefinitionManager form = new DefinitionManager(parent);
            form.setLocationRelativeTo(parent);
            form.setVisible(true);

        } else if (e.getSource() == level1) {
            parent.setUserLevel(1);

        } else if (e.getSource() == level2) {
            parent.setUserLevel(2);

        } else if (e.getSource() == level3) {
            parent.setUserLevel(3);

        } else if (e.getSource() == level4) {
            parent.setUserLevel(4);

        } else if (e.getSource() == level5) {
            parent.setUserLevel(5);

        } else if (e.getSource() == openLogger) {
            EcuLogger.startLogger(DISPOSE_ON_CLOSE, parent.getSettings());

        } else if (e.getSource() == utecLogger) {
        	JutecGUI.startLogger(DISPOSE_ON_CLOSE, parent.getSettings());

        } else if (e.getSource() == updateDefinition) {
            BareBonesBrowserLaunch.openURL(parent.getSettings().getEcuDefsURL());

        } else if (e.getSource() == launchRamTuneTestApp) {
            RamTuneTestApp.startTestApp(DISPOSE_ON_CLOSE);

        }
    }

    public void refreshImage() throws Exception {
        if (parent.getLastSelectedRom() != null) {
            File file = parent.getLastSelectedRom().getFullFileName();
            parent.closeImage();
            parent.openImage(file);
        }
    }

    public void openImageDialog() throws Exception {
        JFileChooser fc = new JFileChooser(parent.getSettings().getLastImageDir());
        fc.setFileFilter(new ECUImageFilter());
        if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            parent.openImage(fc.getSelectedFile());
            parent.getSettings().setLastImageDir(fc.getCurrentDirectory());
        }
    }

    public void closeImage() {
        parent.closeImage();
    }

    public void closeAllImages() {
        parent.closeAllImages();
    }

    public void saveImage(Rom input) throws Exception {
        if (parent.getLastSelectedRom() != null) {
            JFileChooser fc = new JFileChooser(parent.getSettings().getLastImageDir());
            fc.setFileFilter(new ECUImageFilter());
            if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                boolean save = true;
                if (fc.getSelectedFile().exists()) {
                    if (showConfirmDialog(parent, fc.getSelectedFile().getName() + " already exists! Overwrite?") == CANCEL_OPTION) {
                        save = false;
                    }
                }
                if (save) {
                    byte[] output = parent.getLastSelectedRom().saveFile();
                    FileOutputStream fos = new FileOutputStream(fc.getSelectedFile());
                    try {
                        fos.write(output);
                    } finally {
                        fos.close();
                    }
                    parent.getLastSelectedRom().setFullFileName(fc.getSelectedFile().getAbsoluteFile());
                    parent.setLastSelectedRom(parent.getLastSelectedRom());
                }
            }
        }
    }

    private String getLastSelectedRomFileName() {
        Rom lastSelectedRom = parent.getLastSelectedRom();
        return lastSelectedRom == null ? "" : lastSelectedRom.getFileName() + " ";
    }
}