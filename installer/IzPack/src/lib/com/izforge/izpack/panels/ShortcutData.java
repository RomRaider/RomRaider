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

package com.izforge.izpack.panels;

/*---------------------------------------------------------------------------*/
/**
 * This class serves as a data structure in
 * <code>{@link com.izforge.izpack.panels.ShortcutPanel}</code>
 * 
 * @version 0.0.1 / 4/1/02
 * @author Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class ShortcutData implements Cloneable
{

    public String name;

    public String description;

    public String target;

    public String commandLine;

    public int type;

    public int userType;

    public boolean addToGroup = false;

    public String subgroup;

    public String iconFile;

    public int iconIndex;

    public int initialState;

    public String workingDirectory;

    public String deskTopEntryLinux_MimeType;

    public String deskTopEntryLinux_Terminal;

    public String deskTopEntryLinux_TerminalOptions;

    public String deskTopEntryLinux_Type;

    public String deskTopEntryLinux_URL;

    public String deskTopEntryLinux_Encoding;

    public String deskTopEntryLinux_X_KDE_SubstituteUID;
    
    public String deskTopEntryLinux_X_KDE_UserName;
    
    /** Linux Common Menu Categories */
    public String Categories ;
    
    /** Linux Common Menu TryExec */
    public String TryExec;

    public Boolean createForAll;
    
     

    /*--------------------------------------------------------------------------*/
    /**
     * Returns a clone (copy) of this object.
     * 
     * @return a copy of this object
     * @throws OutOfMemoryError
     */
    /*--------------------------------------------------------------------------*/
    public Object clone() throws OutOfMemoryError
    {
        ShortcutData result = new ShortcutData();

        result.type = type;
        result.userType = userType;
        result.iconIndex = iconIndex;
        result.initialState = initialState;
        result.addToGroup = addToGroup;

        result.name = cloneString(name);
        result.description = cloneString(description);
        result.target = cloneString(target);
        result.commandLine = cloneString(commandLine);
        result.subgroup = cloneString(subgroup);
        result.iconFile = cloneString(iconFile);
        result.workingDirectory = cloneString(workingDirectory);
        result.deskTopEntryLinux_MimeType = cloneString(deskTopEntryLinux_MimeType);
        result.deskTopEntryLinux_Terminal = cloneString(deskTopEntryLinux_Terminal);
        result.deskTopEntryLinux_TerminalOptions = cloneString(deskTopEntryLinux_TerminalOptions);
        result.deskTopEntryLinux_Type = cloneString(deskTopEntryLinux_Type);
        result.deskTopEntryLinux_URL = cloneString(deskTopEntryLinux_URL);
        result.deskTopEntryLinux_Encoding = cloneString(deskTopEntryLinux_Encoding);
        result.deskTopEntryLinux_X_KDE_SubstituteUID = cloneString(deskTopEntryLinux_X_KDE_SubstituteUID);
        result.deskTopEntryLinux_X_KDE_UserName = cloneString(deskTopEntryLinux_X_KDE_UserName);
        
        result.Categories = cloneString(Categories);
        result.TryExec = cloneString(TryExec);
        
        result.createForAll = Boolean.valueOf(createForAll.booleanValue());
        return (result);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Clones a <code>String</code>, that is it makes a copy of the content, not of the
     * reference. In addition, if the original is <code>null</code> then an empty
     * <code>String</code> is returned rather than <code>null</code>.
     * 
     * @param original the <code>String</code> to clone
     * 
     * @return a clone of the original
     */
    /*--------------------------------------------------------------------------*/
    private String cloneString(String original)
    {
        if (original == null)
        {
            return ("");
        }
        else
        {
            return (original);
        }
    }
}
/*---------------------------------------------------------------------------*/

