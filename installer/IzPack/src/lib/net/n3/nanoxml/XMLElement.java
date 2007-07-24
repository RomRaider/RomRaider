/* XMLElement.java                                                 NanoXML/Java
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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * XMLElement is an XML element. The standard NanoXML builder generates a tree of such elements.
 * 
 * @see net.n3.nanoxml.StdXMLBuilder
 * 
 * @author Marc De Scheemaecker
 * @version $Name$, $Revision: 1421 $
 */
public class XMLElement implements Serializable
{

    /**
     * Necessary for serialization.
     */
    static final long serialVersionUID = -2383376380548624920L;

    /**
     * No line number defined.
     */
    public static final int NO_LINE = -1;

    /**
     * The attributes of the element.
     */
    private Properties attributes;

    /**
     * The child elements.
     */
    private Vector children;

    /**
     * The name of the element.
     */
    private String name;

    /**
     * The content of the element.
     */
    private String content;

    /**
     * The system ID of the source data where this element is located.
     */
    private String systemID;

    /**
     * The line in the source data where this element starts.
     */
    private int lineNr;

    /**
     * Creates an empty element to be used for #PCDATA content.
     */
    public XMLElement()
    {
        this(null, null, NO_LINE);
    }

    /**
     * Creates an empty element.
     * 
     * @param name the name of the element.
     */
    public XMLElement(String name)
    {
        this(name, null, NO_LINE);
    }

    /**
     * Creates an empty element.
     * 
     * @param name the name of the element.
     * @param systemID the system ID of the XML data where the element starts.
     * @param lineNr the line in the XML data where the element starts.
     */
    public XMLElement(String name, String systemID, int lineNr)
    {
        this.attributes = new Properties();
        this.children = new Vector(8);
        this.name = name;
        this.content = null;
        this.lineNr = lineNr;
        this.systemID = systemID;
    }

    /**
     * Cleans up the object when it's destroyed.
     */
    protected void finalize() throws Throwable
    {
        this.attributes = null;
        this.children = null;
        this.name = null;
        this.content = null;
        this.systemID = null;
        super.finalize();
    }

    /**
     * Returns the name of the element.
     * 
     * @return the name, or null if the element only contains #PCDATA.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the name.
     * 
     * @param name the non-null name.
     */
    public void setName(String name)
    {
        if (name == null) { throw new IllegalArgumentException("name must not be null"); }

        this.name = name;
    }

    /**
     * Adds a child element.
     * 
     * @param child the non-null child to add.
     */
    public void addChild(XMLElement child)
    {
        if (child == null) { throw new IllegalArgumentException("child must not be null"); }

        if ((child.getName() == null) && (!this.children.isEmpty()))
        {
            XMLElement lastChild = (XMLElement) this.children.lastElement();

            if (lastChild.getName() == null)
            {
                lastChild.setContent(lastChild.getContent() + child.getContent());
                return;
            }
        }

        this.children.addElement(child);
    }

    /**
     * Removes a child element.
     * 
     * @param child the non-null child to remove.
     */
    public void removeChild(XMLElement child)
    {
        if (child == null) { throw new IllegalArgumentException("child must not be null"); }

        this.children.removeElement(child);
    }

    /**
     * Removes the child located at a certain index.
     * 
     * @param index the index of the child, where the first child has index 0.
     */
    public void removeChildAtIndex(int index)
    {
        this.children.removeElementAt(index);
    }

    /**
     * Returns an enumeration of all child elements.
     * 
     * @return the non-null enumeration
     */
    public Enumeration enumerateChildren()
    {
        return this.children.elements();
    }

    /**
     * Returns whether the element is a leaf element.
     * 
     * @return true if the element has no children.
     */
    public boolean isLeaf()
    {
        return this.children.isEmpty();
    }

    /**
     * Returns whether the element has children.
     * 
     * @return true if the element has children.
     */
    public boolean hasChildren()
    {
        return (!this.children.isEmpty());
    }

    /**
     * Returns the number of children.
     * 
     * @return the count.
     */
    public int getChildrenCount()
    {
        return this.children.size();
    }

