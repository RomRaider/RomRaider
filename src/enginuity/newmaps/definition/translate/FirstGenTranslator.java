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

package enginuity.newmaps.definition.translate;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import enginuity.maps.Rom;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

public class FirstGenTranslator {
        
    public FirstGenTranslator() {
    }
    
    public static void main(String args[]) {
        FirstGenDefinitionHandler handler = new FirstGenDefinitionHandler();
        File inputFile = new File("ecu_defs/ecu_defs.xml");       
        //File inputFile = new File("xmltest/16bitbasetest.xml");       
        File outputFolder = new File("/newdefs/");
        
        try {
            //
            // Parse existing definitions
            //
            System.out.println("*******************************\n" +
                               "* Parsing old definitions ... *\n" +
                               "*******************************");
            DOMParser parser = new DOMParser();
            InputSource src = new InputSource(new FileInputStream(inputFile));
            parser.parse(src);
            Document doc = parser.getDocument();
            
            System.out.println("\n***************************************\n" +
                               "* Building old definition objects ... *\n" +
                               "***************************************");
            // Create old ROMs
            Vector<Rom> roms = handler.unmarshallXMLDefinition(doc.getDocumentElement());        
            
            // 
            // Create new document
            //
            System.out.println("\n*****************************\n" +
                               "* Generating XML output ... *\n" +
                               "*****************************");
            DefinitionBuilder builder = new DefinitionBuilder(roms, outputFolder);
            
            
            System.out.println("\n*************\n" +
                               "* Finished! *\n" +
                               "*************");
        
        } catch (Exception ex) {
            ex.printStackTrace();
        }        
    }  
    
}
