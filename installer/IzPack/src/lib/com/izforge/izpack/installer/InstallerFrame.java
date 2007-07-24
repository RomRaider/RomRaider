/*
 * $Id: InstallerFrame.java 1816 2007-04-23 19:57:27Z jponge $
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Jan Blok
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.installer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import net.n3.nanoxml.NonValidator;
import net.n3.nanoxml.StdXMLBuilder;
import net.n3.nanoxml.StdXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLWriter;

import com.izforge.izpack.CustomData;
import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Panel;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.EtchedLineBorder;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.rules.RulesEngine;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * The IzPack installer frame.
 * 
 * @author Julien Ponge created October 27, 2002
 * @author Fabrice Mirabile added fix for alert window on cross button, July 06 2005
 * @author Dennis Reil, added RulesEngine November 10 2006, several changes in January 2007
 */
public class InstallerFrame extends JFrame
{

   
    private static final long serialVersionUID = 3257852069162727473L;

    /**
     * VM version to use version dependent methods calls
     */
    private static final float JAVA_SPECIFICATION_VERSION = Float.parseFloat(System
            .getProperty("java.specification.version"));

    private static final String ICON_RESOURCE = "Installer.image";
    
    /**
     * Name of the variable where to find an extension to the resource name of the icon resource
     */
    private static final String ICON_RESOURCE_EXT_VARIABLE_NAME = "installerimage.ext";


    private static final String HEADING_ICON_RESOURCE = "Heading.image";

    // private static final int HEADINGLINES = 1;

    /**
     * The language pack.
     */
    public LocaleDatabase langpack;

    /**
     * The installation data.
     */
    protected InstallData installdata;

    /**
     * The icons database.
     */
    public IconsDatabase icons;

    /**
     * The panels container.
     */
    protected JPanel panelsContainer;

    /**
     * The frame content pane.
     */
    protected JPanel contentPane;

    /**
     * The previous button.
     */
    protected JButton prevButton;

    /**
     * The next button.
     */
    protected JButton nextButton;

    /**
     * The quit button.
     */
    protected JButton quitButton;

    /**
     * Mapping from "raw" panel number to visible panel number.
     */
    protected ArrayList visiblePanelMapping;

    /**
     * Registered GUICreationListener.
     */
    protected ArrayList guiListener;

    /**
     * Heading major text.
     */
    protected JLabel[] headingLabels;

    /**
     * Panel which contains the heading text and/or icon
     */
    protected JPanel headingPanel;

    /**
     * The heading counter component.
     */
    protected JComponent headingCounterComponent;

    /**
     * Image
     */
    private JLabel iconLabel;

    /**
     * Count for discarded interrupt trials.
     */
    private int interruptCount = 1;

    /**
     * Maximum of discarded interrupt trials.
     */
    private static final int MAX_INTERRUPT = 3;

    /**
     * conditions
     */
    protected RulesEngine rules;

    /**
     * Resource name of the conditions specification
     */
    private static final String CONDITIONS_SPECRESOURCENAME = "conditions.xml";
    /**
     * Resource name for custom icons
     */
    private static final String CUSTOM_ICONS_RESOURCEFILE = "customicons.xml";

    /**
     * The constructor (normal mode).
     * 
     * @param title The window title.
     * @param installdata The installation data.
     * @throws Exception Description of the Exception
     */
    public InstallerFrame(String title, InstallData installdata) throws Exception
    {
        super(title);
        guiListener = new ArrayList();
        visiblePanelMapping = new ArrayList();
        this.installdata = installdata;
        this.langpack = installdata.langpack;

        // Sets the window events handler
        addWindowListener(new WindowHandler());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // initialize rules by loading the conditions
        loadConditions();

        // Builds the GUI
        loadIcons();
        loadCustomIcons();
        loadPanels();
        buildGUI();

        // We show the frame
        showFrame();
        switchPanel(0);
    }

    /**
     * Reads the conditions specification file and initializes the rules engine.
     */
    protected void loadConditions()
    {
        try
        {
            InputStream input = null;
            input = this.getResource(CONDITIONS_SPECRESOURCENAME);
            if (input == null)
            {
                this.rules = new RulesEngine(null, installdata);
                return;
            }

            StdXMLParser parser = new StdXMLParser();
            parser.setBuilder(new StdXMLBuilder());
            parser.setValidator(new NonValidator());
            parser.setReader(new StdXMLReader(input));

            // get the data
            XMLElement conditionsxml = (XMLElement) parser.parse();
            this.rules = new RulesEngine(conditionsxml, installdata);
        }
        catch (Exception e)
        {
            Debug.trace("Can not find optional resource " + CONDITIONS_SPECRESOURCENAME);
            // there seem to be no conditions
            this.rules = new RulesEngine(null, installdata);
        }
    }

    /**
     * Loads the panels.
     * 
     * @throws Exception Description of the Exception
     */
    private void loadPanels() throws Exception
    {
        // Initialisation
        java.util.List panelsOrder = installdata.panelsOrder;
        int i;
        int size = panelsOrder.size();
        String className;
        Class objectClass;
        Constructor constructor;
        Object object;
        IzPanel panel;
        Class[] paramsClasses = new Class[2];
        paramsClasses[0] = Class.forName("com.izforge.izpack.installer.InstallerFrame");
        paramsClasses[1] = Class.forName("com.izforge.izpack.installer.InstallData");
        Object[] params = { this, installdata};

        // We load each of them
        int curVisPanelNumber = 0;
        int lastVis = 0;
        int count = 0;
        for (i = 0; i < size; i++)
        {
            // We add the panel
            Panel p = (Panel) panelsOrder.get(i);
            if (!OsConstraint.oneMatchesCurrentSystem(p.osConstraints)) continue;
            className = p.className;
            String praefix = "com.izforge.izpack.panels.";
            if (className.indexOf('.') > -1)
            // Full qualified class name
                praefix = "";
            objectClass = Class.forName(praefix + className);
            constructor = objectClass.getDeclaredConstructor(paramsClasses);
            installdata.currentPanel = p; // A hack to use meta data in IzPanel constructor
            // Do not call constructor of IzPanel or it's derived at an other place else
            // metadata will be not set.
            object = constructor.newInstance(params);
            panel = (IzPanel) object;
            installdata.panels.add(panel);
            if (panel.isHidden())
                visiblePanelMapping.add(count, new Integer(-1));
            else
            {
                visiblePanelMapping.add(count, new Integer(curVisPanelNumber));
                curVisPanelNumber++;
                lastVis = count;
            }
            count++;
            // We add the XML data panel root
            XMLElement panelRoot = new XMLElement(className);
            installdata.xmlData.addChild(panelRoot);
        }
        visiblePanelMapping.add(count, new Integer(lastVis));
    }

