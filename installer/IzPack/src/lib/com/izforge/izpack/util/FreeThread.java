/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Elmar Grom
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

/*---------------------------------------------------------------------------*/
/**
 * This class implements a thred that can be used to free native libraries safely.
 * 
 * @version 0.0.1 / 2/6/02
 * @author Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class FreeThread extends Thread
{

    private String name = "";

    private NativeLibraryClient client = null;

    /*--------------------------------------------------------------------------*/
    /**
     * Standard constructor.
     * 
     * @param name the name of the library to free. The exact form of the name may be operating
     * system dependent. On Microsoft Windows this must be just the library name, without path but
     * with extension.
     * @param client reference of the client object that is linked with the library to be freed.
     */
    /*--------------------------------------------------------------------------*/
    public FreeThread(String name, NativeLibraryClient client)
    {
        this.name = name;
        this.client = client;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * The run() method. Frees the library. Note that the thread is likely to get 'frozen' and the
     * application can only be treminated through a call to <code>System.exit()</code>.
     */
    /*--------------------------------------------------------------------------*/
    public void run()
    {
        client.freeLibrary(name);
    }
}
/*---------------------------------------------------------------------------*/
