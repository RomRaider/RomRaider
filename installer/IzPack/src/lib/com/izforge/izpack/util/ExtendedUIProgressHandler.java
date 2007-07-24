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

package com.izforge.izpack.util;

/**
 * This interface allowes an extended interaction with a user interface handler.
 * 
 * @author Klaus Bartz
 */
public interface ExtendedUIProgressHandler
{

    static final int BEFORE = 0;

    static final int AFTER = 1;

    /**
     * The action restarts.
     * 
     * @param name The name of the action.
     * @param overallMsg message to be used in the overall label.
     * @param tipMsg message to be used in the tip label.
     * @param no_of_steps The number of steps the action consists of.
     */
    void restartAction(String name, String overallMsg, String tipMsg, int no_of_steps);

    /**
     * Notify of progress with automatic counting.
     * 
     * @param stepMessage an additional message describing the substep the type of the substep
     */
    public void progress(String stepMessage);

}
