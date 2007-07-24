/*
 /*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2003 Jonathan Halliday
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

import java.io.IOException;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.CompileHandler;
import com.izforge.izpack.installer.CompileResult;
import com.izforge.izpack.installer.CompileWorker;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.installer.PanelAutomationHelper;

/**
 * Functions to support automated usage of the CompilePanel
 * 
 * @author Jonathan Halliday
 * @author Tino Schwarze
 */
public class CompilePanelAutomationHelper extends PanelAutomationHelper implements PanelAutomation,
        CompileHandler
{

    private CompileWorker worker = null;

    private int job_max = 0;

    private String job_name = null;

    private int last_line_len = 0;

    /**
     * Save data for running automated.
     * 
     * @param installData installation parameters
     * @param panelRoot unused.
     */
    public void makeXMLData(AutomatedInstallData installData, XMLElement panelRoot)
    {
        // not used here - during automatic installation, no automatic
        // installation information is generated
    }

    /**
     * Perform the installation actions.
     * 
     * @param panelRoot The panel XML tree root.
     */
    public boolean runAutomated(AutomatedInstallData idata, XMLElement panelRoot)
    {
        XMLElement compiler_xml = panelRoot.getFirstChildNamed("compiler");

        String compiler = null;

        if (compiler_xml != null) compiler = compiler_xml.getContent();

        if (compiler == null)
        {
            System.out.println("invalid automation data: could not find compiler");
            return false;
        }

        XMLElement args_xml = panelRoot.getFirstChildNamed("arguments");

        String args = null;

        if (args_xml != null) args = args_xml.getContent();

        if (args_xml == null)
        {
            System.out.println("invalid automation data: could not find compiler arguments");
            return false;
        }

        try
        {
            this.worker = new CompileWorker(idata, this);
            this.worker.setCompiler(compiler);
            this.worker.setCompilerArguments(args);

            this.worker.run();
            
            return this.worker.getResult().isSuccess();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reports progress on System.out
     * 
     * @see com.izforge.izpack.util.AbstractUIProgressHandler#startAction(String, int)
     */
    public void startAction(String name, int noOfJobs)
    {
        System.out.println("[ Starting compilation ]");
        this.job_name = "";
    }

    /**
     * Reports the error to System.err
     * 
     * @param error the error
     * @see CompileHandler#handleCompileError(CompileResult)
     */
    public void handleCompileError(CompileResult error)
    {
        System.out.println();
        System.out.println("[ Compilation failed ]");
        System.err.println("Command line: " + error.getCmdline());
        System.err.println();
        System.err.println("stdout of compiler:");
        System.err.println(error.getStdout());
        System.err.println("stderr of compiler:");
        System.err.println(error.getStderr());
        // abort instantly and make installation fail
        error.setAction(CompileResult.ACTION_ABORT);
    }

    /**
     * Sets state variable for thread sync.
     * 
     * @see com.izforge.izpack.util.AbstractUIProgressHandler#stopAction()
     */
    public void stopAction()
    {
        if ((this.job_name != null) && (this.last_line_len > 0))
        {
            String line = this.job_name + ": done.";
            System.out.print("\r" + line);
            for (int i = line.length(); i < this.last_line_len; i++)
                System.out.print(' ');
            System.out.println();
        }

        if (this.worker.getResult().isSuccess()) System.out.println("[ Compilation successful ]");
    }

    /**
     * Tell about progress.
     * 
     * @param val
     * @param msg
     * @see com.izforge.izpack.util.AbstractUIProgressHandler#progress(int, String)
     */
    public void progress(int val, String msg)
    {
        double percentage = ((double) val) * 100.0d / (double) this.job_max;

        String percent = (new Integer((int) percentage)).toString() + '%';
        String line = this.job_name + ": " + percent;

        int line_len = line.length();

        System.out.print("\r" + line);
        for (int i = line_len; i < this.last_line_len; i++)
            System.out.print(' ');

        this.last_line_len = line_len;
    }

    /**
     * Reports progress to System.out
     * 
     * @param jobName The next job's name.
     * @param max unused
     * @param jobNo The next job's number.
     * @see com.izforge.izpack.util.AbstractUIProgressHandler#nextStep(String, int, int)
     */
    public void nextStep(String jobName, int max, int jobNo)
    {
        if ((this.job_name != null) && (this.last_line_len > 0))
        {
            String line = this.job_name + ": done.";
            System.out.print("\r" + line);
            for (int i = line.length(); i < this.last_line_len; i++)
                System.out.print(' ');
            System.out.println();
        }

        this.job_max = max;
        this.job_name = jobName;
        this.last_line_len = 0;
    }
}
