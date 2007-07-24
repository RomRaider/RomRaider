/* StdXMLParser.java                                               NanoXML/Java
 *
 * $Revision: 1422 $
 * $Date: 2006-03-21 19:14:56 +0100 (Tue, 21 Mar 2006) $
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

import java.io.Reader;
import java.util.Enumeration;
import java.util.Properties;

/**
 * StdXMLParser is the core parser of NanoXML.
 * 
 * @author Marc De Scheemaecker
 * @version $Name$, $Revision: 1422 $
 */
public class StdXMLParser implements IXMLParser
{

    /**
     * Delimiter for a processing instructions.
     */
    private static final char[] END_OF_PI = { '>', '?'};

    /**
     * Delimiter for CDATA sections.
     */
    private static final char[] END_OF_CDATA = { '>', ']', ']'};

    /**
     * Delimiter for PCDATA elements.
     */
    private static final char[] END_OF_PCDATA = { '<'};

    /**
     * The builder which creates the logical structure of the XML data.
     */
    private IXMLBuilder builder;

    /**
     * The reader from which the parser retrieves its data.
     */
    private IXMLReader reader;

    /**
     * The entity resolver.
     */
    private IXMLEntityResolver entityResolver;

    /**
     * The validator that will process entity references and validate the XML data.
     */
    private IXMLValidator validator;

    /**
     * Creates a new parser.
     */
    public StdXMLParser()
    {
        this.builder = null;
        this.validator = null;
        this.reader = null;
        this.entityResolver = new XMLEntityResolver();
    }

    /**
     * Cleans up the object when it's destroyed.
     */
    protected void finalize() throws Throwable
    {
        this.builder = null;
        this.reader = null;
        this.entityResolver = null;
        this.validator = null;
        super.finalize();
    }

    /**
     * Sets the builder which creates the logical structure of the XML data.
     * 
     * @param builder the non-null builder
     */
    public void setBuilder(IXMLBuilder builder)
    {
        this.builder = builder;
    }

    /**
     * Returns the builder which creates the logical structure of the XML data.
     * 
     * @return the builder
     */
    public IXMLBuilder getBuilder()
    {
        return this.builder;
    }

    /**
     * Sets the validator that validates the XML data.
     * 
     * @param validator the non-null validator
     */
    public void setValidator(IXMLValidator validator)
    {
        this.validator = validator;
    }

    /**
     * Returns the validator that validates the XML data.
     * 
     * @return the validator
     */
    public IXMLValidator getValidator()
    {
        return this.validator;
    }

    /**
     * Sets the entity resolver.
     * 
     * @param resolver the non-null resolver
     */
    public void setResolver(IXMLEntityResolver resolver)
    {
        this.entityResolver = resolver;
    }

    /**
     * Returns the entity resolver.
     * 
     * @return the non-null resolver
     */
    public IXMLEntityResolver getResolver()
    {
        return this.entityResolver;
    }

    /**
     * Sets the reader from which the parser retrieves its data.
     * 
     * @param reader the reader
     */
    public void setReader(IXMLReader reader)
    {
        this.reader = reader;
    }

    /**
     * Returns the reader from which the parser retrieves its data.
     * 
     * @return the reader
     */
    public IXMLReader getReader()
    {
        return this.reader;
    }

    /**
     * Parses the data and lets the builder create the logical data structure.
     * 
     * @return the logical structure built by the builder
     * 
     * @throws net.n3.nanoxml.XMLException if an error occurred reading or parsing the data
     */
    public Object parse() throws XMLException
    {
        try
        {
            this.builder.startBuilding(this.reader.getSystemID(), this.reader.getLineNr());
            this.scanData();
            return this.builder.getResult();
        }
        catch (XMLException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new XMLException(e);
        }
    }

