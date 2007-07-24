/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2005 Klaus Bartz
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

package com.coi.tools.os.izpack;

import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.NativeLibraryClient;

/**
 * 
 * Base class to handle multiple native methods of multiple classes in one shared library. This is a
 * singelton class.
 * 
 * @author Klaus Bartz
 * 
 */
public class COIOSHelper
{

    private static COIOSHelper self = null;

    private static int used = 0;

    private static boolean destroyed = false;

    /**
     * This method is used to free the library at the end of progam execution. After this call, any
     * instance of this class will not be usable any more!
     * 
     * @param name the name of the library to free. Use only the name and extension but not the
     * path.
     */
    private native void FreeLibrary(String name);

    /**
     * Default constructor, do not use
     */
    private COIOSHelper()
    {
        super();
    }

    /**
     * Returns the one existent object of this class.
     * 
     * @return the one existent object of this class
     */
    public static synchronized COIOSHelper getInstance()
    {
        if (self == null) self = new COIOSHelper();
        return (self);

    }

    /*--------------------------------------------------------------------------*/
    /**
     * This method is used to free the library at the end of progam execution. This is the method of
     * the helper class which will be called from other objects. After this call, any instance of
     * this class will not be usable any more! <b><i><u>Note that this method does NOT return </u>
     * at the first call, but at any other </i> </b> <br>
     * <br>
     * <b>DO NOT CALL THIS METHOD DIRECTLY! </b> <br>
     * It is used by the librarian to free the native library before physically deleting it from its
     * temporary loaction. A call to this method will freeze the application irrecoverably!
     * 
     * @param name the name of the library to free. Use only the name and extension but not the
     * path.
     * 
     * @see com.izforge.izpack.util.NativeLibraryClient#freeLibrary
     */
    /*--------------------------------------------------------------------------*/
    /**
     * @param name
     */
    public void freeLibrary(String name)
    {
        used--;
        if (!destroyed)
        {
            FreeLibrary(name);
            destroyed = true;
        }
    }

    /**
     * Add a NativeLibraryClient as dependant to this object. The method tries to load the shared
     * library COIOSHelper which should contain native methods for the dependant.
     * 
     * @param dependant to be added
     * @throws Exception if loadLibrary for the needed lib fails
     */
    public void addDependant(NativeLibraryClient dependant) throws Exception
    {
        used++;
        try
        {
            Librarian.getInstance().loadLibrary("COIOSHelper", dependant);
        }
        catch (UnsatisfiedLinkError exception)
        {
            throw (new Exception("could not locate native library"));
        }

    }

}
