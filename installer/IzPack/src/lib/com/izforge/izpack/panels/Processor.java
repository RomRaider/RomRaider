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

package com.izforge.izpack.panels;

/*---------------------------------------------------------------------------*/
/**
 * Interface for classes that provide input field processing services.
 * 
 * @see com.izforge.izpack.panels.ProcessingClient
 * 
 * @version 0.0.1 / 10/26/02
 * @author Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public interface Processor
{

    /*--------------------------------------------------------------------------*/
    /**
     * Processes the contend of an input field.
     * 
     * @param client the client object using the services of this processor.
     * 
     * @return The result of the encryption.
     */
    /*--------------------------------------------------------------------------*/
    public String process(ProcessingClient client);
}
/*---------------------------------------------------------------------------*/
