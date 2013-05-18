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

package com.romraider.editor.ecu;

import static com.romraider.Version.ECU_DEFS_URL;
import static com.romraider.Version.PRODUCT_NAME;
import static com.romraider.Version.VERSION;
import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JOptionPane.showOptionDialog;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.tree.TreePath;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.centerkey.utils.BareBonesBrowserLaunch;
import com.romraider.Settings;
import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.ui.handler.table.TableUpdateHandler;
import com.romraider.maps.Rom;
import com.romraider.maps.Table;
import com.romraider.net.URL;
import com.romraider.swing.AbstractFrame;
import com.romraider.swing.CustomToolbarLayout;
import com.romraider.swing.ECUEditorMenuBar;
import com.romraider.swing.ECUEditorToolBar;
import com.romraider.swing.JProgressPane;
import com.romraider.swing.MDIDesktopPane;
import com.romraider.swing.RomTree;
import com.romraider.swing.RomTreeNode;
import com.romraider.swing.RomTreeRootNode;
import com.romraider.swing.TableFrame;
import com.romraider.swing.TableToolBar;
import com.romraider.util.SettingsManager;
import com.romraider.util.SettingsManagerImpl;
import com.romraider.xml.DOMRomUnmarshaller;
import com.romraider.xml.RomNotFoundException;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class ECUEditor extends AbstractFrame {
    private static final long serialVersionUID = -7826850987392016292L;

    private final String titleText = PRODUCT_NAME + " v" + VERSION + " | ECU Editor";

    private final SettingsManager settingsManager = new SettingsManagerImpl();
    private final RomTreeRootNode imageRoot = new RomTreeRootNode("Open Images");
    private final RomTree imageList = new RomTree(imageRoot);
    public MDIDesktopPane rightPanel = new MDIDesktopPane();
    public JProgressPane statusPanel = new JProgressPane();
    private JSplitPane splitPane = new JSplitPane();
    private Rom lastSelectedRom = null;
    private ECUEditorToolBar toolBar;
    private final ECUEditorMenuBar menuBar;
    private Settings settings;
    private final TableToolBar tableToolBar;
    private final JPanel toolBarPanel = new JPanel();
    private OpenImageWorker openImageWorker;
    private CloseImageWorker closeImageWorker;
    private SetUserLevelWorker setUserLevelWorker;
    private LaunchLoggerWorker launchLoggerWorker;
    private final ImageIcon editorIcon = new ImageIcon(getClass().getResource("/graphics/romraider-ico.gif"), "RomRaider ECU Editor");

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
        splitPane.setContinuousLayout(true);
        getContentPane().add(splitPane);

        rightPanel.setBackground(Color.BLACK);
        imageList.setScrollsOnExpand(true);

        //create menubar
        menuBar = new ECUEditorMenuBar(this);
        this.setJMenuBar(menuBar);
        this.add(statusPanel, BorderLayout.SOUTH);

        // create toolbars

        toolBar = new ECUEditorToolBar(this, "Editor Tools");

        tableToolBar = new TableToolBar("Table Tools", this);
        tableToolBar.updateTableToolBar(null);

        CustomToolbarLayout toolBarLayout = new CustomToolbarLayout(FlowLayout.LEFT, 0, 0);

        toolBarPanel.setLayout(toolBarLayout);
        toolBarPanel.add(toolBar);
        toolBarPanel.add(tableToolBar);
        toolBarPanel.setVisible(true);

        this.add(toolBarPanel, BorderLayout.NORTH);

        //set remaining window properties
        setIconImage(editorIcon.getImage());

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
                    sb.append(br.readLine()).append(Settings.NEW_LINE);
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

        // Save when exit to save file settings.
        settingsManager.save(settings, statusPanel);
        statusPanel.update("Ready...", 0);
        repaint();
    }

    @Override
    public void windowClosing(WindowEvent e) {
        handleExit();
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
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
        RomTreeNode romNode = new RomTreeNode(input, getSettings().getUserLevel(), getSettings().isDisplayHighTables());
        getImageRoot().add(romNode);

        getImageList().setVisible(true);
        getImageList().expandPath(new TreePath(getImageRoot()));

        getImageList().expandPath(new TreePath(romNode.getPath()));
        // uncomment collapsePath if you want ROM to open collapsed.
        // imageList.collapsePath(addedRomPath);

        getImageList().setRootVisible(false);
        getImageList().repaint();

        // Only set if no other rom has been selected.
        if(null == getLastSelectedRom()) {
            setLastSelectedRom(input);
        }

        if (input.getRomID().isObsolete() && getSettings().isObsoleteWarning()) {
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new GridLayout(3, 1));
            infoPanel.add(new JLabel("A newer version of this ECU revision exists. " +
                    "Please visit the following link to download the latest revision:"));
            infoPanel.add(new URL(getSettings().getRomRevisionURL()));

            JCheckBox check = new JCheckBox("Always display this message", true);
            check.setHorizontalAlignment(JCheckBox.RIGHT);

            check.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    settings.setObsoleteWarning(((JCheckBox) e.getSource()).isSelected());
                }
            });

            infoPanel.add(check);
            showMessageDialog(this, infoPanel, "ECU Revision is Obsolete", INFORMATION_MESSAGE);
        }
        input.applyTableColorSettings();
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

        updateTableToolBar(null);

        rightPanel.remove(frame);
        rightPanel.repaint();
    }

    public void closeImage() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        closeImageWorker = new CloseImageWorker();
        closeImageWorker.addPropertyChangeListener(getStatusPanel());
        closeImageWorker.execute();
    }

    public void closeAllImages() {
        while (imageRoot.getChildCount() > 0) {
            closeImage();
        }
    }

    public Rom getLastSelectedRom() {
        return lastSelectedRom;
    }

    public String getLastSelectedRomFileName() {
        Rom lastSelectedRom = getLastSelectedRom();
        return lastSelectedRom == null ? "" : lastSelectedRom.getFileName() + " ";
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
    }

    public ECUEditorToolBar getToolBar() {
        return toolBar;
    }

    public void setToolBar(ECUEditorToolBar toolBar) {
        this.toolBar = toolBar;
    }

    public ECUEditorMenuBar getEditorMenuBar() {
        return menuBar;
    }

    public TableToolBar getTableToolBar() {
        return tableToolBar;
    }

    public void updateTableToolBar(Table currentTable) {
        tableToolBar.updateTableToolBar(currentTable);
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
        for (int i = 0; i < imageRoot.getChildCount(); i++) {
            RomTreeNode rtn = (RomTreeNode) imageRoot.getChildAt(i);
            rtn.getRom().applyTableColorSettings();
        }
    }

    public void setUserLevel(int userLevel) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setUserLevelWorker = new SetUserLevelWorker(userLevel);
        setUserLevelWorker.addPropertyChangeListener(getStatusPanel());
        setUserLevelWorker.execute();
    }

    public Vector<Rom> getImages() {
        Vector<Rom> images = new Vector<Rom>();
        for (int i = 0; i < imageRoot.getChildCount(); i++) {
            RomTreeNode rtn = (RomTreeNode) imageRoot.getChildAt(i);
            images.add(rtn.getRom());
        }
        return images;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        imageList.updateUI();
        imageList.repaint();
        rightPanel.updateUI();
        rightPanel.repaint();
    }

    public void refreshUI()
    {
        getToolBar().updateButtons();
        getEditorMenuBar().updateMenu();
        refreshTableMenus();

        imageList.updateUI();
        imageList.repaint();
        rightPanel.updateUI();
        rightPanel.repaint();
    }

    public void openImage(File inputFile) throws Exception {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        openImageWorker = new OpenImageWorker(inputFile);
        openImageWorker.addPropertyChangeListener(getStatusPanel());
        openImageWorker.execute();
    }

    public void openImages(File[] inputFiles) throws Exception {
        if(inputFiles.length < 1) {
            showMessageDialog(this, "Image Not Found", "Error Loading Image(s)", ERROR_MESSAGE);
            return;
        }
        for(int j = 0; j < inputFiles.length; j++) {
            openImage(inputFiles[j]);
        }
    }

    public byte[] readFile(File inputFile) throws IOException {
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

    public void launchLogger() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        launchLoggerWorker = new LaunchLoggerWorker();
        launchLoggerWorker.addPropertyChangeListener(getStatusPanel());
        launchLoggerWorker.execute();
    }

    public SettingsManager getSettingsManager() {
        return this.settingsManager;
    }

    public RomTreeRootNode getImageRoot() {
        return imageRoot;
    }

    public RomTree getImageList() {
        return imageList;
    }

    public JProgressPane getStatusPanel() {
        return this.statusPanel;
    }

    public MDIDesktopPane getRightPanel() {
        return this.rightPanel;
    }

    public void refreshTableMenus() {
        for(Rom rom : getImages()) {
            for(Table table : rom.getTables()) {
                table.getFrame().getTableMenuBar().refreshTableMenuBar();
            }
        }
    }
}

