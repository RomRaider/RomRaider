/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
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

import static com.centerkey.utils.BareBonesBrowserLaunch.openURL;
import com.romraider.Settings;
import static com.romraider.Version.LOGGER_DEFS_URL;
import static com.romraider.Version.PRODUCT_NAME;
import static com.romraider.Version.VERSION;
import com.romraider.editor.ecu.ECUEditor;
import com.romraider.io.serial.port.SerialPortRefresher;
import com.romraider.logger.ecu.comms.controller.LoggerController;
import com.romraider.logger.ecu.comms.controller.LoggerControllerImpl;
import com.romraider.logger.ecu.comms.query.EcuInit;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.reset.ResetManager;
import com.romraider.logger.ecu.comms.reset.ResetManagerImpl;
import com.romraider.logger.ecu.definition.EcuDataLoader;
import com.romraider.logger.ecu.definition.EcuDataLoaderImpl;
import com.romraider.logger.ecu.definition.EcuDefinition;
import com.romraider.logger.ecu.definition.EcuParameter;
import com.romraider.logger.ecu.definition.EcuSwitch;
import com.romraider.logger.ecu.definition.ExternalData;
import com.romraider.logger.ecu.definition.ExternalDataImpl;
import com.romraider.logger.ecu.definition.LoggerData;
import com.romraider.logger.ecu.exception.PortNotFoundException;
import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.ecu.external.ExternalDataSource;
import com.romraider.logger.ecu.external.ExternalDataSourceLoader;
import com.romraider.logger.ecu.external.ExternalDataSourceLoaderImpl;
import com.romraider.logger.ecu.profile.UserProfile;
import com.romraider.logger.ecu.profile.UserProfileImpl;
import com.romraider.logger.ecu.profile.UserProfileItem;
import com.romraider.logger.ecu.profile.UserProfileItemImpl;
import com.romraider.logger.ecu.profile.UserProfileLoader;
import static com.romraider.logger.ecu.profile.UserProfileLoader.BACKUP_PATH;
import com.romraider.logger.ecu.profile.UserProfileLoaderImpl;
import com.romraider.logger.ecu.ui.DataRegistrationBroker;
import com.romraider.logger.ecu.ui.DataRegistrationBrokerImpl;
import com.romraider.logger.ecu.ui.EcuDataComparator;
import com.romraider.logger.ecu.ui.MessageListener;
import com.romraider.logger.ecu.ui.SerialPortComboBox;
import com.romraider.logger.ecu.ui.StatusIndicator;
import com.romraider.logger.ecu.ui.handler.DataUpdateHandlerManager;
import com.romraider.logger.ecu.ui.handler.DataUpdateHandlerManagerImpl;
import com.romraider.logger.ecu.ui.handler.dash.DashboardUpdateHandler;
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
import com.romraider.logger.ecu.ui.swing.menubar.action.ToggleButtonAction;
import static com.romraider.logger.ecu.ui.swing.menubar.util.FileHelper.saveProfileToFile;
import com.romraider.logger.ecu.ui.swing.vertical.VerticalTextIcon;
import static com.romraider.logger.ecu.ui.swing.vertical.VerticalTextIcon.ROTATE_LEFT;
import com.romraider.logger.ecu.ui.tab.injector.InjectorTab;
import com.romraider.logger.ecu.ui.tab.injector.InjectorTabImpl;
import com.romraider.logger.ecu.ui.tab.maf.MafTab;
import com.romraider.logger.ecu.ui.tab.maf.MafTabImpl;
import com.romraider.swing.AbstractFrame;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.isNullOrEmpty;
import com.romraider.util.SettingsManagerImpl;
import com.romraider.util.ThreadUtil;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static com.romraider.util.ThreadUtil.sleep;
import org.apache.log4j.Logger;
import javax.swing.AbstractAction;
import static javax.swing.BorderFactory.createLoweredBevelBorder;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import javax.swing.JFrame;
import javax.swing.JLabel;
import static javax.swing.JLabel.RIGHT;
import javax.swing.JMenuBar;
import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JOptionPane.showOptionDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
import javax.swing.JSeparator;
import static javax.swing.JSeparator.VERTICAL;
import javax.swing.JSplitPane;
import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;
import javax.swing.JTabbedPane;
import static javax.swing.JTabbedPane.BOTTOM;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import static javax.swing.KeyStroke.getKeyStroke;
import static javax.swing.SwingUtilities.invokeLater;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.BorderLayout.WEST;
import static java.awt.Color.BLACK;
import static java.awt.Color.RED;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import static java.lang.System.currentTimeMillis;
import java.util.ArrayList;
import static java.util.Collections.sort;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
    private static final String ECU_LOGGER_TITLE = PRODUCT_NAME + " v" + VERSION + " | ECU Logger";
    private static final String LOGGER_FULLSCREEN_ARG = "-logger.fullscreen";
    private static final String ICON_PATH = "./graphics/romraider-ico.gif";
    private static final String HEADING_PARAMETERS = "Parameters";
    private static final String HEADING_SWITCHES = "Switches";
    private static final String HEADING_EXTERNAL = "External";
    private static final String CAL_ID_LABEL = "CAL ID";
    private static final String ECU_ID_LABEL = "ECU ID";
    private ECUEditor ecuEditor;
    private Settings settings;
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
    private EcuInit ecuInit;
    private JToggleButton logToFileButton;
    private List<ExternalDataSource> externalDataSources;
    private List<EcuParameter> ecuParams;

    public EcuLogger(Settings settings) {
        super(ECU_LOGGER_TITLE);
        construct(settings);
    }

    public EcuLogger(ECUEditor ecuEditor) {
        super(ECU_LOGGER_TITLE);
        this.ecuEditor = ecuEditor;
        construct(ecuEditor.getSettings());
    }

    private void construct(Settings settings) {
        checkNotNull(settings);
        this.settings = settings;
        bootstrap();
        loadEcuDefs();
        loadLoggerPlugins();
        loadLoggerParams();
        initControllerListeners();
        initUserInterface();
        initDataUpdateHandlers();
        startPortRefresherThread();
        if (!isLogging()) startLogging();
    }

    private void bootstrap() {
        EcuInitCallback ecuInitCallback = new EcuInitCallback() {
            public void callback(EcuInit newEcuInit) {
                final String ecuId = newEcuInit.getEcuId();
                LOGGER.info("ECU ID = " + ecuId);
                if (ecuInit == null || !ecuInit.getEcuId().equals(ecuId)) {
                    ecuInit = newEcuInit;
                    invokeLater(new Runnable() {
                        public void run() {
                            String calId = getCalId(ecuId);
                            LOGGER.info("CAL ID = " + calId);
                            calIdLabel.setText(buildEcuInfoLabelText(CAL_ID_LABEL, calId));
                            ecuIdLabel.setText(buildEcuInfoLabelText(ECU_ID_LABEL, ecuId));
                            LOGGER.info("Loading logger config for new ECU (ecuid: " + ecuId + ")...");
                            loadLoggerParams();
                            loadUserProfile(settings.getLoggerProfileFilePath());
                        }

                        private String getCalId(String ecuId) {
                            Map<String, EcuDefinition> ecuDefinitionMap = settings.getLoggerEcuDefinitionMap();
                            if (ecuDefinitionMap == null) return null;
                            EcuDefinition def = ecuDefinitionMap.get(ecuId);
                            return def == null ? null : def.getCalId();
                        }
                    });
                }
            }
        };
        fileUpdateHandler = new FileUpdateHandlerImpl(settings, this);
        dataTableModel = new LiveDataTableModel();
        liveDataUpdateHandler = new LiveDataUpdateHandler(dataTableModel);
        graphPanel = new JPanel(new BorderLayout(2, 2));
        graphUpdateHandler = new GraphUpdateHandler(graphPanel);
        dashboardPanel = new JPanel(new BetterFlowLayout(FlowLayout.CENTER, 3, 3));
        dashboardUpdateHandler = new DashboardUpdateHandler(dashboardPanel);
        mafUpdateHandler = new MafUpdateHandler();
        injectorUpdateHandler = new InjectorUpdateHandler();
        controller = new LoggerControllerImpl(settings, ecuInitCallback, this, liveDataUpdateHandler,
                graphUpdateHandler, dashboardUpdateHandler, mafUpdateHandler, injectorUpdateHandler,
                fileUpdateHandler, TableUpdateHandler.getInstance());
        mafHandlerManager = new DataUpdateHandlerManagerImpl();
        mafTabBroker = new DataRegistrationBrokerImpl(controller, mafHandlerManager);
        mafTab = new MafTabImpl(mafTabBroker, ecuEditor);
        mafUpdateHandler.setMafTab(mafTab);
        injectorHandlerManager = new DataUpdateHandlerManagerImpl();
        injectorTabBroker = new DataRegistrationBrokerImpl(controller, injectorHandlerManager);
        injectorTab = new InjectorTabImpl(injectorTabBroker, ecuEditor);
        injectorUpdateHandler.setInjectorTab(injectorTab);
        resetManager = new ResetManagerImpl(settings, this);
        messageLabel = new JLabel(ECU_LOGGER_TITLE);
        calIdLabel = new JLabel(buildEcuInfoLabelText(CAL_ID_LABEL, null));
        ecuIdLabel = new JLabel(buildEcuInfoLabelText(ECU_ID_LABEL, null));
        statsLabel = buildStatsLabel();
        tabbedPane = new JTabbedPane(BOTTOM);
        portsComboBox = new SerialPortComboBox(settings);
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
        SerialPortRefresher refresher = new SerialPortRefresher(portsComboBox, settings.getLoggerPortDefault());
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
            sleep(100);
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
            Vector<File> ecuDefFiles = settings.getEcuDefinitionFiles();
            if (!ecuDefFiles.isEmpty()) {
                EcuDataLoader dataLoader = new EcuDataLoaderImpl();
                for (File ecuDefFile : ecuDefFiles) {
                    dataLoader.loadEcuDefsFromXml(ecuDefFile);
                    ecuDefinitionMap.putAll(dataLoader.getEcuDefinitionMap());
                }
            }
            settings.setLoggerEcuDefinitionMap(ecuDefinitionMap);
        } catch (Exception e) {
            reportError(e);
        }
    }

    private void loadLoggerConfig() {
        String loggerConfigFilePath = settings.getLoggerDefinitionFilePath();
        if (isNullOrEmpty(loggerConfigFilePath)) showMissingConfigDialog();
        else {
            try {
                EcuDataLoader dataLoader = new EcuDataLoaderImpl();
                dataLoader.loadConfigFromXml(loggerConfigFilePath, settings.getLoggerProtocol(),
                        settings.getFileLoggingControllerSwitchId(), ecuInit);
                List<EcuParameter> ecuParams = dataLoader.getEcuParameters();
                addConvertorUpdateListeners(ecuParams);
                loadEcuParams(ecuParams);
                loadEcuSwitches(dataLoader.getEcuSwitches());
                initFileLoggingController(dataLoader.getFileLoggingControllerSwitch());
                settings.setLoggerConnectionProperties(dataLoader.getConnectionProperties());
            } catch (Exception e) {
                reportError(e);
            }
        }
    }

    private void showMissingConfigDialog() {
        Object[] options = {"Yes", "No"};
        int answer = showOptionDialog(this,
                "Logger definition not configured.\nGo online to download the latest definition file?",
                "Configuration", DEFAULT_OPTION, WARNING_MESSAGE, null, options, options[0]);
        if (answer == 0) {
            openURL(LOGGER_DEFS_URL);
        } else {
            showMessageDialog(this,
                    "The Logger definition file needs to be configured before connecting to the ECU.\nMenu: Settings > Logger Definition Location...",
                    "Configuration", INFORMATION_MESSAGE);
            reportError("Logger definition file not found");
        }
    }

    private void loadLoggerPlugins() {
        try {
            ExternalDataSourceLoader dataSourceLoader = new ExternalDataSourceLoaderImpl();
            dataSourceLoader.loadExternalDataSources();
            externalDataSources = dataSourceLoader.getExternalDataSources();
        } catch (Exception e) {
            reportError(e);
        }
    }

    private void loadFromExternalDataSources() {
        try {
            List<ExternalData> externalDatas = getExternalData(externalDataSources);
            loadExternalDatas(externalDatas);
        } catch (Exception e) {
            reportError(e);
        }
    }

    public void loadUserProfile(String profileFilePath) {
        try {
            UserProfileLoader profileLoader = new UserProfileLoaderImpl();
            String path = isNullOrEmpty(profileFilePath) ? BACKUP_PATH : profileFilePath;
            UserProfile profile = profileLoader.loadProfile(path);
            applyUserProfile(profile);
            File profileFile = new File(path);
            if (profileFile.exists()) reportMessageInTitleBar("Profile: " + profileFile.getAbsolutePath());
        } catch (Exception e) {
            reportError(e);
        }
    }

    private void initFileLoggingController(final EcuSwitch fileLoggingControllerSwitch) {
        controller.setFileLoggerSwitchMonitor(new FileLoggerControllerSwitchMonitorImpl(fileLoggingControllerSwitch, new FileLoggerControllerSwitchHandler() {
            boolean oldDefogStatus = false;

            public void handleSwitch(double switchValue) {
                boolean logToFile = (int) switchValue == 1;
                if (settings.isFileLoggingControllerSwitchActive() && logToFile != oldDefogStatus) {
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

    private void applyUserProfile(UserProfile profile) {
        if (profile != null) {
            applyUserProfileToLiveDataTabParameters(dataTabParamListTableModel, profile);
            applyUserProfileToLiveDataTabParameters(dataTabSwitchListTableModel, profile);
            applyUserProfileToLiveDataTabParameters(dataTabExternalListTableModel, profile);
            applyUserProfileToGraphTabParameters(graphTabParamListTableModel, profile);
            applyUserProfileToGraphTabParameters(graphTabSwitchListTableModel, profile);
            applyUserProfileToGraphTabParameters(graphTabExternalListTableModel, profile);
            applyUserProfileToDashTabParameters(dashboardTabParamListTableModel, profile);
            applyUserProfileToDashTabParameters(dashboardTabSwitchListTableModel, profile);
            applyUserProfileToDashTabParameters(dashboardTabExternalListTableModel, profile);
        }
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
                reportError("Error loading plugin: " + dataSource.getName() + " v" + dataSource.getVersion(), e);
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
        return new UserProfileImpl(paramProfileItems, switchProfileItems, externalProfileItems);
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
        addSplitPaneTab("Data", buildSplitPane(buildParamListPane(dataTabParamListTableModel, dataTabSwitchListTableModel, dataTabExternalListTableModel), buildDataTab()));
        addSplitPaneTab("Graph", buildSplitPane(buildParamListPane(graphTabParamListTableModel, graphTabSwitchListTableModel, graphTabExternalListTableModel), buildGraphTab()));
        addSplitPaneTab("Dashboard", buildSplitPane(buildParamListPane(dashboardTabParamListTableModel, dashboardTabSwitchListTableModel, dashboardTabExternalListTableModel), buildDashboardTab()), buildToggleGaugeStyleButton());
        tabbedPane.add("MAF", mafTab.getPanel());
        tabbedPane.add("Injector", injectorTab.getPanel());
        return tabbedPane;
    }

    private JButton buildToggleGaugeStyleButton() {
        final JButton button = new JButton();
        VerticalTextIcon textIcon = new VerticalTextIcon(button, "Gauge Style", ROTATE_LEFT);
        button.setIcon(textIcon);
        button.setPreferredSize(new Dimension(25, 90));
        button.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(getKeyStroke("F12"), "toggleGaugeStyle");
        button.getActionMap().put("toggleGaugeStyle", new AbstractAction() {
            private static final long serialVersionUID = 6913964758354638587L;

            public void actionPerformed(ActionEvent e) {
                button.doClick();
            }
        });
        button.addActionListener(new AbstractAction() {
            private static final long serialVersionUID = 123232894767995264L;

            public void actionPerformed(ActionEvent e) {
                dashboardUpdateHandler.toggleGaugeStyle();
            }
        });
        return button;
    }

    private void addSplitPaneTab(String name, final JSplitPane splitPane, JComponent... extraControls) {
        final JToggleButton toggleListButton = new JToggleButton();
        toggleListButton.setSelected(true);
        VerticalTextIcon textIcon = new VerticalTextIcon(toggleListButton, "Parameter List", ROTATE_LEFT);
        toggleListButton.setIcon(textIcon);
        toggleListButton.setPreferredSize(new Dimension(25, 90));
        toggleListButton.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(getKeyStroke("F11"), "toggleHideParams");
        toggleListButton.getActionMap().put("toggleHideParams", new AbstractAction() {
            private static final long serialVersionUID = -276854997788647306L;

            public void actionPerformed(ActionEvent e) {
                toggleListButton.doClick();
            }
        });
        toggleListButton.addActionListener(new AbstractAction() {
            private static final long serialVersionUID = -1595098685575657317L;
            private final int min = 1;
            private int size = splitPane.getDividerLocation();

            public void actionPerformed(ActionEvent e) {
                int current = splitPane.getDividerLocation();
                if (toggleListButton.isSelected()) splitPane.setDividerLocation(size);
                else {
                    splitPane.setDividerLocation(min);
                    size = current;
                }
            }
        });

        JPanel tabControlPanel = new JPanel(new BetterFlowLayout(FlowLayout.CENTER, 1, 1));
        tabControlPanel.setPreferredSize(new Dimension(25, 25));
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
        tabs.addTab("ECU Parameters", paramList);
        tabs.addTab("ECU Switches", switchList);
        tabs.addTab("External Sensors", externalList);
        return tabs;
    }

    private JTable buildParamListTable(ParameterListTableModel tableModel) {
        JTable paramListTable = new ParameterListTable(tableModel);
        changeColumnWidth(paramListTable, 0, 20, 55, 55);
        changeColumnWidth(paramListTable, 2, 50, 250, 130);
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
        JSplitPane splitPane = new JSplitPane(HORIZONTAL_SPLIT, leftComponent, rightComponent);
        splitPane.setDividerSize(2);
        splitPane.setDividerLocation(500);
        splitPane.addPropertyChangeListener(this);
        return splitPane;
    }

    private JMenuBar buildMenubar() {
        return new EcuLoggerMenuBar(this, externalDataSources);
    }

    private JPanel buildControlToolbar() {
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(buildPortsComboBox(), WEST);
        //TODO: Finish log playback stuff...
//        controlPanel.add(buildPlaybackControls(), CENTER);
        controlPanel.add(buildStatusIndicator(), EAST);
        return controlPanel;
    }

    private Component buildPlaybackControls() {
        JButton playButton = new JButton("Play");
        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                ThreadUtil.runAsDaemon(new Runnable() {
                    public void run() {
                        PlaybackManagerImpl playbackManager = new PlaybackManagerImpl(ecuParams, liveDataUpdateHandler, graphUpdateHandler, dashboardUpdateHandler, mafUpdateHandler,
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

    private Component buildLogToFileButton() {
        logToFileButton = new JToggleButton("Log to file", new ImageIcon("./graphics/logger_log_to_file.png"));
        logToFileButton.setToolTipText("Start/stop file logging (F1)");
        logToFileButton.setPreferredSize(new Dimension(100, 25));
        logToFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (logToFileButton.isSelected()) {
                    fileUpdateHandler.start();
                } else {
                    fileUpdateHandler.stop();
                }
            }
        });
        logToFileButton.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(getKeyStroke("F1"), "toggleFileLogging");
        logToFileButton.getActionMap().put("toggleFileLogging", new ToggleButtonAction(this, logToFileButton));
        return logToFileButton;
    }

    private JPanel buildPortsComboBox() {
        portsComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                settings.setLoggerPort((String) portsComboBox.getSelectedItem());
                // this is a hack...
                if (!actionEvent.paramString().endsWith("modifiers=")) {
                    restartLogging();
                }
            }
        });
        JPanel comboBoxPanel = new JPanel(new FlowLayout());
        comboBoxPanel.add(new JLabel("COM Port:"));
        comboBoxPanel.add(portsComboBox);
        JButton reconnectButton = new JButton(new ImageIcon("./graphics/logger_restart.png"));
        reconnectButton.setPreferredSize(new Dimension(25, 25));
        reconnectButton.setToolTipText("Reconnect to ECU");
        reconnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    restartLogging();
                } catch (Exception e) {
                    reportError(e);
                }
            }
        });
        comboBoxPanel.add(reconnectButton);
        JButton disconnectButton = new JButton(new ImageIcon("./graphics/logger_stop.png"));
        disconnectButton.setPreferredSize(new Dimension(25, 25));
        disconnectButton.setToolTipText("Disconnect from ECU");
        disconnectButton.addActionListener(new ActionListener() {
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
        return comboBoxPanel;
    }

    public void restartLogging() {
        stopLogging();
        startLogging();
    }

    private StatusIndicator buildStatusIndicator() {
        StatusIndicator statusIndicator = new StatusIndicator();
        controller.addListener(statusIndicator);
        fileUpdateHandler.addListener(statusIndicator);
        return statusIndicator;
    }

    private JComponent buildDataTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton resetButton = new JButton("Reset Data");
        resetButton.addActionListener(new ActionListener() {
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
        JButton resetButton = new JButton("Reset Data");
        resetButton.addActionListener(new ActionListener() {
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
        JButton resetButton = new JButton("Reset Data");
        resetButton.addActionListener(new ActionListener() {
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
        if (tabIndex >= 0 && tabIndex < count) tabbedPane.setSelectedIndex(tabIndex);
    }

    public void windowOpened(WindowEvent windowEvent) {
    }

    public void windowClosing(WindowEvent windowEvent) {
        handleExit();
    }

    public void windowClosed(WindowEvent windowEvent) {
    }

    public void windowIconified(WindowEvent windowEvent) {
    }

    public void windowDeiconified(WindowEvent windowEvent) {
    }

    public void windowActivated(WindowEvent windowEvent) {
    }

    public void windowDeactivated(WindowEvent windowEvent) {
    }

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
    }

    public boolean isLogging() {
        return controller.isStarted();
    }

    public void startLogging() {
        String port = settings.getLoggerPort();
        if (isNullOrEmpty(port)) return;
        controller.start();
    }

    public void stopLogging() {
        controller.stop();
        sleep(1000L);
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

    public boolean resetEcu() {
        return resetManager.resetEcu();
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
            LOGGER.warn("Error stopping logger", e);
        } finally {
            saveSettings();
            backupCurrentProfile();
        }
    }

    private void saveSettings() {
        settings.setLoggerPortDefault((String) portsComboBox.getSelectedItem());
        settings.setLoggerWindowMaximized(getExtendedState() == MAXIMIZED_BOTH);
        settings.setLoggerWindowSize(getSize());
        settings.setLoggerWindowLocation(getLocation());
        settings.setLoggerSelectedTabIndex(tabbedPane.getSelectedIndex());
        new SettingsManagerImpl().save(settings);
    }

    private void backupCurrentProfile() {
        try {
            saveProfileToFile(getCurrentProfile(), new File(BACKUP_PATH));
        } catch (Exception e) {
            LOGGER.warn("Error backing up profile", e);
        }
    }

    private void cleanUpUpdateHandlers() {
        dataHandlerManager.cleanUp();
        graphHandlerManager.cleanUp();
        dashboardHandlerManager.cleanUp();
    }

    public Settings getSettings() {
        return settings;
    }

    public void reportMessage(final String message) {
        if (message != null) {
            invokeLater(new Runnable() {
                public void run() {
                    messageLabel.setText(message);
                    messageLabel.setForeground(BLACK);
                }
            });
        }
    }

    public void reportMessageInTitleBar(String message) {
        if (!isNullOrEmpty(message)) setTitle(message);
    }

    public void reportStats(final String message) {
        if (!isNullOrEmpty(message)) {
            invokeLater(new Runnable() {
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

    public void reportError(final String error) {
        if (!isNullOrEmpty(error)) {
            invokeLater(new Runnable() {
                public void run() {
                    messageLabel.setText("Error: " + error);
                    messageLabel.setForeground(RED);
                }
            });
        }
    }

    public void reportError(Exception e) {
        if (e != null) {
            LOGGER.error("Error occurred", e);
            String error = e.getMessage();
            if (!isNullOrEmpty(error)) reportError(error);
            else reportError(e.toString());
        }
    }

    public void reportError(String error, Exception e) {
        if (e != null) LOGGER.error(error, e);
        reportError(error);
    }

    public void setTitle(String title) {
        if (title != null) {
            if (!title.startsWith(ECU_LOGGER_TITLE)) {
                title = ECU_LOGGER_TITLE + (title.length() == 0 ? "" : " - " + title);
            }
            super.setTitle(title);
        }
    }

    //**********************************************************************


    public static void startLogger(int defaultCloseOperation, ECUEditor ecuEditor) {
        EcuLogger ecuLogger = new EcuLogger(ecuEditor);
        createAndShowGui(defaultCloseOperation, ecuLogger, false);
    }

    public static void startLogger(int defaultCloseOperation, Settings settings, String... args) {
        EcuLogger ecuLogger = new EcuLogger(settings);
        boolean fullscreen = containsFullScreenArg(args);
        createAndShowGui(defaultCloseOperation, ecuLogger, fullscreen);
    }

    private static boolean containsFullScreenArg(String... args) {
        for (String arg : args) {
            if (LOGGER_FULLSCREEN_ARG.equals(arg)) return true;
        }
        return false;
    }

    private static void createAndShowGui(final int defaultCloseOperation, final EcuLogger ecuLogger, final boolean fullscreen) {
        invokeLater(new Runnable() {
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
