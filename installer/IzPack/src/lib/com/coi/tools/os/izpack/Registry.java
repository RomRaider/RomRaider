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

import com.coi.tools.os.win.RegistryImpl;
import com.izforge.izpack.util.NativeLibraryClient;

/**
 * Wrapper class for com.coi.tools.os.win.RegistryImpl for using it with IzPack. This class
 * implements only the methods of interface NativeLibraryClient. All other methods are used directly
 * from RegistryImpl.
 * 
 * @author Klaus Bartz
 * 
 */
public class Registry extends RegistryImpl implements NativeLibraryClient
{

    /**
     * Default constructor.
     * @exception Exception if initialize of native part fails
     */
    public Registry() throws Exception
    {
        super();
        initialize();
    }

    /**
     * Initialize native part of this class and other settings.
     * 
     * @exception Exception if problems are encountered
     */
    /*--------------------------------------------------------------------------*/
    private void initialize() throws Exception
    {
        COIOSHelper.getInstance().addDependant(this);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * This method is used to free the library at the end of progam execution. This class has no own
     * library else it shares it in the COI common lib. To free the library, the helper class is
     * called. After this call, any instance of this class will not be usable any more! <b><i>
     * <u>Note that this method does NOT return </u> at the first call, but at any other </i> </b>
     * <br>
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
    public void freeLibrary(String name)
    {

        COIOSHelper.getInstance().freeLibrary(name);
    }

}
