/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2003 Marc Eppelmann
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

/*
 * This represents a Implementation of the KDE/GNOME DesktopEntry.
 * which is standard from
 * "Desktop Entry Standard"
 *  "The format of .desktop files, supported by KDE and GNOME."
 *  http://www.freedesktop.org/standards/desktop-entry-spec/
 *
 *  [Desktop Entry]
 //  Comment=$Comment
 //  Comment[de]=
 //  Encoding=$UTF-8
 //  Exec=$'/home/marc/CPS/tomcat/bin/catalina.sh' run
 //  GenericName=$
 //  GenericName[de]=$
 //  Icon=$inetd
 //  MimeType=$
 //  Name=$Start Tomcat
 //  Name[de]=$Start Tomcat
 //  Path=$/home/marc/CPS/tomcat/bin/
 //  ServiceTypes=$
 //  SwallowExec=$
 //  SwallowTitle=$
 //  Terminal=$true
 //  TerminalOptions=$
 //  Type=$Application
 //  X-KDE-SubstituteUID=$false
 //  X-KDE-Username=$
 *
 */
package com.izforge.izpack.util.os;

import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.StringTool;
import com.izforge.izpack.util.os.unix.ShellScript;
import com.izforge.izpack.util.os.unix.UnixHelper;
import com.izforge.izpack.util.os.unix.UnixUser;
import com.izforge.izpack.util.os.unix.UnixUsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This is the Implementation of the RFC-Based Desktop-Link. Used in KDE and GNOME.
 * 
 * @author marc.eppelmann&#064;reddot.de
 */
public class Unix_Shortcut extends Shortcut implements Unix_ShortcutConstants
{

    // ~ Static fields/initializers
    // *******************************************************************************************************************************
    /** version = "$Id: Unix_Shortcut.java 1816 2007-04-23 19:57:27Z jponge $" */
    private static String version = "$Id: Unix_Shortcut.java 1816 2007-04-23 19:57:27Z jponge $";

    /** rev = "$Revision: 1816 $" */
    private static String rev = "$Revision: 1816 $";

    /** DESKTOP_EXT = ".desktop" */
    private static String DESKTOP_EXT = ".desktop";

    /** template = "" */
    private static String template = "";

    /** N = "\n" */
    private final static String N = "\n";

    /** H = "#" */
    private final static String H = "#";

    /** S = " " */
    private final static String S = " ";

    /** C = Comment = H+S = "# " */
    private final static String C = H + S;

    /** QM = "\"" : <b>Q</b>uotation<b>M</b>ark */
    private final static String QM = "\"";
    
    private int ShortcutType;
    private static ShellScript rootScript = null;
    private static ShellScript uninstallScript = null;
    private static ArrayList users = UnixUsers.getUsersWithValidShellsExistingHomesAndDesktops();
    //private static ArrayList tempfiles = new ArrayList();
 
    // ~ Instance fields
    // ******************************************************************************************************************************************
    /** internal String createdDirectory */
    private String createdDirectory;

    /** internal int itsUserType */
    private int itsUserType;

    /** internal String itsGroupName */
    private String itsGroupName;

    /** internal String itsName */
    private String itsName;

    /** internal String itsFileName */
    private String itsFileName;

    /** internal String itsApplnkFolder = "applnk" */
    private String itsApplnkFolder = "applnk";

    /** internal Properties Set */
    private Properties props;

    /**
     * forAll = new Boolean(false): A flag to indicate that this should created for all users.
     */
    private Boolean forAll = Boolean.FALSE;

    /** Internal Help Buffer */
    public StringBuffer hlp;

    // ~ Constructors ***********************************************************************

