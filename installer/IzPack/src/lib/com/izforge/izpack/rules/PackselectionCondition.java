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

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.izforge.izpack.Pack;
import com.izforge.izpack.util.Debug;

import net.n3.nanoxml.XMLElement;

/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: PackselectionCondition.java,v 1.1 2006/11/03 13:03:26 dennis Exp $
 */
public class PackselectionCondition extends Condition
{

    protected String packid;

    /**
     * 
     */
    public PackselectionCondition()
    {
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.reddot.installer.rules.Condition#isTrue(java.util.Properties)
     */
    private boolean isTrue(Properties variables)
    {
        // no information about selected packs given, so return false
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.reddot.installer.rules.Condition#readFromXML(net.n3.nanoxml.XMLElement)
     */
    public void readFromXML(XMLElement xmlcondition)
    {
        try
        {
            this.packid = xmlcondition.getFirstChildNamed("packid").getContent();
        }
        catch (Exception e)
        {
            Debug.log("missing element in <condition type=\"variable\"/>");
        }
    }

    private boolean isTrue(Properties variables, List selectedpacks)
    {
        if (selectedpacks != null)
        {
            for (Iterator iter = selectedpacks.iterator(); iter.hasNext();)
            {
                Pack p = (Pack) iter.next();
                if (packid.equals(p.id))
                {
                    // pack is selected
                    return true;
                }
            }
        }
        // pack is not selected
        return false;
    }

    public boolean isTrue()
    {
        return this.isTrue(this.installdata.getVariables(), this.installdata.selectedPacks);
    }

}
