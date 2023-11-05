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

package com.romraider.logger.ecu;

import com.romraider.net.BrowserControl;
import static com.romraider.Version.LOGGER_DEFS_URL;
import static com.romraider.Version.PRODUCT_NAME;
import static com.romraider.Version.VERSION;
import static com.romraider.logger.ecu.profile.UserProfileLoader.BACKUP_PROFILE;
import static com.romraider.logger.ecu.ui.swing.menubar.util.FileHelper.saveProfileToFile;
import static com.romraider.logger.ecu.ui.swing.vertical.VerticalTextIcon.ROTATE_LEFT;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.isNullOrEmpty;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static com.romraider.util.ThreadUtil.sleep;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.BorderLayout.WEST;
import static java.awt.Color.BLACK;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static java.awt.Color.YELLOW;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.sort;
import static javax.swing.BorderFactory.createLoweredBevelBorder;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JOptionPane.showOptionDialog;
import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;
import static javax.swing.KeyStroke.getKeyStroke;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.SwingConstants.BOTTOM;
import static javax.swing.SwingConstants.RIGHT;
import static javax.swing.SwingConstants.VERTICAL;
import static javax.swing.SwingUtilities.invokeLater;
import com.romraider.swing.menubar.RadioButtonMenuItem;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JWindow;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.Version;
import com.romraider.editor.ecu.ECUEditor;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.io.serial.port.SerialPortRefresher;
import com.romraider.logger.ecu.comms.controller.LoggerController;
import com.romraider.logger.ecu.comms.controller.LoggerControllerImpl;
import com.romraider.logger.ecu.comms.globaladjust.GlobalAdjustManager;
import com.romraider.logger.ecu.comms.globaladjust.SSMGlobalAdjustManager;
import com.romraider.logger.ecu.comms.learning.LearningTableValues;
import com.romraider.logger.ecu.comms.learning.LearningTableValuesFactory;
import com.romraider.logger.ecu.comms.query.EcuInit;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.readcodes.ReadCodesManager;
import com.romraider.logger.ecu.comms.readcodes.ReadCodesManagerImpl;
import com.romraider.logger.ecu.comms.reset.ResetManager;
import com.romraider.logger.ecu.comms.reset.ResetManagerImpl;
import com.romraider.logger.ecu.definition.EcuDataLoader;
import com.romraider.logger.ecu.definition.EcuDataLoaderImpl;
import com.romraider.logger.ecu.definition.EcuDefinition;
import com.romraider.logger.ecu.definition.EcuParameter;
import com.romraider.logger.ecu.definition.EcuSwitch;
import com.romraider.logger.ecu.definition.EvaluateEcuDefinition;
import com.romraider.logger.ecu.definition.ExternalData;
import com.romraider.logger.ecu.definition.ExternalDataImpl;
import com.romraider.logger.ecu.definition.LoggerData;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.definition.Transport;
import com.romraider.logger.ecu.exception.ConfigurationException;
import com.romraider.logger.ecu.exception.PortNotFoundException;
import com.romraider.logger.ecu.profile.UserProfile;
import com.romraider.logger.ecu.profile.UserProfileImpl;
import com.romraider.logger.ecu.profile.UserProfileItem;
import com.romraider.logger.ecu.profile.UserProfileItemImpl;
import com.romraider.logger.ecu.profile.UserProfileLoader;
import com.romraider.logger.ecu.profile.UserProfileLoaderImpl;
import com.romraider.logger.ecu.ui.CustomButtonGroup;
import com.romraider.logger.ecu.ui.DataRegistrationBroker;
import com.romraider.logger.ecu.ui.DataRegistrationBrokerImpl;
import com.romraider.logger.ecu.ui.EcuDataComparator;
import com.romraider.logger.ecu.ui.MessageListener;
import com.romraider.logger.ecu.ui.SerialPortComboBox;
import com.romraider.logger.ecu.ui.StatusIndicator;
import com.romraider.logger.ecu.ui.handler.DataUpdateHandlerManager;
import com.romraider.logger.ecu.ui.handler.DataUpdateHandlerManagerImpl;
import com.romraider.logger.ecu.ui.handler.dash.DashboardUpdateHandler;
import com.romraider.logger.ecu.ui.handler.dataflow.DataflowSimulationHandler;
import com.romraider.logger.ecu.ui.handler.dyno.DynoUpdateHandler;
import com.romraider.logger.ecu.ui.handler.file.FileLoggerControllerSwitchHandler;
import com.romraider.logger.ecu.ui.handler.file.FileLoggerControllerSwitchMonitorImpl;
import com.romraider.logger.ecu.ui.handler.file.FileUpdateHandlerImpl;
import com.romraider.logger.ecu.ui.handler.graph.GraphUpdateHandler;
import com.romraider.logger.ecu.ui.handler.injector.InjectorUpdateHandler;
import com.romraider.logger.ecu.ui.handler.livedata.LiveDataTableModel;
import com.romraider.logger.ecu.ui.handler.livedata.LiveDataUpdateHandler;
import com.romraider.logger.ecu.ui.handler.maf.MafUpdateHandler;
import com.romraider.logger.ecu.ui.handler.table.TableUpdateHandler;
import com.romraider.logger.ecu.ui.paramlist.ParameterListTable;
import com.romraider.logger.ecu.ui.paramlist.ParameterListTableModel;
import com.romraider.logger.ecu.ui.paramlist.ParameterRow;
import com.romraider.logger.ecu.ui.playback.PlaybackManagerImpl;
import com.romraider.logger.ecu.ui.swing.layout.BetterFlowLayout;
import com.romraider.logger.ecu.ui.swing.menubar.EcuLoggerMenuBar;
import com.romraider.logger.ecu.ui.swing.menubar.action.LearningTableValuesAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.LogFileNameFieldAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.ToggleButtonAction;
import com.romraider.logger.ecu.ui.swing.vertical.VerticalTextIcon;
import com.romraider.logger.ecu.ui.tab.dyno.DynoTab;
import com.romraider.logger.ecu.ui.tab.dyno.DynoTabImpl;
import com.romraider.logger.ecu.ui.tab.injector.InjectorTab;
import com.romraider.logger.ecu.ui.tab.injector.InjectorTabImpl;
import com.romraider.logger.ecu.ui.tab.maf.MafTab;
import com.romraider.logger.ecu.ui.tab.maf.MafTabImpl;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.logger.external.core.ExternalDataSourceLoader;
import com.romraider.logger.external.core.ExternalDataSourceLoaderImpl;
import com.romraider.swing.AbstractFrame;
import com.romraider.swing.SetFont;
import com.romraider.util.FormatFilename;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;


import com.romraider.util.ThreadUtil;

/*
TODO: add better debug logging, preferably to a file and switchable (on/off)
TODO: Clean up this class!
So much to do, so little time....

TODO: Keyboard accessibility (enable/disable parameters, select tabs, etc)
TODO: Rewrite user profile application and saving to allow tab specific settings (eg. warn levels on dash tab)
TODO: Add custom graph tab (eg. engine speed vs. boost, etc.)
TODO: Add log analysis tab (or maybe new window?), including log playback, custom graphs, map compare, etc
 */

public final class EcuLogger extends AbstractFrame implements MessageListener {
    private static final long serialVersionUID = 7145423251696282784L;
    private static final Logger LOGGER = Logger.getLogger(EcuLogger.class);
    protected static final ResourceBundle rb = new ResourceUtil().getBundle(
            EcuLogger.class.getName());
    private static final String ECU_LOGGER_TITLE = PRODUCT_NAME + " v" + VERSION + " | " + rb.getString("TITLE");
    private static final String LOGGER_FULLSCREEN_ARG = "-logger.fullscreen";
    private static final String LOGGER_TOUCH_ARG = "-logger.touch";
    private static final URL ICON_PATH =  Settings.class.getResource("/graphics/romraider-ico.gif");
    private static final String HEADING_PARAMETERS = "Parameters";
    private static final String HEADING_SWITCHES = "Switches";
    private static final String HEADING_EXTERNAL = "External";
    private static final String CAL_ID_LABEL = "CAL ID";
    private static final String FILE_NAME_EXTENTION = rb.getString("FILENAMEEXTENTION");
    private static final String[] LOG_FILE_TEXT = {"1st PT","2nd PT","3rd PT", // PT = Part Throttle
        "4th PT","5th PT","6th PT",
        "1st WOT","2nd WOT","3rd WOT",
        "4th WOT","5th WOT","6th WOT",
        "cruising"};
    private static final String TOGGLE_LIST_TT_TEXT = rb.getString("TTTEXTF11");
    private static final String UNSELECT_ALL_TT_TEXT = rb.getString("TTTEXTF9");
    private static final String LOG_TO_FILE_FK = "F1";
    private static final String LOG_TO_FILE_ICON = "/graphics/logger_log_to_file.png";
    private static final String LOG_TO_FILE_START = rb.getString("STARTFILELOG");
    private static final String LOG_TO_FILE_STOP = rb.getString("STOPFILELOG");
    private static final String LOG_TO_FILE_TT_TEXT = rb.getString("TTTEXTF1");
    private static String target = "ECU";
    private static String loadResult  = "";
    private String defVersion;
    private ECUEditor ecuEditor;
    private LoggerController controller;
    private ResetManager resetManager;
    private JLabel messageLabel;
    private JLabel calIdLabel;
    private JLabel ecuIdLabel;
    private JLabel statsLabel;
    private JTabbedPane tabbedPane;
    private SerialPortComboBox portsComboBox;
    private DataUpdateHandlerManager dataHandlerManager;
    private DataRegistrationBroker dataTabBroker;
    private ParameterListTableModel dataTabParamListTableModel;
    private ParameterListTableModel dataTabSwitchListTableModel;
    private ParameterListTableModel dataTabExternalListTableModel;
    private DataUpdateHandlerManager graphHandlerManager;
    private DataRegistrationBroker graphTabBroker;
    private ParameterListTableModel graphTabParamListTableModel;
    private ParameterListTableModel graphTabSwitchListTableModel;
    private ParameterListTableModel graphTabExternalListTableModel;
    private DataUpdateHandlerManager dashboardHandlerManager;
    private DataRegistrationBroker dashboardTabBroker;
    private ParameterListTableModel dashboardTabParamListTableModel;
    private ParameterListTableModel dashboardTabSwitchListTableModel;
    private ParameterListTableModel dashboardTabExternalListTableModel;
    private FileUpdateHandlerImpl fileUpdateHandler;
    private LiveDataTableModel dataTableModel;
    private LiveDataUpdateHandler liveDataUpdateHandler;
    private JSplitPane splitPane;
    private JPanel graphPanel;
    private GraphUpdateHandler graphUpdateHandler;
    private JPanel dashboardPanel;
    private DashboardUpdateHandler dashboardUpdateHandler;
    private MafTab mafTab;
    private MafUpdateHandler mafUpdateHandler;
    private DataUpdateHandlerManager mafHandlerManager;
    private DataRegistrationBroker mafTabBroker;
    private InjectorTab injectorTab;
    private InjectorUpdateHandler injectorUpdateHandler;
    private DataUpdateHandlerManager injectorHandlerManager;
    private DataRegistrationBroker injectorTabBroker;
    private DynoTab dynoTab;
    private DynoUpdateHandler dynoUpdateHandler;
    private DataUpdateHandlerManager dynoHandlerManager;
    private DataRegistrationBroker dynoTabBroker;
    private EcuInit ecuInit;
    private JToggleButton logToFileButton;
    private List<ExternalDataSource> externalDataSources;
    private List<EcuParameter> ecuParams;
    private SerialPortRefresher refresher;
    private JWindow startStatus;
    private final JLabel startText = new JLabel(rb.getString("INITIALIZELOGGER"));
    private final String HOME = System.getProperty("user.home");
    private StatusIndicator statusIndicator;
    private List<EcuSwitch> dtcodes = new ArrayList<EcuSwitch>();
    private Map<String, Map<Transport, Collection<Module>>> protocolList =
            new HashMap<String, Map<Transport, Collection<Module>>>();
    private Map<String, Object> componentList = new HashMap<String, Object>();
    private static boolean touchEnabled = false;
    private final JPanel moduleSelectPanel = new JPanel(new FlowLayout());

