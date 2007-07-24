/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2006 Marc Eppelmann&#064;reddot.de
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
package com.izforge.izpack.util.os.unix;

import com.izforge.izpack.util.StringTool;

import java.io.File;

import java.util.ArrayList;

/**
 * Unix Users Collection Class and related static Helper Methods
 * 
 * @author marc.eppelmann&#064;reddot.de
 */
public class UnixUsers extends ArrayList
{

    // ~ Static fields/initializers *********************************************************
    
    /** serialVersionUID = -4804842346742194981L; */
    private static final long serialVersionUID = -4804842346742194981L;

    // ~ Constructors ***********************************************************************
    
    /**
     * Creates a new UnixUsers object.
     */
    public UnixUsers()
    {
        fromUsersArrayList(getEtcPasswdUsersAsArrayList());
        fromUsersArrayList(getYpPasswdUsersAsArrayList());
    }

    // ~ Methods ****************************************************************************
    /**
     * Gets all known users with valid shells
     * 
     * @return an UnixUsers arraylist of these users
     */
    public ArrayList getUsersWithValidShells()
    {
        ArrayList result = new ArrayList();

        for (int idx = 0; idx < size(); idx++)
        {
            UnixUser user = (UnixUser) get(idx);

            if ((user.getShell() != null) && user.getShell().trim().endsWith("sh"))
            {
                result.add(user);
            }
        }

        return result;
    }

    /**
     * Gets all known users with valid shells and really existing (not dummy) Homefolders.
     * 
     * @return an UnixUsers Arraylist of these users
     */
    public ArrayList getUsersWithValidShellsAndExistingHomes()
    {
        ArrayList result = new ArrayList();

        ArrayList usersWithValidShells = getUsersWithValidShells();

        for (int idx = 0; idx < usersWithValidShells.size(); idx++)
        {
            UnixUser user = (UnixUser) usersWithValidShells.get(idx);

            if ((user.getHome() != null) && new File(user.getHome().trim()).exists())
            {
                result.add(user);
            }
        }

        return result;
    }

    /**
     * Gets all known users with valid shells and really existing (not dummy) Home And!
     * freedesktop.org/RFC-based "Desktop" folders.
     * 
     * @return an UnixUsers Arraylist of these users
     */
    public ArrayList _getUsersWithValidShellsExistingHomesAndDesktops()
    {
        ArrayList result = new ArrayList();

        ArrayList usersWithValidShellsAndExistingHomes = getUsersWithValidShellsAndExistingHomes();

        for (int idx = 0; idx < usersWithValidShellsAndExistingHomes.size(); idx++)
        {
            UnixUser user = (UnixUser) usersWithValidShellsAndExistingHomes.get(idx);

            if ((user.getHome() != null)
                    && new File(user.getHome().trim() + File.separator + "Desktop").exists())
            {
                result.add(user);
            }
        }

        return result;
    }

    /**
     * An StringArray of the existing Desktop folders of all valid users.
     * 
     * @return the Stringlist of ValidUsersDesktopFolders
     */
    public ArrayList getValidUsersDesktopFolders()
    {
        ArrayList result = new ArrayList();

        ArrayList validUserDesktops = getUsersWithValidShellsExistingHomesAndDesktops();

        for (int idx = 0; idx < validUserDesktops.size(); idx++)
        {
            UnixUser user = (UnixUser) validUserDesktops.get(idx);
            new File(user.getHome().trim() + File.separator + "Desktop");

            if (user.getHome() != null)
            {
                File DesktopFolder = new File(user.getHome().trim() + File.separator + "Desktop");

                if (DesktopFolder.exists() && DesktopFolder.isDirectory())
                {
                    result.add(DesktopFolder.toString());
                }
            }
        }

        return result;
    }

    /**
     * Gets all known users with valid shells and really existing (not dummy) Home And!
     * freedesktop.org/RFC-based "Desktop" folders.
     * 
     * @return an UnixUsers Arraylist of these users
     */
    public static ArrayList getUsersWithValidShellsExistingHomesAndDesktops()
    {
        UnixUsers users = new UnixUsers();

        return users._getUsersWithValidShellsExistingHomesAndDesktops();
    }

    /**
     * Builds the internal Array from the given UsersArrayList
     * 
     * @param anUsersArrayList an Users ArrayList reded from /etc/passwd
     */
    private void fromUsersArrayList(ArrayList anUsersArrayList)
    {
        for (int idx = 0; idx < anUsersArrayList.size(); idx++)
        {
            add(new UnixUser().fromEtcPasswdLine((String) anUsersArrayList.get(idx)));
        }
    }

    /**
     * Gets all Users from /etc/passwd as StringList
     * 
     * @return the UserNames extracted from the getEtcPasswdArray()
     */
    public static ArrayList getEtcPasswdUsersAsArrayList()
    {
        ArrayList result = new ArrayList();
        ArrayList etcPasswdArray = UnixHelper.getEtcPasswdArray();

        for (int idx = 0; idx < etcPasswdArray.size(); idx++)
        {
            String line = (String) etcPasswdArray.get(idx);
            result.add(line);
        }

        return result;
    }

    /**
     * Gets all Users from /etc/passwd as StringList
     * 
     * @return the UserNames extracted from the getEtcPasswdArray()
     */
    public static ArrayList getYpPasswdUsersAsArrayList()
    {
        return UnixHelper.getYpPasswdArray();
    }

    /**
     * Returns all Users as ColonSeparated String
     * 
     * @return "asterisk:at:avahi:beagleindex:bin:daemon:dhcpd:ftp:games:gdm:haldaemon:icecream:irc:ldap:lp:mail:mailman:man:...."
     */
    public static String getUsersColonString()
    {
        ArrayList usersArrayList = getEtcPasswdUsersAsArrayList();

        String retValue = "";

        for (int user = 0; user < usersArrayList.size(); user++)
        {
            String userline = (String) usersArrayList.get(user);
            retValue += (userline.substring(0, userline.indexOf(":")) + ":");
        }

        if (retValue.endsWith(":"))
        {
            retValue = retValue.substring(0, retValue.length() - 1);
        }

        return retValue;
    }

    /**
     * Test main Method
     * 
     * @param args from Commandline
     */
    public static void main(String[] args)
    {
        System.out.println("UnixUsers:");

        UnixUsers users = new UnixUsers();

        // users.fromUsersArrayList();
        for (int idx = 0; idx < users.size(); idx++)
        {
            System.out.println(((UnixUser) users.get(idx)).getName());
        }

        System.out.println(StringTool
                .stringArrayListToString(getUsersWithValidShellsExistingHomesAndDesktops()));

        // getUsersWithValidShellsAndExistingHomes();
    }
}
