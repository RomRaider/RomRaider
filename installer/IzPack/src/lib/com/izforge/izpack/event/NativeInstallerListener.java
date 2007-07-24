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
import com.izforge.izpack.util.os.WrappedNativeLibException;

/**
 * This class implements some methods which are needed by installer custom actions with native
 * parts.
 * 
 * @author Klaus Bartz
 * 
 */
public class NativeInstallerListener extends SimpleInstallerListener
{

    /**
     * Default constructor
     */
    public NativeInstallerListener()
    {
        super();
    }

    /**
     * Constructs a native installer listener. If useSpecHelper is true, a specification helper will
     * be created.
     * 
     * @param useSpecHelper
     * 
     */
    public NativeInstallerListener(boolean useSpecHelper)
    {
        super(useSpecHelper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.compiler.InstallerListener#beforePacks(com.izforge.izpack.installer.AutomatedInstallData,
     * int, com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void beforePacks(AutomatedInstallData idata, Integer npacks,
            AbstractUIProgressHandler handler) throws Exception
    {
        super.beforePacks(idata, npacks, handler);

        if (SimpleInstallerListener.langpack != null)
        { // Initialize WrappedNativeLibException with the langpack for error messages.
            WrappedNativeLibException.setLangpack(SimpleInstallerListener.langpack);
        }

    }

}
