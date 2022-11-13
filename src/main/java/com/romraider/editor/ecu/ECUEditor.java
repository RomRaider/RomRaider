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
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;

import com.romraider.ECUExec;
import com.romraider.Settings;
import com.romraider.logger.ecu.EcuLogger;
import com.romraider.maps.Rom;
import com.romraider.maps.Table;
import com.romraider.maps.Table1D;
import com.romraider.maps.Table1DView;
import com.romraider.maps.Table1DView.Table1DType;
import com.romraider.maps.Table2D;
import com.romraider.maps.Table2DView;
import com.romraider.maps.Table3D;
import com.romraider.maps.Table3DView;
import com.romraider.maps.TableBitwiseSwitch;
import com.romraider.maps.TableBitwiseSwitchView;
import com.romraider.maps.TableSwitch;
import com.romraider.maps.TableSwitchView;
import com.romraider.maps.TableView;
import com.romraider.net.BrowserControl;
import com.romraider.net.URL;
import com.romraider.swing.AbstractFrame;
import com.romraider.swing.CustomToolbarLayout;
import com.romraider.swing.DebugPanel;
import com.romraider.swing.ECUEditorMenuBar;
import com.romraider.swing.ECUEditorToolBar;
import com.romraider.swing.JProgressPane;
import com.romraider.swing.MDIDesktopPane;
import com.romraider.swing.RomTree;
import com.romraider.swing.RomTreeRootNode;
import com.romraider.swing.TableFrame;
import com.romraider.swing.TableToolBar;
import com.romraider.swing.TableTreeNode;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;
import com.romraider.util.ThreadUtil;
import com.romraider.xml.ConversionLayer.ConversionLayer;

public class ECUEditor extends AbstractFrame {
    private static final long serialVersionUID = -7826850987392016292L;
    protected static final ResourceBundle rb = new ResourceUtil().getBundle(
            ECUEditor.class.getName());

    private final String titleText = MessageFormat.format(
            rb.getString("TITLE"), PRODUCT_NAME, VERSION);

    private final RomTreeRootNode imageRoot = new RomTreeRootNode(
            rb.getString("OPENIMAGES"));
    private final RomTree imageList = new RomTree(imageRoot);
    private final MDIDesktopPane rightPanel = new MDIDesktopPane();
    private final JProgressPane statusPanel = new JProgressPane();
    private final JScrollPane leftScrollPane;
    private final JScrollPane rightScrollPane;
    private JSplitPane splitPane = new JSplitPane();
    private Rom lastSelectedRom = null;
    private ECUEditorToolBar toolBar;
    private ECUEditorMenuBar menuBar;
    private TableToolBar tableToolBar;
    private final JPanel toolBarPanel = new JPanel();
    private OpenImageWorker openImageWorker;
    private SetUserLevelWorker setUserLevelWorker;
    private final ImageIcon editorIcon = new ImageIcon(getClass().getResource(
            "/graphics/romraider-ico.gif"), rb.getString("RRECUED"));
    private final Settings settings = SettingsManager.getSettings();

