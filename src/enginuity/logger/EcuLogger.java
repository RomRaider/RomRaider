package enginuity.logger;

import enginuity.Settings;
import enginuity.logger.definition.EcuDataLoader;
import enginuity.logger.definition.EcuDataLoaderImpl;
import enginuity.logger.definition.EcuParameter;
import enginuity.logger.definition.EcuSwitch;
import enginuity.logger.io.serial.SerialPortRefresher;
import enginuity.logger.ui.LoggerDataTableModel;
import enginuity.logger.ui.MessageListener;
import enginuity.logger.ui.ParameterListTable;
import enginuity.logger.ui.ParameterListTableModel;
import enginuity.logger.ui.ParameterRegistrationBroker;
import enginuity.logger.ui.ParameterRegistrationBrokerImpl;
import enginuity.logger.ui.SerialPortComboBox;
import enginuity.logger.ui.handler.DashboardUpdateHandler;
import enginuity.logger.ui.handler.DataUpdateHandlerManager;
import enginuity.logger.ui.handler.DataUpdateHandlerManagerImpl;
import enginuity.logger.ui.handler.FileUpdateHandler;
import enginuity.logger.ui.handler.GraphUpdateHandler;
import enginuity.logger.ui.handler.LiveDataUpdateHandler;

import javax.swing.*;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;
import javax.swing.border.BevelBorder;
import static javax.swing.border.BevelBorder.LOWERED;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/*
TODO: add better debug logging, preferably to a file and switchable (on/off)
TODO: finish dashboard tab
TODO: add configuration screen (log file destination, etc)
TODO: add user definable addresses
TODO: Clean up this class!
So much to do, so little time....
*/

public final class EcuLogger extends JFrame implements WindowListener, PropertyChangeListener, MessageListener {
    private static final String HEADING_PARAMETERS = "Parameters";
    private static final String HEADING_SWITCHES = "Switches";

    private final Settings settings = new Settings();
    private final LoggerController controller = new LoggerControllerImpl(settings, this);
    private final JLabel statusBarLabel = new JLabel("Enginuity ECU Logger");
    private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
    private final SerialPortComboBox portsComboBox = new SerialPortComboBox(settings);

    private final DataUpdateHandlerManager dataHandlerManager = new DataUpdateHandlerManagerImpl();

    private final ParameterRegistrationBroker dataTabBroker = new ParameterRegistrationBrokerImpl(controller, dataHandlerManager);
    private final ParameterListTableModel dataTabParamListTableModel = new ParameterListTableModel(dataTabBroker, HEADING_PARAMETERS);
    private final ParameterListTableModel dataTabSwitchListTableModel = new ParameterListTableModel(dataTabBroker, HEADING_SWITCHES);

    private final DataUpdateHandlerManager graphHandlerManager = new DataUpdateHandlerManagerImpl();
    private final ParameterRegistrationBroker graphTabBroker = new ParameterRegistrationBrokerImpl(controller, graphHandlerManager);
    private final ParameterListTableModel graphTabParamListTableModel = new ParameterListTableModel(graphTabBroker, HEADING_PARAMETERS);
    private final ParameterListTableModel graphTabSwitchListTableModel = new ParameterListTableModel(graphTabBroker, HEADING_SWITCHES);

    private final DataUpdateHandlerManager dashboardHandlerManager = new DataUpdateHandlerManagerImpl();
    private final ParameterRegistrationBroker dashboardTabBroker = new ParameterRegistrationBrokerImpl(controller, dashboardHandlerManager);
    private final ParameterListTableModel dashboardTabParamListTableModel = new ParameterListTableModel(dashboardTabBroker, HEADING_PARAMETERS);
    private final ParameterListTableModel dashboardTabSwitchListTableModel = new ParameterListTableModel(dashboardTabBroker, HEADING_SWITCHES);

    private final FileUpdateHandler fileUpdateHandler = new FileUpdateHandler(settings);

