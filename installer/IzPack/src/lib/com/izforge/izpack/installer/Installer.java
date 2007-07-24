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

package com.izforge.izpack.installer;

import java.util.Date;

import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.StringTool;

/**
 * The program entry point. Selects between GUI and text install modes.
 * 
 * @author Jonathan Halliday
 */
public class Installer
{

    /**
     * The main method (program entry point).
     * 
     * @param args The arguments passed on the command-line.
     */
    public static void main(String[] args)
    {
        Debug.log(" - Logger initialized at '"+ new Date( System.currentTimeMillis() )+ "'.");
        
        Debug.log(" - commandline args: " + StringTool.stringArrayToSpaceSeparatedString(args) );
        
        // OS X tweakings
        if (System.getProperty("mrj.version") != null)
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "IzPack");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("com.apple.mrj.application.live-resize", "true");
        }

        try
        {
            if (args.length == 0)
            {
                // can't load the GUIInstaller class on headless machines,
                // so we use Class.forName to force lazy loading.
                Class.forName("com.izforge.izpack.installer.GUIInstaller").newInstance();
            }
            else
            {
                AutomatedInstaller ai = new AutomatedInstaller(args[0]);
                // this method will also exit!
                ai.doInstall();
            }
        }
        catch (Exception e)
        {
            System.err.println("- ERROR -");
            System.err.println(e.toString());
            e.printStackTrace();
            System.exit(1);
        }
    }

}
