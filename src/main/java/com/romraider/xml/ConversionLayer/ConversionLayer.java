/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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
 */

package com.romraider.xml.ConversionLayer;
import java.io.File;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class ConversionLayer {	
	
	public final static String xmlRegexFileNameFilter = "^.*\\.(xml|XML)$";
	
	public abstract String getDefinitionPickerInfo();
	
	public abstract String getRegexFileNameFilter();
	
	/*
	 * The actual conversion method. It receives the file and creates a DOM structure
	 * compatible to the default RR structure. 
	 */
	public abstract Document convertToDocumentTree(File f) throws Exception;
	
	/*
	 * This method receives a file and checks if this converter supports this file
	 * extension.
	 */
	public boolean isFileSupported(File f) {
		return f.getName().matches(getRegexFileNameFilter());
	}
		
    public static String convertDocumentToString(Document doc) {
        StringWriter sw = new StringWriter();
        
    	try {
    		TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute("indent-number", 4);
            
	        Transformer trans = tf.newTransformer();
	        trans.setOutputProperty(OutputKeys.ENCODING, "ASCII");
	        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        trans.setOutputProperty(OutputKeys.INDENT, "yes");
	        trans.transform(new DOMSource(doc), new StreamResult(sw));
    	}
        catch(Exception e) {
        	e.printStackTrace();
        	return null;
        }
        
        return sw.toString();
    }

}
