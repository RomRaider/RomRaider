/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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

package com.romraider.swing;

import static com.romraider.Version.ABOUT_ICON;
import static com.romraider.Version.BUILDNUMBER;
import static com.romraider.Version.ECU_DEFS_URL;
import static com.romraider.Version.PRODUCT_NAME;
import static com.romraider.Version.SUPPORT_URL;
import static com.romraider.Version.VERSION;
import static javax.swing.JOptionPane.CANCEL_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import com.centerkey.utils.BareBonesBrowserLaunch;
import com.romraider.editor.ecu.ECUEditor;
import com.romraider.maps.Rom;
import com.romraider.maps.Table;
import com.romraider.ramtune.test.RamTuneTestApp;

public class ECUEditorMenuBar extends JMenuBar implements ActionListener {

    private static final long serialVersionUID = -4777040428837855236L;
    private final JMenu fileMenu = new JMenu("File");
    private final JMenuItem openImage = new JMenuItem("Open Image...");
    private final JMenuItem openImages = new JMenuItem("Open Image(s)...");
    private final JMenuItem saveImage = new JMenuItem("Save Image As...");
    private final JMenuItem saveAsRepository = new JMenuItem("Save Image As Repository...");
    private final JMenuItem refreshImage = new JMenuItem("Refresh Image");
    private final JMenuItem closeImage = new JMenuItem("Close Image");
    private final JMenuItem closeAll = new JMenuItem("Close All Images");
    private final JMenuItem exit = new JMenuItem("Exit");

    private final JMenu definitionMenu = new JMenu("ECU Definitions");
    private final JMenuItem defManager = new JMenuItem("ECU Definition Manager...");
    //    private JMenuItem editDefinition = new JMenuItem("Edit ECU Definitions...");
    private final JMenuItem updateDefinition = new JMenuItem("Get ECU Definitions...");

    private final JMenu editMenu = new JMenu("Edit");
    private final JMenuItem settings = new JMenuItem(PRODUCT_NAME + " Settings...");
    private final JMenuItem compareImages = new JMenuItem("Compare Images...");

    private final JMenu viewMenu = new JMenu("View");
    private final JMenuItem romProperties = new JMenuItem("ECU Image Properties");
    private final ButtonGroup levelGroup = new ButtonGroup();
    private final JMenu levelMenu = new JMenu("User Level");
    private final JRadioButtonMenuItem level1 = new JRadioButtonMenuItem("1 Beginner");
    private final JRadioButtonMenuItem level2 = new JRadioButtonMenuItem("2 Intermediate");
    private final JRadioButtonMenuItem level3 = new JRadioButtonMenuItem("3 Advanced");
    private final JRadioButtonMenuItem level4 = new JRadioButtonMenuItem("4 Highest");
    private final JRadioButtonMenuItem level5 = new JRadioButtonMenuItem("5 Debug Mode");

    private final JMenu loggerMenu = new JMenu("Logger");
    private final JMenuItem openLogger = new JMenuItem("Launch Logger...");

    private final JMenu ramTuneMenu = new JMenu("SSM");
    private final JMenuItem launchRamTuneTestApp = new JMenuItem("Launch Test App...");

    private final JMenu helpMenu = new JMenu("Help");
    private final JMenuItem about = new JMenuItem("About " + PRODUCT_NAME);

    private final ECUEditor parent;

    public ECUEditorMenuBar(ECUEditor parent) {
        this.parent = parent;

        // file menu items
        add(fileMenu);
        fileMenu.setMnemonic('F');
        openImage.setMnemonic('O');
        openImage.setMnemonic('I');
        saveImage.setMnemonic('S');
        saveAsRepository.setMnemonic('D');
        refreshImage.setMnemonic('R');
        closeImage.setMnemonic('C');
        //closeAll.setMnemonic('A');
        exit.setMnemonic('X');
        fileMenu.add(openImage);
        //fileMenu.add(openImages);
        fileMenu.add(saveImage);
        fileMenu.add(saveAsRepository);
        fileMenu.add(refreshImage);
        fileMenu.add(new JSeparator());
        fileMenu.add(closeImage);
        //fileMenu.add(closeAll);
        fileMenu.add(new JSeparator());
        fileMenu.add(exit);
        openImage.addActionListener(this);
        //openImages.addActionListener(this);
        saveImage.addActionListener(this);
        saveAsRepository.addActionListener(this);
        refreshImage.addActionListener(this);
        closeImage.addActionListener(this);
        //closeAll.addActionListener(this);
        exit.addActionListener(this);

        // edit menu items
        add(editMenu);
        editMenu.setMnemonic('E');
        editMenu.add(settings);
        settings.addActionListener(this);
        editMenu.add(compareImages);
        compareImages.addActionListener(this);

        // ecu def menu items
        add(definitionMenu);
        definitionMenu.setMnemonic('D');
        defManager.setMnemonic('D');
        //        editDefinition.setMnemonic('E');
        updateDefinition.setMnemonic('U');
        settings.setMnemonic('S');
        compareImages.setMnemonic('C');
        definitionMenu.add(defManager);
        //        definitionMenu.add(editDefinition);
        definitionMenu.add(updateDefinition);
        defManager.addActionListener(this);
        //        editDefinition.addActionListener(this);
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
        openLogger.addActionListener(this);

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
        //        editDefinition.setEnabled(false);
        updateMenu();
    }

