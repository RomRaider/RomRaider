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

package com.izforge.izpack.util;

import java.io.File;
import java.util.ArrayList;

/**
 * A extended Java Implementation of Pythons string.replace()
 * 
 * @author marc.eppelmann&#064;gmx.de
 */
public class StringTool
{

    // ~ Constructors
    // *********************************************************************************

    /**
     * Default Constructor
     */
    public StringTool()
    {
        super();
    }

    // ~ Methods
    // **************************************************************************************

    /**
     * Standalone callable Test method
     * 
     * @param args Commandline Args
     */
    public static void main(String[] args)
    {
        System.out.println("Test: string.replace(abc$defg,$de ,null ):"
                + StringTool.replace("abc$defg", "$de", null, true));
    }

    /**
     * Replaces <b>from</b> with <b>to</b> in given String: <b>value</b>
     * 
     * @param value original String
     * @param from Search Pattern
     * @param to Replace with this
     * 
     * @return the replaced String
     */
    public static String replace(String value, String from, String to)
    {
        return replace(value, from, to, true);
    }

    /**
     * Replaces <b>from</b> with <b>to</b> in given String: <b>value</b>
     * 
     * @param value original String
     * @param from Search Pattern
     * @param to Replace with this
     * @param aCaseSensitiveFlag set to true be case sensitive.
     * 
     * @return the replaced String
     */
    public static String replace(String value, String from, String to, boolean aCaseSensitiveFlag)
    {
        if ((value == null) || (value.length() == 0) || (from == null) || (from.length() == 0)) { return value; }

        if (to == null)
        {
            to = "";
        }

        if (!aCaseSensitiveFlag)
        {
            from = from.toLowerCase();
        }

        String result = value;
        int lastIndex = 0;
        int index = value.indexOf(from);

        if (index != -1)
        {
            StringBuffer buffer = new StringBuffer();

            while (index != -1)
            {
                buffer.append(value.substring(lastIndex, index)).append(to);
                lastIndex = index + from.length();
                index = value.indexOf(from, lastIndex);
            }

            buffer.append(value.substring(lastIndex));
            result = buffer.toString();
        }

        return result;
    }

    /**
     * Normalizes a Windows or Unix Path.
     * 
     * Reason: Javas File accepts / or \ for Pathes. Batches or ShellScripts does it not!
     * 
     * TODO: implement support for MAC < MacOSX
     * 
     * @param destination
     * @param fileSeparator a target-system fileseparator
     * 
     * @return the normalized path
     */
    public static String normalizePath(String destination, String fileSeparator)
    {
        String FILESEP = (fileSeparator == null) ? File.separator : fileSeparator;

        destination = StringTool.replace(destination, "\\", "/");

        // all occs of "//" by "/"
        destination = StringTool.replace(destination, "//", "/");

        destination = StringTool.replace(destination, ":", ";");
        destination = StringTool.replace(destination, ";", ":");

        destination = StringTool.replace(destination, "/", FILESEP);

        if ("\\".equals(FILESEP))
        {
            destination = StringTool.replace(destination, ":", ";");

            // results in "C;\" instead of "C:\"
            // so correct it:
            destination = StringTool.replace(destination, ";\\", ":\\");
        }

        // Convert the file separator characters
        return (destination);
    }

    /**
     * Normalizes a mixed Windows/Unix Path. Does Only work for Windows or Unix Pathes Reason:
     * Java.File accepts / or \ for Pathes. Batches or ShellScripts does it not!
     * 
     * @param destination accepted mixed form by java.File like "C:/a/mixed\path\accepted/by\Java"
     * 
     * @return the normalized Path
     */
    public static String normalizePath(String destination)
    {
        return (normalizePath(destination, null));
    }

    /**
     * Converts an String Array to a space separated String w/o any check
     * 
     * @param args The StringArray
     * @return the space separated result.
     */
    public static String stringArrayToSpaceSeparatedString(String[] args)
    {
        String result = "";
        for (int idx = 0; idx < args.length; idx++)
        {
            result += args[idx] + " ";
        }
        return result;
    }

    public static String getPlatformEncoding()
    {
        // TODO Auto-generated method stub
        return System.getProperty("file.encoding");
    }

    public static String UTF16()
    {
        return "UTF-16";
    }

    /**
     * Transforms a (Array)List of Strings into a line.separator="\n" separated Stringlist.
     * 
     * @param aStringList
     * 
     * @return a printable list
     */
    public static String stringArrayListToString(ArrayList aStringList)
    {
        return stringArrayListToString(aStringList, null);
    }

    /**
     * Transforms a (Array)List of Strings into an aLineSeparator separated Stringlist.
     * 
     * @param aStringList
     * 
     * @return a printable list
     */
    public static String stringArrayListToString(ArrayList aStringList, String aLineSeparator)
    {
        String LineSeparator = aLineSeparator;
        if (LineSeparator == null) LineSeparator = System.getProperty("line.separator", "\n");

        StringBuffer temp = new StringBuffer();

        for (int idx = 0; idx < aStringList.size(); idx++)
        {
            temp.append(aStringList.get(idx)).append(LineSeparator);
        }

        return temp.toString();
    }

    /**
     * True if a given string starts with the another given String
     * 
     * @param str The String to search in
     * @param prefix The string to search for
     * 
     * @return True if str starts with prefix
     */
    public static boolean startsWith(String str, String prefix)
    {
        return (str != null) && str.startsWith(prefix);
    }

    /**
     * The same as startsWith but ignores the case.
     * 
     * @param str The String to search in
     * @param prefix The string to search for
     * 
     * @return rue if str starts with prefix
     */
    public static boolean startsWithIgnoreCase(String str, String prefix)
    {
        return (str != null) && (prefix != null)
                && str.toUpperCase().startsWith(prefix.toUpperCase());
    }

}
