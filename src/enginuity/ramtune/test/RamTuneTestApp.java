package enginuity.ramtune.test;

import enginuity.Settings;
import enginuity.io.port.SerialPortRefresher;
import enginuity.io.protocol.Protocol;
import enginuity.io.protocol.SSMProtocol;
import enginuity.logger.ecu.ui.SerialPortComboBox;
import enginuity.ramtune.test.command.executor.CommandExecutor;
import enginuity.ramtune.test.command.executor.CommandExecutorImpl;
import enginuity.ramtune.test.command.generator.CommandGenerator;
import enginuity.ramtune.test.command.generator.EcuInitCommandGenerator;
import enginuity.ramtune.test.command.generator.ReadCommandGenerator;
import enginuity.ramtune.test.command.generator.WriteCommandGenerator;
import enginuity.swing.LookAndFeelManager;
import static enginuity.util.HexUtil.asBytes;
import static enginuity.util.HexUtil.asHex;
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
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
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
import java.awt.Font;
import static java.awt.Font.PLAIN;
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
    private static final String REGEX_VALID_ADDRESS_BYTES = "[0-9a-fA-F]{6}";
    private static final String REGEX_VALID_DATA_BYTES = "[0-9a-fA-F]{2,}";
    private final Protocol protocol = new SSMProtocol();
    private final Settings settings = new Settings();
    private final JLabel messageLabel = new JLabel();
    private final JLabel connectionStatusLabel = new JLabel();
    private final JTextField addressField = new JTextField(6);
    private final JTextField lengthField = new JTextField(4);
    private final JTextArea dataField = new JTextArea(10, 80);
    private final JTextArea responseField = new JTextArea(15, 80);
    private final SerialPortComboBox portsComboBox = new SerialPortComboBox(settings);
    private final JComboBox commandComboBox = new JComboBox(new CommandGenerator[]{new EcuInitCommandGenerator(protocol),
                                                                                   new ReadCommandGenerator(protocol), new WriteCommandGenerator(protocol)});

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
//        mainPanel.add(buildStatusBar(), BorderLayout.SOUTH);

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
        inputPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Command"));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = BOTH;
        constraints.insets = new Insets(0, 5, 5, 5);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        inputPanel.add(commandComboBox, constraints);

        JPanel addressPanel = new JPanel(new FlowLayout());
        addressPanel.add(new JLabel("Address (eg. 020000):"));
        addressPanel.add(addressField);
        addressPanel.add(new JLabel("Read Length:"));
        lengthField.setText("1");
        addressPanel.add(lengthField);
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        inputPanel.add(addressPanel, constraints);

        dataField.setFont(new Font("Monospaced", PLAIN, 12));
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

    private Component buildOutputPanel() {
        responseField.setFont(new Font("Monospaced", PLAIN, 12));
        responseField.setLineWrap(true);
        responseField.setEditable(false);
        responseField.setBorder(new BevelBorder(LOWERED));
        JScrollPane responseScrollPane = new JScrollPane(responseField, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
        responseScrollPane.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Trace"));
        return responseScrollPane;
    }

    private JButton buildSendButton() {
        JButton button = new JButton("Send Command");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    CommandExecutor commandExecutor = new CommandExecutorImpl(protocol.getDefaultConnectionProperties(),
                            (String) portsComboBox.getSelectedItem());
                    CommandGenerator commandGenerator = (CommandGenerator) commandComboBox.getSelectedItem();
                    if (validateInput(commandGenerator) && confirmCommandExecution(commandGenerator)) {
                        byte[] command = commandGenerator.createCommand(getAddress(), getData(), getLength());
                        responseField.append("SND [" + commandGenerator + "]:\t" + asHex(command) + "\n");
                        byte[] result = commandExecutor.executeCommand(command);
                        responseField.append("RCV [" + commandGenerator + "]:\t" + asHex(result) + "\n");
                    }
                } catch (Exception ex) {
                    reportError(ex);
                }
            }
        });
        return button;
    }

    private byte[] getAddress() {
        try {
            return asBytes(addressField.getText());
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] getData() {
        try {
            return asBytes(dataField.getText());
        } catch (Exception e) {
            return null;
        }
    }

    private int getLength() {
        try {
            return Integer.parseInt(lengthField.getText().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean validateInput(CommandGenerator commandGenerator) {
        boolean isReadCommandGenerator = ReadCommandGenerator.class.isAssignableFrom(commandGenerator.getClass());
        boolean isWriteCommandGenerator = WriteCommandGenerator.class.isAssignableFrom(commandGenerator.getClass());
        if (isReadCommandGenerator || isWriteCommandGenerator) {
            String address = addressField.getText();
            if (address.trim().length() != 6) {
                showErrorDialog("Invalid address - must be 3 bytes long.");
                return false;
            } else if (!address.matches(REGEX_VALID_ADDRESS_BYTES)) {
                showErrorDialog("Invalid address - bad bytes.");
                return false;
            }
        }
        if (isReadCommandGenerator) {
            try {
                int length = Integer.parseInt(lengthField.getText().trim());
                if (length <= 0) {
                    showErrorDialog("Invalid length - must be greater then zero.");
                    return false;
                }
            } catch (NumberFormatException e) {
                showErrorDialog("Invalid length.");
                return false;
            }
        }
        if (isWriteCommandGenerator) {
            String data = dataField.getText().trim();
            int dataLength = data.length();
            if (dataLength == 0) {
                showErrorDialog("No data specified.");
                return false;
            } else if (dataLength % 2 != 0) {
                showErrorDialog("Invalid data - odd number of characters.");
                return false;
            } else if (!data.matches(REGEX_VALID_DATA_BYTES)) {
                showErrorDialog("Invalid data - bad bytes.");
                return false;
            }
        }
        return true;
    }

    private boolean confirmCommandExecution(CommandGenerator commandGenerator) {
        boolean isWriteCommandGenerator = WriteCommandGenerator.class.isAssignableFrom(commandGenerator.getClass());
        return !isWriteCommandGenerator || showConfirmDialog(this, "Are you sure you want to write to ECU memory?",
                "Confirm Write Command", YES_NO_OPTION, WARNING_MESSAGE) == YES_OPTION;
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
        responseField.append("\n**************************************************************************\n");
        responseField.append("ERROR: ");
        responseField.append(writer.toString());
        responseField.append("\n**************************************************************************\n\n");
        //showErrorDialog("An error occurred:\n\n" + writer.toString());
    }

    private void showErrorDialog(String message) {
        showMessageDialog(this, message, "Error", ERROR_MESSAGE);
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
        startTestApp(EXIT_ON_CLOSE);
    }

    public static void startTestApp(final int defaultCloseOperation) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                RamTuneTestApp ramTuneTestApp = new RamTuneTestApp("RAMTune - Test App");
                ramTuneTestApp.setIconImage(new ImageIcon("./graphics/enginuity-ico.gif").getImage());
                ramTuneTestApp.setDefaultCloseOperation(defaultCloseOperation);
                ramTuneTestApp.addWindowListener(ramTuneTestApp);
                ramTuneTestApp.setLocation(100, 50);
                ramTuneTestApp.pack();
                ramTuneTestApp.setVisible(true);
            }
        });
    }

}
