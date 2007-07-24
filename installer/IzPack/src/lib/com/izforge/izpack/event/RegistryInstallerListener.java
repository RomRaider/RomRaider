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

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import net.n3.nanoxml.XMLElement;

import com.coi.tools.os.win.NativeLibException;
import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.UninstallData;
import com.izforge.izpack.installer.Unpacker;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.SpecHelper;
import com.izforge.izpack.util.VariableSubstitutor;
import com.izforge.izpack.util.os.RegistryDefaultHandler;
import com.izforge.izpack.util.os.RegistryHandler;
import com.izforge.izpack.util.os.WrappedNativeLibException;

/**
 * Installer custom action for handling registry entries on Windows. On Unix nothing will be done.
 * The actions which should be performed are defined in a resource file named "RegistrySpec.xml".
 * This resource should be declared in the installation definition file (install.xml), else an
 * exception will be raised during execution of this custom action. The related DTD is
 * appl/install/IzPack/resources/registry.dtd.
 * 
 * @author Klaus Bartz
 * 
 */
public class RegistryInstallerListener extends NativeInstallerListener
{

    /** The name of the XML file that specifies the registry entries. */
    private static final String SPEC_FILE_NAME = "RegistrySpec.xml";

    private static final String REG_KEY = "key";

    private static final String REG_VALUE = "value";

    private static final String REG_ROOT = "root";

    private static final String REG_BASENAME = "name";

    private static final String REG_KEYPATH = "keypath";

    private static final String REG_DWORD = "dword";

    private static final String REG_STRING = "string";

    private static final String REG_MULTI = "multi";

    private static final String REG_BIN = "bin";

    private static final String REG_DATA = "data";

    private static final String REG_OVERRIDE = "override";

