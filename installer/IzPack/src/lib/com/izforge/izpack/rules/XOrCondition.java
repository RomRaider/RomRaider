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
 * @version $Id: XOrCondition.java,v 1.1 2006/09/29 14:40:38 dennis Exp $
 */
public class XOrCondition extends OrCondition
{

    /**
     * 
     */
    public XOrCondition()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param operand1
     * @param operand2
     */
    public XOrCondition(Condition operand1, Condition operand2)
    {
        super(operand1, operand2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.reddot.installer.util.OrCondition#isTrue()
     */
    /*
    public boolean isTrue(Properties variables)
    {
        boolean op1true = leftoperand.isTrue(variables);
        boolean op2true = rightoperand.isTrue(variables);

        if (op1true && op2true)
        {
            // in case where both are true
            return false;
        }
        return op1true || op2true;
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
            if (xmlcondition.getChildrenCount() != 2)
            {
                Debug.log("xor-condition needs two conditions as operands");
                return;
            }
            this.leftoperand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(0));
            this.rightoperand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(1));
        }
        catch (Exception e)
        {
            Debug.log("missing element in xor-condition");
        }
    }

    public boolean isTrue()
    {
        boolean op1true = leftoperand.isTrue();
        boolean op2true = rightoperand.isTrue();

        if (op1true && op2true)
        {
            // in case where both are true
            return false;
        }
        return op1true || op2true;
    }

    /*
    public boolean isTrue(Properties variables, List selectedpacks)
    {
        boolean op1true = leftoperand.isTrue(variables, selectedpacks);
        boolean op2true = rightoperand.isTrue(variables, selectedpacks);

        if (op1true && op2true)
        {
            // in case where both are true
            return false;
        }
        return op1true || op2true;
    }
    */
}
