/* IXMLBuilder.java                                                NanoXML/Java
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

import java.io.Reader;

/**
 * NanoXML uses IXMLBuilder to construct the XML data structure it retrieved from its data source.
 * You can supply your own builder or you can use the default builder of NanoXML.
 * 
 * @see net.n3.nanoxml.IXMLParser
 * 
 * @author Marc De Scheemaecker
 * @version $Name$, $Revision: 1421 $
 */
public interface IXMLBuilder
{

    /**
     * This method is called before the parser starts processing its input.
     * 
     * @param systemID the system ID of the XML data source
     * @param lineNr the line on which the parsing starts
     * 
     * @throws java.lang.Exception If an exception occurred while processing the event.
     */
    public void startBuilding(String systemID, int lineNr) throws Exception;

    /**
     * This method is called when a processing instruction is encountered. PIs with target "xml" are
     * handled by the parser.
     * 
     * @param target the PI target
     * @param reader to read the data from the PI
     * 
     * @throws java.lang.Exception If an exception occurred while processing the event.
     */
    public void newProcessingInstruction(String target, Reader reader) throws Exception;

    /**
     * This method is called when a new XML element is encountered.
     * 
     * @see #endElement
     * 
     * @param name the name of the element
     * @param nsPrefix the prefix used to identify the namespace
     * @param nsSystemID the system ID associated with the namespace
     * @param systemID the system ID of the XML data source
     * @param lineNr the line in the source where the element starts
     * 
     * @throws java.lang.Exception If an exception occurred while processing the event.
     */
    public void startElement(String name, String nsPrefix, String nsSystemID, String systemID,
            int lineNr) throws Exception;

    /**
     * This method is called when a new attribute of an XML element is encountered.
     * 
     * @param key the key (name) of the attribute
     * @param nsPrefix the prefix used to identify the namespace
     * @param nsSystemID the system ID associated with the namespace
     * @param value the value of the attribute
     * @param type the type of the attribute ("CDATA" if unknown)
     * 
     * @throws java.lang.Exception If an exception occurred while processing the event.
     */
    public void addAttribute(String key, String nsPrefix, String nsSystemID, String value,
            String type) throws Exception;

    /**
     * This method is called when the attributes of an XML element have been processed.
     * 
     * @see #startElement
     * @see #addAttribute
     * 
     * @param name the name of the element
     * @param nsPrefix the prefix used to identify the namespace
     * @param nsSystemID the system ID associated with the namespace
     * 
     * @throws java.lang.Exception If an exception occurred while processing the event.
     */
    public void elementAttributesProcessed(String name, String nsPrefix, String nsSystemID)
            throws Exception;

    /**
     * This method is called when the end of an XML elemnt is encountered.
     * 
     * @see #startElement
     * 
     * @param name the name of the element
     * @param nsPrefix the prefix used to identify the namespace
     * @param nsSystemID the system ID associated with the namespace
     * 
     * @throws java.lang.Exception If an exception occurred while processing the event.
     */
    public void endElement(String name, String nsPrefix, String nsSystemID) throws Exception;

    /**
     * This method is called when a PCDATA element is encountered. A Java reader is supplied from
     * which you can read the data. The reader will only read the data of the element. You don't
     * need to check for boundaries. If you don't read the full element, the rest of the data is
     * skipped. You also don't have to care about entities; they are resolved by the parser.
     * 
     * @param reader the Java reader from which you can retrieve the data
     * @param systemID the system ID of the XML data source
     * @param lineNr the line in the source where the element starts
     * 
     * @throws java.lang.Exception If an exception occurred while processing the event.
     */
    public void addPCData(Reader reader, String systemID, int lineNr) throws Exception;

    /**
     * Returns the result of the building process. This method is called just before the parse()
     * method of IXMLParser returns.
     * 
     * @see net.n3.nanoxml.IXMLParser#parse
     * 
     * @return the result of the building process.
     * 
     * @throws java.lang.Exception If an exception occurred while processing the event.
     */
    public Object getResult() throws Exception;

}
