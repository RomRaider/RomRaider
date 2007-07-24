/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2006 Amit Bhayani / JBoss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack;

/**
 * TwoColumnLayoutTest.java is a 1.4 application that 
 * demonstrates the use of JButton, JTextField and
 * JLabel.  It requires no other files.
 * @author abhayani Amit Bhayani
 */

import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.gui.TwoColumnLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TwoColumnLayoutTest implements ActionListener
{
    JFrame converterFrame;
    JPanel converterPanel;
    JTextField tempText;
    JLabel label;
    JButton addRow;
    JButton removeRow;

    boolean removed = false;

    public TwoColumnLayoutTest()
    {
        //Create and set up the window.
        converterFrame = new JFrame("TwoColumnLayoutTest");
        converterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        converterFrame.setSize(new Dimension(240, 80));

        TwoColumnLayout layout = new TwoColumnLayout(10, 5, 30, 25, TwoColumnLayout.LEFT);

        //Create and set up the panel.
        converterPanel = new JPanel();
        converterPanel.setLayout(layout);

        //Add the widgets.
        addWidgets();

        //Set the default button.
        converterFrame.getRootPane().setDefaultButton(addRow);

        //Add the panel to the window.
        converterFrame.getContentPane().add(converterPanel, BorderLayout.CENTER);

        //Display the window.
        converterFrame.pack();
        converterFrame.setVisible(true);
    }

    /**
     * Create and add the widgets.
     */
    private void addWidgets()
    {
        //Create widgets.
        tempText = new JTextField("10", 30);
        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.EAST;

        label = new JLabel("Label : ");
        TwoColumnConstraints constraints1 = new TwoColumnConstraints();
        constraints1.position = TwoColumnConstraints.WEST;

        addRow = new JButton("Add Row");
        TwoColumnConstraints constraints2 = new TwoColumnConstraints();
        constraints2.position = TwoColumnConstraints.BOTH;

        //Listen to events from the Convert button.
        addRow.addActionListener(this);

        //Add the widgets to the container.
        converterPanel.add(tempText, constraints);
        converterPanel.add(label, constraints1);
        converterPanel.add(addRow, constraints2);


        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    }

    public void actionPerformed(ActionEvent event)
    {

        if (!removed)
        {

            converterPanel.remove(tempText);
            converterPanel.remove(label);
            removed = true;

        }
        else
        {
            TwoColumnConstraints constraints = new TwoColumnConstraints();
            constraints.position = TwoColumnConstraints.EAST;
            converterPanel.add(tempText, constraints);

            TwoColumnConstraints constraints1 = new TwoColumnConstraints();
            constraints1.position = TwoColumnConstraints.WEST;
            converterPanel.add(label, constraints1);
            removed = false;

        }
        converterPanel.repaint();

    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI()
    {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        TwoColumnLayoutTest converter = new TwoColumnLayoutTest();
    }

    public static void main(String[] args)
    {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                createAndShowGUI();
            }
        });
    }
}
