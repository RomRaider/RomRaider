/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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

package com.romraider.logger.utec.gui.bottomControl;

import com.romraider.logger.utec.commEvent.LoggerDataListener;
import com.romraider.logger.utec.commInterface.UtecInterface;
import com.romraider.logger.utec.gui.JutecGUI;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author emorgan
 *         <p/>
 */
public class BottomUtecControl extends JPanel implements ActionListener,
        LoggerDataListener {
    // Buttons to be used
    private JButton openButton;

    private JButton closeButton;

    private JButton startButton;

    private JButton stopButton;

    // Text areas to be used
    private JTextArea textFromUtec;

    //public String totalLog = "";

    public void setOpenPortEnabled(boolean choice) {
        openButton.setEnabled(choice);
    }

    public BottomUtecControl() {

        // Define UTEC output text Area
        textFromUtec = new JTextArea();
        textFromUtec.setCaretPosition(textFromUtec.getDocument().getLength());
        textFromUtec.setText("--==< Live data feed from UTEC will start here. >==--\n");
        JScrollPane utecOutTextScroll = new JScrollPane(textFromUtec);

        // -------------------------
        // Define buttons to be used
        // -------------------------
        // JButton button;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        //this.setLayout(gridbag);
        this.setLayout(new BorderLayout());
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;

        // Button to open defined COM Port
        openButton = new JButton("OPEN PORT");
        openButton.addActionListener(this);
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        gridbag.setConstraints(openButton, c);
        // openButton.setEnabled(false);
        //this.add(openButton);

        // Button to close defined COM port
        closeButton = new JButton("CLOSE PORT");
        closeButton.addActionListener(this);
        closeButton.setEnabled(false);
        c.gridx = 0;
        c.gridy = 1;
        gridbag.setConstraints(closeButton, c);
        //this.add(closeButton);

        // Button to start data capture from UTEC
        startButton = new JButton("START CAPTURE");
        startButton.addActionListener(this);
        startButton.setEnabled(false);
        c.gridx = 0;
        c.gridy = 2;
        gridbag.setConstraints(startButton, c);
        //this.add(startButton);

        // Button to stop data capture from UTEC
        stopButton = new JButton("STOP CAPTURE");
        stopButton.addActionListener(this);
        stopButton.setEnabled(false);
        c.gridx = 0;
        c.gridy = 3;
        gridbag.setConstraints(stopButton, c);
        //this.add(stopButton);

        // Text box that returns live text data from the UTEC
        c.ipadx = 600;
        c.gridheight = 5;
        c.gridx = 1;
        c.gridy = 0;
        //gridbag.setConstraints(utecOutTextScroll, c);
        this.add(utecOutTextScroll, BorderLayout.CENTER);

        // Make this panel listen for comm events
        //UtecInterface.addLoggerListener(this);
    }

    /**
     * Implements actionPerformed
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        // Open Port
        if (cmd.equals("OPEN PORT")) {
            System.out.println("Opening connection to defined port: "
                    + UtecInterface.getPortChoiceUsed());

            // Don't allow use after first press, and until after close port has
            // been pressed
            openButton.setEnabled(false);
            closeButton.setEnabled(true);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);

            // Use interface to open connection
            // UtecInterface.openConnection();

            // VoiceThread vc = new VoiceThread("open port");
            // vc.start();
        }

        // Close Port
        if (cmd.equals("CLOSE PORT")) {
            System.out.println("Closing connection to defined port");

            // Use interface to close the connection to the Utec
            UtecInterface.closeConnection();

            // Set button states
            openButton.setEnabled(true);
            closeButton.setEnabled(false);
            startButton.setEnabled(false);
            stopButton.setEnabled(false);

            // VoiceThread vc = new VoiceThread("close port.");
            // vc.start();
        }

        // Start Capture
        if (cmd.equals("START CAPTURE")) {
            System.out.println("Starting data capture from the UTEC");

            // Use interface to pull logging data from the Utec
            //UtecInterface.startDataLogFromUtec();

            // Set button states
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            openButton.setEnabled(false);
            closeButton.setEnabled(false);

            // Disable log save option
            JutecGUI.getInstance().saveItem.setEnabled(false);

            // VoiceThread vc = new VoiceThread("start capture");
            // vc.start();
        }

        // Stop Capture
        if (cmd.equals("STOP CAPTURE")) {
            System.out.println("Stopping data capture from the UTEC");

            // Use interface to reset the state of the Utec
            UtecInterface.resetUtec();

            // Set button states
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            closeButton.setEnabled(true);
            openButton.setEnabled(false);

            // Enable log save option
            JutecGUI.getInstance().saveItem.setEnabled(true);

            // VoiceThread vc = new VoiceThread("stop capture");
            // vc.start();
        }
    }

    public void getCommEvent(double[] doubleDarta) {

        //String utecData = e.getUtecBuffer();
        //totalLog += utecData;
        //textFromUtec.append(utecData);
        //textFromUtec.setCaretPosition(textFromUtec.getDocument().getLength());

        // System.out.println("Adding data to the text AREA");
    }
}
