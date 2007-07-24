/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Chadwick McHenry
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

package com.izforge.izpack.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import net.n3.nanoxml.XMLElement;

import org.apache.tools.ant.taskdefs.Execute;

import com.izforge.izpack.util.VariableSubstitutor;

/**
 * Sets a property by name, or set of properties (from file or resource) in the project. This is
 * modeled after ant properties
 * <p>
 * 
 * Properties are immutable: once a property is set it cannot be changed. They are most definately
 * not variable.
 * <p>
 * 
 * There are five ways to set properties:
 * <ul>
 * <li>By supplying both the <i>name</i> and <i>value</i> attributes.</li>
 * <li>By setting the <i>file</i> attribute with the filename of the property file to load. This
 * property file has the format as defined by the file used in the class java.util.Properties.</li>
 * <li>By setting the <i>environment</i> attribute with a prefix to use. Properties will be
 * defined for every environment variable by prefixing the supplied name and a period to the name of
 * the variable.</li>
 * </ul>
 * 
 * Combinations of the above are considered an error.
 * <p>
 * 
 * The value part of the properties being set, might contain references to other properties. These
 * references are resolved when the properties are set.
 * <p>
 * 
 * This also holds for properties loaded from a property file.
 * <p>
 * 
 * Properties are case sensitive.
 * <p>
 * 
 * When specifying the environment attribute, it's value is used as a prefix to use when retrieving
 * environment variables. This functionality is currently only implemented on select platforms.
 * <p>
 * 
 * Thus if you specify environment=&quot;myenv&quot; you will be able to access OS-specific
 * environment variables via property names &quot;myenv.PATH&quot; or &quot;myenv.TERM&quot;.
 * <p>
 * 
 * Note also that properties are case sensitive, even if the environment variables on your operating
 * system are not, e.g. it will be ${env.Path} not ${env.PATH} on Windows 2000.
 * <p>
 * 
 * Note that when specifying either the <code>prefix</code> or <code>environment</code>
 * attributes, if you supply a property name with a final &quot;.&quot; it will not be doubled. ie
 * environment=&quot;myenv.&quot; will still allow access of environment variables through
 * &quot;myenv.PATH&quot; and &quot;myenv.TERM&quot;.
 * <p>
 */
public class Property
{

    protected String name;

    protected String value;

    protected File file;

    // protected String resource;
    // protected Path classpath;
    protected String env;

    // protected Reference ref;
    protected String prefix;

    protected XMLElement xmlProp;

    protected CompilerConfig config;
    protected Compiler compiler;

    public Property(XMLElement xmlProp, CompilerConfig config)
    {
        this.xmlProp = xmlProp;
        this.config = config;
        this.compiler = config.getCompiler();
        name = xmlProp.getAttribute("name");
        value = xmlProp.getAttribute("value");
        env = xmlProp.getAttribute("environment");
        if (env != null && !env.endsWith(".")) env += ".";

        prefix = xmlProp.getAttribute("prefix");
        if (prefix != null && !prefix.endsWith(".")) prefix += ".";

        String filename = xmlProp.getAttribute("file");
        if (filename != null) file = new File(filename);
    }

    /**
     * get the value of this property
     * 
     * @return the current value or the empty string
     */
    public String getValue()
    {
        return toString();
    }

    /**
     * get the value of this property
     * 
     * @return the current value or the empty string
     */
    public String toString()
    {
        return value == null ? "" : value;
    }

    /**
     * Set the property in the project to the value. If the task was give a file, resource or env
     * attribute here is where it is loaded.
     */
    public void execute() throws CompilerException
    {
        if (name != null)
        {
            if (value == null)
                config.parseError(xmlProp, "You must specify a value with the name attribute");
        }
        else
        {
            if (file == null && env == null)
                config.parseError(xmlProp,
                        "You must specify file, or environment when not using the name attribute");
        }

        if (file == null && prefix != null)
            config.parseError(xmlProp, "Prefix is only valid when loading from a file ");

        if ((name != null) && (value != null))
            addProperty(name, value);

        else if (file != null)
            loadFile(file);

        else if (env != null) loadEnvironment(env);
    }

    /**
     * load properties from a file
     * 
     * @param file file to load
     */
    protected void loadFile(File file) throws CompilerException
    {
        Properties props = new Properties();
        config.getPackagerListener().packagerMsg("Loading " + file.getAbsolutePath(),
                PackagerListener.MSG_VERBOSE);
        try
        {
            if (file.exists())
            {
                FileInputStream fis = new FileInputStream(file);
                try
                {
                    props.load(fis);
                }
                finally
                {
                    if (fis != null) fis.close();
                }
                addProperties(props);
            }
            else
            {
                config.getPackagerListener().packagerMsg(
                        "Unable to find property file: " + file.getAbsolutePath(),
                        PackagerListener.MSG_VERBOSE);
            }
        }
        catch (IOException ex)
        {
            config.parseError(xmlProp, "Faild to load file: " + file.getAbsolutePath(), ex);
        }
    }

    /**
     * load the environment values
     * 
     * @param prefix prefix to place before them
     */
    protected void loadEnvironment(String prefix) throws CompilerException
    {
        Properties props = new Properties();
        config.getPackagerListener().packagerMsg("Loading Environment " + prefix,
                PackagerListener.MSG_VERBOSE);
        Vector osEnv = Execute.getProcEnvironment();
        for (Enumeration e = osEnv.elements(); e.hasMoreElements();)
        {
            String entry = (String) e.nextElement();
            int pos = entry.indexOf('=');
            if (pos == -1)
            {
                config.getPackagerListener().packagerMsg("Ignoring " + prefix,
                        PackagerListener.MSG_WARN);
            }
            else
            {
                props.put(prefix + entry.substring(0, pos), entry.substring(pos + 1));
            }
        }
        addProperties(props);
    }

    /**
     * Add a name value pair to the project property set
     * 
     * @param name name of property
     * @param value value to set
     */
    protected void addProperty(String name, String value) throws CompilerException
    {
        value = compiler.replaceProperties(value);

        compiler.addProperty(name, value);
    }

    /**
     * iterate through a set of properties, resolve them then assign them
     */
    protected void addProperties(Properties props) throws CompilerException
    {
        resolveAllProperties(props);
        Enumeration e = props.keys();
        while (e.hasMoreElements())
        {
            String name = (String) e.nextElement();
            String value = props.getProperty(name);

            if (prefix != null)
            {
                name = prefix + name;
            }

            addProperty(name, value);
        }
    }

    /**
     * resolve properties inside a properties object
     * 
     * @param props properties to resolve
     */
    private void resolveAllProperties(Properties props) throws CompilerException
    {
        VariableSubstitutor subs = new VariableSubstitutor(props);
        subs.setBracesRequired(true);

        for (Enumeration e = props.keys(); e.hasMoreElements();)
        {
            String name = (String) e.nextElement();
            String value = props.getProperty(name);

            int mods = -1;
            do
            {
                StringReader read = new StringReader(value);
                StringWriter write = new StringWriter();

                try
                {
                    mods = subs.substitute(read, write, "at");
                    // TODO: check for circular references. We need to know
                    // which
                    // variables were substituted to do that
                    props.put(name, value);
                }
                catch (IOException ex)
                {
                    config.parseError(xmlProp, "Faild to load file: " + file.getAbsolutePath(),
                            ex);
                }
            }
            while (mods != 0);
        }
    }
}
