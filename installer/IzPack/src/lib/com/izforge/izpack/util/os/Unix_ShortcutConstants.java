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

package com.izforge.izpack.util.os;

/**
 * Unix_ShortcutConstants
 * 
 * This is the Interface which holds only the Constants for Unix_Shortcut-Placeholders.
 * 
 * One Dollar marks simple placeholders. Two Dollars marks localized placeholders.
 * 
 * @author marc.eppelmann&#064;reddot.de
 */
public interface Unix_ShortcutConstants
{

    /** $Comment = "$Comment" * */
    public final static String $Comment = "$Comment";

    /** $$LANG_Comment = "$$LANG_Comment" * */
    public final static String $$LANG_Comment = "$$LANG_Comment";

    /** $Encoding = "$Encoding" */
    public final static String $Encoding = "$Encoding";

    /** $Exec = "$Exec" */
    public final static String $Exec = "$Exec";

    /** $Arguments = "$Arguments" */
    public final static String $Arguments = "$Arguments";

    /** $GenericName = "$GenericName" */
    public final static String $GenericName = "$GenericName";

    /** $$LANG_GenericName = "$$LANG_GenericName" */
    public final static String $$LANG_GenericName = "$$LANG_GenericName";

    /** $MimeType = "$MimeType" */
    public final static String $MimeType = "$MimeType";

    /** $Name = "$Name" */
    public final static String $Name = "$Name";

    /** $$LANG_Name = "$$LANG_Name" */
    public final static String $$LANG_Name = "$$LANG_Name";

    /** $Path = "$Path" */
    public final static String $Path = "$Path";

    /** $ServiceTypes = "$ServiceTypes" */
    public final static String $ServiceTypes = "$ServiceTypes";

    /** $SwallowExec = "$SwallowExec" */
    public final static String $SwallowExec = "$SwallowExec";

    /** $SwallowTitle = "$SwallowTitle" */
    public final static String $SwallowTitle = "$SwallowTitle";

    /** $Terminal = "$Terminal" */
    public final static String $Terminal = "$Terminal";

    /** $Options_For_Terminal = "$Options_For_Terminal" */
    public final static String $Options_For_Terminal = "$Options_For_Terminal";

    /** $Type = "$Type" */
    public final static String $Type = "$Type";

    /** $X_KDE_SubstituteUID = "$X_KDE_SubstituteUID" */
    public final static String $X_KDE_SubstituteUID = "$X_KDE_SubstituteUID";
    
    /** $X_KDE_Username = "$X_KDE_Username" */
    public final static String $X_KDE_Username = "$X_KDE_Username";

    /** $Icon = "$Icon" */
    public final static String $Icon = "$Icon";

    /** $URL = "$URL" */
    public final static String $URL = "$URL";
    
    /** $E_QUOT = "$E_QUOT": QuotationMark-Placeholder for the "<b>E</b>xec"-line */
    public final static String $E_QUOT = "$E_QUOT";
    
    /** $P_QUOT = "$P_QUOT" QuotationMark-Placeholder for the "<b>P</b>ath/workingDir"-line */
    public final static String $P_QUOT = "$P_QUOT";

    /** $Categories = "$Categories" */
    public final static String $Categories = "$Categories";
    
    /** $TryExec = "$TryExec" */
    public final static String $TryExec = "$TryExec";
}