    // ~ Constructors
    // *********************************************************************************************************************************************
    /**
     * Creates a new Unix_Shortcut object.
     */
    public Unix_Shortcut()
    {
        hlp = new StringBuffer();

        String userLanguage = System.getProperty("user.language", "en");

        hlp.append("[Desktop Entry]" + N);

        // TODO implement Attribute: X-KDE-StartupNotify=true

        hlp.append("Categories=" + $Categories + N);

        hlp.append("Comment=" + $Comment + N);
        hlp.append("Comment[").append(userLanguage).append("]=" + $Comment + N);
        hlp.append("Encoding=" + $Encoding + N);

        // this causes too many problems
        //hlp.append("TryExec=" + $E_QUOT + $Exec + $E_QUOT + S + $Arguments + N);

        hlp.append("Exec=" + $E_QUOT + $Exec + $E_QUOT + S + $Arguments + N);
        hlp.append("GenericName=" + $GenericName + N);

        hlp.append("GenericName[").append(userLanguage).append("]=" + $GenericName + N);
        hlp.append("Icon=" + $Icon + N);
        hlp.append("MimeType=" + $MimeType + N);
        hlp.append("Name=" + $Name + N);
        hlp.append("Name[").append(userLanguage).append("]=" + $Name + N);

        hlp.append("Path=" + $P_QUOT + $Path + $P_QUOT + N);
        hlp.append("ServiceTypes=" + $ServiceTypes + N);
        hlp.append("SwallowExec=" + $SwallowExec + N);
        hlp.append("SwallowTitle=" + $SwallowTitle + N);
        hlp.append("Terminal=" + $Terminal + N);

        hlp.append("TerminalOptions=" + $Options_For_Terminal + N);
        hlp.append("Type=" + $Type + N);
        
        hlp.append("URL=" + $URL + N);
        hlp.append("X-KDE-SubstituteUID=" + $X_KDE_SubstituteUID + N);
        hlp.append("X-KDE-Username=" + $X_KDE_Username + N);
        hlp.append(N);
        hlp.append(C + "created by" + S).append(getClass().getName()).append(S).append(rev).append(
                N);
        hlp.append(C).append(version);

        template = hlp.toString();

        props = new Properties();

        initProps();
        
        if( rootScript==null )
            rootScript = new ShellScript();
        if( uninstallScript == null)
            uninstallScript = new ShellScript();
    }

    // ~ Methods ****************************************************************************

    // ~ Methods
    // **************************************************************************************************************************************************
    /**
     * This initialisizes all Properties Values with &quot;&quot;.
     */
    private void initProps()
    {
        String[] propsArray = { $Comment, $$LANG_Comment, $Encoding, $Exec, $Arguments,
                $GenericName, $$LANG_GenericName, $MimeType, $Name, $$LANG_Name, $Path,
                $ServiceTypes, $SwallowExec, $SwallowTitle, $Terminal, $Options_For_Terminal,
                $Type, $X_KDE_SubstituteUID, $X_KDE_Username, $Icon, $URL, $E_QUOT, $P_QUOT,
                $Categories, $TryExec};

        for (int i = 0; i < propsArray.length; i++)
        {
            props.put(propsArray[i], "");
        }
    }

    /**
     * Overridden Method
     * 
     * @see com.izforge.izpack.util.os.Shortcut#initialize(int, java.lang.String)
     */
    public void initialize(int aType, String aName) throws Exception
    {
        this.itsName = aName;
        props.put($Name, aName);
    }

    /**
     * This indicates that Unix will be supported.
     * 
     * @see com.izforge.izpack.util.os.Shortcut#supported()
     */
    public boolean supported()
    {
        return true;
    }

    /**
     * Dummy
     * 
     * @see com.izforge.izpack.util.os.Shortcut#getDirectoryCreated()
     */
    public String getDirectoryCreated()
    {
        return this.createdDirectory; // while not stored...
    }

    /**
     * Dummy
     * 
     * @see com.izforge.izpack.util.os.Shortcut#getFileName()
     */
    public String getFileName()
    {
        return (this.itsFileName);
    }

