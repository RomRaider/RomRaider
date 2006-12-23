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

import enginuity.newmaps.definition.RomDefinitionHandler;
import enginuity.newmaps.xml.SaxParserFactory;
import static enginuity.util.MD5Checksum.getMD5Checksum;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;

public abstract class IndexUtil {
    
    public static void validateChecksums(File dir) { 
        Index index = null;
        try {
            index = getIndex(dir);
        } catch (Exception ex) {
            // Index file not found, create new
            new IndexBuilder(dir, index);
            return;
        }       
        
        // If no exceptions, iterate through index checking checksums
        Iterator it = index.iterator();
        while (it.hasNext()) {
            IndexItem item = (IndexItem)it.next();
            
            try {
                if (!item.getChecksum().equalsIgnoreCase(getMD5Checksum(item.getFile().getAbsolutePath()))) {

                }
            } catch (Exception ex) {
                // TODO: handle exception
                ex.printStackTrace();
            }
        }
    }
    
    
    
    public static Index getIndex(File dir) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dir.getAbsoluteFile() + "/" + IndexBuilder.INDEX_FILE_NAME));
            return (Index)ois.readObject();
        } catch (Exception ex) {
            return new Index();
        }
    }
    
    
    public static void testMemUsage() {
        try {
            //File dir = new File("/newdefs");
            File dir = new File("/netbeans/enginuity/xmltest");
            Index index = getIndex(dir);
            RomDefinitionHandler handler = new RomDefinitionHandler(index);
            
            Iterator it = index.iterator();
            int i = 0;
            long time = 0;
            while (it.hasNext()) {
                IndexItem item = (IndexItem)it.next();
                System.out.println("Adding " + item.getFile() + " (#" + ++i + ")");
                InputStream inputStream1 = new BufferedInputStream(new FileInputStream(item.getFile()));
                long start = System.currentTimeMillis();
                SaxParserFactory.getSaxParser().parse(inputStream1, handler);
                time += (System.currentTimeMillis() - start);
                
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        testMemUsage();
    }
    
}