    /**
     * Loads the icons.
     * 
     * @throws Exception Description of the Exception
     */
    private void loadIcons() throws Exception
    {
        // Initialisations
        icons = new IconsDatabase();
        URL url;
        ImageIcon img;
        XMLElement icon;
        InputStream inXML = InstallerFrame.class
                .getResourceAsStream("/com/izforge/izpack/installer/icons.xml");

        // Initialises the parser
        StdXMLParser parser = new StdXMLParser();
        parser.setBuilder(new StdXMLBuilder());
        parser.setReader(new StdXMLReader(inXML));
        parser.setValidator(new NonValidator());

        // We get the data
        XMLElement data = (XMLElement) parser.parse();

        // We load the icons
        Vector children = data.getChildrenNamed("icon");
        int size = children.size();
        for (int i = 0; i < size; i++)
        {
            icon = (XMLElement) children.get(i);
            url = InstallerFrame.class.getResource(icon.getAttribute("res"));
            img = new ImageIcon(url);
            icons.put(icon.getAttribute("id"), img);
        }

        // We load the Swing-specific icons
        children = data.getChildrenNamed("sysicon");
        size = children.size();
        for (int i = 0; i < size; i++)
        {
            icon = (XMLElement) children.get(i);
            url = InstallerFrame.class.getResource(icon.getAttribute("res"));
            img = new ImageIcon(url);
            UIManager.put(icon.getAttribute("id"), img);
        }
    }
    
    /**
     * Loads custom icons into the installer.
     * 
     * @throws Exception
     */
    protected void loadCustomIcons() throws Exception {
      // We try to load and add a custom langpack.
      InputStream inXML = null;
      try {
        inXML = ResourceManager.getInstance().getInputStream(
            CUSTOM_ICONS_RESOURCEFILE);
      } catch (Throwable exception) {
        Debug.trace("Resource " + CUSTOM_ICONS_RESOURCEFILE + " not defined. No custom icons available.");
        return;
      }
      Debug.trace("Custom icons available.");
      URL url;
      ImageIcon img;
      XMLElement icon;

      // Initialises the parser
      StdXMLParser parser = new StdXMLParser();
      parser.setBuilder(new StdXMLBuilder());
      parser.setReader(new StdXMLReader(inXML));
      parser.setValidator(new NonValidator());

      // We get the data
      XMLElement data = (XMLElement) parser.parse();

      // We load the icons
      Vector children = data.getChildrenNamed("icon");
      int size = children.size();
      for (int i = 0; i < size; i++) {
        icon = (XMLElement) children.get(i);
        url = InstallerFrame.class.getResource(icon.getAttribute("res"));
        img = new ImageIcon(url);
        Debug.trace("Icon with id found: " + icon.getAttribute("id"));
        icons.put(icon.getAttribute("id"), img);
      }

      // We load the Swing-specific icons
      children = data.getChildrenNamed("sysicon");
      size = children.size();
      for (int i = 0; i < size; i++) {
        icon = (XMLElement) children.get(i);
        url = InstallerFrame.class.getResource(icon.getAttribute("res"));
        img = new ImageIcon(url);
        UIManager.put(icon.getAttribute("id"), img);
      }
    }

    /**
     * Builds the GUI.
     */
    private void buildGUI()
    {
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // patch 06/07/2005,
        // Fabrice Mirabile
        // Sets the frame icon
        setIconImage(icons.getImageIcon("JFrameIcon").getImage());

        // Prepares the glass pane to block the gui interaction when needed
        JPanel glassPane = (JPanel) getGlassPane();
        glassPane.addMouseListener(new MouseAdapter() {/* Nothing todo */
        });
        glassPane.addMouseMotionListener(new MouseMotionAdapter() {/* Nothing todo */
        });
        glassPane.addKeyListener(new KeyAdapter() {/* Nothing todo */
        });
        glassPane.addFocusListener(new FocusAdapter() {/* Nothing todo */
        });

        // We set the layout & prepare the constraint object
        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout()); // layout);

        // We add the panels container
        panelsContainer = new JPanel();
        panelsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panelsContainer.setLayout(new GridLayout(1, 1));
        contentPane.add(panelsContainer, BorderLayout.CENTER);

        // We put the first panel
        installdata.curPanelNumber = 0;
        IzPanel panel_0 = (IzPanel) installdata.panels.get(0);
        panelsContainer.add(panel_0);

        // We add the navigation buttons & labels

