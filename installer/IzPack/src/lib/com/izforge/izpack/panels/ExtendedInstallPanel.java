/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Klaus Bartz
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

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.util.ExtendedUIProgressHandler;

/**
 * The install panel class. Launches the actual installation job with extensions for custom actions.
 * 
 * @author Klaus Bartz
 */
public class ExtendedInstallPanel extends InstallPanel implements ExtendedUIProgressHandler
{

    private static final long serialVersionUID = 3257291344052500789L;

    protected int currentStep = 0;

    /**
     * The constructor.
     * 
     * @param parent The parent window.
     * @param idata The installation data.
     */
    public ExtendedInstallPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.util.ExtendedUIProgressHandler#startAction(java.lang.String,
     * java.lang.String, java.lang.String, int)
     */
    public void restartAction(String name, String overallMsg, String tipMsg, int no_of_steps)
    {
        overallOpLabel.setText(overallMsg);
        tipLabel.setText(tipMsg);
        currentStep = 0;
        startAction(name, no_of_steps);
    }

    /**
     * Normal progress indicator.
     * 
     * @param val The progression value.
     * @param msg The progression message.
     */
    public void progress(int val, String msg)
    {
        packProgressBar.setValue(val + 1);
        packOpLabel.setText(msg);
        currentStep++;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.util.ExtendedUIProgressHandler#progress(java.lang.String,
     * java.lang.String)
     */
    public void progress(String stepMessage)
    {
        packOpLabel.setText(stepMessage);
        currentStep++;
        packProgressBar.setValue(currentStep);
    }

    /**
     * Pack changing.
     * 
     * @param packName The pack name.
     * @param stepno The number of the pack.
     * @param max The new maximum progress.
     */
    public void nextStep(String packName, int stepno, int max)
    {
        currentStep = 0;
        super.nextStep(packName, stepno, max);
    }

}