    public ECUEditor() {
        if (!settings.getRecentVersion().equalsIgnoreCase(VERSION)) {
            showReleaseNotes();
        }

        setSize(settings.getWindowSize());
        setLocation(settings.getWindowLocation());
        if (settings.isWindowMaximized()) {
            setExtendedState(MAXIMIZED_BOTH);
        }

        rightScrollPane = new JScrollPane(rightPanel,
                VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        leftScrollPane = new JScrollPane(imageList,
                VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftScrollPane, rightScrollPane);
        splitPane.setDividerSize(3);
        splitPane.setDividerLocation(settings.getSplitPaneLocation());
        splitPane.addPropertyChangeListener(this);
        splitPane.setContinuousLayout(true);
        getContentPane().add(splitPane);

        rightPanel.setBackground(Color.BLACK);
        imageList.setScrollsOnExpand(true);

        this.add(statusPanel, BorderLayout.SOUTH);

        //set remaining window properties
        setIconImage(editorIcon.getImage());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(this);
        setTitle(titleText);
        setVisible(true);
        toFront();
    }

    public void initializeEditorUI() {
        //create menubar
        if(menuBar != null && toolBar != null) return;

        menuBar = new ECUEditorMenuBar();
        this.setJMenuBar(menuBar);

        // create toolbars
        toolBar = new ECUEditorToolBar(rb.getString("EDTOOLS"));

        tableToolBar = new TableToolBar();

        CustomToolbarLayout toolBarLayout = new CustomToolbarLayout(
                FlowLayout.LEFT, 0, 0);

        toolBarPanel.setLayout(toolBarLayout);
        toolBarPanel.add(toolBar);
        toolBarPanel.add(tableToolBar);
        toolBarPanel.setVisible(true);

        this.add(toolBarPanel, BorderLayout.NORTH);
        validate();
    }

    public void checkDefinitions() {
        if (settings.getEcuDefinitionFiles().size() <= 0) {
            // no ECU definitions configured - let user choose to get latest or configure later
            Object[] options = {rb.getString("YES"), rb.getString("NO")};
            int answer = showOptionDialog(null,
                    rb.getString("ECUDEFNOTCFG"),
                    rb.getString("EDCONFIG"),
                    DEFAULT_OPTION,
                    WARNING_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (answer == 0) {
                BrowserControl.displayURL(ECU_DEFS_URL);
            } else {
                showMessageDialog(this,
                        rb.getString("CFGEDFSMENU"),
                        rb.getString("EDCONFIG"),
                        INFORMATION_MESSAGE);
            }
        }
    }

    private void showReleaseNotes() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    settings.getReleaseNotes()));
            try {
                // new version being used, display release notes
                JTextArea releaseNotes = new JTextArea();
                releaseNotes.setEditable(false);
                releaseNotes.setWrapStyleWord(true);
                releaseNotes.setLineWrap(true);
                releaseNotes.setFont(new Font(rb.getString("RELEASENOTESFONT"),
                        Font.PLAIN, 12));

                StringBuffer sb = new StringBuffer();
                while (br.ready()) {
                    sb.append(br.readLine()).append(Settings.NEW_LINE);
                }
                releaseNotes.setText(sb.toString());
                releaseNotes.setCaretPosition(0);

                JScrollPane scroller = new JScrollPane(releaseNotes,
                        VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);
                scroller.setPreferredSize(new Dimension(600, 500));

                showMessageDialog(this, scroller,
                        PRODUCT_NAME + VERSION + " " + rb.getString("RELEASENOTES"),
                        INFORMATION_MESSAGE);
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
        SettingsManager.save(settings, statusPanel);
        statusPanel.update(rb.getString("STATUSREADY"), 0);
        repaint();

        if(EcuLogger.getEcuLoggerWithoutCreation()== null) {
            System.exit(0);
        }
        else{
            ECUEditorManager.clearECUEditor();
            EcuLogger.getEcuLoggerWithoutCreation().setEcuEditor(null);
        }
    }

    public void handleExportDefinition() {
        Rom r = getLastSelectedRom();

        if(null != r) {
            JFileChooser fileChooser = new JFileChooser(settings.getLastDefinitionDir());
            fileChooser.setFileFilter(new FileNameExtensionFilter("Editor Definition (.xml)","xml"));
            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                if(!fileToSave.getName().endsWith(".xml") ||!fileToSave.getName().endsWith(".XML"))
                        fileToSave = new File(fileToSave.getAbsoluteFile() + ".xml");

                String s = ConversionLayer.convertDocumentToString(r.getDocument());

                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave));
                    writer.write(s);
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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

