/* XMLUtil.java                                                    NanoXML/Java
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

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Utility methods for NanoXML.
 * 
 * @author Marc De Scheemaecker
 * @version $Name$, $Revision: 1421 $
 */
class XMLUtil
{

    /**
     * Skips the remainder of a comment. It is assumed that &lt;!- is already read.
     * 
     * @param reader the reader
     * @param entityResolver the entity resolver
     * 
     * @throws java.io.IOException if an error occurred reading the data
     */
    static void skipComment(IXMLReader reader, IXMLEntityResolver entityResolver)
            throws IOException, XMLParseException
    {
        if (reader.read() != '-')
        {
            XMLUtil.skipTag(reader, '\0', entityResolver);
            return;
        }

        int dashesRead = 0;

        for (;;)
        {
            char ch = reader.read();

            switch (ch)
            {
            case '-':
                dashesRead++;
                break;

            case '>':
                if (dashesRead == 2) { return; }

            default:
                dashesRead = 0;
            }
        }
    }

    /**
     * Skips the remainder of the current XML tag.
     * 
     * @param reader the reader
     * @param escapeChar the escape character (&amp; or %)
     * @param entityResolver the entity resolver
     * 
     * @throws java.io.IOException if an error occurred reading the data
     */
    static void skipTag(IXMLReader reader, char escapeChar, IXMLEntityResolver entityResolver)
            throws IOException, XMLParseException
    {
        int level = 1;

        while (level > 0)
        {
            char ch = XMLUtil.read(reader, null, escapeChar, entityResolver);

            switch (ch)
            {
            case '<':
                ++level;
                break;

            case '>':
                --level;
                break;
            }
        }
    }

    /**
     * Scans a public ID.
     * 
     * @param publicID will contain the public ID
     * @param reader the reader
     * @param escapeChar the escape character (&amp; or %)
     * @param entityResolver the entity resolver
     * 
     * @return the system ID
     * 
     * @throws java.io.IOException if an error occurred reading the data
     */
    static String scanPublicID(StringBuffer publicID, IXMLReader reader, char escapeChar,
            IXMLEntityResolver entityResolver) throws IOException, XMLParseException
    {
        if (!XMLUtil.checkLiteral(reader, escapeChar, entityResolver, "UBLIC")) { return null; }

        XMLUtil.skipWhitespace(reader, escapeChar, null, null);
        publicID.append(XMLUtil.scanString(reader, escapeChar, false, entityResolver));
        XMLUtil.skipWhitespace(reader, escapeChar, null, null);
        return XMLUtil.scanString(reader, escapeChar, false, entityResolver);
    }

    /**
     * Scans a system ID.
     * 
     * @param reader the reader
     * @param escapeChar the escape character (&amp; or %)
     * @param entityResolver the entity resolver
     * 
     * @return the system ID
     * 
     * @throws java.io.IOException if an error occurred reading the data
     */
    static String scanSystemID(IXMLReader reader, char escapeChar, IXMLEntityResolver entityResolver)
            throws IOException, XMLParseException
    {
        if (!XMLUtil.checkLiteral(reader, escapeChar, entityResolver, "YSTEM")) { return null; }

        XMLUtil.skipWhitespace(reader, escapeChar, null, null);
        return XMLUtil.scanString(reader, escapeChar, false, entityResolver);
    }

    /**
     * Retrieves an identifier from the data.
     * 
     * @param reader the reader
     * @param escapeChar the escape character (&amp; or %)
     * @param entityResolver the entity resolver
     * 
     * @throws java.io.IOException if an error occurred reading the data
     */
    static String scanIdentifier(IXMLReader reader, char escapeChar,
            IXMLEntityResolver entityResolver) throws IOException, XMLParseException
    {
        StringBuffer result = new StringBuffer();

        for (;;)
        {
            char ch = XMLUtil.read(reader, null, escapeChar, entityResolver);

            if ((ch == '_') || (ch == ':') || (ch == '-') || (ch == '.')
                    || ((ch >= 'a') && (ch <= 'z')) || ((ch >= 'A') && (ch <= 'Z'))
                    || ((ch >= '0') && (ch <= '9')) || (ch > '\u007E'))
            {
                result.append(ch);
            }
            else
            {
                reader.unread(ch);
                break;
            }
        }

        return result.toString();
    }