    /**
     * Scans the XML data for elements.
     * 
     * @throws java.lang.Exception if something went wrong
     */
    protected void scanData() throws Exception
    {
        while ((!this.reader.atEOF()) && (this.builder.getResult() == null))
        {
            char ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);

            switch (ch)
            {
            case '<':
                this.scanSomeTag(false /* don't allow CDATA */);
                break;

            case ' ':
            case '\t':
            case '\r':
            case '\n':
                // skip whitespace
                break;

            default:
                XMLUtil.errorInvalidInput(reader.getSystemID(), reader.getLineNr(), "`" + ch
                        + "' (0x" + Integer.toHexString((int) ch) + ')');
            }
        }
    }

    /**
     * Scans an XML tag.
     * 
     * @param allowCDATA true if CDATA sections are allowed at this point
     * 
     * @throws java.lang.Exception if something went wrong
     */
    protected void scanSomeTag(boolean allowCDATA) throws Exception
    {
        char ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);

        switch (ch)
        {
        case '?':
            this.processPI();
            break;

        case '!':
            this.processSpecialTag(allowCDATA);
            break;

        default:
            this.reader.unread(ch);
            this.processElement();
        }
    }

    /**
     * Processes a "processing instruction".
     * 
     * @throws java.lang.Exception if something went wrong
     */
    protected void processPI() throws Exception
    {
        XMLUtil.skipWhitespace(this.reader, '&', null, null);
        String target = XMLUtil.scanIdentifier(this.reader, '&', this.entityResolver);
        XMLUtil.skipWhitespace(this.reader, '&', null, null);
        Reader reader = new ContentReader(this.reader, this.entityResolver, '&',
                StdXMLParser.END_OF_PI, true, "");

        if (!"xml".equalsIgnoreCase(target))
        {
            this.builder.newProcessingInstruction(target, reader);
        }

        reader.close();
    }

    /**
     * Processes a tag that starts with a bang (&lt;&#x21;&#x2e;&#x2e;&#x2e;&gt;).
     * 
     * @param allowCDATA true if CDATA sections are allowed at this point
     * 
     * @throws java.lang.Exception if something went wrong
     */
    protected void processSpecialTag(boolean allowCDATA) throws Exception
    {
        char ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);

        switch (ch)
        {
        case '[':
            if (allowCDATA)
            {
                this.processCDATA();
            }
            else
            {
                XMLUtil.skipTag(this.reader, '&', this.entityResolver);
            }

            return;

        case 'D':
            this.processDocType();
            return;

        case '-':
            XMLUtil.skipComment(this.reader, this.entityResolver);
        }
    }

    /**
     * Processes a CDATA section.
     * 
     * @throws java.lang.Exception if something went wrong
     */
    protected void processCDATA() throws Exception
    {
        if (!XMLUtil.checkLiteral(this.reader, '&', this.entityResolver, "CDATA["))
        {
            XMLUtil.skipTag(this.reader, '&', this.entityResolver);
            return;
        }

        this.validator.PCDataAdded(this.reader.getSystemID(), this.reader.getLineNr());
        Reader reader = new ContentReader(this.reader, this.entityResolver, '&',
                StdXMLParser.END_OF_CDATA, true, "");

        this.builder.addPCData(reader, this.reader.getSystemID(), this.reader.getLineNr());
        reader.close();
    }

    /**
     * Processes a document type declaration.
     * 
     * @throws java.lang.Exception if an error occurred reading or parsing the data
     */
    protected void processDocType() throws Exception
    {
        if (!XMLUtil.checkLiteral(this.reader, '&', this.entityResolver, "OCTYPE"))
        {
            XMLUtil.skipTag(this.reader, '&', this.entityResolver);
            return;
        }

        XMLUtil.skipWhitespace(this.reader, '&', null, null);
        String systemID = null;
        StringBuffer publicID = new StringBuffer();
        XMLUtil.skipWhitespace(this.reader, '&', null, null);
        char ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);

        if (ch == 'P')
        {
            systemID = XMLUtil.scanPublicID(publicID, reader, '&', this.entityResolver);
            XMLUtil.skipWhitespace(this.reader, '&', null, null);
            ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);
        }
        else if (ch == 'S')
        {
            systemID = XMLUtil.scanSystemID(reader, '&', this.entityResolver);
            XMLUtil.skipWhitespace(this.reader, '&', null, null);
            ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);
        }

        if (ch == '[')
        {
            this.validator.parseDTD(publicID.toString(), this.reader, this.entityResolver, false);
            XMLUtil.skipWhitespace(this.reader, '&', null, null);
            ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);
        }

        if (ch != '>')
        {
            XMLUtil.errorExpectedInput(reader.getSystemID(), reader.getLineNr(), "`>'");
        }

        if (systemID != null)
        {
            Reader reader = this.reader.openStream(publicID.toString(), systemID);
            this.reader.startNewStream(reader);
            this.reader.setSystemID(systemID);
            this.reader.setPublicID(publicID.toString());
            this.validator.parseDTD(publicID.toString(), this.reader, this.entityResolver, true);
        }
    }

    /**
     * Processes a regular element.
     * 
     * @throws java.lang.Exception if something went wrong
     */
    protected void processElement() throws Exception
    {
        String name = XMLUtil.scanIdentifier(this.reader, '&', this.entityResolver);
        XMLUtil.skipWhitespace(this.reader, '&', null, null);
        String prefix = null;
        int colonIndex = name.indexOf(':');

        if (colonIndex > 0)
        {
            prefix = name.substring(0, colonIndex);
            name = name.substring(colonIndex + 1);
        }

        this.validator.elementStarted(name, prefix, null, this.reader.getSystemID(), this.reader
                .getLineNr());
        this.builder.startElement(name, prefix, null, this.reader.getSystemID(), this.reader
                .getLineNr());
        char ch;

        for (;;)
        {
            ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);

            if ((ch == '/') || (ch == '>'))
            {
                break;
            }

            this.reader.unread(ch);
            this.processAttribute();
            XMLUtil.skipWhitespace(this.reader, '&', null, null);
        }

        Properties extraAttributes = new Properties();
        this.validator.elementAttributesProcessed(name, prefix, null, extraAttributes, this.reader
                .getSystemID(), this.reader.getLineNr());
        Enumeration enumeration = extraAttributes.keys();

        while (enumeration.hasMoreElements())
        {
            String key = (String) enumeration.nextElement();
            String value = extraAttributes.getProperty(key);
            String attPrefix = null;
            colonIndex = key.indexOf(':');

            if (colonIndex > 0)
            {
                attPrefix = key.substring(0, colonIndex);
                key = key.substring(colonIndex + 1);
            }

            this.builder.addAttribute(key, attPrefix, null, value, "CDATA");
        }

        this.builder.elementAttributesProcessed(name, prefix, null);

        if (ch == '/')
        {
            if (XMLUtil.read(this.reader, null, '&', this.entityResolver) != '>')
            {
                XMLUtil.errorExpectedInput(reader.getSystemID(), reader.getLineNr(), "`>'");
            }

            this.validator.elementEnded(name, prefix, null, this.reader.getSystemID(), this.reader
                    .getLineNr());
            this.builder.endElement(name, prefix, null);
            return;
        }

        StringBuffer whitespaceBuffer = new StringBuffer(16);

        for (;;)
        {
            whitespaceBuffer.setLength(0);
            boolean fromEntity[] = new boolean[1];
            XMLUtil.skipWhitespace(this.reader, '&', whitespaceBuffer, fromEntity);
            ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);

            if ((ch == '<') && (!fromEntity[0]))
            {
                ch = reader.read();

                if (ch == '/')
                {
                    XMLUtil.skipWhitespace(this.reader, '&', null, null);
                    String str = XMLUtil.scanIdentifier(this.reader, '&', this.entityResolver);

                    if (!str.equals(name))
                    {
                        XMLUtil.errorWrongClosingTag(reader.getSystemID(), reader.getLineNr(),
                                name, str);
                    }

                    XMLUtil.skipWhitespace(this.reader, '&', null, null);

                    if (XMLUtil.read(this.reader, null, '&', this.entityResolver) != '>')
                    {
                        XMLUtil.errorClosingTagNotEmpty(reader.getSystemID(), reader.getLineNr());
                    }

                    this.validator.elementEnded(name, prefix, null, this.reader.getSystemID(),
                            this.reader.getLineNr());
                    this.builder.endElement(name, prefix, null);
                    break;
                }
                else
                {
                    this.reader.unread(ch);
                    this.scanSomeTag(true /* CDATA allowed */);
                }
            }
            else
            {
                this.validator.PCDataAdded(this.reader.getSystemID(), this.reader.getLineNr());
                this.reader.unread(ch);
                Reader reader = new ContentReader(this.reader, this.entityResolver, '&',
                        StdXMLParser.END_OF_PCDATA, false, whitespaceBuffer.toString());
                this.builder.addPCData(reader, this.reader.getSystemID(), this.reader.getLineNr());
                reader.close();
                this.reader.unread('<');
            }
        }
    }

    /**
     * Processes an attribute of an element.
     * 
     * @throws java.lang.Exception if something went wrong
     */
    protected void processAttribute() throws Exception
    {
        String key = XMLUtil.scanIdentifier(this.reader, '&', this.entityResolver);
        XMLUtil.skipWhitespace(this.reader, '&', null, null);

        if (XMLUtil.read(this.reader, null, '&', this.entityResolver) != '=')
        {
            XMLUtil.errorExpectedInput(reader.getSystemID(), reader.getLineNr(), "`='");
        }

        String value = XMLUtil.scanString(this.reader, '&', true, this.entityResolver);
        this.validator.attributeAdded(key, null, null, value, this.reader.getSystemID(),
                this.reader.getLineNr());
        this.builder.addAttribute(key, null, null, value, "CDATA");
    }

}
