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
 * Any class in IzPack that uses native libraries must implement this interface. See the package
 * documentation for more details on requirements relating to the use of native libraries within
 * IzPack.
 * 
 * @version 0.0.1 / 2/6/2002
 * @author Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public interface NativeLibraryClient
{

    /*--------------------------------------------------------------------------*/
    /**
     * This method is used to free the library at the end of progam execution. After this call, any
     * instance of this calss will not be usable any more! <b><i><u>This method is very likely NOT
     * to return!</u></i></b> <br>
     * <br>
     * <b>DO NOT CALL THIS METHOD DIRECTLY!</b><br>
     * It is used by the librarian to free a native library before physically deleting it from its
     * temporary loaction. A call to this method is likely to irrecoverably freeze the application!
     * <br>
     * <br>
     * The contract for this method implementation is that a call will bring the native library into
     * a state where it can be deleted. This translates into an operation to free the library. Since
     * no libraries should be left behind when the installer shuts down, it is necessary that each
     * library provides the means to free itself. For instance in a MS-Windows environment the
     * library must call <code>FreeLibraryAndExitThread()</code>. This will result in a native
     * fuction call that does not return.
     * 
     * @param name the name of the library, without path but with extension
     */
    /*--------------------------------------------------------------------------*/
    public void freeLibrary(String name);
}
/*---------------------------------------------------------------------------*/
