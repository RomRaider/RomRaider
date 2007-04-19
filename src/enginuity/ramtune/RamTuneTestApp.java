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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import static javax.swing.border.BevelBorder.LOWERED;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.WEST;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import static java.awt.GridBagConstraints.BOTH;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
public final class RamTuneTestApp extends JFrame implements WindowListener {
    private Settings settings = new Settings();
    private SerialPortComboBox portsComboBox = new SerialPortComboBox(settings);
    private JLabel messageLabel = new JLabel();
    private JLabel connectionStatusLabel = new JLabel();

    public RamTuneTestApp(String title) {
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
        JPanel contentPanel = buildContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buildStatusBar(), BorderLayout.SOUTH);

        // add to container
        getContentPane().add(mainPanel);
    }

    private JPanel buildContentPanel() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        JPanel mainPanel = new JPanel(gridBagLayout);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = BOTH;
        constraints.insets = new Insets(3, 5, 3, 5);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        mainPanel.add(buildComPortPanel(), constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        mainPanel.add(buildInputPanel(), constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        mainPanel.add(buildOutputPanel(), constraints);
        
        return mainPanel;
    }

    private Component buildInputPanel() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        JPanel inputPanel = new JPanel(gridBagLayout);
        inputPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "ECU Command"));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = BOTH;
        constraints.insets = new Insets(0, 5, 5, 5);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        inputPanel.add(buildCommandComboBox(), constraints);

        JPanel addressPanel = new JPanel(new FlowLayout());
        addressPanel.add(new JLabel("Address:"));
        addressPanel.add(new JTextField(6));
        addressPanel.add(new JLabel("eg. 200000"));
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        inputPanel.add(addressPanel, constraints);

        JTextArea dataField = new JTextArea(10, 30);
        dataField.setLineWrap(true);
        dataField.setBorder(new BevelBorder(LOWERED));
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 5;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        inputPanel.add(new JScrollPane(dataField, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER), constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 5;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        inputPanel.add(buildSendButton(), constraints);

        return inputPanel;
    }

    private JComboBox buildCommandComboBox() {
        return new JComboBox(new Object[] {"ECU Init", "Read", "Write"});
    }

    private Component buildOutputPanel() {
        JTextArea responseField = new JTextArea(10, 30);
        responseField.setLineWrap(true);
        responseField.setEditable(false);
        responseField.setBorder(new BevelBorder(LOWERED));
        JScrollPane responseScrollPane = new JScrollPane(responseField, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
        responseScrollPane.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "ECU Response"));
        return responseScrollPane;
    }

    private JButton buildSendButton() {
        JButton button = new JButton("Send Command");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connect();
                try {
                    // TODO: finish me
                    write();
                } finally {
                    disconnect();
                }
            }
        });
        return button;
    }

    private void write() {
        // TODO: finish me
    }

    private JComponent buildStatusBar() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        JPanel statusBar = new JPanel(gridBagLayout);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = BOTH;

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

    private JPanel buildComPortPanel() {
        JPanel comboBoxPanel = new JPanel(new FlowLayout());
        comboBoxPanel.add(new JLabel("COM Port:"));
        comboBoxPanel.add(portsComboBox);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(comboBoxPanel, WEST);
        return panel;
    }

    private void reportError(Exception e) {
        final Writer writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        showMessageDialog(this, writer.toString(), "Error", ERROR_MESSAGE);
    }

    private void connect() {
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
                RamTuneTestApp ramTuneTestApp = new RamTuneTestApp("RAM Writer - Test App");
                ramTuneTestApp.setIconImage(new ImageIcon("./graphics/enginuity-ico.gif").getImage());
                ramTuneTestApp.setDefaultCloseOperation(EXIT_ON_CLOSE);
                ramTuneTestApp.addWindowListener(ramTuneTestApp);
                ramTuneTestApp.setLocation(100, 100);
                ramTuneTestApp.pack();
                ramTuneTestApp.setVisible(true);
            }
        });
    }

}
