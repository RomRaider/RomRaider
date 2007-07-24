/* NonValidator.java                                               NanoXML/Java
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
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Stack;

/**
 * NonValidator processes the DTD and handles entity definitions. It does not do any validation
 * itself.
 * 
 * @author Marc De Scheemaecker
 * @version $Name$, $Revision: 1422 $
 */
public class NonValidator implements IXMLValidator
{

    /**
     * Delimiter for CDATA sections.
     */
    private static final char[] END_OF_CONDSECTION = { '>', ']', ']'};

    /**
     * The parameter entity resolver.
     */
    protected IXMLEntityResolver parameterEntityResolver;

    /**
     * The parameter entity level.
     */
    protected int peLevel;

    /**
     * Contains the default values for attributes for the different element types.
     */
    protected Hashtable attributeDefaultValues;

    /**
     * The stack of elements to be processed.
     */
    protected Stack currentElements;

    /**
     * Creates the &quot;validator&quot;.
     */
    public NonValidator()
    {
        this.attributeDefaultValues = new Hashtable();
        this.currentElements = new Stack();
        this.parameterEntityResolver = new XMLEntityResolver();
        this.peLevel = 0;
    }

    /**
     * Cleans up the object when it's destroyed.
     */
    protected void finalize() throws Throwable
    {
        this.parameterEntityResolver = null;
        this.attributeDefaultValues.clear();
        this.attributeDefaultValues = null;
        this.currentElements.clear();
        this.currentElements = null;
        super.finalize();
    }

    /**
     * Sets the parameter entity resolver.
     * 
     * @param resolver the entity resolver.
     */
    public void setParameterEntityResolver(IXMLEntityResolver resolver)
    {
        this.parameterEntityResolver = resolver;
    }

    /**
     * Returns the parameter entity resolver.
     * 
     * @return the entity resolver.
     */
    public IXMLEntityResolver getParameterEntityResolver()
    {
        return this.parameterEntityResolver;
    }