    /**
     * Overridden compatibility method. Returns all directories in $USER/.kde/share/applink.
     * 
     * @see com.izforge.izpack.util.os.Shortcut#getProgramGroups(int)
     */
    public Vector getProgramGroups(int userType)
    {
        Vector groups = new Vector();

        File kdeShareApplnk = getKdeShareApplnkFolder(userType);

        try
        {
            File[] listing = kdeShareApplnk.listFiles();

            for (int i = 0; i < listing.length; i++)
            {
                if (listing[i].isDirectory())
                {
                    groups.add(listing[i].getName());
                }
            }
        }
        catch (Exception e)
        {
            // ignore and return an empty vector.
        }

        return (groups);
    }

    /**
     * Gets the Programsfolder for the given User (non-Javadoc).
     * 
     * @see com.izforge.izpack.util.os.Shortcut#getProgramsFolder(int)
     */
    public String getProgramsFolder(int current_user)
    {
        String result = "";

        // 
        result = getKdeShareApplnkFolder(current_user).toString();
        
        return result;
    }

    /**
     * Gets the kde/share/applink - Folder for the given user and for the currently known and
     * supported distribution.
     * 
     * @param userType to get for.
     * 
     * @return the users or the systems kde share/applink(-redhat/-mdk)
     */
    private File getKdeShareApplnkFolder(int userType)
    {
       /*
        //newer XDG system
        File xdgPath = new File("usr" + File.separator + "share" + File.separator
               + "applications");
        if(xdgPath.exists()) return xdgPath;*/
       
        File kdeBase = getKdeBase(userType);

        File result = new File(kdeBase + File.separator + "share" + File.separator
                + getKdeApplinkFolderName());

        return result;
    }

    /**
     * Gets the name of the applink folder for the currently used distribution. Currently
     * "applnk-redhat for RedHat, "applnk-mdk" for Mandrake, and simply "applnk" for all others.
     * 
     * @return result
     */
    private String getKdeApplinkFolderName()
    {
        String applinkFolderName = "applnk";

        if (OsVersion.IS_REDHAT_LINUX)
        {
            applinkFolderName = "applnk-redhat";
        }

        if (OsVersion.IS_MANDRAKE_LINUX)
        {
            applinkFolderName = "applnk-mdk";
        }

        return applinkFolderName;
    }

    /**
     * Gets the KDEBasedir for the given User.
     * 
     * @param userType one of root or regular user
     * 
     * @return the basedir
     */
    private File getKdeBase(int userType)
    {
        File result = null;

        if (userType == Shortcut.ALL_USERS)
        {
            FileExecutor fe = new FileExecutor();

            String[] execOut = new String[2];

            int execResult = fe.executeCommand(new String[] { "/usr/bin/env", "kde-config",
                    "--prefix"}, execOut);

            result = new File(execOut[0].trim());
        }
        else
        {
            result = new File(System.getProperty("user.home") + File.separator + ".kde");
        }

        return result;
    }

    /**
     * overridden method
     * 
     * @return true
     * 
     * @see com.izforge.izpack.util.os.Shortcut#multipleUsers()
     */
    public boolean multipleUsers()
    {
        // EVER true for UNIXes ;-)
        return (true);
    }