    public void updateMenu() {
        String file = getLastSelectedRomFileName();
        if ("".equals(file)) {
            saveImage.setEnabled(false);
            saveAsRepository.setEnabled(false);
            closeImage.setEnabled(false);
            //closeAll.setEnabled(false);
            romProperties.setEnabled(false);
            saveImage.setText("Save As...");
            saveAsRepository.setText("Save As Repository...");
            compareImages.setEnabled(false);
        } else {
            saveImage.setEnabled(true);
            saveAsRepository.setEnabled(true);
            closeImage.setEnabled(true);
            //closeAll.setEnabled(true);
            romProperties.setEnabled(true);
            saveImage.setText("Save " + file + " As...");
            saveAsRepository.setText("Save "+ file +" As Repository...");
            compareImages.setEnabled(true);
        }
        refreshImage.setText("Refresh " + file);
        closeImage.setText("Close " + file);
        romProperties.setText(file + "Properties");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openImage) {
            try {
                openImageDialog();
            } catch (Exception ex) {
                showMessageDialog(parent,
                        new DebugPanel(ex, parent.getSettings().getSupportURL()), "Exception", ERROR_MESSAGE);
            }

        } else if (e.getSource() == openImages) {
            try {
                openImagesDialog();
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
        } else if (e.getSource() == saveAsRepository) {
            try {
                this.saveAsRepository(parent.getLastSelectedRom(), parent.getSettings().getLastRepositoryDir());
            } catch(Exception ex) {
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

        } else if (e.getSource() == compareImages){
            CompareImagesForm form = new CompareImagesForm(parent.getImages(), parent.getIconImage());
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
            parent.launchLogger();
        } else if (e.getSource() == updateDefinition) {
            BareBonesBrowserLaunch.openURL(ECU_DEFS_URL);

        } else if (e.getSource() == launchRamTuneTestApp) {
            RamTuneTestApp.startTestApp(DISPOSE_ON_CLOSE);

        } else if (e.getSource() == about) {
            //TODO:  change this to use com.romraider.swing.menubar.action.AboutAction
            String message = PRODUCT_NAME + " - ECU Editor\n"
                    + "Version: " + VERSION + "\n"
                    + "Build #: " + BUILDNUMBER + "\n"
                    + SUPPORT_URL;
            String title = "About " + PRODUCT_NAME;
            showMessageDialog(parent, message, title, INFORMATION_MESSAGE, ABOUT_ICON);
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
        fc.setDialogTitle("Open Image");

        if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            parent.openImage(fc.getSelectedFile());
            parent.getSettings().setLastImageDir(fc.getCurrentDirectory());
        }
    }

    public void openImagesDialog() throws Exception {
        JFileChooser fc = new JFileChooser(parent.getSettings().getLastImageDir());
        fc.setFileFilter(new ECUImageFilter());
        fc.setMultiSelectionEnabled(true);
        fc.setDialogTitle("Open Image(s)");

        if(fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            parent.openImages(fc.getSelectedFiles());
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
                File selectedFile = fc.getSelectedFile();
                if (selectedFile.exists()) {
                    int option = showConfirmDialog(parent, selectedFile.getName() + " already exists! Overwrite?");

                    // option: 0 = Cancel, 1 = No
                    if (option == CANCEL_OPTION || option == 1) {
                        save = false;
                    }
                }
                if (save) {
                    byte[] output = parent.getLastSelectedRom().saveFile();
                    FileOutputStream fos = new FileOutputStream(selectedFile);
                    try {
                        fos.write(output);
                    } finally {
                        fos.close();
                    }
                    parent.getLastSelectedRom().setFullFileName(selectedFile.getAbsoluteFile());
                    parent.setLastSelectedRom(parent.getLastSelectedRom());
                    parent.getSettings().setLastImageDir(selectedFile.getParentFile());
                }
            }
        }
    }

    private void saveAsRepository(Rom image, File lastRepositoryDir) throws Exception {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(lastRepositoryDir);
        fc.setDialogTitle("Select Repository Directory");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        // disable the "All files" option
        fc.setAcceptAllFileFilterUsed(false);
        String separator = System.getProperty("file.separator");

        if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            boolean save = true;
            File selectedDir = fc.getSelectedFile();
            if (selectedDir.exists()) {
                int option = showConfirmDialog(parent, selectedDir.getName() + " already exists! Overwrite?");

                // option: 0 = Cancel, 1 = No
                if (option == CANCEL_OPTION || option == 1) {
                    save = false;
                }
            }
            if(save) {
                Vector<Table> romTables = image.getTables();
                for(int i=0;i<romTables.size();i++) {
                    Table curTable = romTables.get(i);
                    String category = curTable.getCategory();
                    String tableName = curTable.getName();
                    String tableDirString = selectedDir.getAbsolutePath() + separator + category;
                    File tableDir = new File(tableDirString.replace('/', '-'));
                    tableDir.mkdirs();
                    String tableFileString = tableDir.getAbsolutePath() + separator + tableName+".txt";
                    File tableFile = new File(tableFileString.replace('/', '-'));
                    if(tableFile.exists())
                    {
                        tableFile.delete();
                    }
                    tableFile.createNewFile();
                    StringBuffer tableData = curTable.getTableAsString();
                    BufferedWriter out = new BufferedWriter(new FileWriter(tableFile));
                    try {
                        out.write(tableData.toString());
                    } finally {
                        out.close();
                    }
                }
                this.parent.getSettings().setLastRepositoryDir(selectedDir);
            }
        }
    }

    private String getLastSelectedRomFileName() {
        Rom lastSelectedRom = parent.getLastSelectedRom();
        return lastSelectedRom == null ? "" : lastSelectedRom.getFileName() + " ";
    }
}