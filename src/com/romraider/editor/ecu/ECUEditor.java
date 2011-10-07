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

package com.romraider.editor.ecu;

import com.centerkey.utils.BareBonesBrowserLaunch;
import com.romraider.Settings;
import static com.romraider.Version.ECU_DEFS_URL;
import static com.romraider.Version.PRODUCT_NAME;
import static com.romraider.Version.VERSION;
import com.romraider.logger.ecu.ui.handler.table.TableUpdateHandler;
import com.romraider.maps.Rom;
import com.romraider.maps.Table;
import com.romraider.net.URL;
import com.romraider.swing.AbstractFrame;
import com.romraider.swing.ECUEditorMenuBar;
import com.romraider.swing.ECUEditorToolBar;
import com.romraider.swing.JProgressPane;
import com.romraider.swing.MDIDesktopPane;
import com.romraider.swing.RomTree;
import com.romraider.swing.RomTreeNode;
import com.romraider.swing.RomTreeRootNode;
import com.romraider.swing.TableFrame;
import com.romraider.util.SettingsManager;
import com.romraider.util.SettingsManagerImpl;
import com.romraider.xml.DOMRomUnmarshaller;
import com.romraider.xml.RomNotFoundException;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JOptionPane.showOptionDialog;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
import static javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class ECUEditor extends AbstractFrame {
    private static final long serialVersionUID = -7826850987392016292L;

    private String titleText = PRODUCT_NAME + " v" + VERSION + " | ECU Editor";

    private static final String NEW_LINE = System.getProperty("line.separator");
    private final SettingsManager settingsManager = new SettingsManagerImpl();
    private RomTreeRootNode imageRoot = new RomTreeRootNode("Open Images");
    private RomTree imageList = new RomTree(imageRoot);
    public MDIDesktopPane rightPanel = new MDIDesktopPane();
    public JProgressPane statusPanel = new JProgressPane();
    private JSplitPane splitPane = new JSplitPane();
    private Rom lastSelectedRom = null;
    private ECUEditorToolBar toolBar;
    private ECUEditorMenuBar menuBar;
    private Settings settings;

    public ECUEditor() {

        // get settings from xml
        settings = settingsManager.load();

        if (!settings.getRecentVersion().equalsIgnoreCase(VERSION)) {
            showReleaseNotes();
        }

        setSize(getSettings().getWindowSize());
        setLocation(getSettings().getWindowLocation());
        if (getSettings().isWindowMaximized()) {
            setExtendedState(MAXIMIZED_BOTH);
        }

        JScrollPane rightScrollPane = new JScrollPane(rightPanel,
                VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane leftScrollPane = new JScrollPane(imageList,
                VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightScrollPane);
        splitPane.setDividerSize(3);
        splitPane.setDividerLocation(getSettings().getSplitPaneLocation());
        splitPane.addPropertyChangeListener(this);

        getContentPane().add(splitPane);
        rightPanel.setBackground(Color.BLACK);
        imageList.setScrollsOnExpand(true);
        imageList.setContainer(this);

        //create menubar and toolbar
        menuBar = new ECUEditorMenuBar(this);
        this.setJMenuBar(menuBar);
        toolBar = new ECUEditorToolBar(this);
        this.add(toolBar, BorderLayout.NORTH);
        this.add(statusPanel, BorderLayout.SOUTH);

        //set remaining window properties
        setIconImage(new ImageIcon("./graphics/romraider-ico.gif").getImage());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(this);
        setTitle(titleText);
        setVisible(true);

        if (settings.getEcuDefinitionFiles().size() <= 0) {
            // no ECU definitions configured - let user choose to get latest or configure later
            Object[] options = {"Yes", "No"};
            int answer = showOptionDialog(null,
                    "ECU definitions not configured.\nGo online to download the latest definition files?",
                    "Editor Configuration",
                    DEFAULT_OPTION,
                    WARNING_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (answer == 0) {
                BareBonesBrowserLaunch.openURL(ECU_DEFS_URL);
            } else {
                showMessageDialog(this,
                        "ECU definition files need to be configured before ROM images can be opened.\nMenu: ECU Definitions > ECU Definition Manager...",
                        "Editor Configuration",
                        INFORMATION_MESSAGE);
            }
        }

    }

    private void showReleaseNotes() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(settings.getReleaseNotes()));
            try {
                // new version being used, display release notes
                JTextArea releaseNotes = new JTextArea();
                releaseNotes.setEditable(false);
                releaseNotes.setWrapStyleWord(true);
                releaseNotes.setLineWrap(true);
                releaseNotes.setFont(new Font("Tahoma", Font.PLAIN, 12));

                StringBuffer sb = new StringBuffer();
                while (br.ready()) {
                    sb.append(br.readLine()).append(NEW_LINE);
                }
                releaseNotes.setText(sb.toString());
                releaseNotes.setCaretPosition(0);

                JScrollPane scroller = new JScrollPane(releaseNotes, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);
                scroller.setPreferredSize(new Dimension(600, 500));

                showMessageDialog(this, scroller,
                        PRODUCT_NAME + VERSION + " Release Notes", INFORMATION_MESSAGE);
            } finally {
                br.close();
            }
        } catch (Exception e) {
            /* Ignore */
        }
    }

    public void handleExit() {
        settings.setSplitPaneLocation(splitPane.getDividerLocation());
        settings.setWindowMaximized(getExtendedState() == MAXIMIZED_BOTH);
        settings.setWindowSize(getSize());
        settings.setWindowLocation(getLocation());

        settingsManager.save(settings, statusPanel);
        statusPanel.update("Ready...", 0);
        repaint();
    }

    public void windowClosing(WindowEvent e) {
        handleExit();
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public String getVersion() {
        return VERSION;
    }

    public Settings getSettings() {
        return settings;
    }

    public void addRom(Rom input) {
        // add to ecu image list pane
        RomTreeNode romNode = new RomTreeNode(input, settings.getUserLevel(), settings.isDisplayHighTables());
        imageList.setRootVisible(true);
        imageRoot.add(romNode);
        imageList.updateUI();

        imageList.expandRow(imageList.getRowCount() - 1);
        imageList.updateUI();
        setLastSelectedRom(input);

        if (input.getRomID().isObsolete() && settings.isObsoleteWarning()) {
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new GridLayout(3, 1));
            infoPanel.add(new JLabel("A newer version of this ECU revision exists. " +
                    "Please visit the following link to download the latest revision:"));
            infoPanel.add(new URL(settings.getRomRevisionURL()));

            JCheckBox check = new JCheckBox("Always display this message", true);
            check.setHorizontalAlignment(JCheckBox.RIGHT);

            check.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    settings.setObsoleteWarning(((JCheckBox) e.getSource()).isSelected());
                }
            }
            );

            infoPanel.add(check);
            showMessageDialog(this, infoPanel, "ECU Revision is Obsolete", INFORMATION_MESSAGE);
        }
        input.setContainer(this);
        imageList.setRootVisible(false);
        imageList.updateUI();
    }

    public void displayTable(TableFrame frame) {
        frame.setVisible(true);
        try {
            rightPanel.add(frame);
        } catch (IllegalArgumentException ex) {
            // table is already open, so set focus
            frame.requestFocus();
        }
        //frame.setSize(frame.getTable().getFrameSize());
        frame.pack();
        rightPanel.repaint();
    }

    public void removeDisplayTable(TableFrame frame) {
        frame.setVisible(false);
        rightPanel.remove(frame);
        rightPanel.repaint();
    }

    public void closeImage() {
        for (int i = 0; i < imageRoot.getChildCount(); i++) {
            RomTreeNode romTreeNode = (RomTreeNode) imageRoot.getChildAt(i);
            Rom rom = romTreeNode.getRom();
            if (rom == lastSelectedRom) {
                Vector<Table> romTables = rom.getTables();
                for (Table t : romTables) {
                    rightPanel.remove(t.getFrame());
                    TableUpdateHandler.getInstance().deregisterTable(t);
                }

                Vector<TreePath> path = new Vector<TreePath>();
                path.add(new TreePath(romTreeNode.getPath()));
                imageRoot.remove(i);
                imageList.removeDescendantToggledPaths(path.elements());

                break;
            }
        }

        imageList.updateUI();

        if (imageRoot.getChildCount() > 0) {
            setLastSelectedRom(((RomTreeNode) imageRoot.getChildAt(0)).getRom());
        } else {
            // no other images open
            setLastSelectedRom(null);
        }
        rightPanel.repaint();
    }

    public void closeAllImages() {
        while (imageRoot.getChildCount() > 0) {
            closeImage();
        }
    }

    public Rom getLastSelectedRom() {
        return lastSelectedRom;
    }

    public void setLastSelectedRom(Rom lastSelectedRom) {
        this.lastSelectedRom = lastSelectedRom;
        if (lastSelectedRom == null) {
            setTitle(titleText);
        } else {
            setTitle(titleText + " - " + lastSelectedRom.getFileName());
        }

        // update filenames
        for (int i = 0; i < imageRoot.getChildCount(); i++) {
            ((RomTreeNode) imageRoot.getChildAt(i)).updateFileName();
        }

        toolBar.updateButtons();
        menuBar.updateMenu();
        imageList.updateUI();
    }

    public ECUEditorToolBar getToolBar() {
        return toolBar;
    }

    public void setToolBar(ECUEditorToolBar toolBar) {
        this.toolBar = toolBar;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
        for (int i = 0; i < imageRoot.getChildCount(); i++) {
            RomTreeNode rtn = (RomTreeNode) imageRoot.getChildAt(i);
            rtn.getRom().setContainer(this);
        }
    }

    public void setUserLevel(int userLevel) {
        settings.setUserLevel(userLevel);
        imageRoot.setUserLevel(userLevel, settings.isDisplayHighTables());
        imageList.updateUI();
    }

    public Vector<Rom> getImages() {
        Vector<Rom> images = new Vector<Rom>();
        for (int i = 0; i < imageRoot.getChildCount(); i++) {
            RomTreeNode rtn = (RomTreeNode) imageRoot.getChildAt(i);
            images.add(rtn.getRom());
        }
        return images;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        imageList.updateUI();
        imageList.repaint();
    }

    public void openImage(File inputFile) throws Exception {

        try {
            update(getGraphics());
            statusPanel.update("Parsing ECU definitions...", 0);
            repaint();

            byte[] input = readFile(inputFile);
            DOMRomUnmarshaller domUms = new DOMRomUnmarshaller(settings, this);
            DOMParser parser = new DOMParser();
            statusPanel.update("Finding ECU definition...", 10);
            repaint();
            Rom rom;

            // parse ecu definition files until result found
            for (int i = 0; i < settings.getEcuDefinitionFiles().size(); i++) {
                InputSource src = new InputSource(new FileInputStream(settings.getEcuDefinitionFiles().get(i)));

                parser.parse(src);
                Document doc = parser.getDocument();

                try {
                    rom = domUms.unmarshallXMLDefinition(doc.getDocumentElement(), input, statusPanel);
                    statusPanel.update("Populating tables...", 50);
                    repaint();
                    rom.populateTables(input, statusPanel);
                    rom.setFileName(inputFile.getName());

                    statusPanel.update("Finalizing...", 90);
                    repaint();
                    addRom(rom);
                    rom.setFullFileName(inputFile);
                    return;

                } catch (RomNotFoundException ex) {
                    // rom was not found in current file, skip to next
                }
            }

            // if code executes to this point, no ROM was found, report to user
            showMessageDialog(this, "ECU Definition Not Found", "Error Loading " + inputFile.getName(), ERROR_MESSAGE);

        } catch (SAXParseException spe) {
            // catch general parsing exception - enough people don't unzip the defs that a better error message is in order
            showMessageDialog(this, "Unable to read XML definitions.  Please make sure the definition file is correct.  If it is in a ZIP archive, unzip the file and try again.", "Error Loading " + inputFile.getName(), ERROR_MESSAGE);

        } catch (StackOverflowError ex) {
            // handles looped inheritance, which will use up all available memory
            showMessageDialog(this, "Looped \"base\" attribute in XML definitions.", "Error Loading ROM", ERROR_MESSAGE);

        } finally {
            // remove progress bar
            //progress.dispose();
            statusPanel.update("Ready...", 0);

        }
    }

    private byte[] readFile(File inputFile) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(inputFile);
        try {
            byte[] buf = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buf)) != -1) {
                baos.write(buf, 0, bytesRead);
            }
        } finally {
            fis.close();
        }
        return baos.toByteArray();
    }


}