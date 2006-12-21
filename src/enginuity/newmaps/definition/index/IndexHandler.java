/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package enginuity.newmaps.definition.index;

import enginuity.util.HexUtil;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class IndexHandler extends DefaultHandler {

    private static final String TAG_ROM = "rom";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_BASE = "base";
    private static final String ATTR_ADDRESS = "idaddress";
    private static final String ATTR_ID = "idstring";    
    private static final String ATTR_ABSTRACT = "abstract";  
    
    IndexItem item = new IndexItem();       

    public void startElement(String uri, String localName, String qName, Attributes attr) {        
        
        if (qName.equalsIgnoreCase(TAG_ROM)) {
            
            // Set all attributes if they exist
            item.setName(attr.getValue(ATTR_NAME));
                
            if (attr.getIndex(ATTR_BASE) > -1 && attr.getValue(ATTR_BASE).length() > 0) 
                item.setBase(attr.getValue(ATTR_BASE));
            
            if (attr.getIndex(ATTR_ADDRESS) > -1 && attr.getValue(ATTR_ADDRESS).length() > 0) 
                item.setIdAddress(HexUtil.hexToInt(attr.getValue(ATTR_ADDRESS)));
            
            if (attr.getIndex(ATTR_ID) > -1 && attr.getValue(ATTR_ID).length() > 0) 
                item.setIdString(attr.getValue(ATTR_ID));
            
            if (attr.getIndex(ATTR_ABSTRACT) > -1 && attr.getValue(ATTR_ABSTRACT).length() > 0) 
                item.setAbstract(Boolean.parseBoolean(attr.getValue(ATTR_ABSTRACT)));           
            
        }  
        
    }
    
    public IndexItem getItem() {
        return item;
    }
    
}
