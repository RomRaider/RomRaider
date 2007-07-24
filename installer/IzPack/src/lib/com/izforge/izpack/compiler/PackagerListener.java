/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
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

package com.izforge.izpack.compiler;

/**
 * An interface for classes that want to listen to a packager events.
 * 
 * @author Julien Ponge
 */
public interface PackagerListener
{

    /** Message priority of "debug". */
    public static final int MSG_DEBUG = 0;

    /** Message priority of "error". */
    public static final int MSG_ERR = 1;

    /** Message priority of "information". */
    public static final int MSG_INFO = 2;

    /** Message priority of "verbose". */
    public static final int MSG_VERBOSE = 3;

    /** Message priority of "warning". */
    public static final int MSG_WARN = 4;

    /**
     * Send a message with the priority MSG_INFO.
     * 
     * @param info The information that has been sent.
     */
    public void packagerMsg(String info);

    /**
     * Send a message with the specified priority.
     * 
     * @param info The information that has been sent.
     * @param priority The priority of the message.
     */
    public void packagerMsg(String info, int priority);

    /** Called when the packager starts. */
    public void packagerStart();

    /** Called when the packager stops. */
    public void packagerStop();
}
