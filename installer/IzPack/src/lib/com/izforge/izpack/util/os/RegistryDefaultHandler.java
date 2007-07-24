/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2005 Klaus Bartz
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

package com.izforge.izpack.util.os;

import com.izforge.izpack.util.TargetFactory;

/**
 * This class provides on windows a registry handler. All classes which needs registry access should
 * be use only one handler.
 * 
 * @author Klaus Bartz
 * 
 */
public class RegistryDefaultHandler
{

    private static RegistryHandler registryHandler = null;

    private static boolean initialized = false;

    /**
     * Default constructor. No instance of this class should be created.
     */
    private RegistryDefaultHandler()
    {
        super();
    }

    public synchronized static RegistryHandler getInstance()
    {
        if (!initialized)
        {
            try
            {
                // Load the system dependant handler.
                registryHandler = (RegistryHandler) (TargetFactory.getInstance()
                        .makeObject("com.izforge.izpack.util.os.RegistryHandler"));
                // Switch to the default handler to use one for complete logging.
                registryHandler = registryHandler.getDefaultHandler();
            }
            catch (Throwable exception)
            {
                registryHandler = null; // 
            }
            initialized = true;
        }
        if (registryHandler != null && (!registryHandler.good() || !registryHandler.doPerform()))
            registryHandler = null;

        return (registryHandler);
    }
}