    /**
     * Default constructor.
     */
    public RegistryInstallerListener()
    {
        super(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.compiler.InstallerListener#beforePacks(com.izforge.izpack.installer.AutomatedInstallData,
     * int, com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void beforePacks(AutomatedInstallData idata, Integer npacks,
            AbstractUIProgressHandler handler) throws Exception
    {
        super.beforePacks(idata, npacks, handler);
        initializeRegistryHandler(idata);
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
        try
        {
            // Start logging
            RegistryHandler rh = RegistryDefaultHandler.getInstance();
            if (rh == null) return;
            XMLElement uninstallerPack = null;
            // No interrupt desired after writing registry entries.
            Unpacker.setDiscardInterrupt(true);
            rh.activateLogging();
            if (getSpecHelper().getSpec() != null)
            {
                VariableSubstitutor substitutor = new VariableSubstitutor(idata.getVariables());
                Iterator iter = idata.selectedPacks.iterator();
                // Get the special pack "UninstallStuff" which contains values
                // for the uninstaller entry.
                uninstallerPack = getSpecHelper().getPackForName("UninstallStuff");
                performPack(uninstallerPack, substitutor);

                // Now perform the selected packs.
                while (iter != null && iter.hasNext())
                {
                    // Resolve data for current pack.
                    XMLElement pack = getSpecHelper().getPackForName(((Pack) iter.next()).name);
                    performPack(pack, substitutor);

                }
            }
            String uninstallSuffix = idata.getVariable("UninstallKeySuffix");
            if (uninstallSuffix != null)
            {
                rh.setUninstallName(rh.getUninstallName() + " " + uninstallSuffix);
            }
            // Generate uninstaller key automatically if not defined in spec.
            if (uninstallerPack == null) rh.registerUninstallKey();
            // Get the logging info from the registry class and put it into
            // the uninstaller. The RegistryUninstallerListener loads that data
            // and rewind the made entries.
            // This is the common way to transport informations from an
            // installer CustomAction to the corresponding uninstaller
            // CustomAction.
            List info = rh.getLoggingInfo();
            if (info != null)
                UninstallData.getInstance().addAdditionalData("registryEntries", info);

        }
        catch (Exception e)
        {
            if (e instanceof NativeLibException)
                throw new WrappedNativeLibException(e);
            else
                throw e;
        }
    }

    /**
     * Performs the registry settings for the given pack.
     * 
     * @param pack XML elemtent which contains the registry settings for one pack
     * @throws Exception
     */
    private void performPack(XMLElement pack, VariableSubstitutor substitutor) throws Exception
    {
        if (pack == null) return;
        // Get all entries for registry settings.
        Vector regEntries = pack.getChildren();
        if (regEntries == null) return;
        Iterator entriesIter = regEntries.iterator();
        while (entriesIter != null && entriesIter.hasNext())
        {
            XMLElement regEntry = (XMLElement) entriesIter.next();
            // Perform one registry entry.
            String type = regEntry.getName();
            if (type.equalsIgnoreCase(REG_KEY))
            {
                performKeySetting(regEntry, substitutor);
            }
            else if (type.equalsIgnoreCase(REG_VALUE))
            {
                performValueSetting(regEntry, substitutor);
            }
            else
                // No valid type.
                getSpecHelper().parseError(regEntry,
                        "Non-valid type of entry; only 'key' and 'value' are allowed.");

        }

    }

    /**
     * Perform the setting of one value.
     * 
     * @param regEntry element which contains the description of the value to be set
     * @param substitutor variable substitutor to be used for revising the regEntry contents
     */
    private void performValueSetting(XMLElement regEntry, VariableSubstitutor substitutor)
            throws Exception
    {
        SpecHelper specHelper = getSpecHelper();
        String name = specHelper.getRequiredAttribute(regEntry, REG_BASENAME);
        name = substitutor.substitute(name, null);
        String keypath = specHelper.getRequiredAttribute(regEntry, REG_KEYPATH);
        keypath = substitutor.substitute(keypath, null);
        String root = specHelper.getRequiredAttribute(regEntry, REG_ROOT);
        int rootId = resolveRoot(regEntry, root, substitutor);

        RegistryHandler rh = RegistryDefaultHandler.getInstance();
        if (rh == null) return;

        rh.setRoot(rootId);

        String override = regEntry.getAttribute(REG_OVERRIDE, "true");
        if (!"true".equalsIgnoreCase(override))
        { // Do not set value if override is not true and the value exist.

            if (rh.getValue(keypath, name, null) != null) return;
        }

        String value = regEntry.getAttribute(REG_DWORD);
        if (value != null)
        { // Value type is DWord; placeholder possible.
            value = substitutor.substitute(value, null);
            rh.setValue(keypath, name, Long.parseLong(value));
            return;
        }
        value = regEntry.getAttribute(REG_STRING);
        if (value != null)
        { // Value type is string; placeholder possible.
            value = substitutor.substitute(value, null);
            rh.setValue(keypath, name, value);
            return;
        }
        Vector values = regEntry.getChildrenNamed(REG_MULTI);
        if (values != null && !values.isEmpty())
        { // Value type is REG_MULTI_SZ; placeholder possible.
            Iterator multiIter = values.iterator();
            String[] multiString = new String[values.size()];
            for (int i = 0; multiIter.hasNext(); ++i)
            {
                XMLElement element = (XMLElement) multiIter.next();
                multiString[i] = specHelper.getRequiredAttribute(element, REG_DATA);
                multiString[i] = substitutor.substitute(multiString[i], null);
            }
            rh.setValue(keypath, name, multiString);
            return;
        }
        values = regEntry.getChildrenNamed(REG_BIN);
        if (values != null && !values.isEmpty())
        { // Value type is REG_BINARY; placeholder possible or not ??? why not
            // ...
            Iterator multiIter = values.iterator();

            StringBuffer buf = new StringBuffer();
            for (int i = 0; multiIter.hasNext(); ++i)
            {
                XMLElement element = (XMLElement) multiIter.next();
                String tmp = specHelper.getRequiredAttribute(element, REG_DATA);
                buf.append(tmp);
                if (!tmp.endsWith(",") && multiIter.hasNext()) buf.append(",");
            }
            byte[] bytes = extractBytes(regEntry, substitutor.substitute(buf.toString(), null));
            rh.setValue(keypath, name, bytes);
            return;
        }
        specHelper.parseError(regEntry, "No data found.");

    }

    private byte[] extractBytes(XMLElement element, String byteString) throws Exception
    {
        StringTokenizer st = new StringTokenizer(byteString, ",");
        byte[] retval = new byte[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens())
        {
            byte value = 0;
            String token = st.nextToken().trim();
            try
            { // Unfortenly byte is signed ...
                int tval = Integer.parseInt(token, 16);
                if (tval < 0 || tval > 0xff)
                    throw new NumberFormatException("Value out of range.");
                if (tval > 0x7f) tval -= 0x100;
                value = (byte) tval;
            }
            catch (NumberFormatException nfe)
            {
                getSpecHelper()
                        .parseError(element,
                                "Bad entry for REG_BINARY; a byte should be written as 2 digit hexvalue followed by a ','.");
            }
            retval[i++] = value;
        }
        return (retval);

    }

    /**
     * Perform the setting of one key.
     * 
     * @param regEntry element which contains the description of the key to be created
     * @param substitutor variable substitutor to be used for revising the regEntry contents
     */
    private void performKeySetting(XMLElement regEntry, VariableSubstitutor substitutor)
            throws Exception
    {
        String keypath = getSpecHelper().getRequiredAttribute(regEntry, REG_KEYPATH);
        keypath = substitutor.substitute(keypath, null);
        String root = getSpecHelper().getRequiredAttribute(regEntry, REG_ROOT);
        int rootId = resolveRoot(regEntry, root, substitutor);
        RegistryHandler rh = RegistryDefaultHandler.getInstance();
        if (rh == null) return;
        rh.setRoot(rootId);
        if (!rh.keyExist(keypath)) rh.createKey(keypath);
    }

    private int resolveRoot(XMLElement regEntry, String root, VariableSubstitutor substitutor)
            throws Exception
    {
        String root1 = substitutor.substitute(root, null);
        Integer tmp = (Integer) RegistryHandler.ROOT_KEY_MAP.get(root1);
        if (tmp != null) return (tmp.intValue());
        getSpecHelper().parseError(regEntry, "Unknown value (" + root1 + ")for registry root.");
        return 0;
    }

    private void initializeRegistryHandler(AutomatedInstallData idata) throws Exception
    {
        RegistryHandler rh = RegistryDefaultHandler.getInstance();
        if (rh == null) return;
        rh.verify(idata);
        getSpecHelper().readSpec(SPEC_FILE_NAME);
    }

}
