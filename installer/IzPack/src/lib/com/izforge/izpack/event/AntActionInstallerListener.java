/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Klaus Bartz
 * Copyright 2004 Thomas Guenter
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallerException;
import com.izforge.izpack.installer.UninstallData;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.ExtendedUIProgressHandler;
import com.izforge.izpack.util.SpecHelper;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * Installer listener for performing ANT actions. The definition what should be done will be made in
 * a specification file which is referenced by the resource id "AntActionsSpec.xml". There should be
 * an entry in the install.xml file in the sub ELEMENT "res" of ELEMENT "resources" which references
 * it. The specification of the xml file is done in the DTD antaction.dtd. The xml file specifies,
 * for what pack what ant call should be performed at what time of installation.
 * 
 * @author Thomas Guenter
 * @author Klaus Bartz
 */
public class AntActionInstallerListener extends SimpleInstallerListener
{

    // ------------------------------------------------------------------------
    // Constant Definitions
    // ------------------------------------------------------------------------
    // --------String constants for parsing the XML specification ------------
    // -------- see class AntAction -----------------------------------------
    /** Name of the specification file */
    public static final String SPEC_FILE_NAME = "AntActionsSpec.xml";

    private HashMap actions = null;

    private ArrayList uninstActions = null;

    /**
     * Default constructor
     */
    public AntActionInstallerListener()
    {
        super(true);
        actions = new HashMap();
        uninstActions = new ArrayList();
    }