    /**
     * Retrieves a delimited string from the data.
     * 
     * @param reader the reader
     * @param escapeChar the escape character (&amp; or %)
     * @param normalizeWhitespace if all whitespace chars need to be converted to spaces
     * @param entityResolver the entity resolver
     * 
     * @throws java.io.IOException if an error occurred reading the data
     */
    static String scanString(IXMLReader reader, char escapeChar, boolean normalizeWhitespace,
            IXMLEntityResolver entityResolver) throws IOException, XMLParseException
    {
        StringBuffer result = new StringBuffer();
        boolean isEntity[] = new boolean[1];
        char delim = XMLUtil.read(reader, null, escapeChar, entityResolver);

        if ((delim != '\'') && (delim != '"'))
        {
            XMLUtil
                    .errorExpectedInput(reader.getSystemID(), reader.getLineNr(),
                            "delimited string");
        }

        for (;;)
        {
            char ch = XMLUtil.read(reader, isEntity, escapeChar, entityResolver);

            if ((!isEntity[0]) && (ch == escapeChar))
            {
                reader.startNewStream(XMLUtil.scanEntity(isEntity, reader, escapeChar,
                        entityResolver));
                ch = reader.read();
            }

            if ((!isEntity[0]) && (ch == delim))
            {
                break;
            }
            else if (normalizeWhitespace && (ch < ' '))
            {
                result.append(' ');
            }
            else
            {
                result.append(ch);
            }
        }

        return result.toString();
    }

    /**
     * Processes an entity.
     * 
     * @param isCharLiteral will contain true if the entity is a char literal
     * @param reader the reader
     * @param escapeChar the escape character (&amp; or %)
     * @param entityResolver the entity resolver
     * 
     * @return a reader from which the entity value can be retrieved
     * 
     * @throws java.io.IOException if an error occurred reading the data
     */
    static Reader scanEntity(boolean[] isCharLiteral, IXMLReader reader, char escapeChar,
            IXMLEntityResolver entityResolver) throws IOException, XMLParseException
    {
        char ch = reader.read();
        StringBuffer keyBuf = new StringBuffer();

        while (ch != ';')
        {
            keyBuf.append(ch);
            ch = reader.read();
        }

        String key = keyBuf.toString();

        if (key.charAt(0) == '#')
        {
            if (isCharLiteral != null)
            {
                isCharLiteral[0] = true;
            }

            char[] chArr = new char[1];

            if (key.charAt(1) == 'x')
            {
                chArr[0] = (char) Integer.parseInt(key.substring(2), 16);
            }
            else
            {
                chArr[0] = (char) Integer.parseInt(key.substring(1), 10);
            }

            return new CharArrayReader(chArr);
        }
        else
        {
            Reader entityReader = entityResolver.getEntity(reader, key);

            if (entityReader == null)
            {
                XMLUtil.errorInvalidEntity(reader.getSystemID(), reader.getLineNr(), key);
            }

            return entityReader;
        }
    }

    /**
     * Skips whitespace from the reader.
     * 
     * @param reader the reader
     * @param escapeChar the escape character (&amp; or %)
     * @param buffer where to put the whitespace; null if the whitespace does not have to be stored.
     * @param isEntity if not null, will contain true if the data following the whitespace is an
     * entity
     * 
     * @throws java.io.IOException if an error occurred reading the data
     */
    static void skipWhitespace(IXMLReader reader, char escapeChar, StringBuffer buffer,
            boolean[] isEntity) throws IOException
    {
        char ch;

        if (buffer == null)
        {
            do
            {
                ch = reader.read();
            }
            while ((ch == ' ') || (ch == '\t') || (ch == '\n') || (ch == '\r'));
        }
        else
        {
            for (;;)
            {
                ch = reader.read();

                if ((ch != ' ') && (ch != '\t') && (ch != '\n') && (ch != '\r'))
                {
                    break;
                }

                buffer.append(ch);
            }
        }

        reader.unread(ch);

        if (isEntity != null)
        {
            isEntity[0] = (ch == escapeChar);
        }
    }

    /**
     * Reads a character from the reader.
     * 
     * @param reader the reader
     * @param isEntityValue if the character is the first character in an entity
     * @param escapeChar the escape character (&amp; or %)
     * @param entityResolver the entity resolver
     * 
     * @throws java.io.IOException if an error occurred reading the data
     */
    static char read(IXMLReader reader, boolean[] isEntityValue, char escapeChar,
            IXMLEntityResolver entityResolver) throws IOException, XMLParseException
    {
        char ch = reader.read();

        if (isEntityValue != null)
        {
            isEntityValue[0] = false;
        }

        if (ch == escapeChar)
        {
            boolean[] charLiteral = new boolean[1];
            reader.startNewStream(XMLUtil.scanEntity(charLiteral, reader, escapeChar,
                    entityResolver));

            if (charLiteral[0])
            {
                ch = reader.read();

                if (isEntityValue != null)
                {
                    isEntityValue[0] = true;
                }
            }
            else
            {
                ch = XMLUtil.read(reader, null, escapeChar, entityResolver);
            }
        }

        return ch;
    }

