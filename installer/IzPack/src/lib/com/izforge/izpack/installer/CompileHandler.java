/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2003 Tino Schwarze
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

/**
 * Interface for monitoring compilation progress.
 * 
 * This is used by <code>CompilePanel</code>, <code>CompileWorker</code> and
 * <code>CompilePanelAutomationHelper</code> to display the progress of the compilation. Most of
 * the functionality, however, is inherited from interface
 * com.izforge.izpack.util.AbstractUIProgressHandler
 * 
 * @author Tino Schwarze
 * @see com.izforge.izpack.util.AbstractUIProgressHandler
 */
public interface CompileHandler extends com.izforge.izpack.util.AbstractUIProgressHandler
{

    /**
     * An error was encountered.
     * 
     * This method should notify the user of the error and request a choice whether to continue,
     * abort or reconfigure. It should alter the error accordingly.
     * 
     * Although a CompileResult is passed in, the method is only called if something failed.
     * 
     * @param error the error to handle
     */
    public void handleCompileError(CompileResult error);

}
