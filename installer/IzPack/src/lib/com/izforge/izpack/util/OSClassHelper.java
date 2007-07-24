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

package com.izforge.izpack.util;

import com.izforge.izpack.installer.AutomatedInstallData;

/*---------------------------------------------------------------------------*/
/**
 * This class is the system independent base class for helpers which are system dependent in its
 * subclasses.
 * 
 * @author Klaus Bartz
 */
/*---------------------------------------------------------------------------*/
public class OSClassHelper
{

    protected AutomatedInstallData installdata;

    protected Class workerClass = null;

    protected Object worker = null;

    /**
     * Default constructor
     */
    public OSClassHelper()
    {
        super();
    }

    /**
     * Creates an object which contains as worker an object of the given class name if possible. If
     * not possible, only the stack trace will be printed, no exception will be raised. To determine
     * the state, there is the method good.
     * 
     * @param className full qualified class name of the needed worker
     */
    public OSClassHelper(String className)
    {
        super();

        try
        {
            workerClass = Class.forName(className);
            worker = workerClass.newInstance();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            // Do nothing, class not bound.
        }
        catch (Exception e4)
        {   // If the native lib is not found an unqualified Exception will be raised.
            Debug.trace("Ctor OSClassHelper for " + className + ": worker not available (" + e4.getMessage() + ").");
            return;
        }
        Debug.trace("Ctor OSClassHelper for " + className + " is good: " + good());

    }

    /**
     * Return whether the helper can do the work or not.
     * @return whether the helper can do the work or not
     */
    public boolean good()
    {
        return (worker != null);
    }

    /**
     * Verifies the helper.
     * @param idata current install data
     * @return whether the helper is good or not
     * @throws Exception
     */
    public boolean verify(AutomatedInstallData idata) throws Exception
    {
        installdata = idata;
        return (false);
    }

}
