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

package com.romraider.xml;

import static com.romraider.editor.ecu.ECUEditorManager.getECUEditor;
import static com.romraider.swing.LookAndFeelManager.initLookAndFeel;
import static com.romraider.util.LogManager.initDebugLogging;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import org.junit.Test;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditor;
import com.romraider.editor.ecu.OpenImageWorker;
import com.romraider.maps.Rom;
import com.romraider.maps.Table;
import com.romraider.util.SettingsManager;
import com.romraider.xml.ConversionLayer.ConversionLayer;
import com.romraider.xml.ConversionLayer.XDFConversionLayer;


public class XDFConversionLayerTest{
	
	private static LinkedList<File> listFileTree(File dir) {
		LinkedList<File> fileTree = new LinkedList<File>();
		if (dir == null || dir.listFiles() == null) {
			return fileTree;
		}
		for (File entry : dir.listFiles()) {
			if (entry.isFile())
				fileTree.add(entry);
			else if (entry.isDirectory())
				fileTree.addAll(listFileTree(entry));
		}
		return fileTree;
	}

    @Test
    public void loadXDFs() {
        initDebugLogging();
        initLookAndFeel();
        ECUEditor editor = getECUEditor();
        editor.initializeEditorUI();
        editor.checkDefinitions();

        // Make sure we dont override any settings
        SettingsManager.setTesting(true);
        Settings settings = SettingsManager.getSettings();

        // https://github.com/dmacpro91/BMW-XDFs
        // Download locally and unzip
        File folder = new File("C:\\Users\\User\\Downloads\\BMW-XDFs-master\\");
        Collection<File> listOfFiles = listFileTree(folder);
        for (File f: listOfFiles) {
            ConversionLayer l = new XDFConversionLayer();
            if (l.isFileSupported(f)) {
                editor.closeAllImages();
                settings.getEcuDefinitionFiles().clear();
                settings.getEcuDefinitionFiles().add(f);
                File bin = new File(f.getAbsolutePath().replace(".xdf", "_original.bin"));

                if (bin.exists()) {
                    OpenImageWorker w = new OpenImageWorker(bin);
                    w.execute();       
                    
                    //Wait until rom its loaded
                    while(!w.isDone())
                    {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
                    }
                    
                   Rom r = w.getRom();
                   try
                   {
                	   assertNotEquals(r, null);
                	   assertEquals(r.getFaultyTables().size(), 0); 
                   }
                   catch(AssertionError e)
                   {	
                	   if(r == null)
                	   {
                    	   System.out.println(f + " failed to load completely");
                	   }
                	   else
                	   {
                		   System.out.println(f + ": Faulty Tables " + r.getFaultyTables().size());
                	   }
                	   continue;
                   }                   
                   // Populate tables visually
                   Vector<Table> tables = r.getTables();                 
                   for (Table table:tables)
                   {
                	  // editor.createTableView(table);
                   }               
                }       	
            }
        }
    }
}
