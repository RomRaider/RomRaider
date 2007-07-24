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
import java.util.List;

/**
 * Encloses information about a parsable file. This class abstracts the way the information is
 * stored to package.
 * 
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class ParsableFile implements Serializable
{

    static final long serialVersionUID = -7761309341843715721L;

    /** The file path */
    public String path = null;

    /** The file type (or null for default) */
    public String type = null;

    /** The file encoding (or null for default) */
    public String encoding = null;

    /** The list of OS constraints limiting file installation. */
    public List osConstraints = null;

    /**
     * Constructs and initializes a new instance.
     * 
     * @param path the file path
     * @param type the file type (or null for default)
     * @param encoding the file encoding (or null for default)
     * @param osConstraints the OS constraint (or null for any OS)
     */
    public ParsableFile(String path, String type, String encoding, List osConstraints)
    {
        this.path = path;
        this.type = type;
        this.encoding = encoding;
        this.osConstraints = osConstraints;
    }

}
