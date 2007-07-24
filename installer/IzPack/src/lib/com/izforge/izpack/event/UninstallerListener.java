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
 * Implementations of this class are used to handle customizing uninstallation. The defined methods
 * are called from the destroyer at different, well defined points of uninstallation.
 * </p>
 * 
 * @author Klaus Bartz
 * 
 */
public interface UninstallerListener
{

    // ------------------------------------------------------------------------
    // Constant Definitions
    // ------------------------------------------------------------------------
    public static final int BEFORE_DELETION = 1;

    public static final int AFTER_DELETION = 2;

    public static final int BEFORE_DELETE = 3;

    public static final int AFTER_DELETE = 4;

    /**
     * This method will be called from the destroyer before the given files will be deleted.
     * 
     * @param files all files which should be deleted
     * @param handler a handler to the current used UIProgressHandler
     * @throws Exception
     */
    void beforeDeletion(List files, AbstractUIProgressHandler handler) throws Exception;

    /**
     * Returns true if this listener would be informed at every delete operation, else false. If it
     * is true, the listener will be called two times (before and after) of every action. Handle
     * carefully, else performance problems are possible.
     * 
     * @return true if this listener would be informed at every delete operation, else false
     */
    boolean isFileListener();

    /**
     * This method will be called from the destroyer before the given file will be deleted.
     * 
     * @param file file which should be deleted
     * @param handler a handler to the current used UIProgressHandler
     * @throws Exception
     */
    void beforeDelete(File file, AbstractUIProgressHandler handler) throws Exception;

    /**
     * This method will be called from the destroyer after the given file was deleted.
     * 
     * @param file file which was just deleted
     * @param handler a handler to the current used UIProgressHandler
     * @throws Exception
     */
    void afterDelete(File file, AbstractUIProgressHandler handler) throws Exception;

    /**
     * This method will be called from the destroyer after the given files are deleted.
     * 
     * @param files all files which where deleted
     * @param handler a handler to the current used UIProgressHandler
     * @throws Exception
     */
    void afterDeletion(List files, AbstractUIProgressHandler handler) throws Exception;

}
