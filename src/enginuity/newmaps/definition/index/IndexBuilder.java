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
import enginuity.newmaps.ecumetadata.RomMetadata;
import enginuity.newmaps.xml.SaxParserFactory;
import static enginuity.util.MD5Checksum.getMD5Checksum;
import enginuity.util.exception.NameableNotFoundException;
import org.xml.sax.SAXParseException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;

public class IndexBuilder {

    public static final String INDEX_FILE_NAME = "index.dat";
    public static final String MEMMODEL_FILE_NAME = "memmodels.xml";

    private File dir;
    private Index index;

    public IndexBuilder(File dir, Index index) {
        this.dir = dir;
        this.index = index;

        // Clean up existing defs
        index.cleanup();

        // Process all definition files
        traverse(dir);

        // Output index
        save(index, dir);
    }

    private void traverse(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                traverse(new File(file, children[i]));
            }
        } else {
            processFile(file);
        }
    }

    private void processFile(File file) {
        if (!file.getName().equalsIgnoreCase(INDEX_FILE_NAME) &&
                !file.getName().equalsIgnoreCase(MEMMODEL_FILE_NAME) &&
                file.getName().length() > 3 && file.getName().endsWith(".xml")) {

            IndexItem idxItem = null;
            try {
                idxItem = index.get(file);
            } catch (NameableNotFoundException ex) {
                idxItem = new IndexItem();
            }

            if (!index.fileCurrent(file, idxItem)) {

                try {

                    IndexHandler handler = new IndexHandler();
                    SaxParserFactory.getSaxParser().parse(new BufferedInputStream(new FileInputStream(file)), handler);
                    IndexItem item = handler.getItem();
                    item.setFile(file);
                    index.add(item);

                } catch (SAXParseException ex) {
                    // Skip file, not valid xml

                } catch (Exception ex) {
                    // TODO: Handle exceptions
                    ex.printStackTrace();
                }
            }
        }
    }

    private static void save(Index index, File file) {

        index.fixInheritance();
        try {
            FileOutputStream fos = new FileOutputStream(file + "/" + INDEX_FILE_NAME);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(index);
            oos.flush();
            oos.close();

        } catch (Exception ex) {
            // TODO: Exception handling
            ex.printStackTrace();
        }
    }


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
            IndexItem item = (IndexItem) it.next();

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
            return (Index) ois.readObject();
        } catch (Exception ex) {
            return new Index();
        }
    }


    public static void testMemUsage() {
        try {
            File dir = new File("/newdefs");            
            //File dir = new File("./xmltest");
            Index index = getIndex(dir);
            RomDefinitionHandler handler = new RomDefinitionHandler(index);

            Iterator it = index.iterator();
            int i = 0;
            long time = 0;
            while (it.hasNext()) {
                IndexItem item = (IndexItem) it.next();
                System.out.println("Adding " + item.getFile() + " (#" + ++i + ")");
                InputStream inputStream1 = new BufferedInputStream(new FileInputStream(item.getFile()));
                long start = System.currentTimeMillis();
                SaxParserFactory.getSaxParser().parse(inputStream1, handler);
                //System.out.println(handler.getRom());
                time += (System.currentTimeMillis() - start);
                
                saveDef(handler.getRom());

            }
            

        } catch (Exception ex) {
            ex.printStackTrace();
        }
            
    }
    
    
    public static void saveDef(RomMetadata rom) {
        
        try {
            FileOutputStream fileOut = new FileOutputStream("/sizetest/" + rom.getName() + ".dat");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            System.out.println("Writing Hashtable Object...");
            out.writeObject(rom);

            System.out.println("Closing all output streams...\n");
            out.close();
            fileOut.close();  
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }                   
    }


    public static void main(String[] args) {
        /*try {
            //File file = new File("/newdefs");
            File file = new File("./xmltest");
            IndexBuilder b = new IndexBuilder(file, getIndex(file));
        } catch (Exception ex) {
            ex.printStackTrace();
        } */
        
        testMemUsage();
    }
}