/*
 * $Id: IzPackTask.java 1816 2007-04-23 19:57:27Z jponge $
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Paul Wilkinson
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

package com.izforge.izpack.ant;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.PropertySet;

import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.CompilerException;
import com.izforge.izpack.compiler.PackagerListener;

/**
 * A IzPack Ant task.
 * 
 * @author Paul Wilkinson
 */
public class IzPackTask extends Task implements PackagerListener
{
    /** The embedded installation configuration */
    private ConfigHolder config;

    /** Holds value of property input. */
    private String input;

    /** Holds value of property basedir. */
    private String basedir;

    /** Holds value of property output. */
    private String output;

    /** Holds value of property compression. */
    private String compression;

    /** Holds value of property compression. */
    private int compressionLevel;

    /** Holds value of property installerType. */
    private InstallerType installerType;

    /**
     * Holds value of property izPackDir. This should point at the IzPack directory
     */
    private String izPackDir;

    /** Holds properties used to make substitutions in the install file */
    private Properties properties;

    /** should we inherit properties from the Ant file? */
    private boolean inheritAll = false;

    /** Creates new IZPackTask */
    public IzPackTask()
    {
        basedir = null;
        config = null;
        input = null;
        output = null;
        installerType = null;
        izPackDir = null;
        compression = "default";
        compressionLevel = -1;
    }

    /**
     * Called by ant to create the object for the config nested element.
     * @return a holder object for the config nested element.
     */
    public ConfigHolder createConfig()
    {
        config = new ConfigHolder(getProject());
        return config;
    }

    /**
     * Logs a message to the Ant log at default priority (MSG_INFO).
     * 
     * @param str The message to log.
     */
    public void packagerMsg(String str)
    {
        packagerMsg(str, MSG_INFO);
    }

    /**
     * Logs a message to the Ant log at the specified priority.
     * 
     * @param str The message to log.
     * @param priority The priority of the message.
     */
    public void packagerMsg(String str, int priority)
    {
        final int antPriority;
        switch (priority)
        // No guarantee of a direct conversion. It's an enum
        {
        case MSG_DEBUG:
            antPriority = Project.MSG_DEBUG;
            break;
        case MSG_ERR:
            antPriority = Project.MSG_ERR;
            break;
        case MSG_INFO:
            antPriority = Project.MSG_INFO;
            break;
        case MSG_VERBOSE:
            antPriority = Project.MSG_VERBOSE;
            break;
        case MSG_WARN:
            antPriority = Project.MSG_WARN;
            break;
        default: // rather than die...
            antPriority = Project.MSG_INFO;
        }
        log(str, antPriority);
    }

    /** Called when the packaging starts. */
    public void packagerStart()
    {
        log(ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString(
                "Packager_starting"), Project.MSG_DEBUG);
    }

    /** Called when the packaging stops. */
    public void packagerStop()
    {
        log(ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString(
                "Packager_ended"), Project.MSG_DEBUG);
    }

    /**
     * Packages.
     * 
     * @exception BuildException Description of the Exception
     */
    public void execute() throws org.apache.tools.ant.BuildException
    {
        // Either the input attribute or config element must be specified
        if (input == null && config == null)
            throw new BuildException(ResourceBundle.getBundle(
                    "com/izforge/izpack/ant/langpacks/messages").getString(
                    "input_must_be_specified"));

        if (output == null)
            throw new BuildException(ResourceBundle.getBundle(
                    "com/izforge/izpack/ant/langpacks/messages").getString(
                    "output_must_be_specified"));

        // if (installerType == null) now optional

        if (basedir == null)
            throw new BuildException(ResourceBundle.getBundle(
                    "com/izforge/izpack/ant/langpacks/messages").getString(
                    "basedir_must_be_specified"));

        // if (izPackDir == null)
        // throw new
        // BuildException(java.util.ResourceBundle.getBundle("com/izforge/izpack/ant/langpacks/messages").getString("izPackDir_must_be_specified"));

        String kind = (installerType == null ? null : installerType.getValue());

        CompilerConfig c = null;
        String configText = null;
        if(config != null )
        {// Pass in the embedded configuration
            configText = config.getText();
            input = null;
        }
        try
        {
            // else use external configuration referenced by the input attribute
            c = new CompilerConfig(input, basedir, kind, output, 
                    compression, compressionLevel, this, configText);
        }
        catch (CompilerException e1)
        {
            throw new BuildException(e1);
        }
        CompilerConfig.setIzpackHome(izPackDir);

        if (properties != null)
        {
            Enumeration e = properties.keys();
            while (e.hasMoreElements())
            {
                String name = (String) e.nextElement();
                String value = properties.getProperty(name);
                value = fixPathString(value);
                c.addProperty(name, value);
            }
        }

        if (inheritAll)
        {
            Hashtable projectProps = getProject().getProperties();
            Enumeration e = projectProps.keys();
            while (e.hasMoreElements())
            {
                String name = (String) e.nextElement();
                String value = (String) projectProps.get(name);
                value = fixPathString(value);
                c.addProperty(name, value);
            }            
        }

        try
        {
            c.executeCompiler();
        }
        catch (Exception e)
        {
            throw new BuildException(e);// Throw an exception if compilation
            // failed
        }
    }
    
