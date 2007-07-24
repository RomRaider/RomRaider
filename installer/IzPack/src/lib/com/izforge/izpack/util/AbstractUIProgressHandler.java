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

package com.izforge.izpack.util;

/**
 * This interface is used by functions which need to notify the user of some progress.
 * 
 * For example, the installation progress and compilation progress are communicated to the user
 * using this interface. The interface supports a two-stage progress indication: The whole action is
 * divided into steps (for example, packs when installing) and sub-steps (for example, files of a
 * pack).
 */
public interface AbstractUIProgressHandler extends AbstractUIHandler
{

    /**
     * The action starts.
     * 
     * @param name The name of the action.
     * @param no_of_steps The number of steps the action consists of.
     */
    public void startAction(String name, int no_of_steps);

    /**
     * The action was finished.
     */
    public void stopAction();

    /**
     * The next step starts.
     * 
     * @param step_name The name of the step which starts now.
     * @param step_no The number of the step.
     * @param no_of_substeps The number of sub-steps this step consists of.
     */
    public void nextStep(String step_name, int step_no, int no_of_substeps);

    /**
     * Notify of progress.
     * 
     * @param substep_no The substep which will be performed next.
     * @param message An additional message describing the substep.
     */
    public void progress(int substep_no, String message);

}