    private static EcuLogger instance;

    public static EcuLogger getEcuLoggerWithoutCreation() {
    	return instance;
    }

    public static EcuLogger getEcuLogger(ECUEditor ecuEditor) {
    	if (instance != null ) return instance;
    	else {
    		instance = new EcuLogger(ecuEditor);
    		return instance;
    	}
    }

    private EcuLogger(ECUEditor ecuEditor) {
        super(ECU_LOGGER_TITLE);
        this.ecuEditor = ecuEditor;
        construct();
    }

    public void setEcuEditor(ECUEditor ecuEditor) {
    	this.ecuEditor = ecuEditor;

        mafTab = new MafTabImpl(mafTabBroker, ecuEditor);
        injectorTab = new InjectorTabImpl(injectorTabBroker, ecuEditor);
        dynoTab = new DynoTabImpl(dynoTabBroker, ecuEditor);

        injectorUpdateHandler.setInjectorTab(injectorTab);
        mafUpdateHandler.setMafTab(mafTab);
        dynoUpdateHandler.setDynoTab(dynoTab);
    }

    private void construct() {
        /**
         * Bitness of supporting libraries must match the bitness of RomRaider
         * and the running JRE.  Notify and exit if mixed bitness is detected.
         */
        if (!System.getProperty("sun.arch.data.model").equals(Version.BUILD_ARCH)) {
            showMessageDialog(null,
                    MessageFormat.format(
                            rb.getString("INCOMPJRE"),
                            PRODUCT_NAME, Version.BUILD_ARCH),
                rb.getString("INCOMPJREERR"),
                ERROR_MESSAGE);
            // this will generate a NullPointerException because we never got
            // things started
            WindowEvent e = new WindowEvent(this, WindowEvent.WINDOW_CLOSED);
            windowClosing(e);
        }
        checkNotNull(getSettings());
        Logger.getRootLogger().setLevel(Level.toLevel(getSettings().getLoggerDebuggingLevel()));
        LOGGER.info("Logger locale: " + System.getProperty("user.language") +
                "_" + System.getProperty("user.country"));

        if (ecuEditor == null) {
            JProgressBar progressBar = startbar();
            bootstrap();
            progressBar.setValue(20);
            startText.setText(rb.getString("LOADINGDEFS"));
            loadEcuDefs();
            progressBar.setValue(40);
            startText.setText(rb.getString("LOADINGPLUG"));
            progressBar.setIndeterminate(true);
            loadLoggerPlugins();
            progressBar.setIndeterminate(false);
            progressBar.setValue(60);
            startText.setText(rb.getString("LOADINGPARAMS"));
            loadLoggerParams();
            progressBar.setValue(80);
            startText.setText(rb.getString("STARTINGLOGGER"));
            initControllerListeners();
            initUserInterface();
            progressBar.setValue(100);
            initDataUpdateHandlers();
            startPortRefresherThread();
            startStatus.dispose();
        }
        else {
            bootstrap();
            ecuEditor.getStatusPanel().update(rb.getString("LOADINGDEFS"), 20);
            loadEcuDefs();
            ecuEditor.getStatusPanel().update(rb.getString("LOADINGPLUG"), 40);
            loadLoggerPlugins();
            ecuEditor.getStatusPanel().update(rb.getString("LOADINGPARAMS"), 60);
            loadLoggerParams();
            ecuEditor.getStatusPanel().update(rb.getString("STARTINGLOGGER"), 80);
            initControllerListeners();
            initUserInterface();
            ecuEditor.getStatusPanel().update(rb.getString("COMPLETE"), 100);
            initDataUpdateHandlers();
            startPortRefresherThread();
            ecuEditor.getStatusPanel().update(rb.getString("READY"),0);
        }
        this.toFront();
    }

