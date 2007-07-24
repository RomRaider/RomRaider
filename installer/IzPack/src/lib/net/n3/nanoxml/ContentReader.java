/* ContentReader.java                                              NanoXML/Java
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

import java.io.IOException;
import java.io.Reader;

/**
 * This reader reads data from another reader until a certain string is encountered.
 * 
 * @author Marc De Scheemaecker
 * @version $Name$, $Version$
 */
class ContentReader extends Reader
{

    /**
     * The encapsulated reader.
     */
    private IXMLReader reader;

    /**
     * The encapsulated entityResolver.
     */
    private IXMLEntityResolver entityResolver;

    /**
     * The escape char (&amp; or %).
     */
    private char escapeChar;

    /**
     * The delimiter that will indicate the end of the stream.
     */
    private char[] delimiter;

    /**
     * The characters that have been read too much.
     */
    private String charsReadTooMuch;

    /**
     * The number of characters in the delimiter that stil need to be scanned.
     */
    private int charsToGo;

    /**
     * True if the escape char (&amp; or %) needs to be left untouched.
     */
    private boolean useLowLevelReader;

    /**
     * True if we are passed the initial prefix.
     */
    private boolean pastInitialPrefix;

    /**
     * Creates the reader.
     * 
     * @param reader the encapsulated reader
     * @param entityResolver resolves entities
     * @param escapeChar escape character (&amp; or %)
     * @param delimiter the delimiter, as a backwards string, that will indicate the end of the
     * stream
     * @param useLowLevelReader true if &amp; needs to be left untouched; false if entities need to
     * be processed
     * @param prefix chars that are already read
     */
    ContentReader(IXMLReader reader, IXMLEntityResolver entityResolver, char escapeChar,
            char[] delimiter, boolean useLowLevelReader, String prefix)
    {
        this.delimiter = delimiter;
        this.charsToGo = this.delimiter.length;
        this.charsReadTooMuch = prefix;
        this.useLowLevelReader = useLowLevelReader;
        this.pastInitialPrefix = false;
        this.reader = reader;
        this.entityResolver = entityResolver;
        this.escapeChar = escapeChar;
    }

    /**
     * Cleans up the object when it's destroyed.
     */
    protected void finalize() throws Throwable
    {
        this.reader = null;
        this.entityResolver = null;
        this.delimiter = null;
        this.charsReadTooMuch = null;
        super.finalize();
    }

    /**
     * Reads a block of data.
     * 
     * @param buffer where to put the read data
     * @param offset first position in buffer to put the data
     * @param size maximum number of chars to read
     * 
     * @return the number of chars read, or -1 if at EOF
     * 
     * @throws java.io.IOException if an error occurred reading the data
     */
    public int read(char[] buffer, int offset, int size) throws IOException
    {
        int charsRead = 0;
        boolean isEntity[] = new boolean[1];
        isEntity[0] = false;

        if ((offset + size) > buffer.length)
        {
            size = buffer.length - offset;
        }

        while ((this.charsToGo > 0) && (charsRead < size))
        {
            char ch;

            if (this.charsReadTooMuch.length() > 0)
            {
                ch = this.charsReadTooMuch.charAt(0);
                this.charsReadTooMuch = this.charsReadTooMuch.substring(1);
            }
            else
            {
                this.pastInitialPrefix = true;

                try
                {
                    if (useLowLevelReader)
                    {
                        ch = this.reader.read();
                    }
                    else
                    {
                        ch = XMLUtil.read(this.reader, isEntity, this.escapeChar,
                                this.entityResolver);

                        if (!isEntity[0])
                        {
                            if (ch == '&')
                            {
                                this.reader.startNewStream(XMLUtil.scanEntity(isEntity,
                                        this.reader, this.escapeChar, this.entityResolver));
                                ch = this.reader.read();
                            }
                        }
                    }
                }
                catch (XMLParseException e)
                {
                    throw new RuntimeException(e.getMessage());
                    // necessary to be able to implement Reader
                }
            }

            if (isEntity[0])
            {
                buffer[offset + charsRead] = ch;
                charsRead++;
            }
            else
            {
                if ((ch == (this.delimiter[this.charsToGo - 1])) && pastInitialPrefix)
                {
                    --this.charsToGo;
                }
                else if (this.charsToGo < this.delimiter.length)
                {
                    this.charsReadTooMuch = new String(this.delimiter, this.charsToGo + 1,
                            this.delimiter.length - this.charsToGo)
                            + ch;
                    this.charsToGo = this.delimiter.length;
                    buffer[offset + charsRead] = this.delimiter[this.charsToGo - 1];
                    charsRead++;
                }
                else
                {
                    buffer[offset + charsRead] = ch;
                    charsRead++;
                }
            }
        }

        if (charsRead == 0)
        {
            charsRead = -1;
        }

        return charsRead;
    }

    /**
     * Skips remaining data and closes the stream.
     * 
     * @throws java.io.IOException if an error occurred reading the data
     */
    public void close() throws IOException
    {
        while (this.charsToGo > 0)
        {
            char ch;

            if (this.charsReadTooMuch.length() > 0)
            {
                ch = this.charsReadTooMuch.charAt(0);
                this.charsReadTooMuch = this.charsReadTooMuch.substring(1);
            }
            else
            {
                if (useLowLevelReader)
                {
                    ch = this.reader.read();
                }
                else
                {
                    try
                    {
                        ch = XMLUtil.read(this.reader, null, this.escapeChar, this.entityResolver);
                    }
                    catch (XMLParseException e)
                    {
                        throw new RuntimeException(e.getMessage());
                        // necessary to be able to implement Reader
                    }
                }
            }

            if (ch == (this.delimiter[this.charsToGo - 1]))
            {
                --this.charsToGo;
            }
            else if (this.charsToGo < this.delimiter.length)
            {
                this.charsReadTooMuch = new String(this.delimiter, this.charsToGo + 1,
                        this.delimiter.length - this.charsToGo)
                        + ch;
                this.charsToGo = this.delimiter.length;
            }
        }
    }

}
