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

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import com.coi.tools.os.win.NativeLibException;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.TargetFactory;
import com.izforge.izpack.util.os.RegistryHandler;
import com.izforge.izpack.util.os.WrappedNativeLibException;

/**
 * Uninstaller custom action for handling registry entries. The needed configuration data are
 * written at installation time from the corresponding installer custom action. An external
 * definiton is not needed.
 * 
 * @author Klaus Bartz
 * 
 */
public class RegistryUninstallerListener extends NativeUninstallerListener
{

    /**
     * Default constructor
     */
    public RegistryUninstallerListener()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#afterDeletion(java.util.List,
     * com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void beforeDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        // Load the defined actions.
        InputStream in = getClass().getResourceAsStream("/registryEntries");
        if (in == null)
        { // No actions, nothing todo.
            return;
        }
        ObjectInputStream objIn = new ObjectInputStream(in);
        List allActions = (List) objIn.readObject();
        objIn.close();
        in.close();
        if (allActions == null || allActions.size() < 1) return;
        try
        {
            RegistryHandler registryHandler = initializeRegistryHandler();
            if (registryHandler == null) return;
            registryHandler.activateLogging();
            registryHandler.setLoggingInfo(allActions);
            registryHandler.rewind();
        }
        catch (Exception e)
        {
            if (e instanceof NativeLibException)
            {
                throw new WrappedNativeLibException(e);
            }
            else
                throw e;
        }
    }

    private RegistryHandler initializeRegistryHandler() throws Exception
    {
        RegistryHandler registryHandler = null;
        try
        {
            registryHandler = (RegistryHandler) (TargetFactory.getInstance()
                    .makeObject("com.izforge.izpack.util.os.RegistryHandler"));
        }
        catch (Throwable exception)
        {
            exception.printStackTrace();
            registryHandler = null; // Do nothing, do not set permissions ...
        }
        if (registryHandler != null && (!registryHandler.good() || !registryHandler.doPerform()))
        {
            System.out.println("initializeRegistryHandler is Bad " + registryHandler.good()
                    + registryHandler.doPerform());
            registryHandler = null;
        }
        return (registryHandler);
    }

}
