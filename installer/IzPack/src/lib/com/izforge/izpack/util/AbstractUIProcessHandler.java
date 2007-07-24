/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Olexij Tkatchenko
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

/**
 * Interface for UIs which need to interface to external processes.
 * 
 * @author tisc
 */
public interface AbstractUIProcessHandler extends AbstractUIHandler
{

    /**
     * Log the given message.
     * 
     * @param message
     * @param stderr true if this is a message received from a program via stderr
     */
    public void logOutput(String message, boolean stderr);

    public void startProcessing(int no_of_processes);

    /**
     * Notify the user that a process has started.
     * 
     * @param name
     */
    public void startProcess(String name);

    public void finishProcess();

    public void finishProcessing();
}