    private final LoggerDataTableModel dataTableModel = new LoggerDataTableModel();
    private final LiveDataUpdateHandler liveDataUpdateHandler = new LiveDataUpdateHandler(dataTableModel);

    private final JPanel graphPanel = new JPanel();
    private final GraphUpdateHandler graphUpdateHandler = new GraphUpdateHandler(graphPanel);

    private final JPanel dashboardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final DashboardUpdateHandler dashboardUpdateHandler = new DashboardUpdateHandler(dashboardPanel);

    public EcuLogger(String title) {
        super(title);

        // setup controller listeners
        initControllerListeners();

        // start port list refresher thread
        startPortRefresherThread();

        // setup the user interface
        initUserInterface();

        // setup parameter update handlers
        initParameterUpdateHandlers();

        // load ecu params from logger config
        loadEcuParamsFromConfig();

    }

    private void initControllerListeners() {
        controller.addListener(dataTabBroker);
        controller.addListener(graphTabBroker);
        controller.addListener(dashboardTabBroker);
    }

    private void startPortRefresherThread() {
        Thread portRefresherThread = new Thread(new SerialPortRefresher(portsComboBox, controller));
        portRefresherThread.setDaemon(true);
        portRefresherThread.start();
    }

    private void initUserInterface() {
        // setup main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(buildControlToolbar(), NORTH);
        mainPanel.add(buildTabbedPane(), CENTER);
        mainPanel.add(buildStatusBar(), SOUTH);

        // add to container
        getContentPane().add(mainPanel);
    }

    private void loadEcuParamsFromConfig() {
        try {
            EcuDataLoader dataLoader = new EcuDataLoaderImpl();
            dataLoader.loadFromXml(settings.getLoggerConfigFilePath(), settings.getLoggerProtocol());
            List<EcuParameter> ecuParams = dataLoader.getEcuParameters();
            for (EcuParameter ecuParam : ecuParams) {
                dataTabParamListTableModel.addParam(ecuParam);
                graphTabParamListTableModel.addParam(ecuParam);
                dashboardTabParamListTableModel.addParam(ecuParam);
                ecuParam.addConvertorUpdateListener(fileUpdateHandler);
                ecuParam.addConvertorUpdateListener(liveDataUpdateHandler);
                ecuParam.addConvertorUpdateListener(graphUpdateHandler);
                ecuParam.addConvertorUpdateListener(dashboardUpdateHandler);
            }
            List<EcuSwitch> ecuSwitches = dataLoader.getEcuSwitches();
            for (EcuSwitch ecuSwitch : ecuSwitches) {
                dataTabSwitchListTableModel.addParam(ecuSwitch);
                graphTabSwitchListTableModel.addParam(ecuSwitch);
                dashboardTabSwitchListTableModel.addParam(ecuSwitch);
            }
        } catch (Exception e) {
            e.printStackTrace();
            reportError(e);
        }
    }

    private void initParameterUpdateHandlers() {
        dataHandlerManager.addHandler(liveDataUpdateHandler);
        dataHandlerManager.addHandler(fileUpdateHandler);
        graphHandlerManager.addHandler(graphUpdateHandler);
        graphHandlerManager.addHandler(fileUpdateHandler);
        dashboardHandlerManager.addHandler(dashboardUpdateHandler);
        dashboardHandlerManager.addHandler(fileUpdateHandler);
    }

    private JComponent buildTabbedPane() {
        tabbedPane.add("Data", buildSplitPane(buildParamListPane(dataTabParamListTableModel, dataTabSwitchListTableModel), buildDataTab()));
        tabbedPane.add("Graph", buildSplitPane(buildParamListPane(graphTabParamListTableModel, graphTabSwitchListTableModel), buildGraphTab()));
        tabbedPane.add("Dashboard", buildSplitPane(buildParamListPane(dashboardTabParamListTableModel, dashboardTabSwitchListTableModel), buildDashboardTab()));
        return tabbedPane;
    }

