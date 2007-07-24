/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2007 Dennis Reil
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

import java.lang.reflect.Constructor;

import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.Debug;


/**
 * A Factory for getting unpacker instances. 
 * 
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public abstract class UnpackerFactory
{
    /**
     * Returns an instance of the desired unpacker class
     * @param unpackerclassname
     * @param installdata
     * @param listener
     * @return
     */
    public static IUnpacker getUnpacker(String unpackerclassname, AutomatedInstallData installdata, AbstractUIProgressHandler listener){
        IUnpacker unpackerobj = null;
        try
        {
            Class unpackerclass = Class.forName(unpackerclassname);
            Class[] parametertypes = {AutomatedInstallData.class, AbstractUIProgressHandler.class};
            Constructor unpackerconstructor = unpackerclass.getConstructor(parametertypes);
            Object[] parameter = {installdata,listener};
            unpackerobj = (IUnpacker) unpackerconstructor.newInstance(parameter);            
        }        
        catch (NoSuchMethodException e)
        {
            Debug.trace("Can't load unpacker: " + unpackerclassname);
            Debug.trace("Unpacker doesn't implement the desired method");
            Debug.trace(e);
        }
        catch (Exception e)
        {
            Debug.trace("Can't load unpacker: " + unpackerclassname);
            Debug.trace(e);
        }                
        return unpackerobj;
    }
}
