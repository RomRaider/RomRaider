package enginuity.logger;

import enginuity.Settings;
import enginuity.logger.definition.EcuParameter;
import enginuity.logger.definition.EcuParameterImpl;
import enginuity.logger.definition.convertor.AcceleratorOpeningAngleConvertor;
import enginuity.logger.definition.convertor.AirFuelRatioLambdaConvertor;
import enginuity.logger.definition.convertor.EngineSpeedConvertor;
import enginuity.logger.definition.convertor.ExhaustGasTemperatureConvertor;
import enginuity.logger.definition.convertor.GenericTemperatureConvertor;
import enginuity.logger.definition.convertor.ThrottleOpeningAngleConvertor;
import enginuity.logger.query.LoggerCallback;
import enginuity.logger.ui.LoggerDataRow;
import enginuity.logger.ui.LoggerDataTableModel;
import enginuity.logger.ui.SpringUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public final class EcuLogger extends JFrame implements WindowListener, PropertyChangeListener {
    private final Settings settings = new Settings();
    private final LoggerController CONTROLLER = new LoggerControllerImpl(settings);
    private final LoggerDataTableModel dataTableModel = new LoggerDataTableModel();
    private final JPanel graphPanel = new JPanel();
    private final JComboBox portsComboBox = new JComboBox();
    private int loggerCount = 0;
    private long loggerStartTime = 0;

    public EcuLogger(String title) {
        super(title);

        //build left and right components of split pane
        JComponent leftComponent = buildLeftComponent();
        JComponent rightComponent = buildRightComponent();

        //build split pane
        JSplitPane splitPane = buildSplitPane(leftComponent, rightComponent);
        splitPane.addPropertyChangeListener(this);

        //add to container
        getContentPane().add(splitPane);

        // add test address to log (0x000008 = coolant temp, 8bit)
        final EcuParameter ecuParam1 = new EcuParameterImpl("Coolant Temperature", "Coolant temperature in degrees C", "0x000008", new GenericTemperatureConvertor());
        registerEcuParameterForLogging(ecuParam1);

        // add test address to log (0x000106 = EGT, 8bit)
        final EcuParameter ecuParam2 = new EcuParameterImpl("EGT", "Exhaust gas temperature in degrees C", "0x000106", new ExhaustGasTemperatureConvertor());
        registerEcuParameterForLogging(ecuParam2);

        // add test address to log (0x000046 = air/fuel ratio, 8bit)
        final EcuParameter ecuParam3 = new EcuParameterImpl("AFR", "Air/Fuel Ratio in Lambda", "0x000046", new AirFuelRatioLambdaConvertor());
        registerEcuParameterForLogging(ecuParam3);

        // add test address to log (0x000029 = accelerator opening angle, 8bit)
        final EcuParameter ecuParam4 = new EcuParameterImpl("Accel Opening Angle", "Accelerator opening angle in %", "0x000029", new AcceleratorOpeningAngleConvertor());
        registerEcuParameterForLogging(ecuParam4);

        // add test address to log (0x000015 = accelerator opening angle, 8bit)
        final EcuParameter ecuParam5 = new EcuParameterImpl("Throttle Opening Angle", "Throttle opening angle in %", "0x000015", new ThrottleOpeningAngleConvertor());
        registerEcuParameterForLogging(ecuParam5);

        // add test address to log (0x00000E 0x00000F = engine speed, 16bit)
        final EcuParameter ecuParam6 = new EcuParameterImpl("Engine Speed", "Engine speed in rpm", "0x00000E00000F", new EngineSpeedConvertor());
        registerEcuParameterForLogging(ecuParam6);

    }

    private void registerEcuParameterForLogging(final EcuParameter ecuParam) {
        // add to data table
        final LoggerDataRow dataRow = new LoggerDataRow(ecuParam);
        dataTableModel.addRow(dataRow);

        // add to charts
        final XYSeries series = new XYSeries(ecuParam.getName());
        final XYDataset xyDataset = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory.createXYLineChart(ecuParam.getName(), "Time (ms)", ecuParam.getName() + " (" + ecuParam.getConvertor().getUnits() + ")",
                xyDataset, PlotOrientation.VERTICAL, true, true, false);
        final JLabel chartLabel = new JLabel();
        chartLabel.setIcon(new ImageIcon(chart.createBufferedImage(500, 300)));
        graphPanel.add(chartLabel);
        SpringUtilities.makeCompactGrid(graphPanel, ++loggerCount, 1, 10, 10, 20, 20);

        // add to dashboard

        // add logger and setup callback
        CONTROLLER.addLogger(ecuParam, new LoggerCallback() {
            public void callback(byte[] value) {
                // update data table
                dataRow.updateValue(value);

                // update graph
                series.add((System.currentTimeMillis() - loggerStartTime), ecuParam.getConvertor().convert(value));
                chartLabel.setIcon(new ImageIcon(chart.createBufferedImage(500, 300)));

                // update dashboard

            }
        });
    }

    private JComponent buildLeftComponent() {
        JTable parameterList = new JTable();
        return new JScrollPane(parameterList, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private JComponent buildRightComponent() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(buildControlToolbar(), BorderLayout.NORTH);
        rightPanel.add(buildTabbedDataPane(), BorderLayout.CENTER);
        return rightPanel;
    }

    private JSplitPane buildSplitPane(JComponent leftComponent, JComponent rightComponent) {
        JSplitPane splitPane = new JSplitPane(HORIZONTAL_SPLIT, leftComponent, rightComponent);
        splitPane.setDividerSize(2);
        splitPane.setDividerLocation(250);
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
        refreshPortsComboBox();
        portsComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                settings.setLoggerPort((String) portsComboBox.getSelectedItem());
                CONTROLLER.stop();
            }
        });
        return portsComboBox;
    }

    private void refreshPortsComboBox() {
        List<String> ports = CONTROLLER.listSerialPorts();
        for (String port : ports) {
            portsComboBox.addItem(port);
        }
        settings.setLoggerPort((String) portsComboBox.getSelectedItem());
    }

    private JButton buildStartButton() {
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                settings.setLoggerPort((String) portsComboBox.getSelectedItem());
                loggerStartTime = System.currentTimeMillis();
                CONTROLLER.start();
            }
        });
        return startButton;
    }

    private JButton buildStopButton() {
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                CONTROLLER.stop();
            }
        });
        return stopButton;
    }

    private JTabbedPane buildTabbedDataPane() {
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabbedPane.add("Data", buildDataTab());
        tabbedPane.add("Graph", buildGraphTab());
        tabbedPane.add("Dashboard", buildDashboardTab());
        return tabbedPane;
    }

    private JComponent buildDataTab() {
        return new JScrollPane(new JTable(dataTableModel), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
    }

    private JComponent buildGraphTab() {
        graphPanel.setLayout(new SpringLayout());
        return new JScrollPane(graphPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
    }

    private JComponent buildDashboardTab() {
        JPanel dashboardPanel = new JPanel();
        return new JScrollPane(dashboardPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
    }

    public void windowOpened(WindowEvent windowEvent) {
    }

    public void windowClosing(WindowEvent windowEvent) {
        CONTROLLER.stop();
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

    public static void main(String... args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
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
        ecuLogger.setDefaultCloseOperation(EXIT_ON_CLOSE);
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
