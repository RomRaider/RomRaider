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

import java.io.File;

import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * The simple finish panel class.
 * 
 * @author Julien Ponge
 */
public class SimpleFinishPanel extends IzPanel
{

    /**
     * 
     */
    private static final long serialVersionUID = 3689911781942572085L;

    /** The variables substitutor. */
    private VariableSubstitutor vs;

    /**
     * The constructor.
     * 
     * @param parent The parent.
     * @param idata The installation data.
     */
    public SimpleFinishPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata, new IzPanelLayout());
        vs = new VariableSubstitutor(idata.getVariables());
    }

    /**
     * Indicates wether the panel has been validated or not.
     * 
     * @return true if the panel has been validated.
     */
    public boolean isValidated()
    {
        return true;
    }

    /** Called when the panel becomes active. */
    public void panelActivate()
    {
        parent.lockNextButton();
        parent.lockPrevButton();
        parent.setQuitButtonText(parent.langpack.getString("FinishPanel.done"));
        parent.setQuitButtonIcon("done");
        if (idata.installSuccess)
        {
            
            // We set the information
            add(LabelFactory.create(parent.icons.getImageIcon("check")));
            add(IzPanelLayout.createParagraphGap());
            add(LabelFactory.create(parent.langpack.getString("FinishPanel.success"),
                    parent.icons.getImageIcon("information"), LEADING), NEXT_LINE);
            add(IzPanelLayout.createParagraphGap());
            if (idata.uninstallOutJar != null)
            {
                // We prepare a message for the uninstaller feature
                String path = translatePath("$INSTALL_PATH") + File.separator + "Uninstaller";

                add(LabelFactory.create(parent.langpack
                        .getString("FinishPanel.uninst.info"), parent.icons
                        .getImageIcon("information"), LEADING), NEXT_LINE);
                add(LabelFactory.create(path, parent.icons.getImageIcon("empty"),
                        LEADING), NEXT_LINE);
            }
        }
        else
            add(LabelFactory.create(parent.langpack.getString("FinishPanel.fail"),
                    parent.icons.getImageIcon("information"),  LEADING));
        getLayoutHelper().completeLayout(); // Call, or call not?

    }

    /**
     * Translates a relative path to a local system path.
     * 
     * @param destination The path to translate.
     * @return The translated path.
     */
    private String translatePath(String destination)
    {
        // Parse for variables
        destination = vs.substitute(destination, null);

        // Convert the file separator characters
        return destination.replace('/', File.separatorChar);
    }
}
