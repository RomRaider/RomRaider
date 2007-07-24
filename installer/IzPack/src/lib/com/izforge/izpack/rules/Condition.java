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

package com.izforge.izpack.rules;

import com.izforge.izpack.installer.AutomatedInstallData;
import net.n3.nanoxml.XMLElement;

/**
 * Abstract base class for all conditions
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public abstract class Condition
{

    protected String id;
    protected AutomatedInstallData installdata;

    public Condition()
    {
        this.id = "UNKNOWN";
        this.installdata = null;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return this.id;
    }


    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    public abstract void readFromXML(XMLElement xmlcondition);

    public abstract boolean isTrue();

    public AutomatedInstallData getInstalldata()
    {
        return installdata;
    }


    public void setInstalldata(AutomatedInstallData installdata)
    {
        this.installdata = installdata;
    }
}
