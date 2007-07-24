/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2007 JBoss Inc
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.panels.InstallationGroupPanel.GroupData;
import com.izforge.izpack.util.Debug;

/**
 * An automation helper for the InstallationGroupPanel
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision:$
 */
public class InstallationGroupPanelAutomationHelper
	implements PanelAutomation
{
	/**
	 * 
	 */
	public void makeXMLData(AutomatedInstallData idata, XMLElement panelRoot)
	{
		GroupData[] rows = (GroupData[]) idata.getAttribute("GroupData");
		HashMap packsByName = (HashMap) idata.getAttribute("packsByName");
        // Write out the group to pack mappings
        for(int n = 0; n < rows.length; n ++)
        {
        	GroupData gd = rows[n];
        	XMLElement xgroup = new XMLElement("group");
        	xgroup.setAttribute("name", gd.name);
        	Iterator names = gd.packNames.iterator();
        	while( names.hasNext() )
        	{
        		String name = (String) names.next();
        		Pack pack = (Pack) packsByName.get(name);
        		int index = idata.availablePacks.indexOf(pack);
        		XMLElement xpack = new XMLElement("pack");
        		xpack.setAttribute("name", name);
        		xpack.setAttribute("index", ""+index);
        		xgroup.addChild(xpack);
        	}
        	panelRoot.addChild(xgroup);
        }
	}

	/**
	 * TODO Need to add a InstallationGroupPanelAutomationHelper to read the
	 * xml data to allow an install group to specify the selected packs.
	 */
	public boolean runAutomated(AutomatedInstallData idata,
			XMLElement panelRoot)
	{
		String installGroup = idata.getVariable("INSTALL_GROUP");
		Debug.trace("InstallationGroupPanelAutomationHelper: runAutomated, INSTALL_GROUP: "+installGroup);
		if( installGroup != null )
		{
			Vector groups = panelRoot.getChildrenNamed("group");
			for(int i = 0; i < groups.size(); i ++)
			{
				XMLElement group = (XMLElement) groups.get(i);
				String name = group.getAttribute("name");
				Debug.trace("InstallationGroupPanelAutomationHelper: Checking INSTALL_GROUP against: "+name);
				if( name.equalsIgnoreCase(installGroup) )
				{
					Debug.trace("Found INSTALL_GROUP match for: "+installGroup);
					idata.selectedPacks.clear();
					Vector packs = group.getChildrenNamed("pack");
					Debug.trace(name+" pack count: "+packs.size());
					Debug.trace("Available pack count: "+idata.availablePacks.size());
					for(int j = 0; j < packs.size(); j ++)
					{
						XMLElement xpack = (XMLElement) packs.get(j);
						String pname = xpack.getAttribute("name");
						String indexStr = xpack.getAttribute("index");
						int index = Integer.parseInt(indexStr);
						if( index >= 0 )
						{
							Pack pack = (Pack) idata.availablePacks.get(index);
							idata.selectedPacks.add(pack);
							Debug.trace("Added pack: "+pack.name);
						}
					}
					Debug.trace("Set selectedPacks to: "+idata.selectedPacks);
					break;
				}
			}
		}
        return true;
	}

}