    private void bootstrap() {
        EcuInitCallback ecuInitCallback = new EcuInitCallback() {
            @Override
            public void callback(EcuInit newEcuInit) {
                final String ecuId = newEcuInit.getEcuId();
                LOGGER.info(target + " ID = " + ecuId);
                if (ecuInit == null || !ecuInit.getEcuId().equals(ecuId)) {
                    ecuInit = newEcuInit;
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            String calId = getCalId(ecuId);
                            String carString = getCarString(ecuId);
                            LOGGER.info("CAL ID: " + calId + ", Car: " + carString);
                            calIdLabel.setText(buildEcuInfoLabelText(CAL_ID_LABEL, calId));
                            ecuIdLabel.setText(buildEcuInfoLabelText(target + " ID", ecuId));
                            loadResult = String.format("Loading logger config for new %s ID: %s, ", target, ecuId);
                            loadLoggerParams();
                            loadUserProfile(getSettings().getLoggerProfileFilePath());
                        }

                        private String getCalId(String ecuId) {
                            Map<String, EcuDefinition> ecuDefinitionMap = getSettings().getLoggerEcuDefinitionMap();
                            if (ecuDefinitionMap == null) return null;
                            EcuDefinition def = ecuDefinitionMap.get(ecuId);
                            return def == null ? null : def.getCalId();
                        }

                        private String getCarString(String ecuId) {
                            Map<String, EcuDefinition> ecuDefinitionMap = getSettings().getLoggerEcuDefinitionMap();
                            if (ecuDefinitionMap == null) return null;
                            EcuDefinition def = ecuDefinitionMap.get(ecuId);
                            return def == null ? null : def.getCarString();
                        }
                    });
                }
            }
        };
        fileUpdateHandler = new FileUpdateHandlerImpl(this);
        dataTableModel = new LiveDataTableModel();
        liveDataUpdateHandler = new LiveDataUpdateHandler(dataTableModel);
        graphPanel = new JPanel(new BorderLayout(2, 2));
        graphUpdateHandler = new GraphUpdateHandler(graphPanel);
        dashboardPanel = new JPanel(new BetterFlowLayout(FlowLayout.CENTER, 3, 3));
        dashboardUpdateHandler = new DashboardUpdateHandler(dashboardPanel, getSettings().getLoggerSelectedGaugeIndex());
        mafUpdateHandler = new MafUpdateHandler();
        injectorUpdateHandler = new InjectorUpdateHandler();
        dynoUpdateHandler = new DynoUpdateHandler();
        controller = new LoggerControllerImpl(ecuInitCallback, this, liveDataUpdateHandler,
                graphUpdateHandler, dashboardUpdateHandler, mafUpdateHandler, injectorUpdateHandler,
                dynoUpdateHandler, fileUpdateHandler, TableUpdateHandler.getInstance(), DataflowSimulationHandler.getInstance());

        mafHandlerManager = new DataUpdateHandlerManagerImpl();
        mafTabBroker = new DataRegistrationBrokerImpl(controller, mafHandlerManager);
        mafTab = new MafTabImpl(mafTabBroker, ecuEditor);
        mafUpdateHandler.setMafTab(mafTab);

        injectorHandlerManager = new DataUpdateHandlerManagerImpl();
        injectorTabBroker = new DataRegistrationBrokerImpl(controller, injectorHandlerManager);
        injectorTab = new InjectorTabImpl(injectorTabBroker, ecuEditor);
        injectorUpdateHandler.setInjectorTab(injectorTab);

        dynoHandlerManager = new DataUpdateHandlerManagerImpl();
        dynoTabBroker = new DataRegistrationBrokerImpl(controller, dynoHandlerManager);
        dynoTab = new DynoTabImpl(dynoTabBroker, ecuEditor);
        dynoUpdateHandler.setDynoTab(dynoTab);

        resetManager = new ResetManagerImpl(this);
        messageLabel = new JLabel(ECU_LOGGER_TITLE);
        calIdLabel = new JLabel(buildEcuInfoLabelText(CAL_ID_LABEL, null));
        ecuIdLabel = new JLabel(buildEcuInfoLabelText(target + " ID", null));
        statsLabel = buildStatsLabel();
        tabbedPane = new JTabbedPane(BOTTOM);
        portsComboBox = new SerialPortComboBox();
        dataHandlerManager = new DataUpdateHandlerManagerImpl();
        dataTabBroker = new DataRegistrationBrokerImpl(controller, dataHandlerManager);
        dataTabParamListTableModel = new ParameterListTableModel(dataTabBroker, HEADING_PARAMETERS);
        dataTabSwitchListTableModel = new ParameterListTableModel(dataTabBroker, HEADING_SWITCHES);
        dataTabExternalListTableModel = new ParameterListTableModel(dataTabBroker, HEADING_EXTERNAL);
        graphHandlerManager = new DataUpdateHandlerManagerImpl();
        graphTabBroker = new DataRegistrationBrokerImpl(controller, graphHandlerManager);
        graphTabParamListTableModel = new ParameterListTableModel(graphTabBroker, HEADING_PARAMETERS);
        graphTabSwitchListTableModel = new ParameterListTableModel(graphTabBroker, HEADING_SWITCHES);
        graphTabExternalListTableModel = new ParameterListTableModel(graphTabBroker, HEADING_EXTERNAL);
        dashboardHandlerManager = new DataUpdateHandlerManagerImpl();
        dashboardTabBroker = new DataRegistrationBrokerImpl(controller, dashboardHandlerManager);
        dashboardTabParamListTableModel = new ParameterListTableModel(dashboardTabBroker, HEADING_PARAMETERS);
        dashboardTabSwitchListTableModel = new ParameterListTableModel(dashboardTabBroker, HEADING_SWITCHES);
        dashboardTabExternalListTableModel = new ParameterListTableModel(dashboardTabBroker, HEADING_EXTERNAL);
    }

    public void loadLoggerParams() {
        loadLoggerConfig();
        loadFromExternalDataSources();
    }

    private void initControllerListeners() {
        controller.addListener(dataTabBroker);
        controller.addListener(graphTabBroker);
        controller.addListener(dashboardTabBroker);
    }

    private void startPortRefresherThread() {
        refresher = new SerialPortRefresher(portsComboBox, getSettings().getLoggerPortDefault());
        runAsDaemon(refresher);
        // wait until port refresher fully started before continuing
        waitForSerialPortRefresher(refresher);
    }

    private void waitForSerialPortRefresher(SerialPortRefresher refresher) {
        try {
            doWait(refresher);
        } catch (PortNotFoundException e) {
            LOGGER.warn("Timeout while waiting for serial port refresher - continuing anyway...");
        }
    }

    private void doWait(SerialPortRefresher refresher) {
        long start = currentTimeMillis();
        while (!refresher.isStarted()) {
            checkSerialPortRefresherTimeout(start);
            sleep(5);
        }
    }

    private void checkSerialPortRefresherTimeout(long start) {
        if (currentTimeMillis() - start > 2000) throw new PortNotFoundException("Timeout while finding serial ports");
    }

    private void initUserInterface() {
        // add menubar to frame
        setJMenuBar(buildMenubar());

        // setup main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buildControlToolbar(), NORTH);
        mainPanel.add(buildTabbedPane(), CENTER);
        mainPanel.add(buildStatusBar(), SOUTH);

        // add to container
        getContentPane().add(mainPanel);
    }

    private void loadEcuDefs() {
        try {
            Map<String, EcuDefinition> ecuDefinitionMap = new HashMap<String, EcuDefinition>();
            Vector<File> ecuDefFiles = getSettings().getEcuDefinitionFiles();
            if (!ecuDefFiles.isEmpty()) {
                EcuDataLoader dataLoader = new EcuDataLoaderImpl();
                for (File ecuDefFile : ecuDefFiles) {
                    if (ecuDefFile.exists()) {
                        dataLoader.loadEcuDefsFromXml(ecuDefFile);
                    }
                    else {
                        LOGGER.error(String.format(
                                "ECU definition file configured but not found: %s",
                                ecuDefFile.toString()));
                    }
                    ecuDefinitionMap.putAll(dataLoader.getEcuDefinitionMap());
                }
            }
            getSettings().setLoggerEcuDefinitionMap(ecuDefinitionMap);
            LOGGER.info(
                    String.format(
                            "%d ECU definitions loaded from %d files",
                            ecuDefinitionMap.size(), ecuDefFiles.size()
                            )
                    );
        } catch (Exception e) {
            reportError(e);
        }
    }

    private void loadLoggerConfig() {
        String loggerConfigFilePath = getSettings().getLoggerDefinitionFilePath();
        if (isNullOrEmpty(loggerConfigFilePath))
        	{
        		showMissingConfigDialog();
        		getSettings().setLogExternalsOnly(true);
        	}
        else {
            try {
                EcuDataLoader dataLoader = new EcuDataLoaderImpl();
                dataLoader.loadConfigFromXml(loggerConfigFilePath, getSettings().getLoggerProtocol(),
                        getSettings().getFileLoggingControllerSwitchId(), ecuInit);
                List<EcuParameter> ecuParams = dataLoader.getEcuParameters();
                addConvertorUpdateListeners(ecuParams);
                loadEcuParams(ecuParams);
                loadEcuSwitches(dataLoader.getEcuSwitches());
                dtcodes = dataLoader.getEcuCodes();
                protocolList = dataLoader.getProtocols();
                buildModuleSelectPanel();
                final EcuSwitch fileLogCntrlSw = dataLoader.getFileLoggingControllerSwitch();
                final RadioButtonMenuItem flc = (RadioButtonMenuItem) componentList.get("fileLoggingControl");
                if (fileLogCntrlSw != null && target.equals("ECU")) {
                    initFileLoggingController(fileLogCntrlSw);
                    if (flc != null) {
                        flc.setEnabled(true);
                        flc.setSelected(getSettings().isFileLoggingControllerSwitchActive());
                    }
                }
                else {
                    if (flc != null) {
                        flc.setSelected(false);
                        flc.setEnabled(false);
                    }
                }
                getSettings().setLoggerConnectionProperties(dataLoader.getConnectionProperties());
                if (dataLoader.getDefVersion() == null) {
                    defVersion = "na";
                }
                else {
                    defVersion = dataLoader.getDefVersion();
                }

                loadResult = String.format(
                        "%sloaded protocol %s: %d parameters, %d switches from def version %s. ",
                        loadResult,
                        getSettings().getLoggerProtocol(),
                        ecuParams.size(),
                        dataLoader.getEcuSwitches().size(),
                        defVersion);
                LOGGER.info(loadResult);
            } catch (ConfigurationException cfe) {
                reportError(cfe);
                showErrorConfigDialog(cfe);
            }
            catch (Exception e) {
                reportError(e);
            }
        }
    }

    private void showErrorConfigDialog(Exception e) {
        Object[] options = {"OK"};
        showOptionDialog(this,
                rb.getString("LOGGERDEFERROR") + e.getMessage(),
                rb.getString("LOGGERCONFIG"),
                DEFAULT_OPTION,
                ERROR_MESSAGE,
                null,
                options,
                options[0]);
    }

    private void showMissingConfigDialog() {
        Object[] options = {rb.getString("YES"), rb.getString("NO")};
        int answer = showOptionDialog(this,
                rb.getString("LOGGERDEFNOTCFG"),
                rb.getString("LOGGERCONFIG"),
                DEFAULT_OPTION,
                WARNING_MESSAGE,
                null,
                options,
                options[0]);
        if (answer == 0) {
            BrowserControl.displayURL(LOGGER_DEFS_URL);
        } else {
            showMessageDialog(this,
                    rb.getString("CFGEDFSMENU"),
                    rb.getString("LOGGERCONFIG"),
                    INFORMATION_MESSAGE);
            reportError("Logger definition file not found");
        }
    }

    private void loadLoggerPlugins() {
        try {
            ExternalDataSourceLoader dataSourceLoader = new ExternalDataSourceLoaderImpl();
            dataSourceLoader.loadExternalDataSources(getSettings().getLoggerPluginPorts());
            externalDataSources = dataSourceLoader.getExternalDataSources();
        } catch (Exception e) {
            reportError(e);
        }
    }

    private void loadFromExternalDataSources() {
        try {
            List<ExternalData> externalDatas = getExternalData(externalDataSources);
            loadExternalDatas(externalDatas);
            addExternalConvertorUpdateListeners(externalDatas);
        } catch (Exception e) {
            reportError(e);
        }
    }

    public boolean loadUserProfile(String profileFilePath) {
        try {
            UserProfileLoader profileLoader = new UserProfileLoaderImpl();
            String path = isNullOrEmpty(profileFilePath) ? (HOME + BACKUP_PROFILE) : profileFilePath;
            UserProfile profile = profileLoader.loadProfile(path);
            if(applyUserProfile(profile)) {
                final File profileFile = new File(path);
                if (profileFile.exists()) {
                    reportMessageInTitleBar(
                            MessageFormat.format(
                                    rb.getString("PROFILENAME"),
                                    FormatFilename.getShortName(profileFile)));
                }
                return true;
            }
        }
        catch (Exception e) {
            reportError(e);
        }
        return false;
    }

    private void initFileLoggingController(final EcuSwitch fileLoggingControllerSwitch) {
        controller.setFileLoggerSwitchMonitor(new FileLoggerControllerSwitchMonitorImpl(fileLoggingControllerSwitch, new FileLoggerControllerSwitchHandler() {
            boolean oldDefogStatus = false;

            @Override
            public void handleSwitch(double switchValue) {
                boolean logToFile = (int) switchValue == 1;
                if (getSettings().isFileLoggingControllerSwitchActive() && logToFile != oldDefogStatus) {
                    logToFileButton.setSelected(logToFile);
                    if (logToFile) {
                        fileUpdateHandler.start();
                    } else {
                        fileUpdateHandler.stop();
                    }
                }
                oldDefogStatus = logToFile;
            }
        }));
    }

    private boolean applyUserProfile(UserProfile profile) {
        if (profile != null) {
            final String profileProto =
                    profile.getProtocol() == null ? "SSM" : profile.getProtocol();

            if (!profileProto.equalsIgnoreCase(getSettings().getLoggerProtocol())) {
                Object[] options = {rb.getString("LOAD"), rb.getString("CANCEL")};
                int answer = showOptionDialog(this,
                        MessageFormat.format(
                                rb.getString("PROFILEWRONGPROTO"),
                                profileProto),
                                rb.getString("PROTOCOLMISMATCH"),
                        DEFAULT_OPTION,
                        WARNING_MESSAGE,
                        null,
                        options,
                        options[1]);
                if (answer == 1) {
                    final String cancelMsg = "Profile load canceled by user";
                    LOGGER.info(cancelMsg);
                    return false;
                }
            }

            applyUserProfileToLiveDataTabParameters(dataTabParamListTableModel, profile);
            applyUserProfileToLiveDataTabParameters(dataTabSwitchListTableModel, profile);
            applyUserProfileToLiveDataTabParameters(dataTabExternalListTableModel, profile);
            applyUserProfileToGraphTabParameters(graphTabParamListTableModel, profile);
            applyUserProfileToGraphTabParameters(graphTabSwitchListTableModel, profile);
            applyUserProfileToGraphTabParameters(graphTabExternalListTableModel, profile);
            applyUserProfileToDashTabParameters(dashboardTabParamListTableModel, profile);
            applyUserProfileToDashTabParameters(dashboardTabSwitchListTableModel, profile);
            applyUserProfileToDashTabParameters(dashboardTabExternalListTableModel, profile);
            return true;
        }
        return false;
    }

    private void applyUserProfileToLiveDataTabParameters(ParameterListTableModel paramListTableModel, UserProfile profile) {
        List<ParameterRow> rows = paramListTableModel.getParameterRows();
        for (ParameterRow row : rows) {
            LoggerData loggerData = row.getLoggerData();
            setDefaultUnits(profile, loggerData);
            paramListTableModel.selectParam(loggerData, isSelectedOnLiveDataTab(profile, loggerData));
        }
    }

    private void applyUserProfileToGraphTabParameters(ParameterListTableModel paramListTableModel, UserProfile profile) {
        List<ParameterRow> rows = paramListTableModel.getParameterRows();
        for (ParameterRow row : rows) {
            LoggerData loggerData = row.getLoggerData();
            setDefaultUnits(profile, loggerData);
            paramListTableModel.selectParam(loggerData, isSelectedOnGraphTab(profile, loggerData));
        }
    }

    private void applyUserProfileToDashTabParameters(ParameterListTableModel paramListTableModel, UserProfile profile) {
        List<ParameterRow> rows = paramListTableModel.getParameterRows();
        for (ParameterRow row : rows) {
            LoggerData loggerData = row.getLoggerData();
            setDefaultUnits(profile, loggerData);
            paramListTableModel.selectParam(loggerData, isSelectedOnDashTab(profile, loggerData));
        }
    }

    private void addConvertorUpdateListeners(List<EcuParameter> ecuParams) {
        for (EcuParameter ecuParam : ecuParams) {
            ecuParam.addConvertorUpdateListener(fileUpdateHandler);
            ecuParam.addConvertorUpdateListener(liveDataUpdateHandler);
            ecuParam.addConvertorUpdateListener(graphUpdateHandler);
            ecuParam.addConvertorUpdateListener(dashboardUpdateHandler);
        }
    }

    private void addExternalConvertorUpdateListeners(List<ExternalData> externalDatas) {
        for (ExternalData externalData : externalDatas) {
            externalData.addConvertorUpdateListener(fileUpdateHandler);
            externalData.addConvertorUpdateListener(liveDataUpdateHandler);
            externalData.addConvertorUpdateListener(graphUpdateHandler);
            externalData.addConvertorUpdateListener(dashboardUpdateHandler);
        }
    }

    private void clearParamTableModels() {
        dataTabParamListTableModel.clear();
        graphTabParamListTableModel.clear();
        dashboardTabParamListTableModel.clear();
    }

    private void clearSwitchTableModels() {
        dataTabSwitchListTableModel.clear();
        graphTabSwitchListTableModel.clear();
        dashboardTabSwitchListTableModel.clear();
    }

    private void clearExternalTableModels() {
        dataTabExternalListTableModel.clear();
        graphTabExternalListTableModel.clear();
        dashboardTabExternalListTableModel.clear();
    }

    private void loadEcuParams(List<EcuParameter> ecuParams) {
        clearParamTableModels();
        sort(ecuParams, new EcuDataComparator());
        for (EcuParameter ecuParam : ecuParams) {
            dataTabParamListTableModel.addParam(ecuParam, false);
            graphTabParamListTableModel.addParam(ecuParam, false);
            dashboardTabParamListTableModel.addParam(ecuParam, false);
        }
        mafTab.setEcuParams(ecuParams);
        injectorTab.setEcuParams(ecuParams);
        dynoTab.setEcuParams(ecuParams);
        this.ecuParams = new ArrayList<EcuParameter>(ecuParams);
    }

    private void loadEcuSwitches(List<EcuSwitch> ecuSwitches) {
        clearSwitchTableModels();
        sort(ecuSwitches, new EcuDataComparator());
        for (EcuSwitch ecuSwitch : ecuSwitches) {
            dataTabSwitchListTableModel.addParam(ecuSwitch, false);
            graphTabSwitchListTableModel.addParam(ecuSwitch, false);
            dashboardTabSwitchListTableModel.addParam(ecuSwitch, false);
        }
        mafTab.setEcuSwitches(ecuSwitches);
        injectorTab.setEcuSwitches(ecuSwitches);
        dynoTab.setEcuSwitches(ecuSwitches);
    }

    private List<ExternalData> getExternalData(List<ExternalDataSource> externalDataSources) {
        List<ExternalData> externalDatas = new ArrayList<ExternalData>();
        for (ExternalDataSource dataSource : externalDataSources) {
            try {
                List<? extends ExternalDataItem> dataItems = dataSource.getDataItems();
                for (ExternalDataItem item : dataItems) {
                    externalDatas.add(new ExternalDataImpl(item, dataSource));
                }
            } catch (Exception e) {
                reportError(MessageFormat.format(
                        rb.getString("LOADPLUGINERR"),
                        dataSource.getName(),
                        dataSource.getVersion()), e);
            }
        }
        return externalDatas;
    }

    private void loadExternalDatas(List<ExternalData> externalDatas) {
        clearExternalTableModels();
        sort(externalDatas, new EcuDataComparator());
        for (ExternalData externalData : externalDatas) {
            dataTabExternalListTableModel.addParam(externalData, false);
            graphTabExternalListTableModel.addParam(externalData, false);
            dashboardTabExternalListTableModel.addParam(externalData, false);
        }
        mafTab.setExternalDatas(externalDatas);
        injectorTab.setExternalDatas(externalDatas);
        dynoTab.setExternalDatas(externalDatas);
    }

    private void setDefaultUnits(UserProfile profile, LoggerData loggerData) {
        if (profile != null) {
            try {
                loggerData.selectConvertor(profile.getSelectedConvertor(loggerData));
            } catch (Exception e) {
                reportError(e);
            }
        }
    }

    private boolean isSelectedOnLiveDataTab(UserProfile profile, LoggerData loggerData) {
        return profile != null && profile.isSelectedOnLiveDataTab(loggerData);
    }

    private boolean isSelectedOnGraphTab(UserProfile profile, LoggerData loggerData) {
        return profile != null && profile.isSelectedOnGraphTab(loggerData);
    }

    private boolean isSelectedOnDashTab(UserProfile profile, LoggerData loggerData) {
        return profile != null && profile.isSelectedOnDashTab(loggerData);
    }

    public UserProfile getCurrentProfile() {
        Map<String, UserProfileItem> paramProfileItems = getProfileItems(dataTabParamListTableModel.getParameterRows(),
                graphTabParamListTableModel.getParameterRows(), dashboardTabParamListTableModel.getParameterRows());
        Map<String, UserProfileItem> switchProfileItems = getProfileItems(dataTabSwitchListTableModel.getParameterRows(),
                graphTabSwitchListTableModel.getParameterRows(), dashboardTabSwitchListTableModel.getParameterRows());
        Map<String, UserProfileItem> externalProfileItems = getProfileItems(dataTabExternalListTableModel.getParameterRows(),
                graphTabExternalListTableModel.getParameterRows(), dashboardTabExternalListTableModel.getParameterRows());
        return new UserProfileImpl(
                paramProfileItems,
                switchProfileItems,
                externalProfileItems,
                getSettings().getLoggerProtocol());
    }

    private Map<String, String> getPluginPorts(List<ExternalDataSource> externalDataSources) {
        Map<String, String> plugins = new HashMap<String, String>();
        for (ExternalDataSource dataSource : externalDataSources) {
            String id = dataSource.getId();
            String port = dataSource.getPort();
            if (port != null && port.trim().length() > 0) plugins.put(id, port.trim());
        }
        return plugins;
    }

    private Map<String, UserProfileItem> getProfileItems(List<ParameterRow> dataTabRows, List<ParameterRow> graphTabRows, List<ParameterRow> dashTabRows) {
        Map<String, UserProfileItem> profileItems = new HashMap<String, UserProfileItem>();
        for (ParameterRow dataTabRow : dataTabRows) {
            String id = dataTabRow.getLoggerData().getId();
            String units = dataTabRow.getLoggerData().getSelectedConvertor().getUnits();
            boolean dataTabSelected = dataTabRow.isSelected();
            boolean graphTabSelected = isEcuDataSelected(id, graphTabRows);
            boolean dashTabSelected = isEcuDataSelected(id, dashTabRows);
            profileItems.put(id, new UserProfileItemImpl(units, dataTabSelected, graphTabSelected, dashTabSelected));
        }
        return profileItems;
    }

    private boolean isEcuDataSelected(String id, List<ParameterRow> parameterRows) {
        for (ParameterRow row : parameterRows) {
            if (id.equals(row.getLoggerData().getId())) {
                return row.isSelected();
            }
        }
        return false;
    }

    private void initDataUpdateHandlers() {
        dataHandlerManager.addHandler(liveDataUpdateHandler);
        dataHandlerManager.addHandler(fileUpdateHandler);
        dataHandlerManager.addHandler(TableUpdateHandler.getInstance());
        graphHandlerManager.addHandler(graphUpdateHandler);
        graphHandlerManager.addHandler(fileUpdateHandler);
        graphHandlerManager.addHandler(TableUpdateHandler.getInstance());
        dashboardHandlerManager.addHandler(dashboardUpdateHandler);
        dashboardHandlerManager.addHandler(fileUpdateHandler);
        dashboardHandlerManager.addHandler(TableUpdateHandler.getInstance());
    }

    private JComponent buildTabbedPane() {
        if (touchEnabled == false)
        {
            addSplitPaneTab(
                    "Data",
                    buildSplitPane(
                            buildParamListPane(
                                    dataTabParamListTableModel,
                                    dataTabSwitchListTableModel,
                                    dataTabExternalListTableModel),
                                    buildDataTab()),
                                    buildUnselectAllButton(),
                                    buildLtvButton());
            addSplitPaneTab(
                    "Graph",
                    buildSplitPane(
                            buildParamListPane(
                                    graphTabParamListTableModel,
                                    graphTabSwitchListTableModel,
                                    graphTabExternalListTableModel),
                                    buildGraphTab()),
                                    buildUnselectAllButton(),
                                    buildLtvButton());
            addSplitPaneTab(
                    "Dashboard",
                    buildSplitPane(
                            buildParamListPane(
                                    dashboardTabParamListTableModel,
                                    dashboardTabSwitchListTableModel,
                                    dashboardTabExternalListTableModel),
                                    buildDashboardTab()),
                                    buildUnselectAllButton(),
                                    buildLtvButton(),
                                    buildToggleGaugeStyleButton());
            tabbedPane.add("MAF", mafTab.getPanel());
            tabbedPane.add("Injector", injectorTab.getPanel());
            tabbedPane.add("Dyno", dynoTab.getPanel());
        }
        else
        {
            addSplitPaneTab(
                    "<html><body leftmargin=15 topmargin=15 marginwidth=15 marginheight=15>" + "Data" + "</body></html>",
                    buildSplitPane(
                            buildParamListPane(
                                    dataTabParamListTableModel,
                                    dataTabSwitchListTableModel,
                                    dataTabExternalListTableModel),
                                    buildDataTab()),
                                    buildUnselectAllButton(),
                                    buildLtvButton());
            addSplitPaneTab(
                    "<html><body leftmargin=15 topmargin=15 marginwidth=15 marginheight=15>" + "Graph" + "</body></html>",
                    buildSplitPane(
                            buildParamListPane(
                                    graphTabParamListTableModel,
                                    graphTabSwitchListTableModel,
                                    graphTabExternalListTableModel),
                                    buildGraphTab()),
                                    buildUnselectAllButton(),
                                    buildLtvButton());
            addSplitPaneTab(
                    "<html><body leftmargin=15 topmargin=15 marginwidth=15 marginheight=15>" + "Dashboard" + "</body></html>",
                    buildSplitPane(
                            buildParamListPane(
                                    dashboardTabParamListTableModel,
                                    dashboardTabSwitchListTableModel,
                                    dashboardTabExternalListTableModel),
                                    buildDashboardTab()),
                                    buildUnselectAllButton(),
                                    buildLtvButton(),
                                    buildToggleGaugeStyleButton());
            tabbedPane.add("<html><body leftmargin=15 topmargin=15 marginwidth=15 marginheight=15>" + "MAF"+ "</body></html>", mafTab.getPanel());
            tabbedPane.add("<html><body leftmargin=15 topmargin=15 marginwidth=15 marginheight=15>" + "Injector"+ "</body></html>", injectorTab.getPanel());
            tabbedPane.add("<html><body leftmargin=15 topmargin=15 marginwidth=15 marginheight=15>" + "Dyno" + "</body></html>", dynoTab.getPanel());
        }

        //Check for definitions only when we open the dyno tab
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(tabbedPane.getSelectedComponent() == dynoTab.getPanel())
                {
                	((DynoTabImpl)dynoTab).getDynoControlPanel().checkDynoDefs();
                }
            }
        });

        return tabbedPane;
    }

    private JButton buildToggleGaugeStyleButton() {
        final JButton button = new JButton();
        SetFont.plain(button);
        VerticalTextIcon textIcon = new VerticalTextIcon(
                button,
                rb.getString("BTNGAUGESTYLE"),
                ROTATE_LEFT);
        button.setIcon(textIcon);
        if (touchEnabled == false)
        {
            button.setPreferredSize(new Dimension(25, 80));
        }
        else
        {
            button.setPreferredSize(new Dimension(56, 80));
        }
        button.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(getKeyStroke("F12"), "toggleGaugeStyle");
        button.getActionMap().put("toggleGaugeStyle", new AbstractAction() {
            private static final long serialVersionUID = 6913964758354638587L;

            @Override
            public void actionPerformed(ActionEvent e) {
                button.doClick();
            }
        });
        button.addActionListener(new AbstractAction() {
            private static final long serialVersionUID = 123232894767995264L;

            @Override
            public void actionPerformed(ActionEvent e) {
                dashboardUpdateHandler.toggleGaugeStyle();
            }
        });
        return button;
    }

    private void clearAllSelectedParameters(ParameterListTableModel paramListTableModel) {
        List<ParameterRow> rows = paramListTableModel.getParameterRows();
        for (ParameterRow row : rows) {
            if (row.isSelected()) {
                row.getLoggerData().setSelected(false);
                row.setSelected(false);
                paramListTableModel.selectParam(row.getLoggerData(), false);
            }
        }
        paramListTableModel.fireTableDataChanged();
    }

    private JButton buildUnselectAllButton() {
        final JButton button = new JButton();
        SetFont.plain(button);
        button.setBackground(YELLOW);
        VerticalTextIcon textIcon = new VerticalTextIcon(
                button,
                rb.getString("BTNUNSELECTALL"),
                ROTATE_LEFT);
        button.setToolTipText(UNSELECT_ALL_TT_TEXT);
        button.setIcon(textIcon);
        if (touchEnabled == false)
        {
            button.setPreferredSize(new Dimension(25, 85));
        }
        else
        {
            button.setPreferredSize(new Dimension(56, 85));
        }
        button.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(getKeyStroke("F9"), "un-selectAll");
        button.getActionMap().put("un-selectAll", new AbstractAction() {
            private static final long serialVersionUID = 4913964758354638588L;

            @Override
            public void actionPerformed(ActionEvent e) {
                button.doClick();
            }
        });
        button.addActionListener(new AbstractAction() {
            private static final long serialVersionUID = 723232894767995265L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clearAllSelectedParameters(dataTabParamListTableModel);
                    clearAllSelectedParameters(dataTabSwitchListTableModel);
                    clearAllSelectedParameters(dataTabExternalListTableModel);
                    clearAllSelectedParameters(graphTabParamListTableModel);
                    clearAllSelectedParameters(graphTabSwitchListTableModel);
                    clearAllSelectedParameters(graphTabExternalListTableModel);
                    clearAllSelectedParameters(dashboardTabParamListTableModel);
                    clearAllSelectedParameters(dashboardTabSwitchListTableModel);
                    clearAllSelectedParameters(dashboardTabExternalListTableModel);
                }
                catch (Exception cae) {
                    LOGGER.error("Un-select ALL error: " + cae);
                }
                finally {
                    LOGGER.info("Un-select all parameters by user action");
                }
            }
        });
        return button;
    }

    private JButton buildLtvButton() {
        final JButton button = new JButton();
        SetFont.plain(button);
        VerticalTextIcon textIcon =
                new VerticalTextIcon(
                        button,
                        "LTV (F6)",
                        ROTATE_LEFT);
        button.setIcon(textIcon);
        if (touchEnabled == false)
        {
            button.setPreferredSize(new Dimension(25, 60));
        }
        else
        {
            button.setPreferredSize(new Dimension(56, 60));
        }
        button.addActionListener(new LearningTableValuesAction(this));
        return button;
    }

    private void addSplitPaneTab(String name, final JSplitPane splitPane, JComponent... extraControls) {
        final JToggleButton toggleListButton = new JToggleButton();
        SetFont.plain(toggleListButton);
        toggleListButton.setToolTipText(TOGGLE_LIST_TT_TEXT);
        toggleListButton.setSelected(true);
        VerticalTextIcon textIcon = new VerticalTextIcon(
                toggleListButton,
                rb.getString("BTNPARAMLIST"),
                ROTATE_LEFT);
        toggleListButton.setIcon(textIcon);
        if (touchEnabled == false)
        {
            toggleListButton.setPreferredSize(new Dimension(25, 95));
        }
        else
        {
            toggleListButton.setPreferredSize(new Dimension(56, 95));
        }
        toggleListButton.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(getKeyStroke("F11"), "toggleHideParams");
        toggleListButton.getActionMap().put("toggleHideParams", new AbstractAction() {
            private static final long serialVersionUID = -276854997788647306L;

            @Override
            public void actionPerformed(ActionEvent e) {
                toggleListButton.doClick();
            }
        });
        toggleListButton.addActionListener(new AbstractAction() {
            private static final long serialVersionUID = -1595098685575657317L;
            private final int min = 1;
            public int size = splitPane.getDividerLocation();

            @Override
            public void actionPerformed(ActionEvent e) {
                int current = splitPane.getDividerLocation();
                if (toggleListButton.isSelected()) {
                    splitPane.setDividerLocation(size);
                    getSettings().setLoggerParameterListState(true);
                }
                else {
                    splitPane.setDividerLocation(min);
                    size = current;
                    getSettings().setLoggerParameterListState(false);
                }
            }
        });

        if (!getSettings().getLoggerParameterListState()) {
            toggleListButton.doClick();
        }

        JPanel tabControlPanel = new JPanel(new BetterFlowLayout(FlowLayout.CENTER, 1, 1));
        if (touchEnabled == false)
        {
            tabControlPanel.setPreferredSize(new Dimension(25, 25));
        }
        else
        {
            tabControlPanel.setPreferredSize(new Dimension(56, 25));
        }
        tabControlPanel.add(toggleListButton);
        for (JComponent control : extraControls) tabControlPanel.add(control);

        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.add(tabControlPanel, WEST);
        panel.add(splitPane, CENTER);

        tabbedPane.add(name, panel);
    }

    private JComponent buildParamListPane(ParameterListTableModel paramListTableModel, ParameterListTableModel switchListTableModel, ParameterListTableModel externalListTableModel) {
        JScrollPane paramList = new JScrollPane(buildParamListTable(paramListTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane externalList = new JScrollPane(buildParamListTable(externalListTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane switchList = new JScrollPane(buildParamListTable(switchListTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        if (touchEnabled == false)
        {
            tabs.addTab(HEADING_PARAMETERS, paramList);
            tabs.addTab(HEADING_SWITCHES, switchList);
            tabs.addTab("External Sensors", externalList);
        }
        else
        {
            tabs.addTab("<html><body leftmargin=15 topmargin=15 marginwidth=15 marginheight=15>" + HEADING_PARAMETERS + "</body></html>", paramList);
            tabs.addTab("<html><body leftmargin=15 topmargin=15 marginwidth=15 marginheight=15>" + HEADING_SWITCHES + "</body></html>", switchList);
            tabs.addTab("<html><body leftmargin=15 topmargin=15 marginwidth=15 marginheight=15>External Sensors</body></html>", externalList);
        }
        return tabs;
    }

    private JTable buildParamListTable(ParameterListTableModel tableModel) {
        JTable paramListTable = new ParameterListTable(tableModel);

        if( touchEnabled == false)
        {
            changeColumnWidth(paramListTable, 0, 20, 55, 55);
            changeColumnWidth(paramListTable, 2, 50, 250, 130);
        }
        else
        {
            changeColumnWidth(paramListTable, 0, 20, 75, 75);
            changeColumnWidth(paramListTable, 2, 50, 75, 75);
        }
        return paramListTable;
    }

    private void changeColumnWidth(JTable paramListTable, int colIndex, int minWidth, int maxWidth, int preferredWidth) {
        TableColumn column = paramListTable.getColumnModel().getColumn(colIndex);
        column.setMinWidth(minWidth);
        column.setMaxWidth(maxWidth);
        column.setPreferredWidth(preferredWidth);
    }

    private JComponent buildStatusBar() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        JPanel statusBar = new JPanel(gridBagLayout);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.BOTH;

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(createLoweredBevelBorder());
        messagePanel.add(messageLabel, WEST);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.weightx = 10;
        constraints.weighty = 1;
        gridBagLayout.setConstraints(messagePanel, constraints);
        statusBar.add(messagePanel);

        JPanel ecuIdPanel = new JPanel(new FlowLayout());
        ecuIdPanel.setBorder(createLoweredBevelBorder());
        ecuIdPanel.add(calIdLabel);
        ecuIdPanel.add(ecuIdLabel);
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        gridBagLayout.setConstraints(ecuIdPanel, constraints);
        statusBar.add(ecuIdPanel);

        JPanel statsPanel = new JPanel(new FlowLayout());
        statsPanel.setBorder(createLoweredBevelBorder());
        statsPanel.add(statsLabel);
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        gridBagLayout.setConstraints(statsPanel, constraints);
        statusBar.add(statsPanel);

        return statusBar;
    }

    private String buildEcuInfoLabelText(String label, String value) {
        return label + ": " + (isNullOrEmpty(value) ? " Unknown " : value);
    }

    private JSplitPane buildSplitPane(JComponent leftComponent, JComponent rightComponent) {
        splitPane = new JSplitPane(HORIZONTAL_SPLIT, leftComponent, rightComponent);
        splitPane.setDividerSize(5);

        if (touchEnabled == false)
        {
            splitPane.setDividerLocation((int) getSettings().getDividerLocation());
        }
        else
        {
            splitPane.setDividerLocation((int) getSettings().getDividerLocation()+200);
        }
        splitPane.addPropertyChangeListener(this);
        return splitPane;
    }

    private JMenuBar buildMenubar() {
        return new EcuLoggerMenuBar(this, externalDataSources);
    }

    private JPanel buildControlToolbar() {
        JPanel controlPanel = new JPanel(new BorderLayout());

        if (touchEnabled == true)
        {
            portsComboBox.setPreferredSize(new Dimension(100,50));
        }

        controlPanel.add(buildPortsComboBox(), WEST);
        //TODO: Finish log playback stuff...
        //        controlPanel.add(buildPlaybackControls(), CENTER);
        controlPanel.add(buildStatusIndicator(), EAST);
        return controlPanel;
    }

    private Component buildPlaybackControls() {
        JButton playButton = new JButton("Play");
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ThreadUtil.runAsDaemon(new Runnable() {
                    @Override
                    public void run() {
                        PlaybackManagerImpl playbackManager = new PlaybackManagerImpl(ecuParams, liveDataUpdateHandler, graphUpdateHandler, dashboardUpdateHandler, mafUpdateHandler, dynoUpdateHandler,
                                TableUpdateHandler.getInstance());
                        playbackManager.load(new File("foo.csv"));
                        playbackManager.play();
                    }
                });
            }
        });
        JPanel panel = new JPanel();
        panel.add(playButton);
        return panel;
    }

    private Component buildFileNameExtention() {
        JLabel fileNameLabel = new JLabel(rb.getString("LOGFILETEXT"));
        final JTextField fileNameExtention = new JTextField("",8);
        if (touchEnabled == true)
        {
             fileNameExtention.setPreferredSize(new Dimension(200, 50));
        }
        fileNameExtention.setToolTipText(FILE_NAME_EXTENTION);
        fileNameExtention.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent arg0) {
            }

            @Override
            public void focusLost(FocusEvent arg0) {
                getSettings().setLogfileNameText(fileNameExtention.getText());
            }
        });

        JPopupMenu fileNamePopup = new JPopupMenu();
        JMenuItem ecuIdItem = new JMenuItem(MessageFormat.format(
                rb.getString("TARGETID"), target));
        componentList.put("ecuIdItem", ecuIdItem);
        ecuIdItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileNameExtention.setText(ecuInit.getEcuId());
                getSettings().setLogfileNameText(fileNameExtention.getText());
            }
        });
        fileNamePopup.add(ecuIdItem);
        for (final String item : LOG_FILE_TEXT) {
            ecuIdItem = new JMenuItem(item);
            if (item.endsWith("PT"))  ecuIdItem.setToolTipText(rb.getString("PT"));
            if (item.endsWith("WOT")) ecuIdItem.setToolTipText(rb.getString("WOT"));
            ecuIdItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fileNameExtention.setText(item.replaceAll(" ", "_"));
                    getSettings().setLogfileNameText(fileNameExtention.getText());
                }
            });
            fileNamePopup.add(ecuIdItem);
        }
        ecuIdItem = new JMenuItem(rb.getString("CLT"));
        ecuIdItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileNameExtention.setText("");
                getSettings().setLogfileNameText(fileNameExtention.getText());
            }
        });
        fileNamePopup.add(ecuIdItem);
        fileNameExtention.addMouseListener(new LogFileNameFieldAction(fileNamePopup));

        JPanel panel = new JPanel();
        panel.add(fileNameLabel);
        panel.add(fileNameExtention);
        return panel;
    }

    private final Component buildLogToFileButton() {
        logToFileButton = new JToggleButton(
                LOG_TO_FILE_START,
                new ImageIcon(getClass().getResource(LOG_TO_FILE_ICON)));
        SetFont.plain(logToFileButton);
        if (touchEnabled == true)
        {
             logToFileButton.setPreferredSize(new Dimension(200, 50));
        }
        logToFileButton.setToolTipText(LOG_TO_FILE_TT_TEXT);
        logToFileButton.setBackground(GREEN);
        logToFileButton.setOpaque(true);
        logToFileButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    if (logToFileButton.isSelected() && controller.isStarted()) {
                        fileUpdateHandler.start();
                        logToFileButton.setBackground(RED);
                        logToFileButton.setText(LOG_TO_FILE_STOP);
                    }
                    else {
                        fileUpdateHandler.stop();
                        if (!controller.isStarted()) statusIndicator.stopped();
                        logToFileButton.setBackground(GREEN);
                        logToFileButton.setSelected(false);
                        logToFileButton.setText(LOG_TO_FILE_START);
                    }
                }
            }
        );
        logToFileButton.getInputMap(
                WHEN_IN_FOCUSED_WINDOW).put(
                        getKeyStroke(LOG_TO_FILE_FK), "toggleFileLogging");
        logToFileButton.getActionMap().put(
                "toggleFileLogging", new ToggleButtonAction(this, logToFileButton));
        return logToFileButton;
    }

    private JPanel buildPortsComboBox() {
        portsComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                getSettings().setLoggerPort((String) portsComboBox.getSelectedItem());
                // this is a hack...
                if (!actionEvent.paramString().endsWith("modifiers=")) {
                    restartLogging();
                }
            }
        });
        JPanel comboBoxPanel = new JPanel(new FlowLayout());
        comboBoxPanel.add(new JLabel(rb.getString("COMPORT")));
        comboBoxPanel.add(portsComboBox);

        comboBoxPanel.add(moduleSelectPanel);

        JButton reconnectButton = new JButton(new ImageIcon( getClass().getResource("/graphics/logger_restart.png")));
        componentList.put("reconnectButton", reconnectButton);
        if (touchEnabled == false)
        {
            reconnectButton.setPreferredSize(new Dimension(25, 25));
        }
        else
        {
            reconnectButton.setPreferredSize(new Dimension(75, 50));
        }
        reconnectButton.setToolTipText(MessageFormat.format(
                rb.getString("TTTEXTRECON"),
                target));
        reconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    restartLogging();
                } catch (Exception e) {
                    reportError(e);
                }
            }
        });
        comboBoxPanel.add(reconnectButton);
        JButton disconnectButton = new JButton(new ImageIcon( getClass().getResource("/graphics/logger_stop.png")));
        componentList.put("disconnectButton", disconnectButton);
        if (touchEnabled == false)
        {
            disconnectButton.setPreferredSize(new Dimension(25, 25));
        }
        else
        {
            disconnectButton.setPreferredSize(new Dimension(75, 50));
        }

        disconnectButton.setToolTipText(MessageFormat.format(
                rb.getString("TTTEXTDISCON"),
                target));
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    stopLogging();
                } catch (Exception e) {
                    reportError(e);
                }
            }
        });
        comboBoxPanel.add(reconnectButton);
        comboBoxPanel.add(disconnectButton);
        comboBoxPanel.add(new JSeparator(VERTICAL));
        comboBoxPanel.add(buildLogToFileButton());
        comboBoxPanel.add(buildFileNameExtention());
        return comboBoxPanel;
    }

    private void buildModuleSelectPanel() {
        moduleSelectPanel.removeAll();
        final CustomButtonGroup moduleGroup = new CustomButtonGroup();
        Collection<Module> moduleList = getModuleList();
        if(moduleList.size() == 0) {
        	getSettings().setLogExternalsOnly(true);
        }
        else {
	        for (Module module : moduleList) {
	            final JCheckBox cb = new JCheckBox(module.getName().toUpperCase());
	            if (touchEnabled == true)
	            {
	                cb.setPreferredSize(new Dimension(75, 50));
	            }
	            cb.setToolTipText(MessageFormat.format(
	                    rb.getString("TTTEXTEXTERNALS"),
	                    module.getDescription()));
	            if (getSettings().getTargetModule().equalsIgnoreCase(module.getName())) {
	                cb.setSelected(true);
	                setTarget(module.getName());
	            }

	            cb.addActionListener(new ActionListener() {
	                @Override
	                public void actionPerformed(ActionEvent actionEvent) {
	                    stopLogging();
	                    final JCheckBox source = (JCheckBox) actionEvent.getSource();
	                    if (source.isSelected()) {
	                        getSettings().setLogExternalsOnly(false);
	                        setTarget(source.getText());
	                    }
	                    else {
	                        getSettings().setLogExternalsOnly(true);
	                    }
	                    startLogging();
	                }
	            });

	            moduleGroup.add(cb);
	            moduleSelectPanel.add(cb);
	            moduleSelectPanel.validate();
	        }
        }
    }

    private void setTarget(String name) {
        target = name.toUpperCase();
        getSettings().setTargetModule(target);
        for (Module module: getModuleList()) {
            if (module.getName().equalsIgnoreCase(name)) {
                getSettings().setDestinationTarget(module);
                final RadioButtonMenuItem fp =
                        (RadioButtonMenuItem) componentList.get("fastPoll");
                if (fp != null) {
                    fp.setEnabled(module.getFastPoll());
                    if(module.getFastPoll()) {
                        fp.setSelected(getSettings().isFastPoll());
                    }
                    else {
                        fp.setSelected(false);
                        getSettings().setFastPoll(false);
                    }
                }
            }
        }

        final String ecuId = ecuIdLabel.getText().split(": ")[1];
        ecuIdLabel.setText(buildEcuInfoLabelText(target + " ID", ecuId));

        for (String key : componentList.keySet()) {
            if (key.equals("ecuIdItem")) {
                final JMenuItem menuItem = (JMenuItem) componentList.get(key);
                menuItem.setText(replaceString(menuItem.getText(), target));
            }
            if (key.equals("resetMenu")) {
                final JMenuItem menuItem = (JMenuItem) componentList.get(key);
                menuItem.setText(MessageFormat.format(
                        rb.getString("RESET"),
                        getSettings().getDestinationTarget().getDescription(),
                        getSettings().getDestinationTarget().getName().toUpperCase()));
            }
            if (key.equals("reconnectButton") ||
                    key.equals("disconnectButton")) {
                final JButton button = (JButton) componentList.get(key);
                button.setToolTipText(replaceString(button.getToolTipText(), target));
            }
        }
    }

    private String replaceString(String inString, String newString) {
        return inString.replaceAll("[A-Z]{3}", newString);
    }


	private Transport getTransportById(String id) {
        Transport loggerTransport = null;
        Map<Transport, Collection<Module>> transportMap = getTransportMap();

        for (Transport transport : transportMap.keySet()) {
            if (transport.getId().equalsIgnoreCase(id))
                loggerTransport = transport;
        }

        return loggerTransport;
    }

	public void updateElmSelectable() {
		boolean value = getSettings().isObdProtocol();
		RadioButtonMenuItem c = (RadioButtonMenuItem)getComponentList().get("elmEnabled");
		c.setEnabled(value);

		if(!value) {
			c.setSelected(false);
			getSettings().setElm327Enabled(false);
		}
	}

    private Map<Transport, Collection<Module>> getTransportMap() {
        return protocolList.get(getSettings().getLoggerProtocol());
    }

    private Collection<Module> getModuleList() {
        return getTransportMap().get(getTransportById(getSettings().getTransportProtocol()));
    }

    public String getDefVersion() {
        return defVersion;
    }

    public String getTarget() {
        return target;
    }

    public Map<String, Map<Transport, Collection<Module>>> getProtocolList() {
        return protocolList;
    }

    public Map<String, Object> getComponentList() {
        return componentList;
    }

    public void restartLogging() {
        stopLogging();
        startLogging();
    }

    private StatusIndicator buildStatusIndicator() {
        statusIndicator = new StatusIndicator();
        controller.addListener(statusIndicator);
        fileUpdateHandler.addListener(statusIndicator);
        return statusIndicator;
    }

    private JComponent buildDataTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton resetButton;

        if (touchEnabled == false)
        {
            resetButton = new JButton(rb.getString("RESETDATA"));
        }
        else
        {
            resetButton = new JButton("<html><body leftmargin=15 topmargin=15 marginwidth=15 marginheight=15>Reset Data</body></html>");
        }
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                liveDataUpdateHandler.reset();
            }
        });
        panel.add(resetButton, NORTH);
        JScrollPane sp = new JScrollPane(new JTable(dataTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(40);
        panel.add(sp, CENTER);
        return panel;
    }

    private JComponent buildGraphTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton resetButton;

        if (touchEnabled == false)
        {
            resetButton = new JButton(rb.getString("RESETDATA"));
        }
        else
        {
            resetButton = new JButton("<html><body leftmargin=15 topmargin=15 marginwidth=15 marginheight=15>Reset Data</body></html>");
        }
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                graphUpdateHandler.reset();
            }
        });
        panel.add(resetButton, NORTH);
        JScrollPane sp = new JScrollPane(graphPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setUnitIncrement(40);
        panel.add(sp, CENTER);
        return panel;
    }

    private JComponent buildDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton resetButton;

        if (touchEnabled == false)
        {
            resetButton = new JButton(rb.getString("RESETDATA"));
        }
        else
        {
            resetButton = new JButton("<html><body leftmargin=15 topmargin=15 marginwidth=15 marginheight=15>Reset Data</body></html>");
        }
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dashboardUpdateHandler.reset();
            }
        });
        panel.add(resetButton, NORTH);
        JScrollPane sp = new JScrollPane(dashboardPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(40);
        panel.add(sp, CENTER);
        return panel;
    }

    private void selectTab(int tabIndex) {
        int count = tabbedPane.getComponentCount();
        if (tabIndex >= 0 && tabIndex < count)
            tabbedPane.setSelectedIndex(tabIndex);
    }

    @Override
    public void windowOpened(WindowEvent windowEvent) {
    }

    @Override
    public void windowClosing(WindowEvent windowEvent) {
        handleExit();
    }

    @Override
    public void windowClosed(WindowEvent windowEvent) {
    }

    @Override
    public void windowIconified(WindowEvent windowEvent) {
    }

    @Override
    public void windowDeiconified(WindowEvent windowEvent) {
    }

    @Override
    public void windowActivated(WindowEvent windowEvent) {
    }

    @Override
    public void windowDeactivated(WindowEvent windowEvent) {
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
    }

    public boolean isLogging() {
        return controller.isStarted();
    }

    public void startLogging() {
        controller.start();
    }

    public void stopLogging() {
        controller.stop();
    }

    private void stopPlugins() {
        for (ExternalDataSource dataSource : externalDataSources) {
            try {
                dataSource.disconnect();
            } catch (Exception e) {
                LOGGER.warn("Error stopping datasource: " + dataSource.getName(), e);
            }
        }
    }

    public boolean resetEcu(int resetCode) {
        return resetManager.resetEcu(resetCode);
    }

    public final boolean getDtcodesEmpty() {
        LOGGER.info("DT codes defined: " + !dtcodes.isEmpty() +
                ", definition version: " + defVersion);
        return dtcodes.isEmpty();
    }

    public final boolean isEcuInit() {
        if (ecuInit == null) {
            LOGGER.info("DT codes ECU Initialized: false");
            return false;
        }
        return true;
    }

    public final int readEcuCodes() {
        final ReadCodesManager readCodesManager =
                new ReadCodesManagerImpl(
                        this,
                        dtcodes,
                        ecuInit.getEcuInitBytes().length);
        return readCodesManager.readCodes();
    }

    public final int ecuGlobalAdjustment() {
        final GlobalAdjustManager globalAdjustManager =
                new SSMGlobalAdjustManager(
                        this,
                        dataTabParamListTableModel);
        return globalAdjustManager.ecuGlobalAdjustments();
    }

    public final void readLearningTables() {
        final EcuDefinition ecuDef = new EvaluateEcuDefinition().getDef(
                getSettings().getLoggerEcuDefinitionMap(),
                ecuInit.getEcuId());
        final LearningTableValues learningTablesManager =
                LearningTableValuesFactory.getManager(
                        getSettings().getLoggerProtocol());
        learningTablesManager.init(
                this,
                dataTabParamListTableModel,
                ecuDef);
        learningTablesManager.execute();
    }

    public final List<ExternalDataSource> getExternalDataSources() {
        return externalDataSources;
    }

    public void handleExit() {
        try {
            try {
                try {
                    stopLogging();
                } finally {
                    stopPlugins();
                }
            } finally {
                cleanUpUpdateHandlers();
            }
        } catch (Exception e) {
            LOGGER.warn("Error stopping logger:", e);
        } finally {
            saveSettings();
            backupCurrentProfile();
            LOGGER.info("Logger shutdown successful");

            if(ECUEditorManager.getECUEditorWithoutCreation() == null) {
            	System.exit(0);
            }
            else {
            	instance = null;
            }
        }
    }

    private void saveSettings() {
        dynoTab.saveSettings();
        getSettings().setLoggerSelectedGaugeIndex(dashboardUpdateHandler.styleIndex);
        getSettings().setLoggerPortDefault((String) portsComboBox.getSelectedItem());
        getSettings().setLoggerWindowMaximized(getExtendedState() == MAXIMIZED_BOTH);
        getSettings().setLoggerWindowSize(getSize());
        getSettings().setLoggerWindowLocation(getLocation());
        if (getSettings().getLoggerParameterListState()) {
            final Component c = tabbedPane.getSelectedComponent();
            if (c instanceof JSplitPane) {
                // Only save the divider location if there is one
                final JSplitPane sp = (JSplitPane) c.getComponentAt(100, 100);
                getSettings().setLoggerDividerLocation(sp.getDividerLocation());
            }
        }
        getSettings().setLoggerSelectedTabIndex(tabbedPane.getSelectedIndex());
        getSettings().setLoggerPluginPorts(getPluginPorts(externalDataSources));
        try {
            SettingsManager.save(getSettings());
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Logger settings saved");
        } catch (Exception e) {
            LOGGER.warn("Error saving logger settings:", e);
        }
    }

    private void backupCurrentProfile() {
        try {
            saveProfileToFile(getCurrentProfile(), new File(HOME + BACKUP_PROFILE));
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Backup profile saved");
        } catch (Exception e) {
            LOGGER.warn("Error backing up profile", e);
        }
    }

    private void cleanUpUpdateHandlers() {
        fileUpdateHandler.cleanUp();
        dataHandlerManager.cleanUp();
        graphHandlerManager.cleanUp();
        dashboardHandlerManager.cleanUp();
    }

    public Settings getSettings() {
        return SettingsManager.getSettings();
    }

    @Override
    public void reportMessage(final String message) {
        if (message != null) {
            invokeLater(new Runnable() {
                @Override
                public void run() {
                    messageLabel.setText(message);
                    messageLabel.setForeground(BLACK);
                }
            });
        }
    }

    @Override
    public void reportMessageInTitleBar(String message) {
        if (!isNullOrEmpty(message)) setTitle(message);
    }

    @Override
    public void reportStats(final String message) {
        if (!isNullOrEmpty(message)) {
            invokeLater(new Runnable() {
                @Override
                public void run() {
                    statsLabel.setText(message);
                }
            });
        }
    }

    private JLabel buildStatsLabel() {
        JLabel label = new JLabel(" ");
        label.setForeground(BLACK);
        label.setHorizontalTextPosition(RIGHT);
        return label;
    }

    @Override
    public void reportError(final String error) {
        if (!isNullOrEmpty(error)) {
            invokeLater(new Runnable() {
                @Override
                public void run() {
                    messageLabel.setText(MessageFormat.format(
                            rb.getString("ERROR"), error));
                    messageLabel.setForeground(RED);
                }
            });
        }
    }

    @Override
    public void reportError(Exception e) {
        if (e != null) {
            LOGGER.error("Error occurred", e);
            String error = e.getMessage();
            if (!isNullOrEmpty(error)) reportError(error);
            else reportError(e.toString());
        }
    }

    @Override
    public void reportError(String error, Exception e) {
        if (e != null) LOGGER.error(error, e);
        reportError(error);
    }

    @Override
    public void setTitle(String title) {
        if (title != null) {
            if (!title.startsWith(ECU_LOGGER_TITLE)) {
                title = ECU_LOGGER_TITLE + (title.length() == 0 ? "" : " - " + title);
            }
            super.setTitle(title);
        }
    }

    public static boolean isTouchEnabled()
    {
        if( touchEnabled == true)
        {
            return true;
        }
        return false;
    }

    public void setRefreshMode(boolean refreshMode) {
        getSettings().setRefreshMode(refreshMode);
        refresher.setRefreshMode(refreshMode);
    }
    
    public void setAutoConnect(boolean connect) {
        getSettings().setAutoConnectOnStartup(connect);
    }

	public void setElmEnabled(Boolean value) {
        getSettings().setElm327Enabled(value);
	}

    private JProgressBar startbar() {
        startStatus = new JWindow();
        startStatus.setAlwaysOnTop(true);
        startStatus.setLocation(
                (int)(getSettings().getLoggerWindowSize().getWidth()/2 + getSettings().getLoggerWindowLocation().getX() - 150),
                (int)(getSettings().getLoggerWindowSize().getHeight()/2 + getSettings().getLoggerWindowLocation().getY() - 36));
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setIndeterminate(false);
        progressBar.setOpaque(true);
        startText.setOpaque(true);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(startText, BorderLayout.SOUTH);
        startStatus.getContentPane().add(panel);
        startStatus.pack();
        startStatus.setVisible(true);
        return progressBar;
    }

    //**********************************************************************

    public static void startLogger(int defaultCloseOperation, ECUEditor ecuEditor, String[] args) {
        touchEnabled = setTouchEnabled(args);
        boolean fullscreen = containsFullScreenArg(args);
        EcuLogger ecuLogger = getEcuLogger(ecuEditor);
        createAndShowGui(defaultCloseOperation, ecuLogger, fullscreen);
        if (ecuLogger.getSettings().getAutoConnectOnStartup() && !ecuLogger.isLogging()) ecuLogger.startLogging();
    }

    private static boolean containsFullScreenArg(String... args) {
    	if(args == null) return false;

        for (String arg : args) {
            if (LOGGER_FULLSCREEN_ARG.equalsIgnoreCase(arg)) return true;
        }
        return false;
    }

    private static boolean setTouchEnabled(String... args) {
    	if(args == null) return false;

        for (String arg : args) {
            if (LOGGER_TOUCH_ARG.equalsIgnoreCase(arg)) return true;
        }
        return false;
    }

    private static void createAndShowGui(final int defaultCloseOperation, final EcuLogger ecuLogger, final boolean fullscreen) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                doCreateAndShowGui(defaultCloseOperation, ecuLogger, fullscreen);
            }
        });
    }

    private static void doCreateAndShowGui(int defaultCloseOperation, EcuLogger ecuLogger, boolean fullscreen) {
        Settings settings = ecuLogger.getSettings();

        // set window properties
        ecuLogger.pack();
        ecuLogger.selectTab(settings.getLoggerSelectedTabIndex());
        ecuLogger.setRefreshMode(settings.getRefreshMode());

        if (fullscreen) {
            // display full screen
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice device = env.getDefaultScreenDevice();
            JFrame frame = new JFrame(ecuLogger.getTitle());
            frame.setIconImage(new ImageIcon(ICON_PATH).getImage());
            frame.setContentPane(ecuLogger.getContentPane());
            frame.addWindowListener(ecuLogger);
            frame.setDefaultCloseOperation(defaultCloseOperation);
            frame.setUndecorated(true);
            frame.setResizable(false);
            device.setFullScreenWindow(frame);
        } else {
            // display in window
            ecuLogger.addWindowListener(ecuLogger);
            ecuLogger.setIconImage(new ImageIcon(ICON_PATH).getImage());
            ecuLogger.setSize(settings.getLoggerWindowSize());
            ecuLogger.setLocation(settings.getLoggerWindowLocation());
            if (settings.isLoggerWindowMaximized()) ecuLogger.setExtendedState(MAXIMIZED_BOTH);
            ecuLogger.setDefaultCloseOperation(defaultCloseOperation);
            ecuLogger.setVisible(true);
        }
    }


}
