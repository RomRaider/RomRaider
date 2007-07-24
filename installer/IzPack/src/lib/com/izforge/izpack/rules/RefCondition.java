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

import net.n3.nanoxml.XMLElement;

/**
 * References an already defined condition
 * 
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class RefCondition extends Condition
{

    Condition referencedcondition;

    public RefCondition()
    {
        this.referencedcondition = null;
    }

    /*
     * public boolean isTrue(Properties variables) { if (referencedcondition == null) { return
     * false; } else { return referencedcondition.isTrue(variables); } }
     */
    public void readFromXML(XMLElement xmlcondition)
    {
        String refid = xmlcondition.getAttribute("refid");
        this.referencedcondition = RulesEngine.getCondition(refid);
    }

    public boolean isTrue()
    {
        if (this.referencedcondition == null)
        {
            return false;
        }
        else
        {
            return this.referencedcondition.isTrue();
        }
    }

    /*
     * public boolean isTrue(Properties variables, List selectedpacks) { return
     * referencedcondition.isTrue(variables, selectedpacks); }
     */
}
