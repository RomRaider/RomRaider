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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

import net.n3.nanoxml.XMLElement;
import com.izforge.izpack.util.Debug;

/**
 * A condition based on the value of a static java field or static java method.
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class JavaCondition extends Condition {
    protected String classname;
    protected String methodname;
    protected String fieldname;
    protected boolean complete;
    protected String returnvalue;
    protected String returnvaluetype;


    protected Class usedclass;
    protected Field usedfield;
    protected Method usedmethod;

    public JavaCondition() {

    }

    private boolean isTrue(Properties variables) {
        if (!this.complete) {
            return false;
        } else {
            if (this.usedclass == null) {
                ClassLoader loader = ClassLoader.getSystemClassLoader();
                try {
                    this.usedclass = loader.loadClass(this.classname);
                } catch (ClassNotFoundException e) {
                    Debug.log("Can't find class " + this.classname);
                    return false;
                }
            }
            if ((this.usedfield == null) && (this.fieldname != null)) {
                try {
                    this.usedfield = this.usedclass.getField(this.fieldname);
                } catch (SecurityException e) {
                    Debug.log("No permission to access specified field: " + this.fieldname);
                    return false;
                } catch (NoSuchFieldException e) {
                    Debug.log("No such field: " + this.fieldname);
                    return false;
                }
            }
            if ((this.usedmethod == null) && (this.methodname != null)) {
                Debug.log("not implemented yet.");
                return false;
            }

            if (this.usedfield != null) {
                // access field
                if ("boolean".equals(this.returnvaluetype)) {
                    try {
                        boolean returnval = this.usedfield.getBoolean(null);
                        boolean expectedreturnval = Boolean.valueOf(this.returnvalue).booleanValue();
                        return returnval == expectedreturnval;
                    } catch (IllegalArgumentException e) {
                        Debug.log("IllegalArgumentexeption " + this.fieldname);
                    } catch (IllegalAccessException e) {
                        Debug.log("IllegalAccessException " + this.fieldname);
                    }
                } else {
                    Debug.log("not implemented yet.");
                    return false;
                }
            }
            return false;
        }
    }

    public void readFromXML(XMLElement xmlcondition) {
        if (xmlcondition.getChildrenCount() != 2) {
            Debug.log("Condition of type java needs (java,returnvalue)");
            return;
        }
        XMLElement javael = xmlcondition.getFirstChildNamed("java");
        XMLElement classel = javael.getFirstChildNamed("class");
        if (classel != null) {
            this.classname = classel.getContent();
        } else {
            Debug.log("Java-Element needs (class,method?,field?)");
            return;
        }
        XMLElement methodel = javael.getFirstChildNamed("method");
        if (methodel != null) {
            this.methodname = methodel.getContent();
        }
        XMLElement fieldel = javael.getFirstChildNamed("field");
        if (fieldel != null) {
            this.fieldname = fieldel.getContent();
        }
        if ((this.methodname == null) && (this.fieldname == null)) {
            Debug.log("java element needs (class, method?,field?)");
            return;
        }
        XMLElement returnvalel = xmlcondition.getFirstChildNamed("returnvalue");
        if (returnvalel != null) {
            this.returnvalue = returnvalel.getContent();
            this.returnvaluetype = returnvalel.getAttribute("type");
        } else {
            Debug.log("no returnvalue-element specified.");
            return;
        }
        this.complete = true;
    }

    public boolean isTrue()
    {
       return this.isTrue(this.installdata.getVariables());
    }

}