class LaunchLoggerWorker extends SwingWorker<Void, Void> {
    private final ECUEditor editor = ECUEditorManager.getECUEditor();

    public LaunchLoggerWorker() {
    }

    @Override
    protected Void doInBackground() throws Exception {
        editor.getStatusPanel().setStatus("Launching Logger...");
        setProgress(10);
        EcuLogger.startLogger(javax.swing.WindowConstants.DISPOSE_ON_CLOSE, editor);
        return null;
    }

    public void propertyChange(PropertyChangeEvent evnt)
    {
        SwingWorker source = (SwingWorker) evnt.getSource();
        if (null != source && "state".equals( evnt.getPropertyName() )
                && (source.isDone() || source.isCancelled() ) )
        {
            source.removePropertyChangeListener(editor.getStatusPanel());
        }
    }

    @Override
    public void done() {
        editor.getStatusPanel().setStatus("Ready...");
        setProgress(0);
        editor.setCursor(null);
        editor.refreshUI();
    }
}

class SetUserLevelWorker extends SwingWorker<Void, Void> {
    private final ECUEditor editor = ECUEditorManager.getECUEditor();
    int userLevel;

    public SetUserLevelWorker(int userLevel) {
        this.userLevel = userLevel;
    }

    @Override
    protected Void doInBackground() throws Exception {
        Settings settings = editor.getSettings();
        RomTreeRootNode imageRoot = editor.getImageRoot();

        settings.setUserLevel(userLevel);
        imageRoot.setUserLevel(userLevel, settings.isDisplayHighTables());
        return null;
    }

