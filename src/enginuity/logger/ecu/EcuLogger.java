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

package enginuity.logger.ecu;

import enginuity.Settings;
import enginuity.io.port.SerialPortRefresher;
import enginuity.logger.ecu.comms.controller.LoggerController;
import enginuity.logger.ecu.comms.controller.LoggerControllerImpl;
import enginuity.logger.ecu.comms.query.EcuInit;
import enginuity.logger.ecu.comms.query.EcuInitCallback;
import enginuity.logger.ecu.definition.EcuDataLoader;
import enginuity.logger.ecu.definition.EcuDataLoaderImpl;
import enginuity.logger.ecu.definition.EcuDefinition;
import enginuity.logger.ecu.definition.EcuParameter;
import enginuity.logger.ecu.definition.EcuSwitch;
import enginuity.logger.ecu.definition.ExternalData;
import enginuity.logger.ecu.definition.LoggerData;
import enginuity.logger.ecu.external.ExternalDataSourceLoader;
import enginuity.logger.ecu.external.ExternalDataSourceLoaderImpl;
import enginuity.logger.ecu.profile.UserProfile;
import enginuity.logger.ecu.profile.UserProfileImpl;
import enginuity.logger.ecu.profile.UserProfileItem;
import enginuity.logger.ecu.profile.UserProfileItemImpl;
import enginuity.logger.ecu.profile.UserProfileLoader;
import enginuity.logger.ecu.profile.UserProfileLoaderImpl;
import enginuity.logger.ecu.ui.DataRegistrationBroker;
import enginuity.logger.ecu.ui.DataRegistrationBrokerImpl;
import enginuity.logger.ecu.ui.EcuDataComparator;
import enginuity.logger.ecu.ui.MessageListener;
import enginuity.logger.ecu.ui.SerialPortComboBox;
import enginuity.logger.ecu.ui.StatusIndicator;
import enginuity.logger.ecu.ui.handler.DataUpdateHandlerManager;
import enginuity.logger.ecu.ui.handler.DataUpdateHandlerManagerImpl;
import enginuity.logger.ecu.ui.handler.dash.DashboardUpdateHandler;
import enginuity.logger.ecu.ui.handler.file.FileLoggerControllerSwitchHandler;
import enginuity.logger.ecu.ui.handler.file.FileLoggerControllerSwitchMonitorImpl;
import enginuity.logger.ecu.ui.handler.file.FileUpdateHandlerImpl;
import enginuity.logger.ecu.ui.handler.graph.GraphUpdateHandler;
import enginuity.logger.ecu.ui.handler.livedata.LiveDataTableModel;
import enginuity.logger.ecu.ui.handler.livedata.LiveDataUpdateHandler;
import enginuity.logger.ecu.ui.handler.table.TableUpdateHandler;
import enginuity.logger.ecu.ui.paramlist.ParameterListTable;
import enginuity.logger.ecu.ui.paramlist.ParameterListTableModel;
import enginuity.logger.ecu.ui.paramlist.ParameterRow;
import enginuity.logger.ecu.ui.swing.menubar.EcuLoggerMenuBar;
import enginuity.logger.ecu.ui.swing.menubar.action.ToggleButtonAction;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.isNullOrEmpty;
import static enginuity.util.ThreadUtil.runAsDaemon;
import static enginuity.util.ThreadUtil.sleep;

import static javax.swing.BorderFactory.createLoweredBevelBorder;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import javax.swing.JFrame;
import javax.swing.JLabel;
import static javax.swing.JLabel.RIGHT;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
import javax.swing.JSeparator;
import static javax.swing.JSeparator.VERTICAL;
import javax.swing.JSplitPane;
import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;
import javax.swing.JTabbedPane;
import static javax.swing.JTabbedPane.BOTTOM;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import static javax.swing.KeyStroke.getKeyStroke;
import javax.swing.SwingUtilities;
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
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
TODO: parse ecu info from ecu defs (old and new formats) based on ecu id and display in UI
TODO: Rewrite user profile application and saving to allow tab specific settings (eg. warn levels on dash tab)
TODO: Add custom graph tab (eg. engine speed vs. boost, etc.)
TODO: Add log analysis tab (or maybe new window?), including log playback, custom graphs, map compare, etc
*/

public final class EcuLogger extends JFrame implements WindowListener, PropertyChangeListener, MessageListener {
    private static final String ENGINUITY_ECU_LOGGER_TITLE = "Enginuity ECU Logger";
    private static final String HEADING_PARAMETERS = "Parameters";
    private static final String HEADING_SWITCHES = "Switches";
    private static final String HEADING_EXTERNAL = "External";
    private static final String CAL_ID_LABEL = "CAL ID";
    private static final String ECU_ID_LABEL = "ECU ID";
    private Settings settings;
    private LoggerController controller;
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
    private EcuInit ecuInit;
    private JToggleButton logToFileButton;

    public EcuLogger(Settings settings) {
        super(ENGINUITY_ECU_LOGGER_TITLE);
        bootstrap(settings);
        loadEcuDefs();
        loadLoggerConfig();
        loadLoggerPlugins();
        initControllerListeners();
        initUserInterface();
        initDataUpdateHandlers();
        startPortRefresherThread();
        if (!isLogging()) {
            startLogging();
        }
    }