    /**
     * Returns true if the data starts with <I>literal</I>. Enough chars are read to determine this
     * result.
     * 
     * @param reader the reader
     * @param escapeChar the escape character (&amp; or %)
     * @param entityResolver the entity resolver
     * @param literal the literal to check
     * 
     * @throws java.io.IOException if an error occurred reading the data
     */
    static boolean checkLiteral(IXMLReader reader, char escapeChar,
            IXMLEntityResolver entityResolver, String literal) throws IOException,
            XMLParseException
    {
        for (int i = 0; i < literal.length(); i++)
        {
            char ch = XMLUtil.read(reader, null, escapeChar, entityResolver);

            if (ch != literal.charAt(i)) { return false; }
        }

        return true;
    }

    /**
     * Throws an XMLParseException to indicate that an expected string is not encountered.
     * 
     * @param systemID the system ID from where the data came
     * @param lineNr the line number in the XML data where the exception occurred.
     * @param expectedString the string that is expected
     */
    static void errorExpectedInput(String systemID, int lineNr, String expectedString)
            throws XMLParseException
    {
        throw new XMLParseException(systemID, lineNr, "Expected: " + expectedString);
    }

    /**
     * Throws an XMLParseException to indicate that an entity could not be resolved.
     * 
     * @param systemID the system ID from where the data came
     * @param lineNr the line number in the XML data where the exception occurred.
     * @param key the name of the entity
     */
    static void errorInvalidEntity(String systemID, int lineNr, String key)
            throws XMLParseException
    {
        throw new XMLParseException(systemID, lineNr, "Invalid entity: `&" + key + ";'");
    }

    /**
     * Throws an XMLParseException to indicate that a string is not expected at this point.
     * 
     * @param systemID the system ID from where the data came
     * @param lineNr the line number in the XML data where the exception occurred.
     * @param unexpectedString the string that is unexpected
     */
    static void errorInvalidInput(String systemID, int lineNr, String unexpectedString)
            throws XMLParseException
    {
        throw new XMLParseException(systemID, lineNr, "Invalid input: " + unexpectedString);
    }

    /**
     * Throws an XMLParseException to indicate that the closing tag of an element does not match the
     * opening tag.
     * 
     * @param systemID the system ID from where the data came
     * @param lineNr the line number in the XML data where the exception occurred.
     * @param expectedName the name of the opening tag
     * @param wrongName the name of the closing tag
     */
    static void errorWrongClosingTag(String systemID, int lineNr, String expectedName,
            String wrongName) throws XMLParseException
    {
        throw new XMLParseException(systemID, lineNr, "Closing tag does not match opening tag: `"
                + wrongName + "' != `" + expectedName + "'");
    }

    /**
     * Throws an XMLParseException to indicate that extra data is encountered in a closing tag.
     * 
     * @param systemID the system ID from where the data came
     * @param lineNr the line number in the XML data where the exception occurred.
     */
    static void errorClosingTagNotEmpty(String systemID, int lineNr) throws XMLParseException
    {
        throw new XMLParseException(systemID, lineNr, "Closing tag must be empty");
    }

    /**
     * Throws an XMLValidationException to indicate that an element is missing.
     * 
     * @param systemID the system ID from where the data came
     * @param lineNr the line number in the XML data where the exception occurred.
     * @param parentElementName the name of the offending element
     * @param missingElementName the name of the offending attribute
     */
    static void errorMissingElement(String systemID, int lineNr, String parentElementName,
            String missingElementName) throws XMLValidationException
    {
        throw new XMLValidationException(XMLValidationException.MISSING_ELEMENT, systemID, lineNr,
                missingElementName,
                /* attributeName */null,
                /* attributeValue */null, "Element " + parentElementName + " expects to have a "
                        + missingElementName);
    }