    public void propertyChange(PropertyChangeEvent evnt)
    {
        SwingWorker source = (SwingWorker) evnt.getSource();
        if (null != source && "state".equals( evnt.getPropertyName() )
                && (source.isDone() || source.isCancelled() ) )
        {
            source.removePropertyChangeListener(editor.getStatusPanel());
        }
    }

    @Override
    public void done() {
        editor.getStatusPanel().setStatus("Ready...");
        setProgress(0);
        editor.setCursor(null);
        editor.refreshUI();
    }
}

class CloseImageWorker extends SwingWorker<Void, Void> {

    private final ECUEditor editor = ECUEditorManager.getECUEditor();

    public CloseImageWorker() {
    }

    @Override
    protected Void doInBackground() throws Exception {
        RomTreeRootNode imageRoot = editor.getImageRoot();
        RomTree imageList = editor.getImageList();

        for (int i = 0; i < imageRoot.getChildCount(); i++) {
            RomTreeNode romTreeNode = (RomTreeNode) imageRoot.getChildAt(i);
            Rom rom = romTreeNode.getRom();
            if (rom == editor.getLastSelectedRom()) {
                for (Table t : rom.getTables()) {
                    editor.getRightPanel().remove(t.getFrame());
                    TableUpdateHandler.getInstance().deregisterTable(t);
                }

                // Cleanup Rom Data
                rom.clearData();

                Vector<TreePath> path = new Vector<TreePath>();
                path.add(new TreePath(romTreeNode.getPath()));
                imageRoot.remove(i);
                imageList.removeDescendantToggledPaths(path.elements());

                path.clear();

                break;
            }
        }

        if (imageRoot.getChildCount() > 0) {
            editor.setLastSelectedRom(((RomTreeNode) imageRoot.getChildAt(0)).getRom());
        } else {
            // no other images open
            editor.setLastSelectedRom(null);
        }
        return null;
    }

