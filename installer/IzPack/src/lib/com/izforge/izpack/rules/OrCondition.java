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

import java.util.List;
import java.util.Properties;

import net.n3.nanoxml.XMLElement;
import com.izforge.izpack.util.Debug;

/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: OrCondition.java,v 1.1 2006/09/29 14:40:38 dennis Exp $
 */
public class OrCondition extends Condition
{

    public static final String RDE_VCS_REVISION = "$Revision: 1.1 $";

    public static final String RDE_VCS_NAME = "$Name:  $";

    protected Condition leftoperand;

    protected Condition rightoperand;

    /**
     * 
     */
    public OrCondition()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * 
     */
    public OrCondition(Condition operand1, Condition operand2)
    {
        this.leftoperand = operand1;
        this.rightoperand = operand2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.reddot.installer.util.Condition#isTrue()
     */
    /*
     * public boolean isTrue(Properties variables) { return this.leftoperand.isTrue(variables) ||
     * this.rightoperand.isTrue(variables); }
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
                Debug.log("or-condition needs two conditions as operands");
                return;
            }
            this.leftoperand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(0));
            this.rightoperand = RulesEngine.analyzeCondition(xmlcondition.getChildAtIndex(1));
        }
        catch (Exception e)
        {
            Debug.log("missing element in or-condition");
        }
    }

    /*
     * public boolean isTrue(Properties variables, List selectedpacks) { return
     * this.leftoperand.isTrue(variables, selectedpacks) || this.rightoperand.isTrue(variables,
     * selectedpacks); }
     */
    public boolean isTrue()
    {
        return this.leftoperand.isTrue() || this.rightoperand.isTrue();
    }
}