    /**
     * Returns a vector containing all the child elements.
     * 
     * @return the vector.
     */
    public Vector getChildren()
    {
        return this.children;
    }

    /**
     * Returns the child at a specific index.
     * 
     * @return the non-null child
     * 
     * @throws java.lang.ArrayIndexOutOfBoundsException if the index is out of bounds.
     */
    public XMLElement getChildAtIndex(int index) throws ArrayIndexOutOfBoundsException
    {
        return (XMLElement) this.children.elementAt(index);
    }

    /**
     * Searches a child element.
     * 
     * @param name the name of the child to search for.
     * 
     * @return the child element, or null if no such child was found.
     */
    public XMLElement getFirstChildNamed(String name)
    {
        Enumeration enumeration = this.children.elements();

        while (enumeration.hasMoreElements())
        {
            XMLElement child = (XMLElement) enumeration.nextElement();
            String cName = child.getName();

            if (cName != null && cName.equals(name)) { return child; }
        }

        return null;
    }

    /**
     * Returns a vector of all child elements named <I>name</I>.
     * 
     * @param name the name of the children to search for.
     * 
     * @return the non-null vector of child elements.
     */
    public Vector getChildrenNamed(String name)
    {
        Vector result = new Vector(this.children.size());
        Enumeration enumeration = this.children.elements();

        while (enumeration.hasMoreElements())
        {
            XMLElement child = (XMLElement) enumeration.nextElement();
            String cName = child.getName();

            if (cName != null && cName.equals(name))
            {
                result.addElement(child);
            }
        }

        return result;
    }

    /**
     * Returns the value of an attribute.
     * 
     * @param name the non-null name of the attribute.
     * 
     * @return the value, or null if the attribute does not exist.
     */
    public String getAttribute(String name)
    {
        return this.getAttribute(name, null);
    }

    /**
     * Returns the value of an attribute.
     * 
     * @param name the non-null name of the attribute.
     * @param defaultValue the default value of the attribute.
     * 
     * @return the value, or defaultValue if the attribute does not exist.
     */
    public String getAttribute(String name, String defaultValue)
    {
        return this.attributes.getProperty(name, defaultValue);
    }

    /**
     * Sets an attribute.
     * 
     * @param name the non-null name of the attribute.
     * @param value the non-null value of the attribute.
     */
    public void setAttribute(String name, String value)
    {
        this.attributes.put(name, value);
    }

    /**
     * Removes an attribute.
     * 
     * @param name the non-null name of the attribute.
     */
    public void removeAttribute(String name)
    {
        this.attributes.remove(name);
    }

    /**
     * Returns an enumeration of all attribute names.
     * 
     * @return the non-null enumeration.
     */
    public Enumeration enumerateAttributeNames()
    {
        return this.attributes.keys();
    }

    /**
     * Returns whether an attribute exists.
     * 
     * @return true if the attribute exists.
     */
    public boolean hasAttribute(String name)
    {
        return this.attributes.containsKey(name);
    }

    /**
     * Returns all attributes as a Properties object.
     * 
     * @return the non-null set.
     */
    public Properties getAttributes()
    {
        return this.attributes;
    }

    /**
     * Returns the system ID of the data where the element started.
     * 
     * @return the system ID, or null if unknown.
     * 
     * @see #getLineNr
     */
    public String getSystemID()
    {
        return this.systemID;
    }

    /**
     * Returns the line number in the data where the element started.
     * 
     * @return the line number, or NO_LINE if unknown.
     * 
     * @see #NO_LINE
     * @see #getSystemID
     */
    public int getLineNr()
    {
        return this.lineNr;
    }

    /**
     * Return the #PCDATA content of the element. If the element has a combination of #PCDATA
     * content and child elements, the #PCDATA sections can be retrieved as unnamed child objects.
     * In this case, this method returns null.
     * 
     * @return the content.
     */
    public String getContent()
    {
        return this.content;
    }

    /**
     * Sets the #PCDATA content. It is an error to call this method with a non-null value if there
     * are child objects.
     * 
     * @param content the (possibly null) content.
     */
    public void setContent(String content)
    {
        this.content = content;
    }

}
