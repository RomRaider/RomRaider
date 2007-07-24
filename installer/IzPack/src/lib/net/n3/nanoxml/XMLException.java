/* XMLException.java                                               NanoXML/Java
 *
 * $Revision: 1421 $
 * $Date: 2006-03-12 17:32:32 +0100 (Sun, 12 Mar 2006) $
 * $Name$
 *
 * This file is part of NanoXML 2 for Java.
 * Copyright (C) 2001 Marc De Scheemaecker, All Rights Reserved.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *  1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software in
 *     a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *  2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 *
 *  3. This notice may not be removed or altered from any source distribution.
 */

package net.n3.nanoxml;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * An XMLException is thrown when an exception occurred while processing the XML data.
 * 
 * @author Marc De Scheemaecker
 * @version $Name$, $Revision: 1421 $
 */
public class XMLException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 3256727298916299056L;

    /**
     * The system ID of the XML data where the exception occurred.
     */
    private String systemID;

    /**
     * The line number in the XML data where the exception occurred.
     */
    private int lineNr;

    /**
     * Encapsulated exception.
     */
    private Exception encapsulatedException;

    /**
     * Creates a new exception.
     * 
     * @param msg the message of the exception.
     */
    public XMLException(String msg)
    {
        this(null, -1, null, msg, false);
    }

    /**
     * Creates a new exception.
     * 
     * @param e the encapsulated exception.
     */
    public XMLException(Exception e)
    {
        this(null, -1, e, e.getMessage(), false);
    }

    /**
     * Creates a new exception.
     * 
     * @param systemID the system ID of the XML data where the exception occurred
     * @param lineNr the line number in the XML data where the exception occurred.
     * @param e the encapsulated exception.
     */
    public XMLException(String systemID, int lineNr, Exception e)
    {
        this(systemID, lineNr, e, "Nested Exception", true);
    }

    /**
     * Creates a new exception.
     * 
     * @param systemID the system ID of the XML data where the exception occurred
     * @param lineNr the line number in the XML data where the exception occurred.
     * @param msg the message of the exception.
     */
    public XMLException(String systemID, int lineNr, String msg)
    {
        this(systemID, lineNr, null, msg, true);
    }

    /**
     * Creates a new exception.
     * 
     * @param systemID the system ID from where the data came
     * @param lineNr the line number in the XML data where the exception occurred.
     * @param e the encapsulated exception.
     * @param msg the message of the exception.
     * @param reportParams true if the systemID, lineNr and e params need to be appended to the
     * message
     */
    public XMLException(String systemID, int lineNr, Exception e, String msg, boolean reportParams)
    {
        super(msg
                + (reportParams ? (((systemID == null) ? "" : (", SystemID='" + systemID + "'"))
                        + ((lineNr == -1) ? "" : (", Line=" + lineNr)) + ((e == null) ? ""
                        : (", Exception: " + e))) : ""));
        this.systemID = systemID;
        this.lineNr = lineNr;
        this.encapsulatedException = e;
    }

    /**
     * Cleans up the object when it's destroyed.
     */
    protected void finalize() throws Throwable
    {
        this.systemID = null;
        this.encapsulatedException = null;
        super.finalize();
    }

    /**
     * Returns the system ID of the XML data where the exception occurred. If there is no system ID
     * known, null is returned.
     */
    public String getSystemID()
    {
        return this.systemID;
    }

    /**
     * Returns the line number in the XML data where the exception occurred. If there is no line
     * number known, -1 is returned.
     */
    public int getLineNr()
    {
        return this.lineNr;
    }

    /**
     * Returns the encapsulated exception, or null if no exception is encapsulated.
     */
    public Exception getException()
    {
        return this.encapsulatedException;
    }

    /**
     * Dumps the exception stack to a print writer.
     * 
     * @param writer the print writer
     */
    public void printStackTrace(PrintWriter writer)
    {
        if (this.encapsulatedException != null)
        {
            this.encapsulatedException.printStackTrace(writer);
        }

        super.printStackTrace(writer);
    }

    /**
     * Dumps the exception stack to an output stream.
     * 
     * @param stream the output stream
     */
    public void printStackTrace(PrintStream stream)
    {
        if (this.encapsulatedException != null)
        {
            this.encapsulatedException.printStackTrace(stream);
        }

        super.printStackTrace(stream);
    }

    /**
     * Dumps the exception stack to System.err.
     */
    public void printStackTrace()
    {
        if (this.encapsulatedException != null)
        {
            this.encapsulatedException.printStackTrace();
        }

        super.printStackTrace();
    }

}
