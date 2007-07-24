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

package com.izforge.izpack.util.os;

import com.coi.tools.os.win.NativeLibException;
import com.izforge.izpack.LocaleDatabase;

/**
 * This class allows it to define error messages for <code>NativeLibException</code> s in the
 * IzPack locale files. The getMessage methode searches in the current langpack for entries which
 * are corresponding to that one which are received from native part. If the langpack do not contain
 * the entry, the resource boundle is used.
 * 
 * @author Klaus Bartz
 * 
 */
public class WrappedNativeLibException extends Exception
{

    private static final long serialVersionUID = 3257562893309720112L;

    /** The packs locale database. */
    protected static LocaleDatabase langpack = null;

    /**
     * Default constructor.
     */
    public WrappedNativeLibException()
    {
        super();
    }

    /**
     * @param message
     */
    public WrappedNativeLibException(String message)
    {
        super(message);
    }

    /**
     * @param cause
     */
    public WrappedNativeLibException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public WrappedNativeLibException(String message, Throwable cause)
    {
        super(message, cause);
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
        boolean ok = false;
        if (getCause() instanceof NativeLibException)
        {
            NativeLibException nle = (NativeLibException) getCause();
            if (langpack != null)
            {
                while (true)
                {
                    if (nle.getLibMessage() != null)
                    {
                        String val = (String) langpack.get("NativeLibException."
                                + nle.getLibMessage());
                        if (val == null) break;
                        retval.append(val);
                        next = true;
                    }
                    else if (nle.getLibErr() != 0)
                    {
                        String val = (String) langpack.get("NativeLibException.libErrNumber."
                                + Integer.toString(nle.getLibErr()));
                        if (val == null) break;
                        if (next) retval.append("\n");
                        next = true;
                        retval.append(val);
                    }
                    if (nle.getOsErr() != 0)
                    {
                        String val = (String) langpack
                                .get("NativeLibException.libInternal.OsErrNumPraefix")
                                + Integer.toString(nle.getOsErr());
                        if (val == null) break;
                        if (next) retval.append("\n");
                        next = true;
                        retval.append(val);
                    }
                    if (nle.getOsMessage() != null)
                    {
                        String val = (String) langpack
                                .get("NativeLibException.libInternal.OsErrStringPraefix")
                                + nle.getOsMessage();
                        if (val == null) break;
                        if (next) retval.append("\n");
                        next = true;
                        retval.append(val);
                    }
                    ok = true;
                    break;
                }
            }
            if (ok && retval.length() > 0)
                return (nle.reviseMsgWithArgs(retval.toString()));
            else
                return (nle.getMessage());

        }
        else
            return (super.getMessage());
    }

    /**
     * Returns the langpack.
     * 
     * @return Returns the langpack.
     */
    public static LocaleDatabase getLangpack()
    {
        return langpack;
    }

    /**
     * Sets the langpack to the given locale database.
     * 
     * @param langpack the langpack to set.
     */
    public static void setLangpack(LocaleDatabase langpack)
    {
        WrappedNativeLibException.langpack = langpack;
    }
}
