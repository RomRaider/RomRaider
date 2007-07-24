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

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.IoHelper;

/**
 * Uninstaller listener for performing ANT actions at uninstall time. The definition of what should
 * be done here will be made in a specification file that is referenced by the resource id
 * "AntActionsSpec.xml". There should be an entry in the install.xml file in the sub ELEMENT "res"
 * of ELEMENT "resources" that references it. The specification of the xml file is done in the DTD
 * antaction.dtd. The xml file may contain an ELEMENT "uninstall_target" that should be performed
 * for uninstalling purposes.
 * 
 * @author Klaus Bartz
 */
public class AntActionUninstallerListener extends SimpleUninstallerListener
{

    /** Ant actions to be performed after deletion */
    private List antActions = null;

    /**
     * Default constructor
     */
    public AntActionUninstallerListener()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#beforeDeletion(java.util.List,
     * com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void beforeDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        // Load the defined actions.
        InputStream in = getClass().getResourceAsStream("/antActions");
        if (in == null)
        { // No actions, nothing todo.
            return;
        }
        ObjectInputStream objIn = new ObjectInputStream(in);
        // The actions are stored at installation time as list of AntAction
        // objects.
        // See AntActionInstallerListener.afterPacks.
        List allActions = (List) objIn.readObject();
        objIn.close();
        in.close();
        ArrayList befDel = new ArrayList();
        antActions = new ArrayList();
        Iterator iter = allActions.iterator();
        // There are two possible orders; before and after deletion.
        // Now we assign the actions to two different lists, the
        // local "before" list which we perform after the scan and
        // the class member "antActions" which should contain the
        // "afterdeletion" actions. Additionally we should save needed
        // files like the properties file for this order because if they're
        // part of the pack the'll be lost after the deletion has been
        // performed.
        while (iter.hasNext())
        {
            AntAction action = (AntAction) iter.next();
            // 
            if (action.getUninstallOrder().equals(ActionBase.BEFOREDELETION))
                befDel.add(action);
            else
            {// We need the build and the properties file(s) outside the
                // install dir.
                File tmpFile = IoHelper.copyToTempFile(action.getBuildFile(), ".xml");
                action.setBuildFile(tmpFile.getCanonicalPath());
                List props = action.getPropertyFiles();
                if (props != null)
                {
                    Iterator iter2 = props.iterator();
                    ArrayList newProps = new ArrayList();
                    while (iter2.hasNext())
                    {
                        String propName = (String) iter2.next();
                        File propFile = IoHelper.copyToTempFile(propName, ".properties");
                        newProps.add(propFile.getCanonicalPath());
                    }
                    action.setPropertyFiles(newProps);
                }
                antActions.add(action);
            }
        }
        // Perform the actions with the order "beforedeletion".
        if (befDel.size() > 0)
        {
            for (int i = 0; i < befDel.size(); i++)
            {
                AntAction act = (AntAction) befDel.get(i);
                act.performUninstallAction();
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#afterDeletion(java.util.List,
     * com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void afterDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        if (antActions != null && antActions.size() > 0)
        { // There are actions of the order "afterdeletion".
            for (int i = 0; i < antActions.size(); i++)
            {
                AntAction act = (AntAction) antActions.get(i);
                act.performUninstallAction();
            }

        }
    }

}