    /**
     * Creates and stores the shortcut-files.
     * 
     * @see com.izforge.izpack.util.os.Shortcut#save()
     */
    public void save() throws Exception
    {
        String FS = File.separator;
        String chmod = UnixHelper.getCustomCommand("chmod");
        String chown = UnixHelper.getCustomCommand("chown");
        String rm = UnixHelper.getRmCommand();
        String copy = UnixHelper.getCpCommand();
        String su = UnixHelper.getSuCommand();
        
        String myHome=System.getProperty("user.home");
        
        String target = null;

        String shortCutDef = this.replace();

        boolean rootUser4All = this.getUserType() == Shortcut.ALL_USERS;
        boolean create4All = this.getCreateForAll().booleanValue();
        
        // Create The Desktop Shortcuts
        if ("".equals(this.itsGroupName) && (this.getLinkType() == Shortcut.DESKTOP))
        {
            //System.out.println("this.itsGroupName: "+this.itsGroupName);
            //System.out.println("this.getLinkType(): "+this.getLinkType());
            target = myHome + FS + "Desktop" + FS + this.itsName
                    + DESKTOP_EXT;
            this.itsFileName = target;

            // write my own ShortCut
            File writtenDesktopFile = writeShortCut(target, shortCutDef);

            // If I'm root and this Desktop.ShortCut should be for all other users
            if (rootUser4All && create4All)
            {
                File dest = null;

                // Create a tempFileName of this ShortCut
                File tempFile = File.createTempFile(this.getClass().getName(), Long.toString(System
                        .currentTimeMillis())
                        + ".tmp");

                copyTo(writtenDesktopFile, tempFile);

                //Debug.log("Wrote Tempfile: " + tempFile.toString());               

                FileExecutor.getExecOutput(new String[] { chmod, "uga+rwx", tempFile.toString()});

                // su marc.eppelmann -c "/bin/cp /home/marc.eppelmann/backup.job.out.txt
                // /home/marc.eppelmann/backup.job.out2.txt"
                
                //StringBuffer script = new StringBuffer();
                //

                for (int idx = 0; idx < users.size(); idx++)
                {
                    UnixUser user = ((UnixUser) users.get(idx));
                    
                    if( user.getHome().equals(myHome) )
                    {
                       Debug.log( "need not to copy for itself: " + user.getHome() + "==" + myHome  );
                       continue;
                    }
                    try
                    {
                        // aHomePath = userHomesList[idx];
                        dest = new File(user.getHome() + FS + "Desktop" + FS
                                + writtenDesktopFile.getName());
                        //
                        // I'm root and cannot write into Users Home as root;
                        // But I'm Root and I can slip in every users skin :-)
                        // 
                        // by# su username
                        //
                        // This works as well
                        // su $username -c "cp /tmp/desktopfile $HOME/Desktop/link.desktop"
                        // chown $username $HOME/Desktop/link.desktop
                        
                        //Debug.log("Will Copy: " + tempFile.toString() + " to " + dest.toString());

                        rootScript.append(su);
                        rootScript.append(S);
                        rootScript.append(user.getName());
                        rootScript.append(S);
                        rootScript.append("-c");
                        rootScript.append(S);
                        rootScript.append('"');
                        rootScript.append(copy);
                        rootScript.append(S);
                        rootScript.append(tempFile.toString());
                        rootScript.append(S);
                        rootScript.append(StringTool.replace(dest.toString(), " ", "\\ "));
                        rootScript.appendln('"');
                        
                        rootScript.append('\n');
                        
                        //Debug.log("Will exec: " + script.toString());
                        
                        rootScript.append( chown );
                        rootScript.append( S );
                        rootScript.append( user.getName() );
                        rootScript.append( S );
                        rootScript.appendln( StringTool.replace(dest.toString(), " ", "\\ ") );
                        rootScript.append('\n');
                        rootScript.append('\n');

                        //Debug.log("Will exec: " + script.toString());
                        
                        uninstallScript.append(su);
                        uninstallScript.append(S);
                        uninstallScript.append(user.getName());
                        uninstallScript.append(S);
                        uninstallScript.append("-c");
                        uninstallScript.append(S);
                        uninstallScript.append('"');
                        uninstallScript.append(rm);
                        uninstallScript.append(S);
                        uninstallScript.append(StringTool.replace(dest.toString(), " ", "\\ "));
                        uninstallScript.appendln('"');
                        uninstallScript.appendln();
                        //Debug.log("Uninstall will exec: " + uninstallScript.toString());
                    }
                    catch (Exception rex)
                    {
                        System.out.println("Error while su Copy: " + rex.getLocalizedMessage()+"\n\n");
                        rex.printStackTrace();

                        /* ignore */
                        // most distros does not allow root to access any user
                        // home (ls -la /home/user drwx------)
                        // But try it anyway...
                    }
                }
                

                rootScript.append( rm );
                rootScript.append( S );
                rootScript.appendln( tempFile.toString());   
                rootScript.appendln();
            }
        }

        // This is - or should be only a Link in the [K?]-Menu
        else
        {
            // the following is for backwards compatibility to older versions of KDE!
            // on newer versions of KDE the icons will appear duplicated unless you set
            // the category=""
            Object categoryobject = props.getProperty($Categories);
            if(categoryobject != null && ((String)categoryobject).length()>0)
            {
               File kdeHomeShareApplnk = getKdeShareApplnkFolder(this.getUserType());
               target = kdeHomeShareApplnk.toString() + FS + this.itsGroupName + FS + this.itsName
                    + DESKTOP_EXT;
               this.itsFileName = target;
               File kdemenufile = writeShortCut(target, shortCutDef);
          
               uninstaller.addFile(kdemenufile.toString());
            }
            
            if (rootUser4All && create4All)
            {
                {
                    // write the icon pixmaps into /usr/share/pixmaps

                    File theIcon = new File(this.getIconLocation());
                    File commonIcon = new File("/usr/share/pixmaps/" + theIcon.getName());

                    try
                    {
                        copyTo(theIcon, commonIcon);
                        uninstaller.addFile(commonIcon.toString());
                    }
                    catch (Exception cnc)
                    {
                        Debug.log("Could Not Copy: " + theIcon + " to " + commonIcon + "( "
                                + cnc.getMessage() + " )");
                    }

                    // write *.desktop.file into /usr/share/applications

                    String commonTarget = "/usr/share/applications/" + this.itsName + DESKTOP_EXT;
                    this.itsFileName = target;
                    File writtenFile = writeShortCut(commonTarget, shortCutDef);

                    uninstaller.addFile(writtenFile.toString());

                }
            }
            else // create local XDG shortcuts
            {
               //System.out.println("Creating gnome shortcut");
               String localApps = myHome + "/.local/share/applications/";
               String localPixmaps = myHome + "/.local/share/pixmaps/";
               //System.out.println("Creating "+localApps);
               try
               {
                  java.io.File f = new java.io.File(localApps);
                  f.mkdirs();
                  
                  f = new java.io.File(localPixmaps);
                  f.mkdirs();
               }
               catch (Exception ignore)
               {
                  //System.out.println("Failed creating "+localApps + " or " + localPixmaps);
                  Debug.log("Failed creating "+localApps + " or " + localPixmaps);
               }
               
               // write the icon pixmaps into ~/share/pixmaps

               File theIcon = new File(this.getIconLocation());
               File commonIcon = new File(localPixmaps + theIcon.getName());

               try
               {
                   copyTo(theIcon, commonIcon);
                   uninstaller.addFile(commonIcon.toString());
               }
               catch (Exception cnc)
               {
                   Debug.log("Could Not Copy: " + theIcon + " to " + commonIcon + "( "
                           + cnc.getMessage() + " )");
               }

               // write *.desktop.file into ~/share/applications

               String commonTarget = localApps + this.itsName + DESKTOP_EXT;
               this.itsFileName = target;
               File writtenFile = writeShortCut(commonTarget, shortCutDef);

               uninstaller.addFile(writtenFile.toString());
            }

        }
    }
    
    
    /**
     * Post Exec Action especially for the Unix Root User.
     * which executes the Root ShortCut Shellscript.
     * to copy all ShellScripts to the users Desktop.
     */
    public void execPostAction()
    {
        Debug.log("Call of Impl. execPostAction Method in " + this.getClass().getName() );
        
        String pseudoUnique = this.getClass().getName()+ Long.toString(System.currentTimeMillis());
        
        String scriptFilename = null;
        
        try
        {
            scriptFilename = File.createTempFile( pseudoUnique, ".sh"  ).toString();
        }
        catch (IOException e)
        {
            scriptFilename = System.getProperty("java.io.tmpdir", "/tmp") + "/" + pseudoUnique + ".sh"; 
            e.printStackTrace();
        }
        
        rootScript.write(scriptFilename);
        rootScript.exec();        
        
        Debug.log(rootScript);
        
        
        Debug.log(uninstallScript);
        
        uninstaller.addRootUninstallScript( uninstallScript.getContentAsString() );
    }

