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

package com.izforge.izpack.event;

import java.util.Map;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.compiler.CompilerException;
import com.izforge.izpack.compiler.IPackager;

/**
 * <p>
 * Implementations of this class are used to add extensions to the packs at compilation.
 * </p>
 * 
 * @author Klaus Bartz
 * 
 */
public interface CompilerListener
{

    public final static int BEGIN = 1;

    public final static int END = 2;

    /**
     * This method is called from the compiler for each file (or dir) parsing. The XMLElement is a
     * node of the file related children of the XML element "pack" (see installation.dtd). Current
     * these are "file", "singlefile" or "fileset". If an additional data should be set, it should
     * be added to the given data map (if exist). If no map exist a new should be created and
     * filled. The data map will be added to the PackFile object after all registered
     * CompilerListener are called. If the map contains an not common object, it is necessary to add
     * the needed class to the installer.
     * 
     * @param existentDataMap attribute set with previos setted attributes
     * @param element current file related XML node
     * @return the given or a new attribute set. If no attribute set is given and no attribute was
     * added, null returns
     * @throws CompilerException
     */
    Map reviseAdditionalDataMap(Map existentDataMap, XMLElement element) throws CompilerException;

    /**
     * This method will be called from each step of packaging.
     * 
     * @param position name of the calling method, e.g. "addVariables"
     * @param state BEGIN or END
     * @param data current install data
     * @param packager current packager object
     */
    void notify(String position, int state, XMLElement data, IPackager packager);

}