    public void addRom(Rom input) {
        input.refreshDisplayedTables();

        // add to ecu image list pane
        getImageRoot().add(input);

        getImageList().setVisible(true);
        getImageList().expandPath(new TreePath(getImageRoot()));
        getImageList().expandPath(new TreePath(input.getPath()));

        if(!settings.isOpenExpanded()) {
            imageList.collapsePath(new TreePath(input.getPath()));
        }

        getImageList().setRootVisible(false);
        getImageList().repaint();

        // Only set if no other rom has been selected.
        if(null == getLastSelectedRom()) {
            setLastSelectedRom(input);
        }

        if (input.getRomID().isObsolete() && settings.isObsoleteWarning()) {
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new GridLayout(3, 1));
            infoPanel.add(new JLabel(rb.getString("OBSOLETEROM")));
            infoPanel.add(new URL(settings.getRomRevisionURL()));

            JCheckBox check = new JCheckBox(rb.getString("DISPLAYMSG"), true);
            check.setHorizontalAlignment(JCheckBox.RIGHT);

            check.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    settings.setObsoleteWarning(
                            ((JCheckBox) e.getSource()).isSelected());
                }
            });

            infoPanel.add(check);
            showMessageDialog(this, infoPanel,
                    rb.getString("ISOBSOLETE"),
                    INFORMATION_MESSAGE);
        }
    }
    
    private void handleAlreadyOpenTable(TableFrame frame)
    {
        // table is already open.
        if(1 == settings.getTableClickBehavior()) { // open/focus frame
            // table is already open, so set focus on the frame.
            boolean selected = true;
            frame.toFront();
            try {
                frame.setSelected(true);
            } catch (PropertyVetoException e) {
                frame.toBack();
                selected = false;
            }
            if(selected) {
                frame.requestFocusInWindow();
            }
        } else {
        	// default to open/close frame
            // table is already open, so close the frame.
            rightPanel.remove(frame);
            frame.getTable().setTableFrame(null);
            try {
                frame.setClosed(true);
            } catch (PropertyVetoException e) {
                 // Do nothing.
            }
            frame.dispose();
        }
    }
    
    private void openClosedTable(TableTreeNode node)
    {
        Table t = node.getTable();
        TableView v = null;
        try {
            if (t != null) {
                if(t instanceof TableSwitch)
                    v = new TableSwitchView((TableSwitch)t);
                else if(t instanceof TableBitwiseSwitch)
                    v = new TableBitwiseSwitchView((TableBitwiseSwitch)t);
                else if(t instanceof Table1D)
                    v = new Table1DView((Table1D)node.getTable(), Table1DType.NO_AXIS);
                else if(t instanceof Table2D)
                    v = new Table2DView((Table2D)t);
                else if(t instanceof Table3D)
                    v = new Table3DView((Table3D)t);
                else
                    return;
                
    	        v.populateTableVisual();
            	v.drawTable();
            	
                Rom rom = RomTree.getRomNode(node);
                TableFrame frame = new TableFrame(node.getTable().getName() + " | " + rom.getFileName(), v);
    	        frame.pack();
            	          	
    	        rightPanel.add(frame);
            }
        }
        catch(Exception e) {
            final String msg = MessageFormat.format(
                    rb.getString("POPULATEFAIL"), t.getName(),
                    e.toString());
            final Exception ex = new Exception(msg);
            showMessageDialog(this,
                    new DebugPanel(ex, settings.getSupportURL()),
                    rb.getString("EXCEPTION"),
                    ERROR_MESSAGE);
        }
    	
    }
    
    public void displayTable(TableTreeNode node) {

        TableFrame frame = node.getFrame();

        // check if frame has been added.
        if (frame != null)
        {
        	handleAlreadyOpenTable(frame);
        }
        else
        {
        	openClosedTable(node);
        }
        
        rightPanel.repaint();
        refreshTableCompareMenus();        
    }

    public void removeDisplayTable(TableFrame frame) {
        frame.setVisible(false);
        rightPanel.remove(frame);
        rightPanel.validate();
        refreshUI();
    }

    public void closeImage() {
        Rom rom = getLastSelectedRom();
        ECUEditor editor = ECUEditorManager.getECUEditor();
        RomTreeRootNode imageRoot = editor.getImageRoot();

        rom.removeFromParent();

        if (imageRoot.getChildCount() > 0) {
            editor.setLastSelectedRom((Rom) imageRoot.getChildAt(0));
        } else {
            editor.setLastSelectedRom(null);
        }

        editor.getStatusPanel().setStatus(ECUEditor.rb.getString("STATUSREADY"));
        editor.setCursor(null);
        editor.refreshAfterNewRom();

        rom.clearData();
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
        Rom lastSelRom = getLastSelectedRom();
        return lastSelRom == null ? "" : lastSelRom.getFileName();
    }

    public void setLastSelectedRom(Rom lastSelectedRom) {
        this.lastSelectedRom = lastSelectedRom;
        if (lastSelectedRom == null) {
            setTitle(titleText);
        } else {
            setTitle(titleText + " - " + lastSelectedRom.getFileName());
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

    public void redrawVisableTables(Settings settings) {

    }

    public void setUserLevel(int userLevel) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        settings.setUserLevel(userLevel);
        setUserLevelWorker = new SetUserLevelWorker();
        setUserLevelWorker.addPropertyChangeListener(getStatusPanel());
        setUserLevelWorker.execute();
    }

    public Vector<Rom> getImages() {
        Vector<Rom> images = new Vector<Rom>();
        for (int i = 0; i < imageRoot.getChildCount(); i++) {
            if(imageRoot.getChildAt(i) instanceof Rom) {
                Rom rom = (Rom) imageRoot.getChildAt(i);
                if(null != rom) {
                    images.add(rom);
                }
            }
        }
        return images;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        refreshUI();
    }

    public void refreshUI()
    {
        imageList.updateUI();
        imageList.repaint();
        rightPanel.updateUI();
        rightPanel.repaint();

        if(getToolBar() != null)
            getToolBar().updateButtons();
        if(getEditorMenuBar() != null)
            getEditorMenuBar().updateMenu();
    }

    public void refreshAfterNewRom() {
        refreshTableCompareMenus();
        refreshUI();
    }

    public void refreshTableCompareMenus() {
        for(JInternalFrame curFrame : getRightPanel().getAllFrames()) {
            TableFrame frame = (TableFrame) curFrame;
            frame.refreshSimilarOpenTables();
        }
    }

    public void openImage(String filePath){
        openImage(new File(filePath));
    }

    public void openImage(File inputFile){
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        openImageWorker = new OpenImageWorker(inputFile);
        openImageWorker.addPropertyChangeListener(getStatusPanel());
        openImageWorker.execute();
    }

    public void openImages(File[] inputFiles){
        for(int j = 0; j < inputFiles.length; j++) {
            openImage(inputFiles[j]);
        }
    }

    public static byte[] readFile(File inputFile) throws IOException {
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
        if(EcuLogger.getEcuLoggerWithoutCreation() != null) {
            ECUExec.showAlreadyRunningMessage();
            return;
        }
        else {
            ThreadUtil.runAsDaemon(new Runnable() {
                @Override
                public void run() {
                    ECUExec.openLogger(DISPOSE_ON_CLOSE, new String[] {"-logger"});
                }
            });
        }
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

    public JScrollPane getLeftScrollPane() {
        return this.leftScrollPane;
    }

    public JScrollPane getRightScrollPane() {
        return this.rightScrollPane;
    }
}

class SetUserLevelWorker extends SwingWorker<Void, Void> {

    @Override
    protected Void doInBackground() throws Exception {
        for(Rom rom : ECUEditorManager.getECUEditor().getImages()) {
            rom.refreshDisplayedTables();
        }
        return null;
    }

    public void propertyChange(PropertyChangeEvent evnt)
    {
        SwingWorker<?, ?> source = (SwingWorker<?, ?>) evnt.getSource();
        if (null != source && "state".equals( evnt.getPropertyName() )
                && (source.isDone() || source.isCancelled() ) )
        {
            source.removePropertyChangeListener(ECUEditorManager.getECUEditor().getStatusPanel());
        }
    }

    @Override
    public void done() {
        ECUEditor editor = ECUEditorManager.getECUEditor();
        editor.getStatusPanel().setStatus(ECUEditor.rb.getString("STATUSREADY"));
        setProgress(0);
        editor.setCursor(null);
        editor.refreshUI();
    }
}
