/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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

package com.romraider.ramtune.test;

import static com.romraider.util.HexUtil.asBytes;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static com.romraider.util.ThreadUtil.sleep;
import static com.romraider.util.ParamChecker.isNullOrEmpty;
import static java.awt.FlowLayout.LEFT;
import static java.awt.Font.PLAIN;
import static java.awt.GridBagConstraints.BOTH;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.border.BevelBorder.LOWERED;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.romraider.Settings;
import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.protocol.Protocol;
import com.romraider.io.protocol.ProtocolFactory;
import com.romraider.io.serial.port.SerialPortRefresher;
import com.romraider.logger.ecu.comms.io.protocol.LoggerProtocol;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.manager.PollingStateImpl;
import com.romraider.logger.ecu.definition.EcuDataLoader;
import com.romraider.logger.ecu.definition.EcuDataLoaderImpl;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.definition.Transport;
import com.romraider.logger.ecu.ui.SerialPortComboBox;
import com.romraider.ramtune.test.command.executor.CommandExecutor;
import com.romraider.ramtune.test.command.executor.CommandExecutorImpl;
import com.romraider.ramtune.test.command.generator.CommandGenerator;
import com.romraider.ramtune.test.command.generator.EcuInitCommandGenerator;
import com.romraider.ramtune.test.command.generator.ReadCommandGenerator;
import com.romraider.ramtune.test.command.generator.WriteCommandGenerator;
import com.romraider.ramtune.test.io.RamTuneTestAppConnectionProperties;
import com.romraider.swing.AbstractFrame;
import com.romraider.swing.LookAndFeelManager;
import com.romraider.util.LogManager;
import com.romraider.util.SettingsManager;

/*
 * This is a test app! Use at your own risk!!
 * It borrows some functionality from the logger which should be rewritten/removed before being released!!
 *
 * It is also a bit of a mess and needs to be cleaned up...
 */
public final class RamTuneTestApp extends AbstractFrame {
    private static final long serialVersionUID = 7140513114169019846L;
    private static final String REGEX_VALID_ADDRESS_BYTES = "[0-9a-fA-F]{6}";
    private static final String REGEX_VALID_DATA_BYTES = "[0-9a-fA-F]{2,}";
    private static final PollingState pollMode = new PollingStateImpl();
    private static final String ISO9141 = "ISO9141";
    private static Protocol protocol;
    private final JTextField addressField = new JTextField(6);
    private final JTextField lengthField = new JTextField(4);
    private final JTextField sendTimeoutField = new JTextField(4);
    private final JTextField blocksize = new JTextField(3);
    private final JTextArea dataField = new JTextArea(5, 80);
    private final JTextArea responseField = new JTextArea(10, 80);
    private final JCheckBox blockRead = new JCheckBox("Block Read");
    private final SerialPortComboBox portsComboBox;
    private final JComboBox commandComboBox;
    private static Module module;
    private static String userTp;
    private static String userLibrary;
    private static String target;
    private static Settings settings = SettingsManager.getSettings();
    private Map<String, Map<Transport, Collection<Module>>> protocolList =
            new HashMap<String, Map<Transport, Collection<Module>>>();

    public RamTuneTestApp(String title) {
        super(title);
        final EcuDataLoader dataLoader = new EcuDataLoaderImpl();
        if (isNullOrEmpty(settings.getLoggerDefinitionFilePath())) {
            showErrorDialog("A Logger definition file needs to be configured before connecting.");
            windowClosing(null);
        }
        dataLoader.loadConfigFromXml(
                settings.getLoggerDefinitionFilePath(),
                settings.getLoggerProtocol(),
                settings.getFileLoggingControllerSwitchId(), null);
        protocolList = dataLoader.getProtocols();
        target = settings.getTargetModule();
        portsComboBox = new SerialPortComboBox();
        userTp = settings.getTransportProtocol();
        userLibrary = settings.getJ2534Device();
        settings.setTransportProtocol(ISO9141);
        // Read Address blocks only seems to work with ISO9141, it
        // may not be implemented in the ECU for ISO15765
        final LoggerProtocol lp = ProtocolFactory.getProtocol(
                settings.getLoggerProtocol(),
                ISO9141
                );
        protocol = lp.getProtocol();
        commandComboBox = new JComboBox(new CommandGenerator[]{
                new EcuInitCommandGenerator(protocol),
                new ReadCommandGenerator(protocol),
                new WriteCommandGenerator(protocol)});
        initUserInterface();
        startPortRefresherThread();
    }

    private void startPortRefresherThread() {
        SerialPortRefresher serialPortRefresher = new SerialPortRefresher(portsComboBox, SettingsManager.getSettings().getLoggerPort());
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
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 0;
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
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 5, 5, 5);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        inputPanel.add(commandComboBox, constraints);

        JPanel addressFieldPanel = new JPanel(new FlowLayout());
        addressFieldPanel.add(new JLabel("Address (eg. 020000):"));
        addressFieldPanel.add(addressField);
        JPanel lengthPanel = new JPanel(new FlowLayout());
        lengthPanel.add(new JLabel("   Read Length:"));
        lengthField.setText("1");
        lengthPanel.add(lengthField);
        lengthPanel.add(new JLabel("byte(s)"));
        JPanel blockReadPanel = new JPanel(new FlowLayout());
        blockRead.setSelected(true);
        blockRead.setToolTipText("uncheck to read range byte at a time");
        blockReadPanel.add(blockRead);
        blockReadPanel.add(new JLabel("Block Size:"));
        blocksize.setText("128");
        blocksize.setToolTipText("Set to value allowed by the ECU");
        blockReadPanel.add(blocksize);

