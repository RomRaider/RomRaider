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

package com.izforge.izpack.event;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.ExtendedUIProgressHandler;

/**
 * Installer listener for reset the progress bar and initialize the simple installer listener to
 * support progress bar interaction. To support progress bar interaction add this installer listener
 * as first listener.
 * 
 * @author Klaus Bartz
 * 
 */
public class ProgressBarInstallerListener extends SimpleInstallerListener
{

    /**
     * 
     */
    public ProgressBarInstallerListener()
    {
        super(false);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.compiler.InstallerListener#afterPacks(com.izforge.izpack.installer.AutomatedInstallData,
     * com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler handler)
            throws Exception
    {
        if (handler instanceof ExtendedUIProgressHandler && getProgressBarCallerCount() > 0)
        {
            String progress = getMsg("CustomActions.progress");
            String tip = getMsg("CustomActions.tip");
            if ("CustomActions.tip".equals(tip) || "CustomActions.progress".equals(progress))
            {
                Debug
                        .trace("No messages found for custom action progress bar interactions; skiped.");
                return;
            }
            ((ExtendedUIProgressHandler) handler).restartAction("Configure", progress, tip,
                    getProgressBarCallerCount());
            SimpleInstallerListener.doInformProgressBar = true;
        }
    }

}
