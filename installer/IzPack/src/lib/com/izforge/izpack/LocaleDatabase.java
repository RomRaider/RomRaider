/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
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

package com.izforge.izpack;

import java.io.InputStream;
import java.util.TreeMap;
import java.util.Vector;

import net.n3.nanoxml.NonValidator;
import net.n3.nanoxml.StdXMLBuilder;
import net.n3.nanoxml.StdXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;

/**
 * Represents a database of a locale.
 * 
 * @author Julien Ponge
 */
public class LocaleDatabase extends TreeMap
{

    static final long serialVersionUID = 4941525634108401848L;

    /**
     * The constructor.
     * 
     * @param in An InputStream to read the translation from.
     * @exception Exception Description of the Exception
     */
    public LocaleDatabase(InputStream in) throws Exception
    {
        // We call the superclass default constructor
        super();
        add(in);
    }

    public void add(InputStream in) throws Exception
    {
        // Initialises the parser
        StdXMLParser parser = new StdXMLParser();
        parser.setBuilder(new StdXMLBuilder());
        parser.setReader(new StdXMLReader(in));
        parser.setValidator(new NonValidator());

        // We get the data
        XMLElement data = (XMLElement) parser.parse();

        // We check the data
        if (!"langpack".equalsIgnoreCase(data.getName()))
            throw new Exception("this is not an IzPack XML langpack file");

        // We fill the Hashtable
        Vector children = data.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++)
        {
            XMLElement e = (XMLElement) children.get(i);
            String text = e.getContent();
            if (text != null && !"".equals(text))
            {
                put(e.getAttribute("id"), text.trim());
            }
            else
            {
                put(e.getAttribute("id"), e.getAttribute("txt"));
            }
        }

    }

    /**
     * Convenience method to retrieve an element.
     * 
     * @param key The key of the element to retrieve.
     * @return The element value or the key if not found.
     */
    public String getString(String key)
    {
        String val = (String) get(key);
        // At a change of the return value at val == null the method
        // com.izforge.izpack.installer.IzPanel.getI18nStringForClass
        // should be also addapted.
        if (val == null) val = key;
        return val;
    }
}

