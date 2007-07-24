/*
 * $Id: Packager.java 1671 2007-01-02 10:28:58Z dreil $
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2006 Dennis Reil
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

package com.izforge.izpack.compiler;

/**
 * Factory class for handling the packager classes 
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class PackagerFactory
{
    /**
     * Returns a new instantiation of the specified packager
     * @param classname
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public static IPackager getPackager(String classname) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
        return (IPackager) Class.forName(classname).newInstance();
    }
}