    /**
     * Throws an XMLValidationException to indicate that an element is unexpected.
     * 
     * @param systemID the system ID from where the data came
     * @param lineNr the line number in the XML data where the exception occurred.
     * @param parentElementName the name of the parent attribute
     * @param unexpectedElementName the name of the offending attribute
     */
    static void errorUnexpectedElement(String systemID, int lineNr, String parentElementName,
            String unexpectedElementName) throws XMLValidationException
    {
        throw new XMLValidationException(XMLValidationException.UNEXPECTED_ELEMENT, systemID,
                lineNr, unexpectedElementName,
                /* attributeName */null,
                /* attributeValue */null, "Unexpected " + unexpectedElementName + " in a "
                        + parentElementName);
    }

    /**
     * Throws an XMLValidationException to indicate that an attribute is missing.
     * 
     * @param systemID the system ID from where the data came
     * @param lineNr the line number in the XML data where the exception occurred.
     * @param elementName the name of the offending element
     * @param attributeName the name of the offending attribute
     */
    static void errorMissingAttribute(String systemID, int lineNr, String elementName,
            String attributeName) throws XMLValidationException
    {
        throw new XMLValidationException(XMLValidationException.MISSING_ATTRIBUTE, systemID,
                lineNr, elementName, attributeName,
                /* attributeValue */null, "Element " + elementName
                        + " expects an attribute named " + attributeName);
    }

    /**
     * Throws an XMLValidationException to indicate that an attribute is unexpected.
     * 
     * @param systemID the system ID from where the data came
     * @param lineNr the line number in the XML data where the exception occurred.
     * @param elementName the name of the offending element
     * @param attributeName the name of the offending attribute
     */
    static void errorUnexpectedAttribute(String systemID, int lineNr, String elementName,
            String attributeName) throws XMLValidationException
    {
        throw new XMLValidationException(XMLValidationException.UNEXPECTED_ATTRIBUTE, systemID,
                lineNr, elementName, attributeName,
                /* attributeValue */null, "Element " + elementName
                        + " did not expect an attribute " + "named " + attributeName);
    }

    /**
     * Throws an XMLValidationException to indicate that an attribute has an invalid value.
     * 
     * @param systemID the system ID from where the data came
     * @param lineNr the line number in the XML data where the exception occurred.
     * @param elementName the name of the offending element
     * @param attributeName the name of the offending attribute
     * @param attributeValue the value of the offending attribute
     */
    static void errorInvalidAttributeValue(String systemID, int lineNr, String elementName,
            String attributeName, String attributeValue) throws XMLValidationException
    {
        throw new XMLValidationException(XMLValidationException.ATTRIBUTE_WITH_INVALID_VALUE,
                systemID, lineNr, elementName, attributeName, attributeValue,
                "Invalid value for attribute " + attributeName);
    }

    /**
     * Throws an XMLValidationException to indicate that a #PCDATA element was missing.
     * 
     * @param systemID the system ID from where the data came
     * @param lineNr the line number in the XML data where the exception occurred.
     * @param parentElementName the name of the offending element
     */
    static void errorMissingPCData(String systemID, int lineNr, String parentElementName)
            throws XMLValidationException
    {
        throw new XMLValidationException(XMLValidationException.MISSING_PCDATA, systemID, lineNr,
        /* elementName */null,
        /* attributeName */null,
        /* attributeValue */null, "Missing #PCDATA in element " + parentElementName);
    }

    /**
     * Throws an XMLValidationException to indicate that a #PCDATA element was unexpected.
     * 
     * @param systemID the system ID from where the data came
     * @param lineNr the line number in the XML data where the exception occurred.
     * @param parentElementName the name of the offending element
     */
    static void errorUnexpectedPCData(String systemID, int lineNr, String parentElementName)
            throws XMLValidationException
    {
        throw new XMLValidationException(XMLValidationException.UNEXPECTED_PCDATA, systemID,
                lineNr,
                /* elementName */null,
                /* attributeName */null,
                /* attributeValue */null, "Unexpected #PCDATA in element " + parentElementName);
    }

    /**
     * Throws an XMLValidationException.
     * 
     * @param systemID the system ID from where the data came
     * @param lineNr the line number in the XML data where the exception occurred.
     * @param message the message of the exception.
     * @param elementName the name of the offending element
     * @param attributeName the name of the offending attribute
     * @param attributeValue the value of the offending attribute
     */
    static void validationError(String systemID, int lineNr, String message, String elementName,
            String attributeName, String attributeValue) throws XMLValidationException
    {
        throw new XMLValidationException(XMLValidationException.MISC_ERROR, systemID, lineNr,
                elementName, attributeName, attributeValue, message);
    }

}
