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

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.util.os.WrappedNativeLibException;

/**
 * This class implements some methods which are needed by installer custom actions with native
 * parts.
 * 
 * @author Klaus Bartz
 * 
 */
public class NativeUninstallerListener extends SimpleUninstallerListener
{

    /** The packs locale database. */
    protected static LocaleDatabase langpack = null;

    /**
     * Default constructor.
     */
    public NativeUninstallerListener()
    {
        super();
        // Create langpack for error messages.
        if (langpack == null)
        {
            // Load langpack. Do not stop uninstall if not found.
            try
            {
                NativeUninstallerListener.langpack = new LocaleDatabase(
                        NativeUninstallerListener.class.getResourceAsStream("/langpack.xml"));
                WrappedNativeLibException.setLangpack(NativeUninstallerListener.langpack);
            }
            catch (Throwable exception)
            {}
        }
    }

    /**
     * Returns the langpack.
     * 
     * @return Returns the langpack.
     */
    public static LocaleDatabase getLangpack()
    {
        return langpack;
    }
}