    private void bootstrap(final Settings settings) {
        checkNotNull(settings);
        this.settings = settings;
        EcuInitCallback ecuInitCallback = new EcuInitCallback() {
            public void callback(EcuInit newEcuInit) {
                System.out.println("ECU ID = " + newEcuInit.getEcuId());
                if (ecuInit == null || !ecuInit.getEcuId().equals(newEcuInit.getEcuId())) {
                    ecuInit = newEcuInit;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            String ecuId = ecuInit.getEcuId();
                            Map<String, EcuDefinition> ecuDefinitionMap = settings.getLoggerEcuDefinitionMap();
                            if (!isNullOrEmpty(ecuDefinitionMap)) {
                                String calId = ecuDefinitionMap.get(ecuId).getCalId();
                                System.out.println("CAL ID = " + calId);
                                calIdLabel.setText(buildEcuInfoLabelText(CAL_ID_LABEL, calId));
                            }
                            ecuIdLabel.setText(buildEcuInfoLabelText(ECU_ID_LABEL, ecuId));
                            System.out.println("Loading logger config for new ECU (ecuid: " + ecuId + ")...");
                            loadLoggerConfig();
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
        dashboardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        dashboardUpdateHandler = new DashboardUpdateHandler(dashboardPanel);
        controller = new LoggerControllerImpl(settings, ecuInitCallback, this, liveDataUpdateHandler,
                graphUpdateHandler, dashboardUpdateHandler, fileUpdateHandler, TableUpdateHandler.getInstance());
        messageLabel = new JLabel(ENGINUITY_ECU_LOGGER_TITLE);
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

    private void initControllerListeners() {
        controller.addListener(dataTabBroker);
        controller.addListener(graphTabBroker);
        controller.addListener(dashboardTabBroker);
    }

    private void startPortRefresherThread() {
        SerialPortRefresher serialPortRefresher = new SerialPortRefresher(portsComboBox, settings.getLoggerPort());
        runAsDaemon(serialPortRefresher);
        // wait until port refresher fully started before continuing
        while (!serialPortRefresher.isStarted()) {
            sleep(100);
        }
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
        try {
            EcuDataLoader dataLoader = new EcuDataLoaderImpl();
            dataLoader.loadConfigFromXml(settings.getLoggerConfigFilePath(), settings.getLoggerProtocol(),
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

    private void loadLoggerPlugins() {
        try {
            ExternalDataSourceLoader dataSourceLoader = new ExternalDataSourceLoaderImpl();
            dataSourceLoader.loadFromExternalDataSources();
            loadExternalDatas(dataSourceLoader.getExternalDatas());
        } catch (Exception e) {
            reportError(e);
        }
    }

    public void loadUserProfile(String profileFilePath) {
        try {
            UserProfileLoader profileLoader = new UserProfileLoaderImpl();
            UserProfile profile = profileLoader.loadProfile(profileFilePath);
            setSelectedPort(profile);
            applyUserProfile(profile);
            File profileFile = new File(profileFilePath);
            if (profileFile.exists()) {
                reportMessageInTitleBar("Profile: " + profileFile.getAbsolutePath());
            }
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

    private void setSelectedPort(UserProfile profile) {
        if (profile != null) {
            settings.setLoggerPort(profile.getSerialPort());
            portsComboBox.setSelectedItem(profile.getSerialPort());
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
    }

    private void loadEcuSwitches(List<EcuSwitch> ecuSwitches) {
        clearSwitchTableModels();
        sort(ecuSwitches, new EcuDataComparator());
        for (EcuSwitch ecuSwitch : ecuSwitches) {
            dataTabSwitchListTableModel.addParam(ecuSwitch, false);
            graphTabSwitchListTableModel.addParam(ecuSwitch, false);
            dashboardTabSwitchListTableModel.addParam(ecuSwitch, false);
        }
    }

    private void loadExternalDatas(List<ExternalData> externalDatas) {
        clearExternalTableModels();
        sort(externalDatas, new EcuDataComparator());
        for (ExternalData externalData : externalDatas) {
            dataTabExternalListTableModel.addParam(externalData, false);
            graphTabExternalListTableModel.addParam(externalData, false);
            dashboardTabExternalListTableModel.addParam(externalData, false);
        }
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
        return new UserProfileImpl((String) portsComboBox.getSelectedItem(), paramProfileItems, switchProfileItems, externalProfileItems);
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
        tabbedPane.add("Data", buildSplitPane(buildParamListPane(dataTabParamListTableModel, dataTabSwitchListTableModel, dataTabExternalListTableModel), buildDataTab()));
        tabbedPane.add("Graph", buildSplitPane(buildParamListPane(graphTabParamListTableModel, graphTabSwitchListTableModel, graphTabExternalListTableModel), buildGraphTab()));
        tabbedPane.add("Dashboard", buildSplitPane(buildParamListPane(dashboardTabParamListTableModel, dashboardTabSwitchListTableModel, dashboardTabExternalListTableModel), buildDashboardTab()));
        return tabbedPane;
    }

    private JComponent buildParamListPane(ParameterListTableModel paramListTableModel, ParameterListTableModel switchListTableModel, ParameterListTableModel externalListTableModel) {
        JScrollPane paramList = new JScrollPane(buildParamListTable(paramListTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane switchList = new JScrollPane(buildParamListTable(switchListTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane externalList = new JScrollPane(buildParamListTable(externalListTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JSplitPane subSplitPane = new JSplitPane(VERTICAL_SPLIT, paramList, externalList);
        subSplitPane.setDividerSize(2);
        subSplitPane.setDividerLocation(360);
        JSplitPane splitPane = new JSplitPane(VERTICAL_SPLIT, subSplitPane, switchList);
        splitPane.setDividerSize(2);
        splitPane.setDividerLocation(410);
        return splitPane;
    }

    private JTable buildParamListTable(ParameterListTableModel tableModel) {
        JTable paramListTable = new ParameterListTable(tableModel);
        changeColumnWidth(paramListTable, 0, 20, 55, 55);
        changeColumnWidth(paramListTable, 2, 50, 250, 80);
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
        splitPane.setDividerLocation(300);
        splitPane.addPropertyChangeListener(this);
        return splitPane;
    }

    private JMenuBar buildMenubar() {
        return new EcuLoggerMenuBar(this);
    }

    private JPanel buildControlToolbar() {
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(buildPortsComboBox(), WEST);
        controlPanel.add(buildStatusIndicator(), EAST);
        return controlPanel;
    }

    private Component buildLogToFileButton() {
        logToFileButton = new JToggleButton("Log to file");
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
        JButton resetConnectionButton = new JButton(new ImageIcon("./graphics/logger_restart.png"));
        resetConnectionButton.setPreferredSize(new Dimension(25, 25));
        resetConnectionButton.setToolTipText("Reset ECU Connection");
        resetConnectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    restartLogging();
                } catch (Exception e) {
                    reportError(e);
                }
            }
        });
        comboBoxPanel.add(resetConnectionButton);
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
        comboBoxPanel.add(resetConnectionButton);
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
        panel.add(new JScrollPane(new JTable(dataTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER), CENTER);
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
        JScrollPane scrollPane = new JScrollPane(graphPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(40);
        panel.add(scrollPane, CENTER);
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
        panel.add(dashboardPanel, CENTER);
        return panel;
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
        settings.setLoggerPort((String) portsComboBox.getSelectedItem());
        controller.start();
    }

    public void stopLogging() {
        controller.stop();
        sleep(1000L);
    }

    public void handleExit() {
        try {
            try {
                stopLogging();
            } finally {
                cleanUpUpdateHandlers();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rememberWindowProperties();
        }
    }

    private void rememberWindowProperties() {
        settings.setLoggerWindowMaximized(getExtendedState() == MAXIMIZED_BOTH);
        settings.setLoggerWindowSize(getSize());
        settings.setLoggerWindowLocation(getLocation());
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
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    messageLabel.setText(message);
                    messageLabel.setForeground(BLACK);
                }
            });
        }
    }

    public void reportMessageInTitleBar(String message) {
        if (!isNullOrEmpty(message)) {
            setTitle(message);
        }
    }

    public void reportStats(final String message) {
        if (!isNullOrEmpty(message)) {
            SwingUtilities.invokeLater(new Runnable() {
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
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    messageLabel.setText("Error: " + error);
                    messageLabel.setForeground(RED);
                }
            });
        }
    }

    public void reportError(Exception e) {
        if (e != null) {
            e.printStackTrace();
            String error = e.getMessage();
            if (!isNullOrEmpty(error)) {
                reportError(error);
            } else {
                reportError(e.toString());
            }
        }
    }

    public void setTitle(String title) {
        if (title != null) {
            if (!title.startsWith(ENGINUITY_ECU_LOGGER_TITLE)) {
                title = ENGINUITY_ECU_LOGGER_TITLE + (title.length() == 0 ? "" : " - " + title);
            }
            super.setTitle(title);
        }
    }

    //**********************************************************************


    public static void startLogger(final int defaultCloseOperation, final Settings settings) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(defaultCloseOperation, settings);
            }
        });
    }

    private static void createAndShowGUI(int defaultCloseOperation, Settings settings) {
        // instantiate the controlling class.
        EcuLogger ecuLogger = new EcuLogger(settings);

        // set remaining window properties
        ecuLogger.pack();
        ecuLogger.setSize(settings.getLoggerWindowSize());
        ecuLogger.setIconImage(new ImageIcon("./graphics/enginuity-ico.gif").getImage());
        ecuLogger.setDefaultCloseOperation(defaultCloseOperation);
        ecuLogger.addWindowListener(ecuLogger);

        // display the window
        ecuLogger.setLocation(settings.getLoggerWindowLocation());
        if (settings.isWindowMaximized()) {
            ecuLogger.setExtendedState(MAXIMIZED_BOTH);
        }
        ecuLogger.setVisible(true);
    }

}