    @Override
    public void done() {
        editor.getStatusPanel().setStatus("Ready...");
        setProgress(0);
        editor.setCursor(null);
        editor.refreshUI();
        System.gc();
    }
}

class OpenImageWorker extends SwingWorker<Void, Void> {

    private final ECUEditor editor = ECUEditorManager.getECUEditor();
    private final File inputFile;

    public OpenImageWorker(File inputFile) {
        this.inputFile = inputFile;
    }

    @Override
    protected Void doInBackground() throws Exception {
        try {
            Settings settings = editor.getSettings();

            editor.getStatusPanel().setStatus("Parsing ECU definitions...");
            setProgress(0);

            byte[] input = editor.readFile(inputFile);
            DOMParser parser = new DOMParser();

            editor.getStatusPanel().setStatus("Finding ECU definition...");
            setProgress(10);

            // parse ecu definition files until result found
            for (int i = 0; i < settings.getEcuDefinitionFiles().size(); i++) {
                FileInputStream fileStream = new FileInputStream(settings.getEcuDefinitionFiles().get(i));
                InputSource src = new InputSource(fileStream);

                parser.parse(src);
                Document doc = parser.getDocument();

                try {
                    Rom rom = new DOMRomUnmarshaller().unmarshallXMLDefinition(doc.getDocumentElement(), input, editor.getStatusPanel());
                    editor.getStatusPanel().setStatus("Populating tables...");
                    setProgress(50);

                    rom.populateTables(input, editor.getStatusPanel());
                    rom.setFileName(inputFile.getName());

                    editor.getStatusPanel().setStatus("Finalizing...");
                    setProgress(75);

                    editor.addRom(rom);
                    rom.setFullFileName(inputFile);

                    editor.getStatusPanel().setStatus("Done loading image...");
                    setProgress(100);
                    parser.reset();
                    try{
                        fileStream.close();
                    } catch(IOException ioex) {
                        ;// Do nothing
                    }
                    return null;

                } catch (RomNotFoundException ex) {
                    // rom was not found in current file, skip to next
                }
                parser = null;
                doc.removeChild(doc.getDocumentElement());
                doc = null;
            }

            // if code executes to this point, no ROM was found, report to user
            showMessageDialog(editor, "ECU Definition Not Found", "Error Loading " + inputFile.getName(), ERROR_MESSAGE);

        } catch (SAXParseException spe) {
            // catch general parsing exception - enough people don't unzip the defs that a better error message is in order
            showMessageDialog(editor, "Unable to read XML definitions.  Please make sure the definition file is correct.  If it is in a ZIP archive, unzip the file and try again.", "Error Loading " + inputFile.getName(), ERROR_MESSAGE);

        } catch (StackOverflowError ex) {
            // handles looped inheritance, which will use up all available memory
            showMessageDialog(editor, "Looped \"base\" attribute in XML definitions.", "Error Loading " + inputFile.getName(), ERROR_MESSAGE);

        } catch (OutOfMemoryError ome) {
            // handles Java heap space issues when loading multiple Roms.
            showMessageDialog(editor, "Error loading Image. Out of memeory.", "Error Loading " + inputFile.getName(), ERROR_MESSAGE);

        }
        return null;
    }

    public void propertyChange(PropertyChangeEvent evnt)
    {
        SwingWorker source = (SwingWorker) evnt.getSource();
        if (null != source && "state".equals( evnt.getPropertyName() )
                && (source.isDone() || source.isCancelled() ) )
        {
            source.removePropertyChangeListener(editor.getStatusPanel());
        }
    }

    @Override
    public void done() {
        editor.getStatusPanel().setStatus("Ready...");
        setProgress(0);
        editor.setCursor(null);
        editor.refreshUI();
        System.gc();
    }
}
