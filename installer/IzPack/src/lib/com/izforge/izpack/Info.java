/*
 * $Id: Info.java 1816 2007-04-23 19:57:27Z jponge $
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

package com.izforge.izpack;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Contains some informations for an installer, as defined in the <info> section of the XML files.
 * 
 * @author Julien Ponge
 */
public class Info implements Serializable
{

    static final long serialVersionUID = 13288410782044775L;

    /** The application name and version */
    private String appName = "";
    private String appVersion = "";

    /** The installation subpath */
    private String installationSubPath = null;

    /** The application authors */
    private ArrayList authors = new ArrayList();

    /** The application URL */
    private String appURL = null;

    /** The required Java version (min) */
    private String javaVersion = "1.2";

    /** The name of the installer file (name without jar suffix) */
    private String installerBase = null;

    /** The application Web Directory URL */
    private String webDirURL = null;

    /** The uninstaller name */
    private String uninstallerName = "uninstaller.jar";

    /** The path of the summary log file */
    private String summaryLogFilePath = "$INSTALL_PATH/Uninstaller/InstallSummary.htm";

    /** The full qualified name of the class which should be
     *  used for decoding the packs.
     */
    private String packDecoderClassName = null;
    
    private String unpackerClassName = null;
    
    /** The constructor, deliberatly void. */
    public Info()
    {
    }

    /**
     * Sets the application name.
     * 
     * @param appName The new application name.
     */
    public void setAppName(String appName)
    {
        this.appName = appName;
    }

    /**
     * Gets the application name.
     * 
     * @return The application name.
     */
    public String getAppName()
    {
        return appName;
    }

    /**
     * Sets the version.
     * 
     * @param appVersion The application version.
     */
    public void setAppVersion(String appVersion)
    {
        this.appVersion = appVersion;
    }

    /**
     * Gets the version.
     * 
     * @return The application version.
     */
    public String getAppVersion()
    {
        return appVersion;
    }

    /**
     * Adds an author to the authors list.
     * 
     * @param author The author to add.
     */
    public void addAuthor(Author author)
    {
        authors.add(author);
    }

    /**
     * Gets the authors list.
     * 
     * @return The authors list.
     */
    public ArrayList getAuthors()
    {
        return authors;
    }

    /**
     * Sets the application URL.
     * 
     * @param appURL The application URL.
     */
    public void setAppURL(String appURL)
    {
        this.appURL = appURL;
    }

    /**
     * Gets the application URL.
     * 
     * @return The application URL.
     */
    public String getAppURL()
    {
        return appURL;
    }

    /**
     * Sets the minimum Java version required.
     * 
     * @param javaVersion The Java version.
     */
    public void setJavaVersion(String javaVersion)
    {
        this.javaVersion = javaVersion;
    }

    /**
     * Gets the Java version required.
     * 
     * @return The Java version.
     */
    public String getJavaVersion()
    {
        return javaVersion;
    }

    /**
     * Sets the installer name.
     * 
     * @param installerBase The new installer name.
     */
    public void setInstallerBase(String installerBase)
    {
        this.installerBase = installerBase;
    }

    /**
     * Gets the installer name.
     * 
     * @return The name of the installer file, without the jar suffix.
     */
    public String getInstallerBase()
    {
        return installerBase;
    }

    /**
     * Sets the webDir URL.
     * 
     * @param url The application URL.
     */
    public void setWebDirURL(String url)
    {
        this.webDirURL = url;
    }

    /**
     * Gets the webDir URL if it has been specified
     * 
     * @return The webDir URL from which the installer is retrieved, or <tt>null</tt> if non has
     * been set.
     */
    public String getWebDirURL()
    {
        return webDirURL;
    }

    /**
     * Sets the name of the uninstaller.
     * 
     * @param name the name of the uninstaller.
     */
    public void setUninstallerName(String name)
    {
        this.uninstallerName = name;
    }

    /**
     * Returns the name of the uninstaller.
     * 
     * @return the name of the uninstaller.
     */
    public String getUninstallerName()
    {
        return this.uninstallerName;
    }

    /**
     * This class represents an author.
     * 
     * @author Julien Ponge
     */
    public static class Author implements Serializable
    {

        static final long serialVersionUID = -3090178155004960243L;

        /** The author name */
        private String name;

        /** The author email */
        private String email;

        /**
         * Gets the author name.
         * 
         * @return The author name.
         */
        public String getName()
        {
            return name;
        }

        /**
         * Gets the author email.
         * 
         * @return The author email.
         */
        public String getEmail()
        {
            return email;
        }

        /**
         * The constructor.
         * 
         * @param name The author name.
         * @param email The author email.
         */
        public Author(String name, String email)
        {
            this.name = name;
            this.email = email;
        }

        /**
         * Gets a String representation of the author.
         * 
         * @return The String representation of the author, in the form : name <email> .
         */
        public String toString()
        {
            return name + " <" + email + ">";
        }

    }

    /**
     * Gets the installation subpath.
     * 
     * @return the installation subpath
     */
    public String getInstallationSubPath()
    {
        return installationSubPath;
    }

    /**
     * Sets the installation subpath.
     * 
     * @param string subpath to be set
     */
    public void setInstallationSubPath(String string)
    {
        installationSubPath = string;
    }

    /**
     * Returns the summary log file path.
     * 
     * @return the summary log file path
     */
    public String getSummaryLogFilePath()
    {
        return summaryLogFilePath;
    }

    /**
     * Sets the summary log file path.
     * 
     * @param summaryLogFilePath the summary log file path to set
     */
    public void setSummaryLogFilePath(String summaryLogFilePath)
    {
        this.summaryLogFilePath = summaryLogFilePath;
    }
    /**
     * Returns the full qualified class name of the class which
     * should be used for decoding the packs.
     * @return Returns the packDecoderClassName.
     */
    public String getPackDecoderClassName()
    {
        return packDecoderClassName;
    }
    /**
     * Sets the full qualified class name of the class which
     * should be used for decoding the packs.
     * @param packDecoderClassName The packDecoderClassName to set.
     */
    public void setPackDecoderClassName(String packDecoderClassName)
    {
        this.packDecoderClassName = packDecoderClassName;
    }

    
    public String getUnpackerClassName()
    {
        return unpackerClassName;
    }

    
    public void setUnpackerClassName(String unpackerClassName)
    {
        this.unpackerClassName = unpackerClassName;
    }
}
