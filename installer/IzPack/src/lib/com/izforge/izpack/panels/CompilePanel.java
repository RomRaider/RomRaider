/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2003 Tino Schwarze
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
package com.izforge.izpack.panels;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.CompileHandler;
import com.izforge.izpack.installer.CompileResult;
import com.izforge.izpack.installer.CompileWorker;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;

/**
 * The compile panel class.
 * 
 * This class allows .java files to be compiled after installation.
 * 
 * Parts of the code have been taken from InstallPanel.java and modified a lot.
 * 
 * @author Tino Schwarze
 * @author Julien Ponge
 */
public class CompilePanel extends IzPanel implements ActionListener, CompileHandler
{

    /**
     * 
     */
    private static final long serialVersionUID = 3258408430669674552L;

    /** The combobox for compiler selection. */
    protected JComboBox compilerComboBox;

    /** The combobox for compiler argument selection. */
    protected JComboBox argumentsComboBox;

    /** The start button. */
    protected JButton startButton;

    /** The browse button. */
    protected JButton browseButton;

    /** The tip label. */
    protected JLabel tipLabel;

    /** The operation label . */
    protected JLabel opLabel;

    /** The pack progress bar. */
    protected JProgressBar packProgressBar;

    /** The operation label . */
    protected JLabel overallLabel;

    /** The overall progress bar. */
    protected JProgressBar overallProgressBar;

    /** True if the compilation has been done. */
    private boolean validated = false;

    /** The compilation worker. Does all the work. */
    private CompileWorker worker;

    /** Number of jobs to compile. Used for progress indication. */
    private int noOfJobs;

