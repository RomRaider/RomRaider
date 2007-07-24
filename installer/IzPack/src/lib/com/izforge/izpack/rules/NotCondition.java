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

import com.izforge.izpack.util.Debug;

/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class NotCondition extends Condition
{

    protected Condition operand;

    /**
     * 
     */
    public NotCondition()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * 
     */
    public NotCondition(Condition operand)
    {
        this.operand = operand;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.reddot.installer.util.Condition#isTrue()
     */
    /*
    public boolean isTrue(Properties variables)
    {
        return !operand.isTrue(variables);
    }
    */

    /*
     * (non-Javadoc)
     * 
     * @see de.reddot.installer.rules.Condition#readFromXML(net.n3.nanoxml.XMLElement)
     */
    public void readFromXML(XMLElement xmlcondition)
    {
        try
        {
            if (xmlcondition.getChildrenCount() != 1)
            {
                Debug.log("not-condition needs one condition as operand");
                return;
            }
            this.operand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(0));
        }
        catch (Exception e)
        {
            Debug.log("missing element in not-condition");
        }
    }

    /*
    public boolean isTrue(Properties variables, List selectedpacks)
    {
        return !operand.isTrue(variables, selectedpacks);
    }
    */
    public boolean isTrue()
    {        
        return !operand.isTrue();
    }
}