        JPanel addressPanel = new JPanel(new FlowLayout(LEFT));
        addressPanel.add(addressFieldPanel);
        addressPanel.add(lengthPanel);
        addressPanel.add(blockReadPanel);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 4;
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
        final JButton button = new JButton("Send Command");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runAsDaemon(new Runnable() {
                    @Override
                    public void run() {
                        button.setEnabled(false);
                        CommandExecutor commandExecutor = null;
                        try {
                            ConnectionProperties connectionProperties = new RamTuneTestAppConnectionProperties(protocol.getDefaultConnectionProperties(), getSendTimeout());
                            commandExecutor = new CommandExecutorImpl(connectionProperties, (String) portsComboBox.getSelectedItem());
                            final CommandGenerator commandGenerator = (CommandGenerator) commandComboBox.getSelectedItem();
                            if (validateInput(commandGenerator) && confirmCommandExecution(commandGenerator)) {
                                StringBuilder builder = new StringBuilder();
                                List<byte[]> commands = commandGenerator.createCommands(module, getData(), getAddress(), getLength(), getBlockRead(), getBlockSize());
                                for (byte[] command : commands) {
                                    appendResponseLater("SND [" + commandGenerator + "]:\t" + asHex(command) + "\n");
                                    byte[] response = protocol.preprocessResponse(command, commandExecutor.executeCommand(command), pollMode);
                                    appendResponseLater("RCV [" + commandGenerator + "]:\t" + asHex(response) + "\n");
                                    builder.append(asHex(protocol.parseResponseData(response)));
                                }
                                appendResponseLater("DATA [Raw]:\t" + builder.toString() + "\n\n");
                            }
                        } catch (Exception ex) {
                            reportError(ex);
                        } finally {
                            commandExecutor.close();
                            button.setEnabled(true);
                        }
                    }
                });
            }
        });
        return button;
    }

    private void appendResponseLater(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                responseField.append(text);
            }
        });
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
        return getIntFromField(lengthField);
    }

    private boolean getBlockRead() {
        return blockRead.isSelected();
    }

    private int getBlockSize() {
        return getIntFromField(blocksize);
    }

    private int getSendTimeout() {
        return getIntFromField(sendTimeoutField);
    }

    private int getIntFromField(JTextField field) {
        try {
            return Integer.parseInt(field.getText().trim());
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

    private JPanel buildComPortPanel() {
        JPanel panel = new JPanel(new FlowLayout(LEFT));
        panel.add(buildComPorts());
        panel.add(buildSendTimeout());

        final ButtonGroup moduleGroup = new ButtonGroup();
        for (Module module : getModuleList()) {
            final JCheckBox cb = new JCheckBox(module.getName().toUpperCase());
            final String tipText = String.format(
                    "%s Polling.", module.getDescription());
            cb.setToolTipText(tipText);
            if (settings.getTargetModule().equalsIgnoreCase(module.getName())) {
                cb.setSelected(true);
                setTarget(module.getName());
            }
            cb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    final JCheckBox source = (JCheckBox) actionEvent.getSource();
                    if (source.isSelected()) {
                        setTarget(source.getText());
                    }
                }
            });

            moduleGroup.add(cb);
            panel.add(cb);
        }
    return panel;
    }

    private void setTarget(String name) {
        for (Module module: getModuleList()) {
            if (module.getName().equalsIgnoreCase(name)) {
                RamTuneTestApp.module = module;
            }
        }
    }

    private Transport getTransportById(String id) {
        Transport loggerTransport = null;
        for (Transport transport : getTransportMap().keySet()) {
            if (transport.getId().equalsIgnoreCase(id))
                loggerTransport = transport;
        }
        return loggerTransport;
    }

    private Map<Transport, Collection<Module>> getTransportMap() {
        return protocolList.get(settings.getLoggerProtocol());
    }

    private Collection<Module> getModuleList() {
        return getTransportMap().get(getTransportById(settings.getTransportProtocol()));
    }

    private Component buildSendTimeout() {
        sendTimeoutField.setText("55");
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel("Send Timeout:"));
        panel.add(sendTimeoutField);
        panel.add(new JLabel("ms"));
        return panel;
    }

    private JPanel buildComPorts() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel("COM Port:"));
        panel.add(portsComboBox);
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

    //**********************************************************************

    public static void main(String[] args) {
        LogManager.initDebugLogging();
        LookAndFeelManager.initLookAndFeel();
        startTestApp(EXIT_ON_CLOSE);
    }

    public static void startTestApp(final int defaultCloseOperation) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                RamTuneTestApp ramTuneTestApp = new RamTuneTestApp("Control Module Read/Write");
                ramTuneTestApp.setIconImage(new ImageIcon( getClass().getResource("/graphics/romraider-ico.gif")).getImage());
                ramTuneTestApp.setDefaultCloseOperation(defaultCloseOperation);
                ramTuneTestApp.addWindowListener(ramTuneTestApp);
                ramTuneTestApp.setLocation(100, 50);
                ramTuneTestApp.pack();
                ramTuneTestApp.setVisible(true);
            }
        });
    }

    @Override
    public void windowClosing(WindowEvent e) {
        setTarget(target);
        settings.setTransportProtocol(userTp);
        settings.setJ2534Device(userLibrary);
    }
}
