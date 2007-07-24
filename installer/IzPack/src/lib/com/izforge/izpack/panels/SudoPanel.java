/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2003 Jan Blok
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.ParsableFile;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ScriptParser;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * The packs selection panel class.
 * 
 * @author Jan Blok
 * @since November 27, 2003
 */
public class SudoPanel extends IzPanel implements ActionListener
{

    /**
     * 
     */
    private static final long serialVersionUID = 3689628116465561651L;

    private JTextField passwordField;

    private boolean isValid = false;

    /**
     * The constructor.
     * 
     * @param parent The parent window.
     * @param idata The installation data.
     */
    public SudoPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(LabelFactory
                .create(
                        /* parent.langpack.getString("SudoPanel.info") */"For installing administrator privileges are necessary",
                        JLabel.TRAILING));

        add(Box.createRigidArea(new Dimension(0, 5)));

        add(LabelFactory
                .create(
                        /* parent.langpack.getString("SudoPanel.tip") */"Please note that passwords are case-sensitive",
                        parent.icons.getImageIcon("tip"), JLabel.TRAILING));

        add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel spacePanel = new JPanel();
        spacePanel.setAlignmentX(LEFT_ALIGNMENT);
        spacePanel.setAlignmentY(CENTER_ALIGNMENT);
        spacePanel.setBorder(BorderFactory.createEmptyBorder(80, 30, 0, 50));
        spacePanel.setLayout(new BorderLayout(5, 5));
        spacePanel
                .add(
                        LabelFactory
                                .create(
                                /* parent.langpack.getString("SudoPanel.specifyAdminPassword") */"Please specify your password:"),
                        BorderLayout.NORTH);
        passwordField = new JPasswordField();
        passwordField.addActionListener(this);
        JPanel space2Panel = new JPanel();
        space2Panel.setLayout(new BorderLayout());
        space2Panel.add(passwordField, BorderLayout.NORTH);
        space2Panel.add(Box.createRigidArea(new Dimension(0, 5)), BorderLayout.CENTER);
        spacePanel.add(space2Panel, BorderLayout.CENTER);
        add(spacePanel);
    }

    /** Called when the panel becomes active. */
    public void panelActivate()
    {
        passwordField.requestFocus();
    }

    /**
     * Actions-handling method.
     * 
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e)
    {
        doSudoCmd();
    }

    // check if sudo password is correct (so sudo can be used in all other
    // scripts, even without password, lasts for 5 minutes)
    private void doSudoCmd()
    {
        String pass = passwordField.getText();

        File file = null;
        try
        {
            // write file in /tmp
            file = new File("/tmp/cmd_sudo.sh");// ""c:/temp/run.bat""
            FileOutputStream fos = new FileOutputStream(file);
            fos.write("echo $password | sudo -S ls\nexit $?".getBytes()); // "echo
            // $password
            // >
            // pipo.txt"
            fos.close();

            // execute
            Properties vars = new Properties();
            vars.put("password", pass);

            List oses = new ArrayList();
            oses.add(new OsConstraint("unix", null, null, null));

            ArrayList plist = new ArrayList();
            ParsableFile pf = new ParsableFile(file.getAbsolutePath(), null, null, oses);
            plist.add(pf);
            ScriptParser sp = new ScriptParser(plist, new VariableSubstitutor(vars));
            sp.parseFiles();

            ArrayList elist = new ArrayList();
            ExecutableFile ef = new ExecutableFile(file.getAbsolutePath(),
                    ExecutableFile.POSTINSTALL, ExecutableFile.ABORT, oses, false);
            elist.add(ef);
            FileExecutor fe = new FileExecutor(elist);
            int retval = fe.executeFiles(ExecutableFile.POSTINSTALL, this);
            if (retval == 0)
            {
                idata.setVariable("password", pass);
                isValid = true;
            }
            // else is already showing dialog
            // {
            // JOptionPane.showMessageDialog(this, "Cannot execute 'sudo' cmd,
            // check your password", "Error", JOptionPane.ERROR_MESSAGE);
            // }
        }
        catch (Exception e)
        {
            // JOptionPane.showMessageDialog(this, "Cannot execute 'sudo' cmd,
            // check your password", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            isValid = false;
        }
        try
        {
            if (file != null && file.exists()) file.delete();// you don't
            // want the file
            // with password
            // tobe arround,
            // in case of
            // error
        }
        catch (Exception e)
        {
            // ignore
        }
    }

    /**
     * Indicates wether the panel has been validated or not.
     * 
     * @return Always true.
     */
    public boolean isValidated()
    {
        if (!isValid)
        {
            doSudoCmd();
        }
        if (!isValid)
        {
            JOptionPane.showInternalMessageDialog(this, "Password", "Password is not valid",
                    JOptionPane.ERROR_MESSAGE);
        }
        return isValid;
    }
}