    private static String fixPathString(String path)
    {
       /*
        * The following code fixes a bug in in codehaus classworlds loader,
        * which can't handle mixed path strings like "c:\test\../lib/mylib.jar".
        * The bug is in org.codehaus.classworlds.UrlUtils.normalizeUrlPath().
        */
       StringBuffer fixpath = new StringBuffer(path);
       for(int q=0; q<fixpath.length(); q++)
          if(fixpath.charAt(q) == '\\')
             fixpath.setCharAt(q, '/');
       return fixpath.toString();
    }
    
    /**
     * Setter for property input.
     * 
     * @param input New value of property input.
     */
    public void setInput(String input)
    {
        this.input = input;
    }

    /**
     * Setter for property basedir.
     * 
     * @param basedir New value of property basedir.
     */
    public void setBasedir(String basedir)
    {
        this.basedir = basedir;
    }

    /**
     * Setter for property output.
     * 
     * @param output New value of property output.
     */
    public void setOutput(String output)
    {
        this.output = output;
    }

    /**
     * Setter for property installerType.
     * 
     * @param installerType New value of property installerType.
     */
    public void setInstallerType(InstallerType installerType)
    {
        this.installerType = installerType;
    }

    /**
     * Setter for property izPackDir.
     * 
     * @param izPackDir New value of property izPackDir.
     */
    public void setIzPackDir(String izPackDir)
    {
        if (!(izPackDir.endsWith("/"))) izPackDir += "/";
        this.izPackDir = izPackDir;
    }

    /**
     * If true, pass all Ant properties to IzPack. Defaults to false;
     */
    public void setInheritAll(boolean value)
    {
        inheritAll = value;
    }

    /**
     * Setter for property compression.
     * @param compression The type compression to set for pack compression.
     */
    public void setCompression(String compression)
    {
        this.compression = compression;
    }

    /**
     * @param compressionLevel The compressionLevel to set.
     */
    public void setCompressionLevel(int compressionLevel)
    {
        this.compressionLevel = compressionLevel;
    }

    /**
     * Ant will call this for each &lt;property&gt; tag to the IzPack task.
     */
    public void addConfiguredProperty(Property property)
    {
        if (properties == null) properties = new Properties();

        property.execute(); // don't call perform(), so no build events triggered

        Properties props = property.getProperties();
        Enumeration e = props.keys();
        while (e.hasMoreElements())
        {
            String name = (String) e.nextElement();
            String value = props.getProperty(name);
            log("Adding property: " + property.getClass() + name+"=" + value,
                Project.MSG_VERBOSE);

            properties.setProperty(name, value);
        }
    }

    /**
     * A set of properties to pass from the build environment to the install compile
     *
     * @param ps The propertyset collection of properties
     */
    public void addConfiguredPropertyset(PropertySet ps)
    {
        if (properties == null) properties = new Properties();

        properties.putAll(ps.getProperties());
    }

    /**
     * Enumerated attribute with the values "asis", "add" and "remove".
     * 
     * @author Paul Wilkinson
     */
    public static class InstallerType extends EnumeratedAttribute
    {

        public String[] getValues()
        {
            return new String[] { CompilerConfig.STANDARD, CompilerConfig.WEB};
        }
    }
}
