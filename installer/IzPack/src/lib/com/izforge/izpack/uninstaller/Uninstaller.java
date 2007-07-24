/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
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

package com.izforge.izpack.uninstaller;

import javax.swing.*;
import java.lang.reflect.Method;

/**
 * The uninstaller class.
 *
 * @author Julien Ponge
 */
public class Uninstaller
{

    /**
     * The main method (program entry point).
     *
     * @param args The arguments passed on the command line.
     */
    public static void main(String[] args)
    {
        boolean cmduninstall = false;
        for (int q = 0; q < args.length; q++) if (args[q].equals("-c")) cmduninstall = true;
        if (cmduninstall) System.out.println("Command line uninstaller.\n");
        try
        {
            Class clazz = Uninstaller.class;
            Method target;
            if (cmduninstall)
                target = clazz.getMethod("cmduninstall", new Class[]{String[].class});
            else
                target = clazz.getMethod("uninstall", new Class[]{String[].class});
            new SelfModifier(target).invoke(args);
        }
        catch (Exception ioeOrTypo)
        {
            System.err.println(ioeOrTypo.getMessage());
            ioeOrTypo.printStackTrace();
            System.err.println("Unable to exec java as a subprocess.");
            System.err.println("The uninstall may not fully complete.");
            uninstall(args);
        }
    }

    public static void cmduninstall(String[] args)
    {
        try
        {
            UninstallerConsole uco = new UninstallerConsole();
            boolean force = false;
            for (int q = 0; q < args.length; q++) if (args[q].equals("-f")) force = true;
            System.out.println("Force deletion: " + force);
            uco.runUninstall(force);
        }
        catch (Exception err)
        {
            System.err.println("- Error -");
            err.printStackTrace();
            System.exit(0);
        }
    }

    public static void uninstall(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    new UninstallerFrame();
                }
                catch (Exception err)
                {
                    System.err.println("- Error -");
                    err.printStackTrace();
                    System.exit(0);
                }
            }
        });
    }
}
