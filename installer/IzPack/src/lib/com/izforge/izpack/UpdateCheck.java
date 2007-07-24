/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2001 Johannes Lehtinen
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

package com.izforge.izpack;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Encloses information about an update check.
 * 
 * @author Tino Schwarze <tino.schwarze@community4you.de>
 */
public class UpdateCheck implements Serializable
{

    static final long serialVersionUID = -3721254065037691999L;

    /**
     * ant-fileset-like list of include patterns, based on INSTALL_PATH if relative
     */
    public ArrayList includesList = null;

    /**
     * ant-fileset-like list of exclude patterns, based on INSTALL_PATH if relative
     */
    public ArrayList excludesList = null;

    /** Whether pattern matching is performed case-sensitive */
    boolean caseSensitive = true;

    /** Constructs a new uninitialized instance. */
    public UpdateCheck()
    {
    }

    /**
     * Constructs and initializes a new instance.
     * 
     * @param includes The patterns to include in the check.
     * @param excludes The patterns to exclude from the check.
     */
    public UpdateCheck(ArrayList includes, ArrayList excludes)
    {
        this.includesList = includes;
        this.excludesList = excludes;
    }

    /**
     * Constructs and initializes a new instance.
     * 
     * @param includes The patterns to include in the check.
     * @param excludes The patterns to exclude from the check.
     * @param casesensitive If "yes", matches are performed case sensitive.
     */
    public UpdateCheck(ArrayList includes, ArrayList excludes, String casesensitive)
    {
        this.includesList = includes;
        this.excludesList = excludes;
        this.caseSensitive = ((casesensitive != null) && "yes".equalsIgnoreCase(casesensitive));
    }

}