    /**
     * The constructor.
     * 
     * @param parent The parent window.
     * @param idata The installation data.
     * @throws IOException 
     */
    public CompilePanel(InstallerFrame parent, InstallData idata) throws IOException
    {
        super(parent, idata);

        this.worker = new CompileWorker(idata, this);

        GridBagConstraints gridBagConstraints;

        JLabel heading = new JLabel();
        // put everything but the heading into it's own panel
        // (to center it vertically)
        JPanel subpanel = new JPanel();
        JLabel compilerLabel = new JLabel();
        compilerComboBox = new JComboBox();
        this.browseButton = ButtonFactory.createButton(parent.langpack
                .getString("CompilePanel.browse"), idata.buttonsHColor);
        JLabel argumentsLabel = new JLabel();
        this.argumentsComboBox = new JComboBox();
        this.startButton = ButtonFactory.createButton(parent.langpack
                .getString("CompilePanel.start"), idata.buttonsHColor);
        this.tipLabel = LabelFactory.create(parent.langpack.getString("CompilePanel.tip"),
                parent.icons.getImageIcon("tip"), SwingConstants.TRAILING);
        this.opLabel = new JLabel();
        packProgressBar = new JProgressBar();
        this.overallLabel = new JLabel();
        this.overallProgressBar = new JProgressBar();

        setLayout(new GridBagLayout());

        Font font = heading.getFont();
        font = font.deriveFont(Font.BOLD, font.getSize() * 2.0f);
        heading.setFont(font);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setText(parent.langpack.getString("CompilePanel.heading"));
        heading.setVerticalAlignment(SwingConstants.TOP);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.1;
        add(heading, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.9;
        add(subpanel, gridBagConstraints);

        subpanel.setLayout(new GridBagLayout());

        int row = 0;

        compilerLabel.setHorizontalAlignment(SwingConstants.LEFT);
        compilerLabel.setLabelFor(compilerComboBox);
        compilerLabel.setText(parent.langpack.getString("CompilePanel.choose_compiler"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = row;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        // gridBagConstraints.weighty = 0.1;
        subpanel.add(compilerLabel, gridBagConstraints);

        compilerComboBox.setEditable(true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = row++;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        // gridBagConstraints.weighty = 0.1;

        Iterator it = this.worker.getAvailableCompilers().iterator();

        while (it.hasNext())
            compilerComboBox.addItem(it.next());

        subpanel.add(compilerComboBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = row++;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        browseButton.addActionListener(this);
        subpanel.add(browseButton, gridBagConstraints);

        argumentsLabel.setHorizontalAlignment(SwingConstants.LEFT);
        argumentsLabel.setLabelFor(argumentsComboBox);
        argumentsLabel.setText(parent.langpack.getString("CompilePanel.additional_arguments"));
        // argumentsLabel.setToolTipText("");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = row;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        // gridBagConstraints.weighty = 0.1;
        subpanel.add(argumentsLabel, gridBagConstraints);

        argumentsComboBox.setEditable(true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = row++;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        // gridBagConstraints.weighty = 0.1;

        it = this.worker.getAvailableArguments().iterator();

        while (it.hasNext())
            argumentsComboBox.addItem(it.next());

        subpanel.add(argumentsComboBox, gridBagConstraints);

        // leave some space above the label
        gridBagConstraints.insets = new Insets(10, 0, 0, 0);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = row++;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        subpanel.add(tipLabel, gridBagConstraints);

        opLabel.setText(" ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = row++;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        subpanel.add(opLabel, gridBagConstraints);

        packProgressBar.setValue(0);
        packProgressBar.setString(parent.langpack.getString("CompilePanel.progress.initial"));
        packProgressBar.setStringPainted(true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = row++;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.SOUTH;
        subpanel.add(packProgressBar, gridBagConstraints);

        overallLabel.setText(parent.langpack.getString("CompilePanel.progress.overall"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = row++;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        subpanel.add(overallLabel, gridBagConstraints);

        overallProgressBar.setValue(0);
        overallProgressBar.setString("");
        overallProgressBar.setStringPainted(true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = row++;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.SOUTH;
        subpanel.add(overallProgressBar, gridBagConstraints);

        startButton.setText(parent.langpack.getString("CompilePanel.start"));
        startButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridy = row++;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        // leave some space above the button
        gridBagConstraints.insets = new Insets(5, 0, 0, 0);
        subpanel.add(startButton, gridBagConstraints);
    }

    /**
     * Indicates wether the panel has been validated or not.
     * 
     * @return The validation state.
     */
    public boolean isValidated()
    {
        return validated;
    }

    /**
     * Action function, called when the start button is pressed.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == this.startButton)
        {
            this.worker.setCompiler((String) this.compilerComboBox.getSelectedItem());

            this.worker.setCompilerArguments((String) this.argumentsComboBox.getSelectedItem());

            this.blockGUI();
            this.worker.startThread();
        }
        else if (e.getSource() == this.browseButton)
        {
            this.parent.blockGUI();
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File((String) this.compilerComboBox.getSelectedItem())
                    .getParentFile());
            int result = chooser.showDialog(this.parent, this.parent.langpack
                    .getString("CompilePanel.browse.approve"));
            if (result == JFileChooser.APPROVE_OPTION)
            {
                File file_chosen = chooser.getSelectedFile();

                if (file_chosen.isFile())
                {
                    this.compilerComboBox.setSelectedItem(file_chosen.getAbsolutePath());
                }

            }

            this.parent.releaseGUI();
        }

    }

    /**
     * Block the GUI - disalow input.
     */
    protected void blockGUI()
    {
        // disable all controls
        this.startButton.setEnabled(false);
        this.browseButton.setEnabled(false);
        this.compilerComboBox.setEnabled(false);
        this.argumentsComboBox.setEnabled(false);

        this.parent.blockGUI();
    }

    /**
     * Release the GUI - allow input.
     * 
     * @param allowconfig allow the user to enter new configuration
     */
    protected void releaseGUI(boolean allowconfig)
    {
        // disable all controls
        if (allowconfig)
        {
            this.startButton.setEnabled(true);
            this.browseButton.setEnabled(true);
            this.compilerComboBox.setEnabled(true);
            this.argumentsComboBox.setEnabled(true);
        }

        this.parent.releaseGUI();
    }

    /**
     * An error was encountered.
     * 
     * @param error The error information.
     * @see com.izforge.izpack.installer.CompileHandler
     */
    public void handleCompileError(CompileResult error)
    {
        String message = error.getMessage();
        opLabel.setText(message);
        CompilerErrorDialog dialog = new CompilerErrorDialog(parent, message, idata.buttonsHColor);
        dialog.show(error);

        if (dialog.getResult() == CompilerErrorDialog.RESULT_IGNORE)
        {
            error.setAction(CompileResult.ACTION_CONTINUE);
        }
        else if (dialog.getResult() == CompilerErrorDialog.RESULT_RECONFIGURE)
        {
            error.setAction(CompileResult.ACTION_RECONFIGURE);
        }
        else
        // default case: abort
        {
            error.setAction(CompileResult.ACTION_ABORT);
        }

    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.util.AbstractUIProgressHandler#startAction(java.lang.String, int)
     */
    public void startAction(String name, int noOfJobs1)
    {
        this.noOfJobs = noOfJobs1;
        overallProgressBar.setMaximum(noOfJobs1);
        parent.lockPrevButton();
    }

    /** The compiler stops. */
    public void stopAction()
    {
        CompileResult result = this.worker.getResult();

        this.releaseGUI(result.isReconfigure());

        if (result.isContinue())
        {
            parent.lockPrevButton();

            packProgressBar.setString(parent.langpack.getString("CompilePanel.progress.finished"));
            packProgressBar.setEnabled(false);
            packProgressBar.setValue(packProgressBar.getMaximum());

            overallProgressBar.setValue(this.noOfJobs);
            String no_of_jobs = Integer.toString(this.noOfJobs);
            overallProgressBar.setString(no_of_jobs + " / " + no_of_jobs);
            overallProgressBar.setEnabled(false);

            opLabel.setText(" ");
            opLabel.setEnabled(false);

            validated = true;
            idata.installSuccess = true;
            if (idata.panels.indexOf(this) != (idata.panels.size() - 1)) parent.unlockNextButton();
        }
        else
        {
            idata.installSuccess = false;
        }

    }

    /**
     * Normal progress indicator.
     * 
     * @param val The progression value.
     * @param msg The progression message.
     */
    public void progress(int val, String msg)
    {
        // Debug.trace ("progress: " + val + " " + msg);
        packProgressBar.setValue(val + 1);
        opLabel.setText(msg);
    }

    /**
     * Job changing.
     * 
     * @param jobName The job name.
     * @param max The new maximum progress.
     * @param jobNo The job number.
     */
    public void nextStep(String jobName, int max, int jobNo)
    {
        packProgressBar.setValue(0);
        packProgressBar.setMaximum(max);
        packProgressBar.setString(jobName);

        opLabel.setText("");

        overallProgressBar.setValue(jobNo);
        overallProgressBar.setString(Integer.toString(jobNo) + " / "
                + Integer.toString(this.noOfJobs));
    }

    /** Called when the panel becomes active. */
    public void panelActivate()
    {
        // get compilers again (because they might contain variables from former
        // panels)
        Iterator it = this.worker.getAvailableCompilers().iterator();

        compilerComboBox.removeAllItems();

        while (it.hasNext())
            compilerComboBox.addItem(it.next());

        // We clip the panel
        Dimension dim = parent.getPanelsContainerSize();
        dim.width -= (dim.width / 4);
        dim.height = 150;
        setMinimumSize(dim);
        setMaximumSize(dim);
        setPreferredSize(dim);

        parent.lockNextButton();
    }

    /** Create XML data for automated installation. */
    public void makeXMLData(XMLElement panelRoot)
    {
        // just save the compiler chosen and the arguments
        XMLElement compiler = new XMLElement("compiler");
        compiler.setContent(this.worker.getCompiler());
        panelRoot.addChild(compiler);

        XMLElement args = new XMLElement("arguments");
        args.setContent(this.worker.getCompilerArguments());
        panelRoot.addChild(args);
    }

    /**
     * Show a special dialog for compiler errors.
     * 
     * This dialog is neccessary because we have lots of information if compilation failed. We'd
     * also like the user to chose whether to ignore the error or not.
     */
    protected class CompilerErrorDialog extends JDialog implements ActionListener
    {

        private static final long serialVersionUID = 3762537797721995317L;

        /** user closed the dialog without pressing "Ignore" or "Abort" */
        public static final int RESULT_NONE = 0;

        /** user pressed "Ignore" button */
        public static final int RESULT_IGNORE = 23;

        /** user pressed "Abort" button */
        public static final int RESULT_ABORT = 42;

        /** user pressed "Reconfigure" button */
        public static final int RESULT_RECONFIGURE = 47;

        /** visual goodie: button hightlight color */
        private java.awt.Color buttonHColor = null;

        /** Creates new form compilerErrorDialog 
         * @param parent parent to be used
         * @param title String to be used as title
         * @param buttonHColor highlight color to be used*/
        public CompilerErrorDialog(java.awt.Frame parent, String title, java.awt.Color buttonHColor)
        {
            super(parent, title, true);
            this.buttonHColor = buttonHColor;
            initComponents();
        }

        /**
         * This method is called from within the constructor to initialize the form.
         * 
         * Generated with help from NetBeans IDE.
         */
        private void initComponents()
        {
            JPanel errorMessagePane = new JPanel();
            errorMessageText = new JTextArea();
            JTextArea seeBelowText = new JTextArea();
            JTabbedPane errorDisplayPane = new JTabbedPane();
            JScrollPane commandScrollPane = new JScrollPane();
            commandText = new JTextArea();
            JScrollPane stdOutScrollPane = new JScrollPane();
            stdOutText = new JTextArea();
            JScrollPane stdErrScrollPane = new JScrollPane();
            stdErrText = new JTextArea();
            JPanel buttonsPanel = new JPanel();
            reconfigButton = ButtonFactory.createButton(parent.langpack
                    .getString("CompilePanel.error.reconfigure"), this.buttonHColor);
            ignoreButton = ButtonFactory.createButton(parent.langpack
                    .getString("CompilePanel.error.ignore"), this.buttonHColor);
            abortButton = ButtonFactory.createButton(parent.langpack
                    .getString("CompilePanel.error.abort"), this.buttonHColor);

            addWindowListener(new java.awt.event.WindowAdapter() {

                public void windowClosing(java.awt.event.WindowEvent evt)
                {
                    closeDialog();
                }
            });

            errorMessagePane.setLayout(new BoxLayout(errorMessagePane, BoxLayout.Y_AXIS));
            errorMessageText.setBackground(super.getBackground());
            errorMessageText.setEditable(false);
            errorMessageText.setLineWrap(true);
            // errorMessageText.setText("The compiler does not seem to work. See
            // below for the command we tried to execute and the results.");
            // errorMessageText.setToolTipText("null");
            errorMessageText.setWrapStyleWord(true);
            errorMessagePane.add(errorMessageText);

            seeBelowText.setBackground(super.getBackground());
            seeBelowText.setEditable(false);
            seeBelowText.setLineWrap(true);
            seeBelowText.setWrapStyleWord(true);
            seeBelowText.setText(parent.langpack.getString("CompilePanel.error.seebelow"));
            errorMessagePane.add(seeBelowText);

            getContentPane().add(errorMessagePane, java.awt.BorderLayout.NORTH);

            // use 12pt monospace font for compiler output etc.
            Font output_font = new Font("Monospaced", Font.PLAIN, 12);

            // errorDisplayPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            // errorDisplayPane.setName("null");
            commandText.setFont(output_font);
            commandText.setEditable(false);
            commandText.setRows(10);
            commandText.setColumns(82);
            commandText.setWrapStyleWord(true);
            commandText.setLineWrap(true);
            // commandText.setText("akjfkajfeafjakefjakfkaejfja");
            commandScrollPane.setViewportView(commandText);

            errorDisplayPane.addTab("Command", commandScrollPane);

            stdOutText.setFont(output_font);
            stdOutText.setEditable(false);
            stdOutText.setWrapStyleWord(true);
            stdOutText.setLineWrap(true);
            stdOutScrollPane.setViewportView(stdOutText);

            errorDisplayPane.addTab("Standard Output", null, stdOutScrollPane);

            stdErrText.setFont(output_font);
            stdErrText.setEditable(false);
            stdErrText.setWrapStyleWord(true);
            stdErrText.setLineWrap(true);
            stdErrScrollPane.setViewportView(stdErrText);

            errorDisplayPane.addTab("Standard Error", null, stdErrScrollPane);

            getContentPane().add(errorDisplayPane, java.awt.BorderLayout.CENTER);

            buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

            reconfigButton.addActionListener(this);
            buttonsPanel.add(reconfigButton);

            ignoreButton.addActionListener(this);
            buttonsPanel.add(ignoreButton);

            abortButton.addActionListener(this);
            buttonsPanel.add(abortButton);

            getContentPane().add(buttonsPanel, java.awt.BorderLayout.SOUTH);

            pack();
        }

        /** 
         * Close the panel.
         */
        protected void closeDialog()
        {
            setVisible(false);
            dispose();
        }

        /**
         * Shows the given errors
         * @param error error messages to be shown
         */
        public void show(CompileResult error)
        {
            this.errorMessageText.setText(error.getMessage());
            this.commandText.setText(error.getCmdline());
            this.stdOutText.setText(error.getStdout());
            this.stdErrText.setText(error.getStderr());
            super.setVisible(true);
        }

        /**
         * Returns the result of this dialog.
         * @return the result of this dialog
         */
        public int getResult()
        {
            return this.result;
        }

        public void actionPerformed(ActionEvent e)
        {
            boolean closenow = false;

            if (e.getSource() == this.ignoreButton)
            {
                this.result = RESULT_IGNORE;
                closenow = true;
            }
            else if (e.getSource() == this.abortButton)
            {
                this.result = RESULT_ABORT;
                closenow = true;
            }
            else if (e.getSource() == this.reconfigButton)
            {
                this.result = RESULT_RECONFIGURE;
                closenow = true;
            }

            if (closenow)
            {
                this.setVisible(false);
                this.dispose();
            }

        }

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private JTextArea commandText;

        // private JScrollPane stdOutScrollPane;
        private JTextArea stdErrText;

        // private JPanel buttonsPanel;
        // private JScrollPane commandScrollPane;
        private JTextArea errorMessageText;

        // private JScrollPane stdErrScrollPane;
        private JButton ignoreButton;

        private JTextArea stdOutText;

        private JButton abortButton;

        private JButton reconfigButton;

        // private JTabbedPane errorDisplayPane;
        // End of variables declaration//GEN-END:variables

        private int result = RESULT_NONE;
    }
}
