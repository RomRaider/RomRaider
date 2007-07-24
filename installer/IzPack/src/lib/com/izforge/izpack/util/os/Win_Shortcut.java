/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Elmar Grom
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

package com.izforge.izpack.util.os;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.StringTool;

/*---------------------------------------------------------------------------*/
/**
 * This is the Microsoft Windows specific implementation of <code>Shortcut</code>.
 * 
 * @version 0.0.1 / 3/4/02
 * @author Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class Win_Shortcut extends Shortcut 
{

    // ------------------------------------------------------------------------
    // Constant Definitions
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Variable Declarations
    // ------------------------------------------------------------------------
    private ShellLink shortcut;
    
    private static String myClass = Win_Shortcut.class.getName() + ": ";
    
    private static final String CLASS = "Class: ";

    /** SUPPORTED = true */
    private static final boolean SUPPORTED = true;

    /*--------------------------------------------------------------------------*/
    /**
     * This method initializes the object. It is used as a replacement for the constructor because
     * of the way it is instantiated through the <code>TargetFactory</code>.
     * 
     * @param type the type or classification of the program group in which the link should exist.
     * The following types are recognized: <br>
     * <ul>
     * <li>{@link com.izforge.izpack.util.os.Shortcut#APPLICATIONS}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#START_MENU}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#DESKTOP}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#START_UP}
     * </ul>
     * @param name the name of the shortcut.
     */
    public void initialize(int type, String name) throws Exception
    {
        Debug.log( CLASS + myClass + ".initialize() '" + Integer.toString(type)+ "', '" + name + "'" );
        switch (type)
        {
        case APPLICATIONS: {
            shortcut = new ShellLink(ShellLink.PROGRAM_MENU, name);
            break;
        }
        case START_MENU: {
            shortcut = new ShellLink(ShellLink.START_MENU, name);
            break;
        }
        case DESKTOP: {
            shortcut = new ShellLink(ShellLink.DESKTOP, name);
            break;
        }
        case START_UP: {
            shortcut = new ShellLink(ShellLink.STARTUP, name);
            break;
        }
        default: {
            shortcut = new ShellLink(ShellLink.PROGRAM_MENU, name);
            break;
        }
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the base path of the shortcut depending on type. The base path is the directory that
     * the short cut, (or its program group) will be created in. For instance, on Windows NT, a
     * shortcut with user-type ALL_USERS, and link-type DESKTOP might have the base path
     * "C:\Program&nbsp;Files\All&nbsp;Users\Desktop"
     * 
     * @see #setLinkType(int)
     * @see #setUserType(int)
     * 
     * translates from ShellLink-UserTypes to Shortcut-UserTypes.
     */
    public String getBasePath() throws Exception
    {
      String result = shortcut.getLinkPath(shortcut.getUserType());
      Debug.log( CLASS + myClass + ".getBasePath() '" + result + "'" );
      return result;
    }

    /**
     * Returns a list of currently existing program groups, based on the requested type. For example
     * if the type is <code>APPLICATIONS</code> then all the names of the program groups in the
     * Start Menu\Programs menu would be returned.
     * 
     * @param userType the type of user for the program group set. (as Shortcut.utype)
     * 
     * @return a <code>Vector</code> of <code>String</code> objects that represent the names of
     * the existing program groups. It is theoretically possible that this list is empty.
     * 
     * @see #APPLICATIONS
     * @see #START_MENU
     */
    public Vector getProgramGroups(int userType)
    {
        int logentry = 0;
        Debug.log( CLASS + myClass + ".getProgramGroups()-" + logentry++ + " '" + Integer.toString(userType) + "'" );
        // ----------------------------------------------------
        // translate the user type
        // ----------------------------------------------------
        int type = ShellLink.CURRENT_USER;

        if (userType == ALL_USERS)
        {
            type = ShellLink.ALL_USERS;
        }
        else
        {
            type = ShellLink.CURRENT_USER;
        }

        // ----------------------------------------------------
        // get a list of all files and directories that are
        // located at the link path.
        // ----------------------------------------------------
        String linkPath = shortcut.getLinkPath(type);
        
        Debug.log( CLASS + myClass + ".getProgramGroups()-" + logentry++ + " '" + linkPath + "'" );

        // in case there is a problem obtaining a path return
        // an empty vector (there are no preexisting program
        // groups)
        if (linkPath == null) { return (new Vector()); }

        File path = new File(linkPath);
        File[] file = path.listFiles();

        // ----------------------------------------------------
        // build a vector that contains only the names of
        // the directories.
        // ----------------------------------------------------
        Vector groups = new Vector();

        if (file != null)
        {
            for (int i = 0; i < file.length; i++)
            {
                File aFile = file[i];
                String aFilename = aFile.getName();
                if (aFile.isDirectory())
                {
                    
                    Debug.log( CLASS + myClass + ".getProgramGroups()-" + logentry++ + " '" + aFilename + "'" );
                    groups.add( aFilename );
                }
                else
                    Debug.log( CLASS + myClass + ".getProgramGroups()-" + logentry++ + " Skip (NoDirectory): '" + aFilename + "'" );  
            }
        }

        return (groups);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the fully qualified file name under which the link is saved on disk. <b>Note: </b>
     * this method returns valid results only if the instance was created from a file on disk or
     * after a successful save operation.
     * 
     * @return the fully qualified file name for the shell link
     */
    public String getFileName()
    {
      String aFilename = shortcut.getFileName();  
      Debug.log( CLASS + myClass + ".getFileName() '" + aFilename + "'" );
      return( aFilename );
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the path of the directory where the link file is stored, if it was necessary during
     * the previous save operation to create the directory. This method returns <code>null</code>
     * if no save operation was carried out or there was no need to create a directory during the
     * previous save operation.
     * 
     * @return the path of the directory where the link file is stored or <code>null</code> if no
     * save operation was carried out or there was no need to create a directory during the previous
     * save operation.
     */
    public String getDirectoryCreated()
    {
      String directoryCreated = shortcut.getDirectoryCreated();
      Debug.log( CLASS + myClass + ".getDirectoryCreated() '" + directoryCreated + "'" );
      return( directoryCreated );
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns <code>true</code> if the target OS supports current user and all users.
     * 
     * @return <code>true</code> if the target OS supports current and all users.
     */
    public boolean multipleUsers()
    {
        boolean result = false;
        // Win NT4 won't have PROGRAMS for CURRENT_USER.
        // Win 98 may not have 'Start Menu\Programs' for ALL_USERS
        String allUsers = shortcut.getallUsersLinkPath();
        
        Debug.log( CLASS + myClass + ".multipleUsers()-1 '" + allUsers + "'" );
        
        String currentUsers = shortcut.getcurrentUserLinkPath();
        Debug.log( CLASS + myClass + ".multipleUsers()-2 '" + currentUsers + "'" );

        if( allUsers == null || currentUsers == null ) 
            result = false;
        else 
          result = allUsers.length() > 0 && currentUsers.length() > 0;

        Debug.log( CLASS + myClass + ".multipleUsers()-3 '" + result + "'" );  
        return (result);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Signals that this flavor of <code>{@link com.izforge.izpack.util.os.Shortcut}</code>
     * supports the creation of shortcuts.
     * 
     * @return always <code>true</code>
     */
    public boolean supported()
    { 
      Debug.log( CLASS + myClass + ".supported() '" + SUPPORTED + "'" );
      return( SUPPORTED );
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets the command line arguments that will be passed to the target when the link is activated.
     * 
     * @param arguments the command line arguments
     */
    public void setArguments(String arguments)
    {
      Debug.log( CLASS + myClass + ".setArguments() '" + arguments + "'" );
      shortcut.setArguments(arguments);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets the description string that is used to identify the link in a menu or on the desktop.
     * 
     * @param description the descriptiojn string
     */
    public void setDescription(String description)
    {
      Debug.log( CLASS + myClass + ".setDescription() '" + description + "'" );
      shortcut.setDescription( description );
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets the location of the icon that is shown for the shortcut on the desktop.
     * 
     * @param path a fully qualified file name of a file that contains the icon.
     * @param index the index of the specific icon to use in the file. If there is only one icon in
     * the file, use an index of 0.
     */
    public void setIconLocation(String path, int index)
    {
        Debug.log( CLASS + myClass + ".setIconLocation() '" + path + "', '" + Integer.toString(index) + "'" );  
      shortcut.setIconLocation(path, index);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * returns icon Location
     * 
     * @return iconLocation
     */
    public String getIconLocation()
    {
      String result = shortcut.getIconLocation();
        Debug.log( CLASS + myClass + ".getIconLocation() '" + result + "'" );
      return result;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets the name of the program group this ShellLinbk should be placed in.
     * 
     * @param groupName the name of the program group
     */
    public void setProgramGroup(String groupName)
    {
        Debug.log( CLASS + myClass + ".setProgramGroup() '" + groupName + "'" );
      shortcut.setProgramGroup(groupName);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets the show command that is passed to the target application when the link is activated.
     * The show command determines if the the window will be restored to the previous size,
     * minimized, maximized or visible at all. <br>
     * <br>
     * <b>Note: </b> <br>
     * Using <code>HIDE</code> will cause the target window not to show at all. There is not even
     * a button on the taskbar. This is a very useful setting when batch files are used to launch a
     * Java application as it will then appear to run just like any native Windows application. <br>
     * 
     * 
     * @param show the show command. Valid settings are: <br>
     * <ul>
     * <li>{@link com.izforge.izpack.util.os.Shortcut#HIDE}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#NORMAL}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#MINIMIZED}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#MAXIMIZED}
     * </ul>
     * 
     * @see #getShowCommand internally maps from Shortcut. to ShellLink.
     */
    public void setShowCommand(int show) throws IllegalArgumentException
    {
        Debug.log( CLASS + myClass + ".setShowCommand() '" + Integer.toString(show) + "'" );
        switch (show)
        {
        case HIDE: {
            shortcut.setShowCommand(ShellLink.MINNOACTIVE);
            break;
        }
        case NORMAL: {
            shortcut.setShowCommand(ShellLink.NORMAL);
            break;
        }
        case MINIMIZED: {
            shortcut.setShowCommand(ShellLink.MINNOACTIVE);
            break;
        }
        case MAXIMIZED: {
            shortcut.setShowCommand(ShellLink.MAXIMIZED);
            break;
        }
        default: {
            throw (new IllegalArgumentException(show + "is not recognized as a show command"));
        }
        }
    }

    /*
     * returns current showCommand. internally maps from ShellLink. to Shortcut.
     *  
     */
    public int getShowCommand()
    {        
        int showCommand = shortcut.getShowCommand();
        
        Debug.log( CLASS + myClass + ".getShowCommand() '" + Integer.toString(showCommand) + "'" );

        switch (showCommand)
        {
        case ShellLink.NORMAL:
            showCommand = NORMAL;
            break;
        // both MINNOACTIVE and MINIMIZED map to Shortcut.MINIMIZED
        case ShellLink.MINNOACTIVE:
        case ShellLink.MINIMIZED:
            showCommand = MINIMIZED;
            break;
        case ShellLink.MAXIMIZED:
            showCommand = MAXIMIZED;
            break;
        default:
            break;
        }

        return showCommand;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets the absolute path to the shortcut target.
     * 
     * @param path the fully qualified file name of the target
     */
    public void setTargetPath(String path)
    {
        Debug.log( CLASS + myClass + ".setTargetPath() '" + path + "'" );
        shortcut.setTargetPath(path);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets the working directory for the link target.
     * 
     * @param dir the working directory
     */
    public void setWorkingDirectory(String dir)
    {
        Debug.log( CLASS + myClass + ".setWorkingDirectory() '" + dir + "'" );
        shortcut.setWorkingDirectory(dir);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Gets the working directory for the link target.
     * 
     * @return the working directory.
     */
    public String getWorkingDirectory()
    {
        String result = shortcut.getWorkingDirectory();
          Debug.log( CLASS + myClass + ".getWorkingDirectory() '" + result + "'" );
        return result;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets the name shown in a menu or on the desktop for the link.
     * 
     * @param name The name that the link should display on a menu or on the desktop. Do not include
     * a file extension.
     */
    public void setLinkName(String name)
    {
        Debug.log( CLASS + myClass + ".setLinkName() '" + name + "'" );
        shortcut.setLinkName(name);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Gets the type of link types are: <br>
     * <ul>
     * <li>{@link com.izforge.izpack.util.os.Shortcut#DESKTOP}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#APPLICATIONS}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#START_MENU}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#START_UP}
     * </ul>
     * maps from ShellLink-types to Shortcut-types.
     */
    public int getLinkType()
    {
        int typ = shortcut.getLinkType();
          Debug.log( CLASS + myClass + ".getLinkType() '" + typ + "'" );
        switch (typ)
        {
        case ShellLink.DESKTOP:
            typ = DESKTOP;
            break;
        case ShellLink.PROGRAM_MENU:
            typ = APPLICATIONS;
            break;
        case ShellLink.START_MENU:
            typ = START_MENU;
            break;
        case ShellLink.STARTUP:
            typ = START_UP;
            break;
        default:
            break;
        }

        return typ;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets the type of link
     * 
     * @param type The type of link desired. The following values can be set: <br>
     * (note APPLICATION on Windows is 'Start Menu\Programs') APPLICATION is a Mac term.
     * <ul>
     * <li>{@link com.izforge.izpack.util.os.Shortcut#DESKTOP}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#APPLICATIONS}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#START_MENU}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#START_UP}
     * </ul>
     * 
     * @exception IllegalArgumentException if an an invalid type is passed
     * @throws UnsupportedEncodingException 
     */
    public void setLinkType(int type) throws IllegalArgumentException, UnsupportedEncodingException
    {
        Debug.log( CLASS + myClass + ".setLinkType() '" + type + "'" );
        switch (type)
        {
        case DESKTOP: {
            shortcut.setLinkType(ShellLink.DESKTOP);
            break;
        }
        case APPLICATIONS: {
            shortcut.setLinkType(ShellLink.PROGRAM_MENU);
            break;
        }
        case START_MENU: {
            shortcut.setLinkType(ShellLink.START_MENU);
            break;
        }
        case START_UP: {
            shortcut.setLinkType(ShellLink.STARTUP);
            break;
        }
        default: {
            throw (new IllegalArgumentException(type + "is not recognized as a valid link type"));
        }
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Gets the user type for the link
     * 
     * @return userType
     * @see #CURRENT_USER
     * @see #ALL_USERS
     */
    public int getUserType()
    {
        int utype = shortcut.getUserType();
        
          Debug.log( CLASS + myClass + ".getUserType() '" + utype + "'" );

        switch (utype)
        {
        case ShellLink.ALL_USERS:
            utype = ALL_USERS;
            break;

        case ShellLink.CURRENT_USER:
            utype = CURRENT_USER;
            break;
        }

        return utype;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets the user type for the link
     * 
     * @param type the type of user for the link.
     * 
     * @see Shortcut#CURRENT_USER
     * @see Shortcut#ALL_USERS
     * 
     * if the linkPath for that type is empty, refuse to set.
     */
    /*--------------------------------------------------------------------------*/
    public void setUserType(int type)
    {
        Debug.log( CLASS + myClass + ".setUserType() '" + type + "'" );
        if (type == CURRENT_USER)
        {
            if (shortcut.getcurrentUserLinkPath().length() > 0)
            {
                shortcut.setUserType(ShellLink.CURRENT_USER);
            }
        }
        else if (type == ALL_USERS)
        {
            if (shortcut.getallUsersLinkPath().length() > 0)
            {
                shortcut.setUserType(ShellLink.ALL_USERS);
            }
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Saves this link.
     * 
     * @exception Exception if problems are encountered
     */
    public void save() throws Exception
    {
        
        shortcut.save();
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Gets the link hotKey
     * 
     * @return int hotKey
     */
    public int getHotkey()
    {
        int result = shortcut.getHotkey();  
        Debug.log( CLASS + myClass + ".getHotkey() '" + result + "'" );
        return result;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets the link hotKey
     * 
     * @param hotkey
     * 
     * incoming 2 byte hotkey is: high byte modifier: SHIFT = 0x01 CONTROL= 0x02 ALT = 0x04 EXT =
     * 0x08
     * 
     * lower byte contains ascii letter. ie 0x0278 represents CTRL+x 0x068a represents CTRL+ALT+z
     */
    public void setHotkey(int hotkey)
    {
        Debug.log( CLASS + myClass + ".setHotkey() '" + hotkey + "'" );
        shortcut.setHotkey(hotkey);
    }

    /**
     * Gets the Folders where to place the program-groups and their shortcuts, for the given
     * usertype.
     * @see com.izforge.izpack.util.os.Shortcut#getProgramsFolder(int)
     */
    public String getProgramsFolder(int current_user) 
    {
        /** CURRENT_USER = 0; the constant to use for selecting the current user. */
        int USER = 0;
        
        if( current_user == Shortcut.CURRENT_USER )
            USER = ShellLink.CURRENT_USER;
        
        else if( current_user == Shortcut.ALL_USERS )
            USER = ShellLink.ALL_USERS;       
        
        String result = null;
        try
        {
            result = new String( shortcut.getLinkPath(USER).getBytes( StringTool.getPlatformEncoding() ), StringTool.getPlatformEncoding() );
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Debug.log( CLASS + myClass + ".getProgramsFolder() '" + current_user + "', '" + result + "'" );
        return result; 
    }
}
/*---------------------------------------------------------------------------*/