    /**
     * Copies the inFile file to outFile using cbuff as buffer.
     * 
     * @param inFile The File to read from.
     * @param outFile The targetFile to write to.
     * 
     * @throws IOException If an IO Error occurs
     */
    public static void copyTo(File inFile, File outFile) throws IOException
    {
        char[] cbuff = new char[32768];
        BufferedReader reader = new BufferedReader(new FileReader(inFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

        int readedBytes = 0;
        long absWrittenBytes = 0;

        while ((readedBytes = reader.read(cbuff, 0, cbuff.length)) != -1)
        {
            writer.write(cbuff, 0, readedBytes);
            absWrittenBytes += readedBytes;
        }

        reader.close();
        writer.close();
    }

    /**
     * Writes the given Shortcutdefinition to the given Target. Returns the written File.
     * 
     * @param target
     * @param shortCutDef
     * 
     * @return the File of the written shortcut.
     */
    private File writeShortCut(String target, String shortCutDef)
    {
        File targetPath = new File(target.substring(0, target.lastIndexOf(File.separatorChar)));

        if (!targetPath.exists())
        {
            targetPath.mkdirs();
            this.createdDirectory = targetPath.toString();
        }

        File targetFileName = new File(target);
        File backupFile = new File(targetPath + File.separator + "." + targetFileName.getName()
                + System.currentTimeMillis());

        if (targetFileName.exists())
        {
            try
            {
                // create a hidden backup.file of the existing shortcut with a timestamp name.
                copyTo(targetFileName, backupFile); // + System.e );
                targetFileName.delete();
            }
            catch (IOException e3)
            {
                System.out.println("cannot create backup file " + backupFile + " of "
                        + targetFileName); // e3.printStackTrace();
            }
        }

        FileWriter fileWriter = null;

        try
        {
            fileWriter = new FileWriter(targetFileName);
        }
        catch (IOException e1)
        {
            System.out.println(e1.getMessage());
        }

        try
        {
            fileWriter.write(shortCutDef);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            fileWriter.close();
        }
        catch (IOException e2)
        {
            e2.printStackTrace();
        }

        return targetFileName;
    }

    /**
     * Set the Commandline Arguments
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setArguments(java.lang.String)
     */
    public void setArguments(String args)
    {
        props.put($Arguments, args);
    }

    /**
     * Sets the Description
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setDescription(java.lang.String)
     */
    public void setDescription(String description)
    {
        props.put($Comment, description);
    }

    /**
     * Sets The Icon Path
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setIconLocation(java.lang.String, int)
     */
    public void setIconLocation(String path, int index)
    {
        props.put($Icon, path);
    }

    /**
     * Sets the Name of this Shortcut
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setLinkName(java.lang.String)
     */
    public void setLinkName(String aName)
    {
        this.itsName = aName;
        props.put($Name, aName);
    }

    /**
     * Sets the type of this Shortcut
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setLinkType(int)
     */
    public void setLinkType(int aType) throws IllegalArgumentException, UnsupportedEncodingException
    {
       ShortcutType = aType;
    }

    /**
     * Sets the ProgramGroup
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setProgramGroup(java.lang.String)
     */
    public void setProgramGroup(String aGroupName)
    {
        this.itsGroupName = aGroupName;
    }

    /**
     * Sets the ShowMode
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setShowCommand(int)
     */
    public void setShowCommand(int show)
    {
    }

    /**
     * Sets The TargetPath
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setTargetPath(java.lang.String)
     */
    public void setTargetPath(String aPath)
    {
        StringTokenizer whiteSpaceTester = new StringTokenizer(aPath);

        if (whiteSpaceTester.countTokens() > 1)
        {
            props.put($E_QUOT, QM);
        }

        props.put($Exec, aPath);
    }

    /**
     * Sets the usertype.
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setUserType(int)
     */
    public void setUserType(int aUserType)
    {
        this.itsUserType = aUserType;
    }

    /**
     * Sets the working-directory
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setWorkingDirectory(java.lang.String)
     */
    public void setWorkingDirectory(String aDirectory)
    {
        StringTokenizer whiteSpaceTester = new StringTokenizer(aDirectory);

        if (whiteSpaceTester.countTokens() > 1)
        {
            props.put($P_QUOT, QM);
        }

        props.put($Path, aDirectory);
    }

    /**
     * Dumps the Name to console.
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return this.itsName + N + template;
    }

    /**
     * Creates the Shortcut String which will be stored as File.
     * 
     * @return contents of the shortcut file
     */
    public String replace()
    {
        String result = template;
        Enumeration enumeration = props.keys();

        while (enumeration.hasMoreElements())
        {
            String key = (String) enumeration.nextElement();

            result = StringTool.replace(result, key, props.getProperty(key));
        }

        return result;
    }

    /**
     * Test Method
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        Unix_Shortcut aSample = new Unix_Shortcut();

        try
        {
            aSample.initialize(APPLICATIONS, "Start Tomcat");
        }
        catch (Exception exc)
        {
            System.err.println("Could not init Unix_Shourtcut");
        }

        aSample.replace();
        System.out.println(aSample);

        File targetFileName = new File(System.getProperty("user.home") + File.separator
                + "Start Tomcat" + DESKTOP_EXT);
        FileWriter fileWriter = null;

        try
        {
            fileWriter = new FileWriter(targetFileName);
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        try
        {
            fileWriter.write(template);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            fileWriter.close();
        }
        catch (IOException e2)
        {
            e2.printStackTrace();
        }
    }

    /**
     * Sets The Encoding
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setEncoding(java.lang.String)
     */
    public void setEncoding(String aEncoding)
    {
        props.put($Encoding, aEncoding);
    }

    /**
     * Sets The KDE Specific subst UID property
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setKdeSubstUID(java.lang.String)
     */
    public void setKdeSubstUID(String trueFalseOrNothing)
    {
        props.put($X_KDE_SubstituteUID, trueFalseOrNothing);
    }

    /**
     * Sets The KDE Specific subst UID property
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setKdeSubstUID(java.lang.String)
     */
    public void setKdeUserName(String aUserName)
    {
        props.put($X_KDE_Username, aUserName);
    }

    /**
     * Sets the MimeType
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setMimetype(java.lang.String)
     */
    public void setMimetype(String aMimetype)
    {
        props.put($MimeType, aMimetype);
    }

    /**
     * Sets the terminal
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setTerminal(java.lang.String)
     */
    public void setTerminal(String trueFalseOrNothing)
    {
        props.put($Terminal, trueFalseOrNothing);
    }

    /**
     * Sets the terminal options
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setTerminalOptions(java.lang.String)
     */
    public void setTerminalOptions(String someTerminalOptions)
    {
        props.put($Options_For_Terminal, someTerminalOptions);
    }

    /**
     * Sets the Shortcut type (one of Application, Link or Device)
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setType(java.lang.String)
     */
    public void setType(String aType)
    {
        props.put($Type, aType);
    }

    /**
     * Sets the Url for type Link. Can be also a apsolute file/path
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setURL(java.lang.String)
     */
    public void setURL(String anUrl)
    {
        props.put($URL, anUrl);
    }

    /**
     * Gets the Usertype of the Shortcut.
     * 
     * @see com.izforge.izpack.util.os.Shortcut#getUserType()
     */
    public int getUserType()
    {
        return itsUserType;
    }

    /**
     * Sets the Categories Field
     * 
     * @param theCategories the categories
     */
    public void setCategories(String theCategories)
    {
        props.put($Categories, theCategories);
    }

    /**
     * Sets the TryExecField.
     * 
     * @param aTryExec the try exec command
     */
    public void setTryExec(String aTryExec)
    {
        props.put($TryExec, aTryExec);
    }
    public int getLinkType()
    {
        return ShortcutType;
        //return Shortcut.DESKTOP;
    }
}
