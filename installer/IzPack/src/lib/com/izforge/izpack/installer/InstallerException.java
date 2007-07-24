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

package com.izforge.izpack.installer;

/**
 * Indicates a Failure in a custom action.
 * 
 * @author Klaus Bartz
 * 
 */
public class InstallerException extends Exception
{

    private static final long serialVersionUID = 3978984358113982004L;

    /**
     * 
     */
    public InstallerException()
    {
        super();
    }

    /**
     * @param message
     */
    public InstallerException(String message)
    {
        super(message);
    }

    /**
     * @param cause
     */
    public InstallerException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public InstallerException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
