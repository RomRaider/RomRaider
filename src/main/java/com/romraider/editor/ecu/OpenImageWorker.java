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

package com.romraider.editor.ecu;
import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JOptionPane.showOptionDialog;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.romraider.Settings;
import com.romraider.maps.Rom;
import com.romraider.swing.DefinitionFilter;
import com.romraider.util.SettingsManager;
import com.romraider.xml.DOMRomUnmarshaller;
import com.romraider.xml.ConversionLayer.ConversionLayer;
import com.romraider.xml.ConversionLayer.ConversionLayerFactory;

public class OpenImageWorker extends SwingWorker<Void, Void> {
    private final File inputFile;
    
    public OpenImageWorker(File inputFile) {
        this.inputFile = inputFile;
    }
    
    private void loadRom(Rom rom, byte[] input) {
          ECUEditor editor = ECUEditorManager.getECUEditor();
    	  editor.getStatusPanel().setStatus(
                  ECUEditor.rb.getString("POPULATING"));
          setProgress(50);

          rom.setFullFileName(inputFile);
          rom.populateTables(input, editor.getStatusPanel());

          editor.getStatusPanel().setStatus(
                  ECUEditor.rb.getString("FINALIZING"));
          setProgress(90);

          editor.addRom(rom);
          editor.refreshTableCompareMenus();

          editor.getStatusPanel().setStatus(
                  ECUEditor.rb.getString("DONELOAD"));
          setProgress(95);

           editor.getStatusPanel().setStatus(
                  ECUEditor.rb.getString("CHECKSUM"));
            rom.validateChecksum();
          
          setProgress(100);              
    }
    