    /**
     * Returns the actions map.
     * 
     * @return the actions map
     */
    public HashMap getActions()
    {
        return (actions);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.installer.InstallerListener#beforePacks(com.izforge.izpack.installer.AutomatedInstallData,
     * java.lang.Integer, com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void beforePacks(AutomatedInstallData idata, Integer npacks,
            AbstractUIProgressHandler handler) throws Exception
    {
        super.beforePacks(idata, npacks, handler);

        getSpecHelper().readSpec(SPEC_FILE_NAME, new VariableSubstitutor(idata.getVariables()));

        if (getSpecHelper().getSpec() == null) return;

        // Selected packs.
        Iterator iter = idata.selectedPacks.iterator();
        Pack p = null;
        while (iter != null && iter.hasNext())
        {
            p = (Pack) iter.next();

            // Resolve data for current pack.
            XMLElement pack = getSpecHelper().getPackForName(p.name);
            if (pack == null) continue;

            // Prepare the action cache
            HashMap packActions = new HashMap();
            packActions.put(ActionBase.BEFOREPACK, new ArrayList());
            packActions.put(ActionBase.AFTERPACK, new ArrayList());
            packActions.put(ActionBase.BEFOREPACKS, new ArrayList());
            packActions.put(ActionBase.AFTERPACKS, new ArrayList());

            // Get all entries for antcalls.
            Vector antCallEntries = pack.getChildrenNamed(AntAction.ANTCALL);
            if (antCallEntries != null && antCallEntries.size() >= 1)
            {
                Iterator entriesIter = antCallEntries.iterator();
                while (entriesIter != null && entriesIter.hasNext())
                {
                    AntAction act = readAntCall((XMLElement) entriesIter.next());
                    if (act != null)
                    {
                        ((ArrayList) packActions.get(act.getOrder())).add(act);
                    }
                }
                // Set for progress bar interaction.
                if (((ArrayList) packActions.get(ActionBase.AFTERPACKS)).size() > 0)
                    this.setProgressBarCaller();
            }

            actions.put(p.name, packActions);
        }
        iter = idata.availablePacks.iterator();
        while (iter.hasNext())
        {
            String currentPack = ((Pack) iter.next()).name;
            performAllActions(currentPack, ActionBase.BEFOREPACKS, null);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.installer.InstallerListener#beforePack(com.izforge.izpack.Pack,
     * java.lang.Integer, com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void beforePack(Pack pack, Integer i, AbstractUIProgressHandler handler)
            throws Exception
    {
        performAllActions(pack.name, ActionBase.BEFOREPACK, handler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.installer.InstallerListener#afterPack(com.izforge.izpack.Pack,
     * java.lang.Integer, com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void afterPack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception
    {
        performAllActions(pack.name, ActionBase.AFTERPACK, handler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.compiler.InstallerListener#afterPacks(com.izforge.izpack.installer.AutomatedInstallData,
     * com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler handler)
            throws Exception
    {
        if (informProgressBar())
        {
            handler.nextStep(getMsg("AntAction.pack"), getProgressBarCallerId(), getActionCount(
                    idata, ActionBase.AFTERPACKS));
        }
        Iterator iter = idata.selectedPacks.iterator();
        while (iter.hasNext())
        {
            String currentPack = ((Pack) iter.next()).name;
            performAllActions(currentPack, ActionBase.AFTERPACKS, handler);
        }
        if (uninstActions.size() > 0)
        {
            UninstallData.getInstance().addAdditionalData("antActions", uninstActions);
        }
    }

    private int getActionCount(AutomatedInstallData idata, String order)
    {
        int retval = 0;
        Iterator iter = idata.selectedPacks.iterator();
        while (iter.hasNext())
        {
            String currentPack = ((Pack) iter.next()).name;
            ArrayList actList = getActions(currentPack, order);
            if (actList != null) retval += actList.size();
        }
        return (retval);
    }

    /**
     * Returns the defined actions for the given pack in the requested order.
     * 
     * @param packName name of the pack for which the actions should be returned
     * @param order order to be used; valid are <i>beforepack</i> and <i>afterpack</i>
     * @return a list which contains all defined actions for the given pack and order
     */
    // -------------------------------------------------------
    protected ArrayList getActions(String packName, String order)
    {
        if (actions == null) return null;

        HashMap packActions = (HashMap) actions.get(packName);
        if (packActions == null || packActions.size() == 0) return null;

        return (ArrayList) packActions.get(order);
    }

    /**
     * Performs all actions which are defined for the given pack and order.
     * 
     * @param packName name of the pack for which the actions should be performed
     * @param order order to be used; valid are <i>beforepack</i> and <i>afterpack</i>
     * @throws InstallerException
     */
    private void performAllActions(String packName, String order, AbstractUIProgressHandler handler)
            throws InstallerException
    {
        ArrayList actList = getActions(packName, order);
        if (actList == null || actList.size() == 0) return;

        Debug.trace("******* Executing all " + order + " actions of " + packName + " ...");
        for (int i = 0; i < actList.size(); i++)
        {
            AntAction act = (AntAction) actList.get(i);
            // Inform progress bar if needed. Works only
            // on AFTER_PACKS
            if (informProgressBar() && handler != null
                    && handler instanceof ExtendedUIProgressHandler
                    && order.equals(ActionBase.AFTERPACKS))
            {
                ((ExtendedUIProgressHandler) handler)
                        .progress((act.getMessageID() != null) ? getMsg(act.getMessageID()) : "");
            }
            try
            {
                act.performInstallAction();
            }
            catch (Exception e)
            {
                throw new InstallerException(e);
            }
            if (act.getUninstallTargets().size() > 0) uninstActions.add(act);
        }
    }

    /**
     * Returns an ant call which is defined in the given XML element.
     * 
     * @param el XML element which contains the description of an ant call
     * @return an ant call which is defined in the given XML element
     * @throws InstallerException
     */
    private AntAction readAntCall(XMLElement el) throws InstallerException
    {
        if (el == null) return null;
        SpecHelper spec = getSpecHelper();
        AntAction act = new AntAction();
        try
        {
            act.setOrder(spec.getRequiredAttribute(el, ActionBase.ORDER));
            act.setUninstallOrder(el.getAttribute(ActionBase.UNINSTALL_ORDER,
                    ActionBase.BEFOREDELETION));
        }
        catch (Exception e)
        {
            throw new InstallerException(e);
        }

        act.setQuiet(spec.isAttributeYes(el, ActionBase.QUIET, false));
        act.setVerbose(spec.isAttributeYes(el, ActionBase.VERBOSE, false));
        act.setBuildFile(spec.getRequiredAttribute(el, ActionBase.BUILDFILE));
        String str = el.getAttribute(ActionBase.LOGFILE);
        if (str != null)
        {
            act.setLogFile(str);
        }
        String msgId = el.getAttribute(ActionBase.MESSAGEID);
        if (msgId != null && msgId.length() > 0) act.setMessageID(msgId);

        // read propertyfiles
        Iterator iter = el.getChildrenNamed(ActionBase.PROPERTYFILE).iterator();
        while (iter.hasNext())
        {
            XMLElement propEl = (XMLElement) iter.next();
            act.addPropertyFile(spec.getRequiredAttribute(propEl, ActionBase.PATH));
        }

        // read properties
        iter = el.getChildrenNamed(ActionBase.PROPERTY).iterator();
        while (iter.hasNext())
        {
            XMLElement propEl = (XMLElement) iter.next();
            act.setProperty(spec.getRequiredAttribute(propEl, ActionBase.NAME), spec
                    .getRequiredAttribute(propEl, ActionBase.VALUE));
        }

        // read targets
        iter = el.getChildrenNamed(ActionBase.TARGET).iterator();
        while (iter.hasNext())
        {
            XMLElement targEl = (XMLElement) iter.next();
            act.addTarget(spec.getRequiredAttribute(targEl, ActionBase.NAME));
        }

        // read uninstall rules
        iter = el.getChildrenNamed(ActionBase.UNINSTALL_TARGET).iterator();
        while (iter.hasNext())
        {
            XMLElement utargEl = (XMLElement) iter.next();
            act.addUninstallTarget(spec.getRequiredAttribute(utargEl, ActionBase.NAME));
        }

        return act;
    }

}
