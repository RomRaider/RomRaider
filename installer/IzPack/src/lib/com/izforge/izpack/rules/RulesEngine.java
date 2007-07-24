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

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.util.Debug;

import net.n3.nanoxml.XMLElement;

/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 *         created: 09.11.2006, 13:48:39
 */
public class RulesEngine {
    protected Map panelconditions;
    protected Map packconditions;
    protected Map optionalpackconditions;
    protected XMLElement conditionsspec;
    protected static Map conditionsmap = new Hashtable();
    protected InstallData installdata;

    /**
     *
     */
    public RulesEngine(XMLElement conditionsspecxml, InstallData installdata) {
        this.conditionsspec = conditionsspecxml;
        conditionsmap = new Hashtable();
        this.panelconditions = new Hashtable();
        this.packconditions = new Hashtable();
        this.optionalpackconditions = new Hashtable();
        this.installdata = installdata;
        this.readConditions();
    }

    /**
     * Checks if an attribute for an xmlelement is set.
     *
     * @param val       value of attribute to check
     * @param attribute the attribute which is checked
     * @param element   the element
     * @return true value was set
     *         false no value was set
     */
    protected boolean checkAttribute(String val, String attribute, String element) {
        if ((val != null) && (val.length() > 0)) {
            return true;
        } else {
            Debug.trace("Element " + element + " has to specify an attribute " + attribute);
            return false;
        }
    }

    public static Condition analyzeCondition(XMLElement condition) {
        String condid = condition.getAttribute("id");
        String condtype = condition.getAttribute("type");
        Condition result = null;
        if (condtype != null) {

            String conditiontype = condtype.toLowerCase();
            // TODO: externalize package name
            String conditionclassname = "com.izforge.izpack.rules." + conditiontype.substring(0, 1).toUpperCase() + conditiontype.substring(1, conditiontype.length());
            conditionclassname += "Condition";
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            try {
                Class conditionclass = loader.loadClass(conditionclassname);
                result = (Condition) conditionclass.newInstance();
            } catch (ClassNotFoundException e) {
                Debug.trace(conditionclassname + " not found.");
            } catch (InstantiationException e) {
                Debug.trace(conditionclassname + " couldn't be instantiated.");
            } catch (IllegalAccessException e) {
                Debug.trace("Illegal access to " + conditionclassname);
            }
            result.readFromXML(condition);
            result.setId(condid);
        }
        return result;
    }


    /**
     * Read the spec for the conditions
     */
    protected void readConditions() {
        if (this.conditionsspec == null){
            Debug.trace("No specification for conditions found.");
            return;
        }
        try {
            if (this.conditionsspec.hasChildren()) {
                // read in the condition specs
                Vector childs = this.conditionsspec.getChildrenNamed("condition");

                for (int i = 0; i < childs.size(); i++) {
                    XMLElement condition = (XMLElement) childs.get(i);
                    Condition cond = analyzeCondition(condition);
                    if (cond != null) {
                        // this.conditionslist.add(cond);
                        String condid = cond.getId();
                        cond.setInstalldata(this.installdata);
                        if ((condid != null) && !("UNKNOWN".equals(condid))) {                            
                            conditionsmap.put(condid, cond);
                        }
                    }
                }

                Vector panelconditionels = this.conditionsspec.getChildrenNamed("panelcondition");
                for (int i = 0; i < panelconditionels.size(); i++) {
                    XMLElement panelel = (XMLElement) panelconditionels.get(i);
                    String panelid = panelel.getAttribute("panelid");
                    String conditionid = panelel.getAttribute("conditionid");
                    this.panelconditions.put(panelid, conditionid);
                }

                Vector packconditionels = this.conditionsspec.getChildrenNamed("packcondition");
                for (int i = 0; i < packconditionels.size(); i++) {
                    XMLElement panelel = (XMLElement) packconditionels.get(i);
                    String panelid = panelel.getAttribute("packid");
                    String conditionid = panelel.getAttribute("conditionid");
                    this.packconditions.put(panelid, conditionid);
                    // optional install allowed, if condition is not met?
                    String optional = panelel.getAttribute("optional");
                    if (optional != null) {
                        boolean optionalinstall = Boolean.valueOf(optional).booleanValue();
                        if (optionalinstall) {
                            // optional installation is allowed
                            this.optionalpackconditions.put(panelid, conditionid);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Condition getCondition(String id) {
        return (Condition) conditionsmap.get(id);
    }

    public boolean isConditionTrue(String id, Properties variables) {
        Condition cond = (Condition) conditionsmap.get(id);
        if (cond == null) {
            Debug.trace("Condition (" + id + ") not found.");
            return true;
        } else {
            Debug.trace("Checking condition");
            return cond.isTrue();
        }
    }

    public boolean isConditionTrue(Condition cond, Properties variables) {
        if (cond == null) {
            Debug.trace("Condition not found.");
            return true;
        } else {
            Debug.trace("Checking condition");
            return cond.isTrue();
        }
    }

    /**
     * Can a panel be shown?
     *
     * @param panelid   - id of the panel, which should be shown
     * @param variables - the variables
     * @return true - there is no condition or condition is met
     *         false - there is a condition and the condition was not met
     */
    public boolean canShowPanel(String panelid, Properties variables) {
        Debug.trace("can show panel with id " + panelid + " ?");
        if (!this.panelconditions.containsKey(panelid)) {
            Debug.trace("no condition, show panel");
            return true;
        }
        Debug.trace("there is a condition");
        Condition condition = (Condition) conditionsmap.get(this.panelconditions.get(panelid));
        if (condition != null) {
            return condition.isTrue();
        }
        return false;
    }

    /**
     * Is the installation of a pack possible?
     *
     * @param packid
     * @param variables
     * @return true - there is no condition or condition is met
     *         false - there is a condition and the condition was not met
     */
    public boolean canInstallPack(String packid, Properties variables) {
        if (packid == null){
            return true;
        }
        Debug.trace("can install pack with id " + packid + "?");
        if (!this.packconditions.containsKey(packid)) {
            Debug.trace("no condition, can install pack");
            return true;
        }
        Debug.trace("there is a condition");
        Condition condition = (Condition) conditionsmap.get(this.packconditions.get(packid));
        if (condition != null) {
            return condition.isTrue();
        }
        return false;
    }

    /**
     * Is an optional installation of a pack possible if the condition is not met?
     *
     * @param packid
     * @param variables
     * @return
     */
    public boolean canInstallPackOptional(String packid, Properties variables) {
        Debug.trace("can install pack optional with id " + packid + "?");
        if (!this.optionalpackconditions.containsKey(packid)) {
            Debug.trace("not in optionalpackconditions.");
            return false;
        } else {
            Debug.trace("optional install possible");
            return true;
        }
    }
}
