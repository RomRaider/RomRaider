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

import java.io.File;
import java.util.List;

import com.izforge.izpack.util.AbstractUIProgressHandler;

/**
 * <p>
 * This class implements all methods of interface UninstallerListener, but do not do enything. It
 * can be used as base class to save implementation of unneeded methods.
 * </p>
 * 
 * @author Klaus Bartz
 * 
 */
public class SimpleUninstallerListener implements UninstallerListener
{

    /**
     * 
     */
    public SimpleUninstallerListener()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#beforeDeletion(java.util.List,
     * com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void beforeDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#beforeDelete(java.io.File,
     * com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void beforeDelete(File file, AbstractUIProgressHandler handler) throws Exception
    {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#afterDelete(java.io.File,
     * com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void afterDelete(File file, AbstractUIProgressHandler handler) throws Exception
    {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#afterDeletion(java.util.List,
     * com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void afterDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#isFileListener()
     */
    public boolean isFileListener()
    {
        return false;
    }

}
