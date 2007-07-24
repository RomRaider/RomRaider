/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
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

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;

/**
 * This panel adds some conditional behavior to the standard UserInputPanel. <br/> <b>Usage:</b><br/>
 * In the "panels" list, just use ConditionalUserInputPanel like the normal UserInputPanel. The
 * specification goes also into userInputSpec.xml and userInputLang.xml_XXX. To specify a condition
 * for a certain ConditionalUserInputPanel, you have to specify the condition in the
 * "variables"-section by defining the following variables:<br/>
 * <li><i>compareToVariable."panel-order"</i>: The variable name containing the value to compare
 * with
 * <li><i>compareToOperator."panel-order"</i>: The compare operator to use, currently only "=" and
 * "!=" are allowed
 * <li><i>compareToValue."panel-order"</i>: The value to compare with<br/> If the compare fails,
 * the panel will be skipped.
 * 
 * @see UserInputPanel
 * 
 * @author $author$
 * @version $Revision: 1816 $
 */
public class ConditionalUserInputPanel extends UserInputPanel
{

    private static final long serialVersionUID = 3257283617406465844L;

    /**
     * Creates a new ConditionalUserInputPanel object.
     * 
     * @param parent
     * @param installData
     */
    public ConditionalUserInputPanel(InstallerFrame parent, InstallData installData)
    {
        super(parent, installData);
    }

    /**
     * Panel is only activated, if the configured condition is true
     */
    public void panelActivate()
    {
        // get configured condition for this panel
        String compareToValue = idata.getVariable("compareToValue." + instanceNumber);
        String compareToVariable = idata.getVariable("compareToVariable." + instanceNumber);
        String compareToOperator = idata.getVariable("compareToOperator." + instanceNumber);
        String compareValue = null;

        // get value of the compareVariable
        if (null != compareToVariable)
        {
            compareValue = idata.getVariable(compareToVariable);
        }

        if ("=".equalsIgnoreCase(compareToOperator))
        {
            // compare using equal
            if (((null != compareToValue) && compareToValue.equalsIgnoreCase(compareValue))
                    || ((null != compareValue) && compareValue.equalsIgnoreCase(compareToValue)))
            {
                super.panelActivate();
            }
            else
            {
                parent.skipPanel();
            }
        }
        else if ("!=".equalsIgnoreCase(compareToOperator))
        {
            // compare using un-equal
            if (((null != compareToValue) && !compareToValue.equalsIgnoreCase(compareValue))
                    || ((null != compareValue) && !compareValue.equalsIgnoreCase(compareToValue)))
            {
                super.panelActivate();
            }
            else
            {
                parent.skipPanel();
            }
        }
        else
        {
            // wrong operator!
            emitError("Invalid operator specified for compareToOperator." + instanceNumber,
                    "Only '=' and '!=' are currently allowed!");
            parent.skipPanel();
        }
    }
}
