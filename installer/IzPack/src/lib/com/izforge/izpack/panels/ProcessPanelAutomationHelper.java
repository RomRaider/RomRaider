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
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.installer.PanelAutomationHelper;
import com.izforge.izpack.installer.ProcessPanelWorker;
import com.izforge.izpack.util.AbstractUIProcessHandler;

/**
 * Functions to support automated usage of the CompilePanel
 * 
 * @author Jonathan Halliday
 * @author Tino Schwarze
 */
public class ProcessPanelAutomationHelper extends PanelAutomationHelper implements PanelAutomation,
        AbstractUIProcessHandler
{

    private int noOfJobs = 0;

    private int currentJob = 0;

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
     * 
     * @return true if processes were run successfully.
     */
    public boolean runAutomated(AutomatedInstallData idata, XMLElement panelRoot)
    {
        try
        {
            ProcessPanelWorker worker = new ProcessPanelWorker(idata, this);

            worker.run();
            
            return worker.getResult();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

    }

    public void logOutput(String message, boolean stderr)
    {
        if (stderr)
        {
            System.err.println(message);
        }
        else
        {
            System.out.println(message);
        }
    }

    /**
     * Reports progress on System.out
     * 
     * @see com.izforge.izpack.util.AbstractUIProcessHandler#startProcessing(int)
     */
    public void startProcessing(int noOfJobs)
    {
        System.out.println("[ Starting processing ]");
        this.noOfJobs = noOfJobs;
    }

    /**
     * 
     * @see com.izforge.izpack.util.AbstractUIProcessHandler#finishProcessing()
     */
    public void finishProcessing()
    {
        System.out.println("[ Processing finished ]");
    }

    /**
     * 
     */
    public void startProcess(String name)
    {
        this.currentJob++;
        System.out.println("Starting process " + name + " (" + Integer.toString(this.currentJob)
                + "/" + Integer.toString(this.noOfJobs) + ")");
    }

    public void finishProcess()
    {
    }
}