    /**
     * Parses the DTD. The validator object is responsible for reading the full DTD.
     * 
     * @param publicID the public ID, which may be null.
     * @param reader the reader to read the DTD from.
     * @param entityResolver the entity resolver.
     * @param external true if the DTD is external.
     * 
     * @throws java.lang.Exception if something went wrong.
     */
    public void parseDTD(String publicID, IXMLReader reader, IXMLEntityResolver entityResolver,
            boolean external) throws Exception
    {
        XMLUtil.skipWhitespace(reader, '%', null, null);

        for (;;)
        {
            char ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);

            if (ch == '<')
            {
                this.processElement(reader, entityResolver);
            }
            else if (ch == ']')
            {
                return; // end internal DTD
            }
            else
            {
                XMLUtil.errorInvalidInput(reader.getSystemID(), reader.getLineNr(), "" + ch);
            }

            do
            {
                if (external && (peLevel == 0) && reader.atEOFOfCurrentStream()) { return; // end
                // external
                // DTD
                }

                ch = reader.read();
            }
            while ((ch == ' ') || (ch == '\t') || (ch == '\n') || (ch == '\r'));

            reader.unread(ch);
            XMLUtil.skipWhitespace(reader, '%', null, null);
        }
    }

    /**
     * Processes an element in the DTD.
     * 
     * @param reader the reader to read data from
     * @param entityResolver the entity resolver
     * 
     * @throws java.lang.Exception if something went wrong.
     */
    protected void processElement(IXMLReader reader, IXMLEntityResolver entityResolver)
            throws Exception
    {
        char ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);

        if (ch != '!')
        {
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }

        ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);

        switch (ch)
        {
        case '-':
            XMLUtil.skipComment(reader, this.parameterEntityResolver);
            break;

        case '[':
            this.processConditionalSection(reader, entityResolver);
            break;

        case 'E':
            this.processEntity(reader, entityResolver);
            break;

        case 'A':
            this.processAttList(reader, entityResolver);
            break;

        default:
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
        }
    }

    /**
     * Processes a conditional section.
     * 
     * @param reader the reader to read data from
     * @param entityResolver the entity resolver
     * 
     * @throws java.lang.Exception if something went wrong.
     */
    protected void processConditionalSection(IXMLReader reader, IXMLEntityResolver entityResolver)
            throws Exception
    {
        XMLUtil.skipWhitespace(reader, '%', null, null);

        char ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);

        if (ch != 'I')
        {
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }

        ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);

        switch (ch)
        {
        case 'G':
            this.processIgnoreSection(reader, entityResolver);
            return;

        case 'N':
            break;

        default:
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }

        if (!XMLUtil.checkLiteral(reader, '%', this.parameterEntityResolver, "CLUDE"))
        {
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }

        XMLUtil.skipWhitespace(reader, '%', null, null);

        ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);

        if (ch != '[')
        {
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }

        Reader subreader = new ContentReader(reader, this.parameterEntityResolver, '\0',
                NonValidator.END_OF_CONDSECTION, true, "");
        StringBuffer buf = new StringBuffer(1024);

        for (;;)
        {
            int ch2 = subreader.read();

            if (ch2 < 0)
            {
                break;
            }

            buf.append((char) ch2);
        }

        subreader.close();
        reader.startNewStream(new StringReader(buf.toString()));
    }

    /**
     * Processes an ignore section.
     * 
     * @param reader the reader to read data from
     * @param entityResolver the entity resolver
     * 
     * @throws java.lang.Exception if something went wrong.
     */
    protected void processIgnoreSection(IXMLReader reader, IXMLEntityResolver entityResolver)
            throws Exception
    {
        if (!XMLUtil.checkLiteral(reader, '%', this.parameterEntityResolver, "NORE"))
        {
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }

        XMLUtil.skipWhitespace(reader, '%', null, null);

        char ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);

        if (ch != '[')
        {
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }

        Reader subreader = new ContentReader(reader, this.parameterEntityResolver, '\0',
                NonValidator.END_OF_CONDSECTION, true, "");
        subreader.close();
    }

    /**
     * Processes an ATTLIST element.
     * 
     * @param reader the reader to read data from
     * @param entityResolver the entity resolver
     * 
     * @throws java.lang.Exception if something went wrong.
     */
    protected void processAttList(IXMLReader reader, IXMLEntityResolver entityResolver)
            throws Exception
    {
        if (!XMLUtil.checkLiteral(reader, '%', this.parameterEntityResolver, "TTLIST"))
        {
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }

        XMLUtil.skipWhitespace(reader, '%', null, null);
        String elementName = XMLUtil.scanIdentifier(reader, '%', this.parameterEntityResolver);
        XMLUtil.skipWhitespace(reader, '%', null, null);
        char ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
        Properties props = new Properties();

        while (ch != '>')
        {
            reader.unread(ch);
            String attName = XMLUtil.scanIdentifier(reader, '%', this.parameterEntityResolver);
            XMLUtil.skipWhitespace(reader, '%', null, null);
            ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);

            if (ch == '(')
            {
                while (ch != ')')
                {
                    ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
                }
            }
            else
            {
                reader.unread(ch);
                XMLUtil.scanIdentifier(reader, '%', this.parameterEntityResolver);
            }

            XMLUtil.skipWhitespace(reader, '%', null, null);
            ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);

            if (ch == '#')
            {
                String str = XMLUtil.scanIdentifier(reader, '%', this.parameterEntityResolver);
                XMLUtil.skipWhitespace(reader, '%', null, null);

                if (!"FIXED".equals(str))
                {
                    XMLUtil.skipWhitespace(reader, '%', null, null);
                    ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
                    continue;
                }
            }
            else
            {
                reader.unread(ch);
            }

            String value = XMLUtil.scanString(reader, '%', false, this.parameterEntityResolver);
            props.put(attName, value);
            XMLUtil.skipWhitespace(reader, '%', null, null);
            ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
        }

        if (!props.isEmpty())
        {
            this.attributeDefaultValues.put(elementName, props);
        }
    }

    /**
     * Processes an ENTITY element.
     * 
     * @param reader the reader to read data from
     * @param entityResolver the entity resolver
     * 
     * @throws java.lang.Exception if something went wrong.
     */
    protected void processEntity(IXMLReader reader, IXMLEntityResolver entityResolver)
            throws Exception
    {
        if (!XMLUtil.checkLiteral(reader, '%', this.parameterEntityResolver, "NTITY"))
        {
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }

        XMLUtil.skipWhitespace(reader, '\0', null, null);
        char ch = XMLUtil.read(reader, null, '\0', this.parameterEntityResolver);

        if (ch == '%')
        {
            XMLUtil.skipWhitespace(reader, '%', null, null);
            entityResolver = this.parameterEntityResolver;
        }
        else
        {
            reader.unread(ch);
        }

        String key = XMLUtil.scanIdentifier(reader, '%', this.parameterEntityResolver);
        XMLUtil.skipWhitespace(reader, '%', null, null);
        ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
        String systemID = null;
        String publicID = null;

        switch (ch)
        {
        case 'P':
            if (!XMLUtil.checkLiteral(reader, '%', this.parameterEntityResolver, "UBLIC"))
            {
                XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
                return;
            }

            XMLUtil.skipWhitespace(reader, '%', null, null);
            publicID = XMLUtil.scanString(reader, '%', false, this.parameterEntityResolver);
            XMLUtil.skipWhitespace(reader, '%', null, null);
            systemID = XMLUtil.scanString(reader, '%', false, this.parameterEntityResolver);
            XMLUtil.skipWhitespace(reader, '%', null, null);
            XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
            break;

        case 'S':
            if (!XMLUtil.checkLiteral(reader, '%', this.parameterEntityResolver, "YSTEM"))
            {
                XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
                return;
            }

            XMLUtil.skipWhitespace(reader, '%', null, null);
            systemID = XMLUtil.scanString(reader, '%', false, this.parameterEntityResolver);
            XMLUtil.skipWhitespace(reader, '%', null, null);
            XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
            break;

        case '"':
        case '\'':
            reader.unread(ch);
            String value = XMLUtil.scanString(reader, '%', false, this.parameterEntityResolver);
            entityResolver.addInternalEntity(key, value);
            XMLUtil.skipWhitespace(reader, '%', null, null);
            XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
            break;
        default:
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
        }

        if (systemID != null)
        {
            entityResolver.addExternalEntity(key, publicID, systemID);
        }
    }

    /**
     * Indicates that an element has been started.
     * 
     * @param name the name of the element.
     * @param nsPrefix the prefix used to identify the namespace
     * @param nsSystemId the system ID associated with the namespace
     * @param systemId the system ID of the XML data of the element.
     * @param lineNr the line number in the XML data of the element.
     */
    public void elementStarted(String name, String nsPrefix, String nsSystemId, String systemId,
            int lineNr)
    {
        Properties attribs = (Properties) this.attributeDefaultValues.get(name);

        if (attribs == null)
        {
            attribs = new Properties();
        }
        else
        {
            attribs = (Properties) attribs.clone();
        }

        this.currentElements.push(attribs);
    }

    /**
     * Indicates that the current element has ended.
     * 
     * @param name the name of the element.
     * @param nsPrefix the prefix used to identify the namespace
     * @param nsSystemId the system ID associated with the namespace
     * @param systemId the system ID of the XML data of the element.
     * @param lineNr the line number in the XML data of the element.
     */
    public void elementEnded(String name, String nsPrefix, String nsSystemId, String systemId,
            int lineNr)
    {
        // nothing to do
    }

    /**
     * This method is called when the attributes of an XML element have been processed. If there are
     * attributes with a default value which have not been specified yet, they have to be put into
     * <I>extraAttributes</I>.
     * 
     * @param name the name of the element.
     * @param nsPrefix the prefix used to identify the namespace
     * @param nsSystemId the system ID associated with the namespace
     * @param extraAttributes where to put extra attributes.
     * @param systemId the system ID of the XML data of the element.
     * @param lineNr the line number in the XML data of the element.
     */
    public void elementAttributesProcessed(String name, String nsPrefix, String nsSystemId,
            Properties extraAttributes, String systemId, int lineNr)
    {
        Properties props = (Properties) this.currentElements.pop();
        Enumeration enumeration = props.keys();

        while (enumeration.hasMoreElements())
        {
            String key = (String) enumeration.nextElement();
            extraAttributes.put(key, props.get(key));
        }
    }

    /**
     * Indicates that an attribute has been added to the current element.
     * 
     * @param key the name of the attribute.
     * @param nsPrefix the prefix used to identify the namespace
     * @param nsSystemId the system ID associated with the namespace
     * @param value the value of the attribute.
     * @param systemId the system ID of the XML data of the element.
     * @param lineNr the line number in the XML data of the element.
     */
    public void attributeAdded(String key, String nsPrefix, String nsSystemId, String value,
            String systemId, int lineNr)
    {
        Properties props = (Properties) this.currentElements.peek();

        if (props.containsKey(key))
        {
            props.remove(key);
        }
    }

    /**
     * Indicates that a new #PCDATA element has been encountered.
     * 
     * @param systemId the system ID of the XML data of the element.
     * @param lineNr the line number in the XML data of the element.
     */
    public void PCDataAdded(String systemId, int lineNr)
    {
        // nothing to do
    }

}
