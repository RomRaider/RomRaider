/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2005 Klaus Bartz
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

package com.coi.tools.os.win;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A exception class which will be used from the native part of system dependent classes to signal
 * exceptions. The native methods writes only symbolic error messages, the language dependant
 * mapping will be done in this class.
 * 
 * @author Klaus Bartz
 * 
 */
public class NativeLibException extends Exception
{

    private static final long serialVersionUID = 3257002172494721080L;

    /** Map of founded resource bundles which contains the localized error messages. */
    private final static HashMap messageResourceBundles = new HashMap();

    /** Internal error as number. */
    private int libErr;

    /** OS error as number. */
    private int osErr;

    /** Internal error message; contains most the symbolic error name. */
    private String libErrString;

    /** OS error string; if possible localized. */
    private String osErrString;

    /** Additional arguments. */
    private ArrayList args = new ArrayList();

    static
    {
        // add the first resource bundle
        addResourceBundle("com.coi.tools.os.win.resources.NativeLibErr");
    }

    /**
     * Adds a resource bundle which contains localized error messages. The bundlePath should contain
     * a string with which the bundle is loadable with ResourceBundle.getBundle, may be the full
     * class path to a ListResourceBundle. The localize is done by getBundle, therefore the path
     * should not contain the locale substring. At a call to getMessage the bundle is searched with
     * the libErrString as key. If it exist, the value of it is used by getMessage, else the
     * libErrString self.
     * 
     * @param bundlePath path of bundle without locale
     */
    public static void addResourceBundle(String bundlePath)
    {
        ResourceBundle bd = null;
        if (messageResourceBundles.containsKey(bundlePath)) return;
        try
        {
            bd = ResourceBundle.getBundle(bundlePath);
        }
        catch (MissingResourceException mre)
        {
            mre.printStackTrace();
        }
        messageResourceBundles.put(bundlePath, bd);

    }

    /**
     * The constructor.
     */
    public NativeLibException()
    {
        super();
    }

    /**
     * Creates a NativeLibException with the given message.
     * 
     * @param message to be used
     */
    public NativeLibException(String message)
    {
        super(message);
    }

    /**
     * Creates a NativeLibException with the given cause.
     * 
     * @param cause to be used
     */
    public NativeLibException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a NativeLibException with the given message and cause.
     * 
     * @param message message to be used
     * @param cause cause to be used
     */
    public NativeLibException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Creates a NativeLibException with the given values.
     * 
     * @param libErr identifier of the internal handled error
     * @param osErr system error number
     * @param libString message for the internal handled error
     * @param osString system error message
     */
    public NativeLibException(int libErr, int osErr, String libString, String osString)
    {
        super();
        this.libErr = libErr;
        this.osErr = osErr;
        libErrString = libString;
        osErrString = osString;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Throwable#getMessage()
     */
    public String getMessage()
    {
        StringBuffer retval = new StringBuffer();
        boolean next = false;
        if (libErrString != null)
        {
            retval.append(getLocalizedLibMessage());
            next = true;
        }
        else if (libErr != 0)
        {
            if (next) retval.append("\n");
            next = true;
            retval.append(getMsg("libErrNumber." + Integer.toString(libErr)));
        }
        if (osErr != 0)
        {
            if (next) retval.append("\n");
            next = true;
            retval.append(getMsg("libInternal.OsErrNumPraefix")).append(Integer.toString(osErr));
        }
        if (osErrString != null)
        {
            if (next) retval.append("\n");
            next = true;
            // Message self should be localized in the native part
            retval.append(getMsg("libInternal.OsErrStringPraefix")).append(getOsMessage());
        }
        if (retval.length() > 0) return (reviseMsgWithArgs(retval.toString()));
        return null;
    }

    /**
     * Returns the number of the internal handled error.
     * 
     * @return the number of the internal handled error
     */
    public int getLibErr()
    {
        return libErr;
    }

    /**
     * Returns the message of the internal handled error.
     * 
     * @return the messager of the internal handled error
     */
    public String getLibMessage()
    {
        return libErrString;
    }

    /**
     * Returns the localized message of the internal handled error.
     * 
     * @return the localized message of the internal handled error
     */
    public String getLocalizedLibMessage()
    {
        return (getMsg(libErrString));
    }

    /**
     * Returns the number of the system error.
     * 
     * @return the number of the system error
     */
    public int getOsErr()
    {
        return (osErr);
    }

    /**
     * Returns the message of the system error.
     * 
     * @return the messager of the system error
     */
    public String getOsMessage()
    {
        return (osErrString);
    }

    /**
     * Adds a string to the internal argument list.
     * 
     * @param arg string to be added to the internal argument list
     */
    public void addArgument(String arg)
    {
        args.add(arg);
    }

    /**
     * Returns the internal argument list.
     * 
     * @return the internal argument list
     */
    public ArrayList getArguments()
    {
        return (args);
    }

    /**
     * Revise placeholder in the given message with the setted arguments 
     * @param msg message to be revised
     * @return revised message 
     */
    public String reviseMsgWithArgs(String msg)
    {
        for (int i = 0; i < args.size(); ++i)
        {
            String key = "{" + Integer.toString(i) + "}";
            msg = replaceString(msg, key, (String) args.get(i));
        }
        return (msg);
    }

    /**
     * Searches the resource bundles for a string which coresponds to the given string as key.
     * 
     * @param s string which should be used as keys for the resource bundle
     * @return the founded message as int value
     */

    private String getMsg(String s)
    {
        Iterator it = messageResourceBundles.values().iterator();
        while (it.hasNext())
        {
            try
            {
                return (((ResourceBundle) it.next()).getString(s));
            }
            catch (MissingResourceException missingresourceexception)
            { // do not throw, else look in next bundle.
                ;
            }
        }
        return (s);
    }

    /**
     * Returns a string resulting from replacing all occurrences of what in this string with with.
     * In opposite to the String.replaceAll method this method do not use regular expression or
     * other methods which are only available in JRE 1.4 and later.
     * 
     * @param destination string for which the replacing should be performed
     * @param what what string should be replaced
     * @param with with what string what should be replaced
     * @return a new String object if what was found in the given string, else the given string self
     */
    private static String replaceString(String destination, String what, String with)
    {
        if (destination.indexOf(what) >= 0)
        { // what found, with (placeholder) not included in destination ->
            // perform changing.
            StringBuffer buf = new StringBuffer();
            int last = 0;
            int current = destination.indexOf(what);
            int whatLength = what.length();
            while (current >= 0)
            { // Do not use Methods from JRE 1.4 and higher ...
                if (current > 0) buf.append(destination.substring(last, current));
                buf.append(with);
                last = current + whatLength;
                current = destination.indexOf(what, last);
            }
            if (destination.length() > last) buf.append(destination.substring(last));
            return buf.toString();
        }
        return destination;
    }

}
