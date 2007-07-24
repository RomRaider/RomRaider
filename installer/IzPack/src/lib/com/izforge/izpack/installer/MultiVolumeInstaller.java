/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/ http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2007 Dennis Reil
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.installer;

import java.io.File;
import java.lang.reflect.Method;

import com.izforge.izpack.uninstaller.SelfModifier;
import com.izforge.izpack.util.Debug;

/**
 * Main class, for starting the installer if it was build to support more than one volume.
 * 
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * 
 */
public class MultiVolumeInstaller
{

    // where is the installer looking for media files
    protected static String mediadirectory;

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // default is to look in the current directory
        MultiVolumeInstaller.setMediadirectory(new File(".").getParent());
        if ((args.length > 0) && ("-direct".equals(args[0])))
        {
            String[] newargs;
            if (args.length > 1)
            {
                // cut out the direct parameter
                newargs = new String[args.length - 1];
                System.arraycopy(args, 1, newargs, 0, args.length - 1);
            }
            else
            {
                // set arguments to empty string array
                newargs = new String[0];
            }
            MultiVolumeInstaller.install(newargs);
        }
        else
        {
            try
            {
                Class clazz = MultiVolumeInstaller.class;
                Method target = clazz.getMethod("install", new Class[] { String[].class});
                String[] newargs = new String[args.length + 2];

                System.arraycopy(args, 0, newargs, 2, args.length);
                // try to find the directory, where the jar file is located, this class was loaded
                // from
                newargs[0] = "-mediadir";
                newargs[1] = SelfModifier.findJarFile(clazz).getParent();
                System.out.println("Setting mediadir: " + newargs[1]);
                MultiVolumeInstaller.setMediadirectory(SelfModifier.findJarFile(clazz).getParent());
                new SelfModifier(target).invoke(newargs);
            }
            catch (Exception e)
            {
                Debug.trace(e);
            }
        }
    }

    public static String getMediadirectory()
    {
        return MultiVolumeInstaller.mediadirectory;
    }

    public static void setMediadirectory(String mediadirectory)
    {
        MultiVolumeInstaller.mediadirectory = mediadirectory;
    }

    public static void install(String[] args)
    {
        if ((args.length >= 2) && ("-mediadir".equals(args[0])))
        {
            // mediadir option given
            MultiVolumeInstaller.setMediadirectory(args[1]);
            if (args.length > 2)
            {
                // cut out this option
                String[] newargs = new String[args.length - 2];
                System.arraycopy(args, 2, newargs, 0, args.length - 2);
                args = newargs;
            }
            else
            {
                // put in an empty string array
                args = new String[0];
            }
        }
        // just call the izpack installer
        Installer.main(args);
    }
}
