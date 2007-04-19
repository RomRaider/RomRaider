package enginuity.ramtune;

import enginuity.Settings;
import enginuity.io.port.SerialPortRefresher;
import enginuity.logger.ecu.ui.SerialPortComboBox;
import enginuity.swing.LookAndFeelManager;
import static enginuity.util.ThreadUtil.runAsDaemon;
import static enginuity.util.ThreadUtil.sleep;

import static javax.swing.BorderFactory.createLoweredBevelBorder;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.BorderLayout.WEST;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/*
 * This is a test app! Use at your own risk!!
 * It borrows some functionality from the logger which should be rewritten/removed before being released!!
 */
public final class RamWriter extends JFrame implements WindowListener {
    private Settings settings = new Settings();
    private SerialPortComboBox portsComboBox = new SerialPortComboBox(settings);
    private JLabel messageLabel = new JLabel();
    private JLabel connectionStatusLabel = new JLabel();

    public RamWriter(String title) {
        super(title);
        initUserInterface();
        startPortRefresherThread();
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
        // setup main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buildControlToolbar(), NORTH);
        mainPanel.add(buildInputPanel(), CENTER);
        mainPanel.add(buildStatusBar(), SOUTH);

        // add to container
        getContentPane().add(mainPanel);
    }

    private JPanel buildControlToolbar() {
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(buildPortsComboBox());
        return controlPanel;
    }

    private Component buildInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout(3, 3));
        inputPanel.add(new JTextField(), NORTH);
        inputPanel.add(new JTextArea(10, 50), CENTER);
        inputPanel.add(new JButton("Write to ECU"), SOUTH);
        return inputPanel;
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

        JPanel statsPanel = new JPanel(new FlowLayout());
        statsPanel.setBorder(createLoweredBevelBorder());
        statsPanel.add(connectionStatusLabel);
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        gridBagLayout.setConstraints(statsPanel, constraints);
        statusBar.add(statsPanel);

        return statusBar;
    }

    private JPanel buildPortsComboBox() {
        portsComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                settings.setLoggerPort((String) portsComboBox.getSelectedItem());
                // this is a hack...
                if (!actionEvent.paramString().endsWith("modifiers=")) {
                    reconnect();
                }
            }
        });
        JPanel comboBoxPanel = new JPanel(new FlowLayout());
        comboBoxPanel.add(new JLabel("COM Port:"));
        comboBoxPanel.add(portsComboBox);
        return comboBoxPanel;
    }

    private void reportError(Exception e) {
        final Writer writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        showMessageDialog(this, writer.toString(), "Error", ERROR_MESSAGE);
    }

    private void reconnect() {
        // TODO: Finish me
    }

    private void disconnect() {
        // TODO: Finish me
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
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

    //**********************************************************************

    public static void main(String[] args) {
        LookAndFeelManager.initLookAndFeel();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                RamWriter ramWriter = new RamWriter("RAM Writer - Test App");
                ramWriter.setSize(400, 400);
                ramWriter.setIconImage(new ImageIcon("./graphics/enginuity-ico.gif").getImage());
                ramWriter.setDefaultCloseOperation(EXIT_ON_CLOSE);
                ramWriter.addWindowListener(ramWriter);
                ramWriter.setLocation(100, 100);
                ramWriter.setVisible(true);
            }
        });
    }

}
