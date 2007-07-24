/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Marcus Stursberg
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
 * Describes that a resource could not be found
 * 
 * @author Marcus Stursberg
 */

public class ResourceNotFoundException extends Exception
{

    private static final long serialVersionUID = 3258688827575906353L;

    /** creates a new ResourceNotFoundException */
    public ResourceNotFoundException()
    {
        super();
    }

    /**
     * creates a new ResourceNotFoundException
     * 
     * @param s description of the exception
     */
    public ResourceNotFoundException(String s)
    {
        super(s);
    }
}