    private Document createDocument(File f) {
        ECUEditor editor = ECUEditorManager.getECUEditor();
	    Document doc = null;
	    FileInputStream fileStream = null;  
	    
	    final String errorLoading = MessageFormat.format(ECUEditor.rb.getString("ERRORFILE"),
	    		inputFile.getName());
	    
	    try {
		    fileStream = new FileInputStream(f); 
		    
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    factory.setNamespaceAware(true);
		    factory.setXIncludeAware(true);
		    DocumentBuilder docBuilder = factory.newDocumentBuilder();
		    
		    //Check if definition is standard or
		    //if it has to be converted first                
		    if(ConversionLayerFactory.requiresConversionLayer(f)) {
		    	ConversionLayer l = ConversionLayerFactory.getConversionLayerForFile(f);
		    	if(l != null) {
		    		doc = l.convertToDocumentTree(f);
		    		
		    		if(doc == null) {
		    			throw new SAXParseException("Unknown file format!", null);
		    		}
		    	}		    	
		    }        
			//Default case
		    else {
		    	doc = docBuilder.parse(fileStream, f.getAbsolutePath());
		    	}
		   }
	    catch (SAXException spe) {
            // catch general parsing exception - enough people don't unzip the defs that a better error message is in order
            showMessageDialog(editor,
                    ECUEditor.rb.getString("UNREADABLEDEF"),
                    errorLoading,
                    ERROR_MESSAGE);
	    } catch (ParserConfigurationException e) {
			e.printStackTrace();
	    } catch (IOException e) {
			e.printStackTrace();
		}
	    finally {
			try {
				fileStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
	    return doc;
    }
    
    private Node checkDefinitionMatch(File f, byte[] input) {
        ECUEditor editor = ECUEditorManager.getECUEditor();
        final String errorLoading = MessageFormat.format(ECUEditor.rb.getString("ERRORFILE"),
        		inputFile.getName());

        try {  
		    Document doc = createDocument(f);
            Node romNode = new DOMRomUnmarshaller().checkDefinitionMatch(doc.getDocumentElement(), input);
            
	        if(romNode != null) return romNode;
	        else 
	        	return null;    
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        showMessageDialog(editor,
	                ECUEditor.rb.getString("LOADEXCEPTION"),
	                errorLoading,
	                ERROR_MESSAGE);
	        return null;
	    }
      }
	        
	 private Rom openRomWithDefinition(File f, Node romNode, byte[] input) {
	        ECUEditor editor = ECUEditorManager.getECUEditor();
	        
	        final String errorLoading = MessageFormat.format(
	                ECUEditor.rb.getString("ERRORFILE"),
	                inputFile.getName());
	        try {  
			    Rom rom = null;
			    Document doc = createDocument(f);
	            rom = new DOMRomUnmarshaller().unmarshallXMLDefinition(doc.getDocumentElement(), romNode,
	            		input, editor.getStatusPanel());
	            
	    	    rom.setDocument(doc);    	    
	    	    loadRom(rom, input);	    	    
        } catch (StackOverflowError ex) {
            // handles looped inheritance, which will use up all available memory
            showMessageDialog(editor,
                    ECUEditor.rb.getString("LOOPEDBASE"),
                    errorLoading,
                    ERROR_MESSAGE);
        } catch (OutOfMemoryError ome) {
            // handles Java heap space issues when loading multiple Roms.
            showMessageDialog(editor,
                    ECUEditor.rb.getString("OUTOFMEMORY"),
                    errorLoading,
                    ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessageDialog(editor,
                    ECUEditor.rb.getString("LOADEXCEPTION"),
                    errorLoading,
                    ERROR_MESSAGE);
            return null;
        }
        
        return null;
    }
    
    @Override
    protected Void doInBackground() throws Exception {
        ECUEditor editor = ECUEditorManager.getECUEditor();
        Settings settings = SettingsManager.getSettings();
        
        editor.getStatusPanel().setStatus(
                ECUEditor.rb.getString("STATUSPARSING"));
        setProgress(0);

        byte[] input = ECUEditor.readFile(inputFile);

        editor.getStatusPanel().setStatus(
                ECUEditor.rb.getString("STATUSFINDING"));
        setProgress(10);

        boolean found = false;
        
        // parse ecu definition files until result found
        for (int i = 0; i < settings.getEcuDefinitionFiles().size(); i++) {
        	File f = settings.getEcuDefinitionFiles().get(i);
        	
            if (!f.exists()) {
                showMessageDialog(editor,
                        MessageFormat.format(
                                ECUEditor.rb.getString("MISSINGMOVED"),
                                settings.getEcuDefinitionFiles().get(i).getAbsolutePath()),
                        MessageFormat.format(
                                ECUEditor.rb.getString("MISSINGFILE"),
                                settings.getEcuDefinitionFiles().get(i).getName()),
                        ERROR_MESSAGE);
                continue;
            }      
            
            Node romNode = checkDefinitionMatch(f, input);
            
            if(romNode != null) {
            	openRomWithDefinition(f, romNode, input);
            	found = true;
            	break;
            }
         }
        
        if(!found) {
        	showNoDefinitionFoundPopup(input);
        }
        
		return null;    
    }
    
    private String showNoDefinitionFoundPopup(byte[] input) {    	
    	// no ECU definitions configured - let user choose to get latest or configure later
        Object[] options = {ECUEditor.rb.getString("YES"), ECUEditor.rb.getString("NO")};
        int answer = showOptionDialog(null,
        		ECUEditor.rb.getString("DEFNOTFOUND"),
        		ECUEditor.rb.getString("EDCONFIG"),
                DEFAULT_OPTION,
                WARNING_MESSAGE,
                null,
                options,
                options[0]);
        
        if (answer == 0) {
    		Settings settings = SettingsManager.getSettings();
            JFileChooser fc = new JFileChooser(settings.getLastDefinitionDir());
            fc.setFileFilter(new DefinitionFilter());

            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
            	settings.setLastDefinitionDir(file.getParentFile());
            	
                Node romNode = checkDefinitionMatch(file, input);
         	
            	if(romNode == null) {
	                int answerForceLoad = showOptionDialog(null,
	                		ECUEditor.rb.getString("DEFNOMATCH"),
	                		ECUEditor.rb.getString("EDCONFIG"),
	                        DEFAULT_OPTION,
	                        JOptionPane.INFORMATION_MESSAGE,
	                        null,
	                        options,
	                        options[0]);
	                
	                if(answerForceLoad == 0) {
	                	Document doc = createDocument(file);
	                	Node n = DOMRomUnmarshaller.findFirstRomNode(doc.getDocumentElement());
	                	openRomWithDefinition(file, n, input);
	                }
            	}
            	else {
                	openRomWithDefinition(file, romNode, input);
            	}
            }
        }
        
        return null;
    }

    public void propertyChange(PropertyChangeEvent evnt)
    {
        SwingWorker<?, ?> source = (SwingWorker<?, ?>) evnt.getSource();
        if (null != source && "state".equals( evnt.getPropertyName() )
                && (source.isDone() || source.isCancelled() ) )
        {
            source.removePropertyChangeListener(ECUEditorManager.getECUEditor().getStatusPanel());
        }
    }

    @Override
    public void done() {
        ECUEditor editor = ECUEditorManager.getECUEditor();
        editor.getStatusPanel().setStatus(ECUEditor.rb.getString("STATUSREADY"));
        setProgress(0);
        editor.setCursor(null);
        editor.refreshUI();
        System.gc();
    }
}