        NavigationHandler navHandler = new NavigationHandler();

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.X_AXIS));
        navPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8, 8,
                8, 8), BorderFactory.createTitledBorder(new EtchedLineBorder(), langpack
                .getString("installer.madewith")
                + " ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(
                "Dialog", Font.PLAIN, 10))));
        navPanel.add(Box.createHorizontalGlue());

        prevButton = ButtonFactory.createButton(langpack.getString("installer.prev"), icons
                .getImageIcon("stepback"), installdata.buttonsHColor);
        navPanel.add(prevButton);
        prevButton.addActionListener(navHandler);

        navPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        nextButton = ButtonFactory.createButton(langpack.getString("installer.next"), icons
                .getImageIcon("stepforward"), installdata.buttonsHColor);
        navPanel.add(nextButton);
        nextButton.addActionListener(navHandler);

        navPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        quitButton = ButtonFactory.createButton(langpack.getString("installer.quit"), icons
                .getImageIcon("stop"), installdata.buttonsHColor);
        navPanel.add(quitButton);
        quitButton.addActionListener(navHandler);
        contentPane.add(navPanel, BorderLayout.SOUTH);

        try
        {
            ImageIcon icon = loadIcon(ICON_RESOURCE, 0, true);
            if (icon != null)
            {
                JPanel imgPanel = new JPanel();
                imgPanel.setLayout(new BorderLayout());
                imgPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
                iconLabel = new JLabel(icon);
                iconLabel.setBorder(BorderFactory.createLoweredBevelBorder());
                imgPanel.add(iconLabel, BorderLayout.NORTH);
                contentPane.add(imgPanel, BorderLayout.WEST);
            }
        }
        catch (Exception e)
        {
            // ignore
        }

        loadAndShowImage(0);
        getRootPane().setDefaultButton(nextButton);
        callGUIListener(GUIListener.GUI_BUILDED, navPanel);
        createHeading(navPanel);
    }

    private void callGUIListener(int what)
    {
        callGUIListener(what, null);
    }

    private void callGUIListener(int what, Object param)
    {
        Iterator iter = guiListener.iterator();
        while (iter.hasNext())
            ((GUIListener) iter.next()).guiActionPerformed(what, param);
    }

    private ImageIcon loadIcon(String resPrefix, int PanelNo, boolean tryBaseIcon)
            throws ResourceNotFoundException, IOException
    {
        ResourceManager rm = ResourceManager.getInstance();
        ImageIcon icon = null;
        String iconext = this.getIconResourceNameExtension();
        if (tryBaseIcon)
        {
            try
            {
                icon = rm.getImageIconResource(resPrefix);
            }
            catch (Exception e) // This is not that clean ...
            {
                icon = rm.getImageIconResource(resPrefix + "." + PanelNo + iconext);
            }
        }
        else
            icon = rm.getImageIconResource(resPrefix + "." + PanelNo + iconext);
        return (icon);
    }

    private ImageIcon loadIcon(String resPrefix, String panelid, boolean tryBaseIcon)
            throws ResourceNotFoundException, IOException
    {
        ResourceManager rm = ResourceManager.getInstance();
        ImageIcon icon = null;
        String iconext = this.getIconResourceNameExtension();
        if (tryBaseIcon)
        {
            try
            {
                icon = rm.getImageIconResource(resPrefix);
            }
            catch (Exception e) // This is not that clean ...
            {
                icon = rm.getImageIconResource(resPrefix + "." + panelid + iconext);
            }
        }
        else
            icon = rm.getImageIconResource(resPrefix + "." + panelid + iconext);
        return (icon);
    }
    
    /**
     * Returns the current set extension to icon resource names. Can be used to change
     * the static installer image based on user input
     * @return a resource extension 
     *         or an empty string if the variable was not set.
     */
    private String getIconResourceNameExtension()
    {
        try 
        {
            String iconext = this.installdata.getVariable(ICON_RESOURCE_EXT_VARIABLE_NAME);
            if (iconext == null){
              iconext = "";
            }
            else {
              
              if ((iconext.length() > 0) && (iconext.charAt(0) != '.')){
                iconext = "." + iconext;
              }
            }
            iconext = iconext.trim();
            return iconext;
        }
        catch (Exception e)
        {
            // in case of error, return an empty string
            return "";
        }
   }

    private void loadAndShowImage(int panelNo)
    {
        loadAndShowImage(iconLabel, ICON_RESOURCE, panelNo);
    }

    private void loadAndShowImage(int panelNo, String panelid)
    {
        loadAndShowImage(iconLabel, ICON_RESOURCE, panelNo, panelid);
    }

    private void loadAndShowImage(JLabel iLabel, String resPrefix, int panelno, String panelid)
    {
        ImageIcon icon = null;
        try
        {
            icon = loadIcon(resPrefix, panelid, false);
        }
        catch (Exception e)
        {
            try
            {
                icon = loadIcon(resPrefix, panelno, false);
            }
            catch (Exception ex)
            {
                try
                {
                    icon = loadIcon(resPrefix, panelid, true);
                }
                catch (Exception e1)
                {
                    // ignore
                }
            }
        }
        if (icon != null)
        {
            iLabel.setVisible(false);
            iLabel.setIcon(icon);
            iLabel.setVisible(true);
        }
    }

    private void loadAndShowImage(JLabel iLabel, String resPrefix, int panelNo)
    {
        ImageIcon icon = null;
        try
        {
            icon = loadIcon(resPrefix, panelNo, false);
        }
        catch (Exception e)
        {
            try
            {
                icon = loadIcon(resPrefix, panelNo, true);
            }
            catch (Exception e1)
            {
                // ignore
            }
        }
        if (icon != null)
        {
            iLabel.setVisible(false);
            iLabel.setIcon(icon);
            iLabel.setVisible(true);
        }
    }

    /**
     * Shows the frame.
     */
    private void showFrame()
    {
        pack();
        setSize(installdata.guiPrefs.width, installdata.guiPrefs.height);
        setResizable(installdata.guiPrefs.resizable);
        centerFrame(this);
        setVisible(true);
    }

    private boolean isBack = false;

    /**
     * Switches the current panel.
     * 
     * @param last Description of the Parameter
     */
    protected void switchPanel(int last)
    {
        try
        {
            if (installdata.curPanelNumber < last)
            {
                isBack = true;
            }
            panelsContainer.setVisible(false);
            IzPanel panel = (IzPanel) installdata.panels.get(installdata.curPanelNumber);
            IzPanel l_panel = (IzPanel) installdata.panels.get(last);
            // instead of writing data here which leads to duplicated entries in
            // auto-installation script (bug # 4551), let's make data only immediately before
            // writing out that script.
            // l_panel.makeXMLData(installdata.xmlData.getChildAtIndex(last));
            // No previos button in the first visible panel
            if (((Integer) visiblePanelMapping.get(installdata.curPanelNumber)).intValue() == 0)
            {
                prevButton.setVisible(false);
                lockPrevButton();
                unlockNextButton(); // if we push the button back at the license
                // panel
            }
            // Only the exit button in the last panel.
            else if (((Integer) visiblePanelMapping.get(installdata.panels.size())).intValue() == installdata.curPanelNumber)
            {
                prevButton.setVisible(false);
                nextButton.setVisible(false);
                lockNextButton();
            }
            else
            {
                prevButton.setVisible(true);
                nextButton.setVisible(true);
                unlockPrevButton();
                unlockNextButton();
            }
            // With VM version >= 1.5 setting default button one time will not work.
            // Therefore we set it every panel switch and that also later. But in
            // the moment it seems so that the quit button will not used as default button.
            // No idea why... (Klaus Bartz, 06.09.25)
            SwingUtilities.invokeLater(new Runnable() {

                public void run()
                {
                   JButton cdb = null;
                   if (nextButton.isEnabled()) {
                     cdb = nextButton;
                     quitButton.setDefaultCapable(false);
                     prevButton.setDefaultCapable(false);
                     nextButton.setDefaultCapable(true);
                   } else if (quitButton.isEnabled()) {
                     cdb = quitButton;
                     quitButton.setDefaultCapable(true);
                     prevButton.setDefaultCapable(false);
                     nextButton.setDefaultCapable(false);
                   }
                   getRootPane().setDefaultButton(cdb);
                 }
            });

            // Change panels container to the current one.
            panelsContainer.remove(l_panel);
            l_panel.panelDeactivate();
            panelsContainer.add(panel);

            if (panel.getInitialFocus() != null)
            { // Initial focus hint should be performed after current panel
                // was added to the panels container, else the focus hint will
                // be ignored.
                // Give a hint for the initial focus to the system.
                final Component inFoc = panel.getInitialFocus();
                if (JAVA_SPECIFICATION_VERSION < 1.35)
                {
                    inFoc.requestFocus();
                }
                else
                { // On java VM version >= 1.5 it works only if
                    // invoke later will be used.
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run()
                        {
                            inFoc.requestFocusInWindow();
                        }
                    });
                }
                /*
                 * On editable text components position the caret to the end of the cust existent
                 * text.
                 */
                if (inFoc instanceof JTextComponent)
                {
                    JTextComponent inText = (JTextComponent) inFoc;
                    if (inText.isEditable() && inText.getDocument() != null)
                    {
                        inText.setCaretPosition(inText.getDocument().getLength());
                    }
                }
            }
            performHeading(panel);
            performHeadingCounter(panel);
            panel.panelActivate();
            panelsContainer.setVisible(true);
            Panel metadata = panel.getMetadata();
            if ((metadata != null) && (!"UNKNOWN".equals(metadata.getPanelid())))
            {
                loadAndShowImage(((Integer) visiblePanelMapping.get(installdata.curPanelNumber))
                        .intValue(), metadata.getPanelid());
            }
            else
            {
                loadAndShowImage(((Integer) visiblePanelMapping.get(installdata.curPanelNumber))
                        .intValue());
            }
            isBack = false;
            callGUIListener(GUIListener.PANEL_SWITCHED);
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }
    }

    /**
     * Writes the uninstalldata.
     */
    private void writeUninstallData()
    {
        // Show whether a separated logfile should be also written or not.
        String logfile = installdata.getVariable("InstallerFrame.logfilePath");
        BufferedWriter extLogWriter = null;
        if (logfile != null)
        {
            if (logfile.toLowerCase().startsWith("default"))
                logfile = "$INSTALL_PATH/Uninstaller/install.log";
            logfile = IoHelper.translatePath(logfile, new VariableSubstitutor(installdata
                    .getVariables()));
            File outFile = new File(logfile);
            if (!outFile.getParentFile().exists()) outFile.getParentFile().mkdirs();
            FileOutputStream out = null;
            try
            {
                out = new FileOutputStream(outFile);
            }
            catch (FileNotFoundException e)
            {
                Debug.trace("Cannot create logfile!");
                Debug.error(e);
            }
            if (out != null) extLogWriter = new BufferedWriter(new OutputStreamWriter(out));
        }
        try
        {
            // We get the data
            UninstallData udata = UninstallData.getInstance();
            List files = udata.getFilesList();
            ZipOutputStream outJar = installdata.uninstallOutJar;

            if (outJar == null) return;

            // We write the files log
            outJar.putNextEntry(new ZipEntry("install.log"));
            BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
            logWriter.write(installdata.getInstallPath());
            logWriter.newLine();
            Iterator iter = files.iterator();
            if (extLogWriter != null)
            { // Write intern (in uninstaller.jar) and extern log file.
                while (iter.hasNext())
                {
                    String txt = (String) iter.next();
                    logWriter.write(txt);
                    extLogWriter.write(txt);
                    if (iter.hasNext())
                    {
                        logWriter.newLine();
                        extLogWriter.newLine();
                    }
                }
                logWriter.flush();
                extLogWriter.flush();
                extLogWriter.close();
            }
            else
            {
                while (iter.hasNext())
                {
                    logWriter.write((String) iter.next());
                    if (iter.hasNext()) logWriter.newLine();
                }
                logWriter.flush();
            }
            outJar.closeEntry();

            // We write the uninstaller jar file log
            outJar.putNextEntry(new ZipEntry("jarlocation.log"));
            logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
            logWriter.write(udata.getUninstallerJarFilename());
            logWriter.newLine();
            logWriter.write(udata.getUninstallerPath());
            logWriter.flush();
            outJar.closeEntry();

            // Write out executables to execute on uninstall
            outJar.putNextEntry(new ZipEntry("executables"));
            ObjectOutputStream execStream = new ObjectOutputStream(outJar);
            iter = udata.getExecutablesList().iterator();
            execStream.writeInt(udata.getExecutablesList().size());
            while (iter.hasNext())
            {
                ExecutableFile file = (ExecutableFile) iter.next();
                execStream.writeObject(file);
            }
            execStream.flush();
            outJar.closeEntry();

            // Write out additional uninstall data
            // Do not "kill" the installation if there is a problem
            // with custom uninstall data. Therefore log it to Debug,
            // but do not throw.
            Map additionalData = udata.getAdditionalData();
            if (additionalData != null && !additionalData.isEmpty())
            {
                Iterator keys = additionalData.keySet().iterator();
                HashSet exist = new HashSet();
                while (keys != null && keys.hasNext())
                {
                    String key = (String) keys.next();
                    Object contents = additionalData.get(key);
                    if ("__uninstallLibs__".equals(key))
                    {
                        Iterator nativeLibIter = ((List) contents).iterator();
                        while (nativeLibIter != null && nativeLibIter.hasNext())
                        {
                            String nativeLibName = (String) ((List) nativeLibIter.next()).get(0);
                            byte[] buffer = new byte[5120];
                            long bytesCopied = 0;
                            int bytesInBuffer;
                            outJar.putNextEntry(new ZipEntry("native/" + nativeLibName));
                            InputStream in = getClass().getResourceAsStream(
                                    "/native/" + nativeLibName);
                            while ((bytesInBuffer = in.read(buffer)) != -1)
                            {
                                outJar.write(buffer, 0, bytesInBuffer);
                                bytesCopied += bytesInBuffer;
                            }
                            outJar.closeEntry();
                        }
                    }
                    else if ("uninstallerListeners".equals(key) || "uninstallerJars".equals(key))
                    { // It is a ArrayList of ArrayLists which contains the
                        // full
                        // package paths of all needed class files.
                        // First we create a new ArrayList which contains only
                        // the full paths for the uninstall listener self; thats
                        // the first entry of each sub ArrayList.
                        ArrayList subContents = new ArrayList();

                        // Secound put the class into uninstaller.jar
                        Iterator listenerIter = ((List) contents).iterator();
                        while (listenerIter.hasNext())
                        {
                            byte[] buffer = new byte[5120];
                            long bytesCopied = 0;
                            int bytesInBuffer;
                            CustomData customData = (CustomData) listenerIter.next();
                            // First element of the list contains the listener
                            // class path;
                            // remind it for later.
                            if (customData.listenerName != null)
                                subContents.add(customData.listenerName);
                            Iterator liClaIter = customData.contents.iterator();
                            while (liClaIter.hasNext())
                            {
                                String contentPath = (String) liClaIter.next();
                                if (exist.contains(contentPath)) continue;
                                exist.add(contentPath);
                                try
                                {
                                    outJar.putNextEntry(new ZipEntry(contentPath));
                                }
                                catch (ZipException ze)
                                { // Ignore, or ignore not ?? May be it is a
                                    // exception because
                                    // a doubled entry was tried, then we should
                                    // ignore ...
                                    Debug.trace("ZipException in writing custom data: "
                                            + ze.getMessage());
                                    continue;
                                }
                                InputStream in = getClass().getResourceAsStream("/" + contentPath);
                                if (in != null)
                                {
                                    while ((bytesInBuffer = in.read(buffer)) != -1)
                                    {
                                        outJar.write(buffer, 0, bytesInBuffer);
                                        bytesCopied += bytesInBuffer;
                                    }
                                }
                                else
                                    Debug.trace("custom data not found: " + contentPath);
                                outJar.closeEntry();

                            }
                        }
                        // Third we write the list into the
                        // uninstaller.jar
                        outJar.putNextEntry(new ZipEntry(key));
                        ObjectOutputStream objOut = new ObjectOutputStream(outJar);
                        objOut.writeObject(subContents);
                        objOut.flush();
                        outJar.closeEntry();

                    }
                    else
                    {
                        outJar.putNextEntry(new ZipEntry(key));
                        if (contents instanceof ByteArrayOutputStream)
                        {
                            ((ByteArrayOutputStream) contents).writeTo(outJar);
                        }
                        else
                        {
                            ObjectOutputStream objOut = new ObjectOutputStream(outJar);
                            objOut.writeObject(contents);
                            objOut.flush();
                        }
                        outJar.closeEntry();
                    }
                }
            }
            // write the files which should be deleted by root for another user

            outJar.putNextEntry(new ZipEntry(UninstallData.ROOTSCRIPT));
            ObjectOutputStream rootStream = new ObjectOutputStream(outJar);

            String rootScript = udata.getRootScript();

            rootStream.writeUTF(rootScript);

            rootStream.flush();
            outJar.closeEntry();

            // Cleanup
            outJar.flush();
            outJar.close();
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }
    }

    /**
     * Gets the stream to a resource.
     * 
     * @param res The resource id.
     * @return The resource value, null if not found
     * @throws Exception
     */
    public InputStream getResource(String res) throws Exception
    {
        InputStream result;
        String basePath = "";
        ResourceManager rm = null;

        try
        {
            rm = ResourceManager.getInstance();
            basePath = rm.resourceBasePath;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        result = this.getClass().getResourceAsStream(basePath + res);

        if (result == null) { throw new ResourceNotFoundException("Warning: Resource not found: "
                + res); }
        return result;
    }

    /**
     * Centers a window on screen.
     * 
     * @param frame The window tp center.
     */
    public void centerFrame(Window frame)
    {
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        Dimension frameSize = frame.getSize();
        frame.setLocation(center.x - frameSize.width / 2, center.y - frameSize.height / 2 - 10);
    }

    /**
     * Returns the panels container size.
     * 
     * @return The panels container size.
     */
    public Dimension getPanelsContainerSize()
    {
        return panelsContainer.getSize();
    }

    /**
     * Sets the parameters of a GridBagConstraints object.
     * 
     * @param gbc The constraints object.
     * @param gx The x coordinates.
     * @param gy The y coordinates.
     * @param gw The width.
     * @param wx The x wheight.
     * @param wy The y wheight.
     * @param gh Description of the Parameter
     */
    public void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, double wx,
            double wy)
    {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }

    /**
     * Makes a clean closing.
     */
    public void exit()
    {
        if (installdata.canClose)
        {
            // this does nothing if the uninstaller was not included
            writeUninstallData();
            Housekeeper.getInstance().shutDown(0);
        }
        else
        {
            // The installation is not over
            if (Unpacker.isDiscardInterrupt() && interruptCount < MAX_INTERRUPT)
            { // But we should not interrupt.
                interruptCount++;
                return;
            }
            // Use a alternate message and title if defined.
            final String mkey = "installer.quit.reversemessage";
            final String tkey = "installer.quit.reversetitle";
            String message = langpack.getString(mkey);
            String title = langpack.getString(tkey);
            // message equal to key -> no alternate message defined.
            if (message.indexOf(mkey) > -1) message = langpack.getString("installer.quit.message");
            // title equal to key -> no alternate title defined.
            if (title.indexOf(tkey) > -1) title = langpack.getString("installer.quit.title");
            // Now replace variables in message or title.
            VariableSubstitutor vs = new VariableSubstitutor(installdata.getVariables());
            message = vs.substitute(message, null);
            title = vs.substitute(title, null);
            int res = JOptionPane
                    .showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION)
            {
                wipeAborted();
                Housekeeper.getInstance().shutDown(0);
            }
        }
    }

    /**
     * Wipes the written files when you abort the installation.
     */
    protected void wipeAborted()
    {
        Iterator it;

        // We set interrupt to all running Unpacker and wait 40 sec for maximum.
        // If interrupt is discarded (return value false), return immediately:
        if (!Unpacker.interruptAll(40000)) return;

        // Wipes them all in 2 stages
        UninstallData u = UninstallData.getInstance();
        it = u.getFilesList().iterator();
        if (!it.hasNext()) return;
        while (it.hasNext())
        {
            String p = (String) it.next();
            File f = new File(p);
            f.delete();
        }
        String fullCleanup = installdata.getVariable("InstallerFrame.cleanAllAtInterrupt");
        if (fullCleanup == null || !"no".equalsIgnoreCase(fullCleanup))
            cleanWipe(new File(installdata.getInstallPath()));
    }

    /**
     * Recursive files wiper.
     * 
     * @param file The file to wipe.
     */
    private void cleanWipe(File file)
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            int size = files.length;
            for (int i = 0; i < size; i++)
                cleanWipe(files[i]);
        }
        file.delete();
    }

    /**
     * Launches the installation.
     * 
     * @param listener The installation listener.
     */
    public void install(AbstractUIProgressHandler listener)
    {       
        IUnpacker unpacker = UnpackerFactory.getUnpacker(this.installdata.info.getUnpackerClassName(), installdata, listener);
        Thread unpackerthread = new Thread(unpacker, "IzPack - Unpacker thread");
        unpackerthread.start();
        /*
        Unpacker unpacker = new Unpacker(installdata, listener);
        unpacker.start();
        */
    }

    /**
     * Writes an XML tree.
     * 
     * @param root The XML tree to write out.
     * @param out The stream to write on.
     * @throws Exception Description of the Exception
     */
    public void writeXMLTree(XMLElement root, OutputStream out) throws Exception
    {
        XMLWriter writer = new XMLWriter(out);
        // fix bug# 4551
        // writer.write(root);
        for (int i = 0; i < installdata.panels.size(); i++)
        {
            IzPanel panel = (IzPanel) installdata.panels.get(i);
            panel.makeXMLData(installdata.xmlData.getChildAtIndex(i));
        }
        writer.write(installdata.xmlData);
    }

    /**
     * Changes the quit button text. If <tt>text</tt> is null, the default quit text is used.
     * 
     * @param text text to be used for changes
     */
    public void setQuitButtonText(String text)
    {
        String text1 = text;
        if (text1 == null) text1 = langpack.getString("installer.quit");
        quitButton.setText(text1);
    }

    /**
     * Sets a new icon into the quit button if icons should be used, else nothing will be done.
     * 
     * @param iconName name of the icon to be used
     */
    public void setQuitButtonIcon(String iconName)
    {
        String useButtonIcons = (String) installdata.guiPrefs.modifier.get("useButtonIcons");

        if (useButtonIcons == null || "yes".equalsIgnoreCase(useButtonIcons))
        {
            quitButton.setIcon(icons.getImageIcon(iconName));
        }
    }

    /*
     * FocusTraversalPolicy objects to handle keybord blocking; the declaration os Object allows to
     * use a pre version 1.4 VM.
     */
    private Object usualFTP = null;

    private Object blockFTP = null;

    /**
     * Blocks GUI interaction.
     */
    public void blockGUI()
    {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getGlassPane().setVisible(true);
        getGlassPane().setEnabled(true);
        // No traversal handling before VM version 1.4
        if (JAVA_SPECIFICATION_VERSION < 1.35) return;
        if (usualFTP == null) usualFTP = getFocusTraversalPolicy();
        if (blockFTP == null) blockFTP = new BlockFocusTraversalPolicy();
        setFocusTraversalPolicy((java.awt.FocusTraversalPolicy) blockFTP);
        getGlassPane().requestFocus();
        callGUIListener(GUIListener.GUI_BLOCKED);

    }

    /**
     * Releases GUI interaction.
     */
    public void releaseGUI()
    {
        getGlassPane().setEnabled(false);
        getGlassPane().setVisible(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        // No traversal handling before VM version 1.4
        if (JAVA_SPECIFICATION_VERSION < 1.35) return;
        setFocusTraversalPolicy((java.awt.FocusTraversalPolicy) usualFTP);
        callGUIListener(GUIListener.GUI_RELEASED);
    }

    /**
     * Locks the 'previous' button.
     */
    public void lockPrevButton()
    {
        prevButton.setEnabled(false);
    }

    /**
     * Locks the 'next' button.
     */
    public void lockNextButton()
    {
        nextButton.setEnabled(false);
    }

    /**
     * Unlocks the 'previous' button.
     */
    public void unlockPrevButton()
    {
        prevButton.setEnabled(true);
    }

    /**
     * Unlocks the 'next' button.
     */
    public void unlockNextButton()
    {
        unlockNextButton(true);
    }

    /**
     * Unlocks the 'next' button.
     * @param requestFocus if <code>true</code> focus goes to <code>nextButton</code> 
     */
    public void unlockNextButton(boolean requestFocus)
    {
        nextButton.setEnabled(true);
        if (requestFocus)
            nextButton.requestFocus();
    }

    /**
     * Allows a panel to ask to be skipped.
     */
    public void skipPanel()
    {
        if (installdata.curPanelNumber < installdata.panels.size() - 1)
        {
            if (isBack)
            {
                installdata.curPanelNumber--;
                switchPanel(installdata.curPanelNumber + 1);
            }
            else
            {
                installdata.curPanelNumber++;
                switchPanel(installdata.curPanelNumber - 1);
            }

        }
    }

    public boolean canShow(int panelnumber)
    {
        IzPanel panel = (IzPanel) installdata.panels.get(panelnumber);
        String panelid = panel.getMetadata().getPanelid();
        Debug.trace("Current Panel: " + panelid);

        if (!this.getRules().canShowPanel(panelid, this.installdata.variables))
        {
            // skip panel, if conditions for panel aren't met
            Debug.log("Skip panel with panelid=" + panelid);
            // panel should be skipped, so we have to decrement panelnumber for skipping
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * This function moves to the next panel
     */
    public void navigateNext()
    {
        // If the button is inactive this indicates that we cannot move
        // so we don't do the move
        if (!nextButton.isEnabled()) return;
        this.navigateNext(installdata.curPanelNumber);
    }

    public void navigateNext(int last)
    {
        if ((installdata.curPanelNumber < installdata.panels.size() - 1))
        {
            // We must trasfer all fields into the variables before
            // panelconditions try to resolve the rules based on unassigned vars.
            boolean isValid = 
              ((IzPanel) installdata.panels.get(last)).isValidated();
          
            // if this is not here, validation will
            // occur mutilple times while skipping panels through the recursion
            if(!isValid) return; 
            
            installdata.curPanelNumber++;
            if (!canShow(installdata.curPanelNumber))
            {
                this.navigateNext(last);
                return;
            }
            else
            {
                if (isValid)
                {
                    switchPanel(last);
                }
                else {
                    installdata.curPanelNumber--;
                }
            }
        }
    }

    /**
     * This function moves to the previous panel
     */
    public void navigatePrevious()
    {
        // If the button is inactive this indicates that we cannot move
        // so we don't do the move
        if (!prevButton.isEnabled()) return;
        this.navigatePrevious(installdata.curPanelNumber);
    }

    public void navigatePrevious(int last)
    {
        if ((installdata.curPanelNumber > 0))
        {
            installdata.curPanelNumber--;
            if (!canShow(installdata.curPanelNumber))
            {
                this.navigatePrevious(last);
                return;
            }
            else
            {
                switchPanel(last);
            }
        }
    }

    /**
     * Handles the events from the navigation bar elements.
     * 
     * @author Julien Ponge
     */
    class NavigationHandler implements ActionListener
    {

        /**
         * Actions handler.
         * 
         * @param e The event.
         */
        public void actionPerformed(ActionEvent e)
        {
            Object source = e.getSource();
            if (source == prevButton)
            {
                navigatePrevious();
            }
            else if (source == nextButton)
            {
                navigateNext();
            }
            else if (source == quitButton) exit();

        }
    }

    /**
     * The window events handler.
     * 
     * @author julien created October 27, 2002
     */
    class WindowHandler extends WindowAdapter
    {

        /**
         * Window close is pressed,
         * 
         * @param e The event.
         */
        public void windowClosing(WindowEvent e)
        {
            // We ask for confirmation
            exit();
        }

        /**
         * OLD VERSION We can't avoid the exit here, so don't call exit anywhere else.
         * 
         * @param e The event.
         * 
         * public void windowClosing(WindowEvent e) { if (Unpacker.isDiscardInterrupt() &&
         * interruptCount < MAX_INTERRUPT) { // But we should not interrupt. interruptCount++;
         * return; } // We show an alert anyway if (!installdata.canClose)
         * JOptionPane.showMessageDialog(null, langpack.getString("installer.quit.message"),
         * langpack.getString("installer.warning"), JOptionPane.ERROR_MESSAGE); wipeAborted();
         * Housekeeper.getInstance().shutDown(0); }
         */
    }

    /**
     * A FocusTraversalPolicy that only allows the block panel to have the focus
     */
    private class BlockFocusTraversalPolicy extends java.awt.DefaultFocusTraversalPolicy
    {

        private static final long serialVersionUID = 3258413928261169209L;

        /**
         * Only accepts the block panel
         * 
         * @param aComp the component to check
         * @return true if aComp is the block panel
         */
        protected boolean accept(Component aComp)
        {
            return aComp == getGlassPane();
        }
    }

    /**
     * Returns the gui creation listener list.
     * 
     * @return the gui creation listener list
     */
    public List getGuiListener()
    {
        return guiListener;
    }

    /**
     * Add a listener to the listener list.
     * 
     * @param listener to be added as gui creation listener
     */
    public void addGuiListener(GUIListener listener)
    {
        guiListener.add(listener);
    }

    private void createHeadingLabels(int headingLines, Color back)
    {
        // headingLabels are an array which contains the labels for header (0),
        // description lines and the icon (last).
        headingLabels = new JLabel[headingLines + 1];
        headingLabels[0] = new JLabel("");
        // First line ist the "main heading" which should be bold.
        headingLabels[0].setFont(headingLabels[0].getFont().deriveFont(Font.BOLD));
        if (installdata.guiPrefs.modifier.containsKey("headingFontSize"))
        {
            float fontSize = Float.parseFloat((String) installdata.guiPrefs.modifier
                    .get("headingFontSize"));
            if (fontSize > 0.0 && fontSize <= 5.0)
            {
                float currentSize = headingLabels[0].getFont().getSize2D();
                headingLabels[0].setFont(headingLabels[0].getFont().deriveFont(
                        currentSize * fontSize));
            }
        }
        for (int i = 1; i < headingLines; ++i)
        {
            headingLabels[i] = new JLabel();
            // Minor headings should be a little bit more to the right.
            headingLabels[i].setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        }

    }

    private void createHeadingCounter(Color back, JPanel navPanel, JPanel leftHeadingPanel)
    {
        int i;
        String counterPos = "inHeading";
        if (installdata.guiPrefs.modifier.containsKey("headingPanelCounterPos"))
            counterPos = (String) installdata.guiPrefs.modifier.get("headingPanelCounterPos");
        // Do not create counter if it should be in the heading, but no heading should be used.
        if (leftHeadingPanel == null && "inHeading".equalsIgnoreCase(counterPos)) return;
        if (installdata.guiPrefs.modifier.containsKey("headingPanelCounter"))
        {
            headingCounterComponent = null;
            if ("progressbar".equalsIgnoreCase((String) installdata.guiPrefs.modifier
                    .get("headingPanelCounter")))
            {
                JProgressBar headingProgressBar = new JProgressBar();
                headingProgressBar.setStringPainted(true);
                headingProgressBar.setString("");
                headingProgressBar.setValue(0);
                headingCounterComponent = headingProgressBar;
            }
            else if ("text".equalsIgnoreCase((String) installdata.guiPrefs.modifier
                    .get("headingPanelCounter")))
            {
                JLabel headingCountPanels = new JLabel(" ");
                headingCounterComponent = headingCountPanels;
                headingCounterComponent.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
            }
            if ("inHeading".equals(counterPos))
            {
                leftHeadingPanel.add(headingCounterComponent);
            }
            else if ("inNavigationPanel".equals(counterPos))
            {
                Component[] comps = navPanel.getComponents();
                for (i = 0; i < comps.length; ++i)
                {
                    if (comps[i].equals(prevButton)) break;
                }
                if (i <= comps.length)
                {
                    navPanel.add(Box.createHorizontalGlue(), i);
                    navPanel.add(headingCounterComponent, i);
                }

            }
        }
    }

    private JPanel createHeadingIcon(Color back)
    {
        // the icon
        ImageIcon icon = null;
        try
        {
            icon = loadIcon(HEADING_ICON_RESOURCE, 0, true);
        }
        catch (Exception e)
        {
            // ignore
        }
        JPanel imgPanel = new JPanel();
        imgPanel.setLayout(new BoxLayout(imgPanel, BoxLayout.Y_AXIS));
        imgPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        if (back != null) imgPanel.setBackground(back);
        JLabel iconLab = new JLabel(icon);
        imgPanel.add(iconLab, BorderLayout.EAST);
        headingLabels[headingLabels.length - 1] = iconLab;
        return (imgPanel);

    }

    private void createHeading(JPanel navPanel)
    {
        headingPanel = null;
        int headingLines = 1;
        // The number of lines can be determined in the config xml file.
        // The first is the header, additonals are descriptions for the header.
        if (installdata.guiPrefs.modifier.containsKey("headingLineCount"))
            headingLines = Integer.parseInt((String) installdata.guiPrefs.modifier
                    .get("headingLineCount"));
        Color back = null;
        int i = 0;
        // It is possible to determine the used background color of the heading panel.
        if (installdata.guiPrefs.modifier.containsKey("headingBackgroundColor"))
            back = Color.decode((String) installdata.guiPrefs.modifier
                    .get("headingBackgroundColor"));
        // Try to create counter if no heading should be used.
        if (!isHeading(null))
        {
            createHeadingCounter(back, navPanel, null);
            return;
        }

        // We create the text labels and the needed panels. From inner to outer.
        // Labels
        createHeadingLabels(headingLines, back);
        // Panel which contains the labels
        JPanel leftHeadingPanel = new JPanel();
        if (back != null) leftHeadingPanel.setBackground(back);
        leftHeadingPanel.setLayout(new BoxLayout(leftHeadingPanel, BoxLayout.Y_AXIS));
        for (i = 0; i < headingLines; ++i)
            leftHeadingPanel.add(headingLabels[i]);
        // HeadingPanel counter: this is a label or a progress bar which can be placed
        // in the leftHeadingPanel or in the navigation bar. It is facultative. If
        // exist, it shows the current panel number and the amount of panels.
        createHeadingCounter(back, navPanel, leftHeadingPanel);
        // It is possible to place an icon on the right side of the heading panel.
        JPanel imgPanel = createHeadingIcon(back);

        // The panel for text and icon.
        JPanel northPanel = new JPanel();
        if (back != null) northPanel.setBackground(back);
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
        northPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        northPanel.add(leftHeadingPanel);
        northPanel.add(Box.createHorizontalGlue());
        northPanel.add(imgPanel);
        headingPanel = new JPanel(new BorderLayout());
        headingPanel.add(northPanel);
        headingPanel.add(new JSeparator(), BorderLayout.SOUTH);

        // contentPane.add(northPanel, BorderLayout.NORTH);
        contentPane.add(headingPanel, BorderLayout.NORTH);

    }

    /**
     * Returns whether this installer frame uses with the given panel a separated heading panel or
     * not. Be aware, this is an other heading as given by the IzPanel which will be placed in the
     * IzPanel. This heading will be placed if the gui preferences contains an modifier with the key
     * "useHeadingPanel" and the value "yes" and there is a message with the key "&lt;class
     * name&gt;.headline".
     * 
     * @param caller the IzPanel for which heading should be resolved
     * @return whether an heading panel will be used or not
     */
    public boolean isHeading(IzPanel caller)
    {
        if (!installdata.guiPrefs.modifier.containsKey("useHeadingPanel")
                || !((String) installdata.guiPrefs.modifier.get("useHeadingPanel"))
                        .equalsIgnoreCase("yes")) return (false);
        if (caller == null) return (true);
        return (caller.getI18nStringForClass("headline", null) != null);

    }

    private void performHeading(IzPanel panel)
    {
        int i;
        int headingLines = 1;
        if (installdata.guiPrefs.modifier.containsKey("headingLineCount"))
            headingLines = Integer.parseInt((String) installdata.guiPrefs.modifier
                    .get("headingLineCount"));

        if (headingLabels == null) return;
        String headline = panel.getI18nStringForClass("headline");
        if (headline == null)
        {
            headingPanel.setVisible(false);
            return;
        }
        for (i = 0; i <= headingLines; ++i)
            if (headingLabels[i] != null) headingLabels[i].setVisible(false);
        String info;
        for (i = 0; i < headingLines - 1; ++i)
        {
            info = panel.getI18nStringForClass("headinfo" + Integer.toString(i));
            if (info == null) info = " ";
            if (info.endsWith(":"))
            {
                info = info.substring(0, info.length() - 1) + ".";
            }
            headingLabels[i + 1].setText(info);
            headingLabels[i + 1].setVisible(true);
        }
        // Do not forgett the first headline.
        headingLabels[0].setText(headline);
        headingLabels[0].setVisible(true);
        int curPanelNo = ((Integer) visiblePanelMapping.get(installdata.curPanelNumber)).intValue();
        if (headingLabels[headingLines] != null)
        {
            loadAndShowImage(headingLabels[headingLines], HEADING_ICON_RESOURCE, curPanelNo);
            headingLabels[headingLines].setVisible(true);
        }
        headingPanel.setVisible(true);

    }

    private void performHeadingCounter(IzPanel panel)
    {
        if (headingCounterComponent != null)
        {
            int curPanelNo = ((Integer) visiblePanelMapping.get(installdata.curPanelNumber))
                    .intValue();
            int visPanelsCount = ((Integer) visiblePanelMapping.get(((Integer) visiblePanelMapping
                    .get(installdata.panels.size())).intValue())).intValue();

            StringBuffer buf = new StringBuffer();
            buf.append(langpack.getString("installer.step")).append(" ").append(curPanelNo + 1)
                    .append(" ").append(langpack.getString("installer.of")).append(" ").append(
                            visPanelsCount + 1);
            if (headingCounterComponent instanceof JProgressBar)
            {
                JProgressBar headingProgressBar = (JProgressBar) headingCounterComponent;
                headingProgressBar.setMaximum(visPanelsCount + 1);
                headingProgressBar.setValue(curPanelNo + 1);
                headingProgressBar.setString(buf.toString());
            }
            else
                ((JLabel) headingCounterComponent).setText(buf.toString());
        }
    }

    /**
     * @return the rules
     */
    public RulesEngine getRules()
    {
        return this.rules;
    }

    /**
     * @param rules the rules to set
     */
    public void setRules(RulesEngine rules)
    {
        this.rules = rules;
    }
}