    private JComponent buildParamListPane(TableModel paramListTableModel, TableModel switchListTableModel) {
        JScrollPane paramList = new JScrollPane(buildParamListTable(paramListTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane switchList = new JScrollPane(buildParamListTable(switchListTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JSplitPane splitPane = new JSplitPane(VERTICAL_SPLIT, paramList, switchList);
        splitPane.setDividerSize(2);
        splitPane.setDividerLocation(300);
        return splitPane;
    }

    private JTable buildParamListTable(TableModel tableModel) {
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
        JPanel statusBar = new JPanel();
        statusBar.setLayout(new BorderLayout());
        statusBar.add(statusBarLabel, CENTER);
        statusBar.setBorder(new BevelBorder(LOWERED));
        return statusBar;
    }

    private JSplitPane buildSplitPane(JComponent leftComponent, JComponent rightComponent) {
        JSplitPane splitPane = new JSplitPane(HORIZONTAL_SPLIT, leftComponent, rightComponent);
        splitPane.setDividerSize(2);
        splitPane.setDividerLocation(300);
        splitPane.addPropertyChangeListener(this);
        return splitPane;
    }

    private JPanel buildControlToolbar() {
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(buildStartButton());
        controlPanel.add(buildStopButton());
        controlPanel.add(buildPortsComboBox());
        return controlPanel;
    }

    private JComboBox buildPortsComboBox() {
        portsComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                settings.setLoggerPort((String) portsComboBox.getSelectedItem());
                controller.stop();
            }
        });
        return portsComboBox;
    }

    private JButton buildStartButton() {
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                settings.setLoggerPort((String) portsComboBox.getSelectedItem());
                controller.start();
            }
        });
        return startButton;
    }

    private JButton buildStopButton() {
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                controller.stop();
            }
        });
        return stopButton;
    }

    private JComponent buildDataTab() {
        return new JScrollPane(new JTable(dataTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
    }

    private JComponent buildGraphTab() {
        graphPanel.setLayout(new SpringLayout());
        return new JScrollPane(graphPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private JComponent buildDashboardTab() {
        return new JScrollPane(dashboardPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
    }

    public void windowOpened(WindowEvent windowEvent) {
    }

    public void windowClosing(WindowEvent windowEvent) {
        try {
            controller.stop();
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            cleanUpUpdateHandlers();
        }
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

    private synchronized void cleanUpUpdateHandlers() {
        dataHandlerManager.cleanUp();
        graphHandlerManager.cleanUp();
        dashboardHandlerManager.cleanUp();
    }

    public void reportMessage(String message) {
        if (message != null) {
            statusBarLabel.setText(message);
        }
    }

    public void reportError(String error) {
        if (error != null) {
            JOptionPane.showMessageDialog(null, error);
        }
    }

    public void reportError(Exception e) {
        if (e != null) {
            String message = e.getMessage();
            int i = message.indexOf(": ");
            if (i >= 0) {
                message = message.substring(i + 2);
            }
            reportError(message);
        }
    }

    //**********************************************************************


    public static void main(String... args) {
        startLogger(EXIT_ON_CLOSE);
    }

    public static void startLogger(final int defaultCloseOperation) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(defaultCloseOperation);
            }
        });
    }

    private static void createAndShowGUI(int defaultCloseOperation) {
        //set look and feel
        setLookAndFeel();

        //make sure we have nice window decorations.
        setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        //instantiate the controlling class.
        EcuLogger ecuLogger = new EcuLogger("Enginuity ECU Logger");

        //set remaining window properties
        ecuLogger.setSize(new Dimension(1000, 600));
        ecuLogger.setIconImage(new ImageIcon("./graphics/enginuity-ico.gif").getImage());
        ecuLogger.setDefaultCloseOperation(defaultCloseOperation);
        ecuLogger.addWindowListener(ecuLogger);

        //display the window
        ecuLogger.setLocationRelativeTo(null); //center it
        ecuLogger.setVisible(true);
    }

    private static void setLookAndFeel() {
        try {
            //use the system look and feel.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
