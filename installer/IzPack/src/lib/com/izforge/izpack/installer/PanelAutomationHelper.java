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

import com.izforge.izpack.util.AbstractUIHandler;

/**
 * Abstract class implementing basic functions needed by all panel automation helpers.
 * 
 * @author tisc
 */
abstract public class PanelAutomationHelper implements AbstractUIHandler
{

    /*
     * @see com.izforge.izpack.util.AbstractUIHandler#emitNotification(java.lang.String)
     */
    public void emitNotification(String message)
    {
        System.out.println(message);
    }

    /*
     * @see com.izforge.izpack.util.AbstractUIHandler#emitWarning(java.lang.String,
     * java.lang.String)
     */
    public boolean emitWarning(String title, String message)
    {
        System.err.println("[ WARNING: " + message + " ]");
        // default: continue
        return true;
    }

    /*
     * @see com.izforge.izpack.util.AbstractUIHandler#emitError(java.lang.String, java.lang.String)
     */
    public void emitError(String title, String message)
    {
        System.err.println("[ ERROR: " + message + " ]");
    }

    /*
     * @see com.izforge.izpack.util.AbstractUIHandler#askQuestion(java.lang.String,
     * java.lang.String, int)
     */
    public int askQuestion(String title, String question, int choices)
    {
        // don't know what to answer
        return AbstractUIHandler.ANSWER_CANCEL;
    }

    /*
     * @see com.izforge.izpack.util.AbstractUIHandler#askQuestion(java.lang.String,
     * java.lang.String, int, int)
     */
    public int askQuestion(String title, String question, int choices, int default_choice)
    {
        return default_choice;
    }

}
