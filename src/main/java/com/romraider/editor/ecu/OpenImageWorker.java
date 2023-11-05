/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

import org.apache.log4j.Logger;
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
    private static final Logger LOGGER = Logger.getLogger(OpenImageWorker.class);
    private final File inputFile;
    private Rom rom;
    private String finalStatus;
   

    public OpenImageWorker(File inputFile) {
        this.inputFile = inputFile;
    }
    
    public Rom getRom()
    {
    	return rom;
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

          editor.getStatusPanel().setStatus(
                  ECUEditor.rb.getString("DONELOAD"));
          setProgress(95);

	      if(rom.getNumChecksumsManagers() == 0) {
	    	  finalStatus = ECUEditor.rb.getString("STATUSREADY");
	      }
	      else {
		      editor.getStatusPanel().setStatus(
			             ECUEditor.rb.getString("CHECKSUM"));

	    	  finalStatus = String.format(ECUEditor.rb.getString("CHECKSUMSTATE"),
	    			  rom.validateChecksum(), rom.getTotalAmountOfChecksums());
	      }

	      this.rom = rom;     
    }

    private Document createDocument(File f) throws Exception {
	    Document doc = null;
	    FileInputStream fileStream = null;

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

		    	if(l != null)
		    		doc = l.convertToDocumentTree(f);

	    		if(doc == null)
	    			throw new SAXParseException(ECUEditor.rb.getString("UNREADABLEDEF"), null);
		    }
			//Default case
		    else {
		    	doc = docBuilder.parse(fileStream, f.getAbsolutePath());
		   }
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

    private void showExceptionPopup(Exception ex, File defFile) {

    	String errorMessage = defFile.getName() + ": " + (ex.getMessage() == null || ex.getMessage().isEmpty() ?
        		ECUEditor.rb.getString("LOADEXCEPTION") : ex.getMessage());

        final String errorLoading = MessageFormat.format(
                ECUEditor.rb.getString("ERRORFILE"),
                inputFile.getName());

        ECUEditor editor = ECUEditorManager.getECUEditor();
        showMessageDialog(editor,
        		errorMessage,
                errorLoading,
                ERROR_MESSAGE);

        if(ex instanceof SAXException)
        	LOGGER.error(errorMessage);
        else
        	ex.printStackTrace();
    }
    
	 private Rom openRomWithDefinition(File f, Document doc, Node romNode, byte[] input) {
	        ECUEditor editor = ECUEditorManager.getECUEditor();
	        final String errorLoading = MessageFormat.format(
	                ECUEditor.rb.getString("ERRORFILE"),
	                inputFile.getName());

	        try {
	            Rom rom = new DOMRomUnmarshaller().unmarshallXMLDefinition(f, doc.getDocumentElement(), romNode,
	            		input, editor.getStatusPanel());
	    	    rom.setDocument(doc);
	    	    rom.setDefinitionPath(f);
	    	    loadRom(rom, input);

        } catch (StackOverflowError ex) {
        	ex.printStackTrace();
            // handles looped inheritance, which will use up all available memory
            showMessageDialog(editor,
                    ECUEditor.rb.getString("LOOPEDBASE"),
                    errorLoading,
                    ERROR_MESSAGE);
        } catch (OutOfMemoryError ome) {
        	ome.printStackTrace();
            // handles Java heap space issues when loading multiple Roms.
            showMessageDialog(editor,
                    ECUEditor.rb.getString("OUTOFMEMORY"),
                    errorLoading,
                    ERROR_MESSAGE);
        } catch (Exception ex) {
            showExceptionPopup(ex, f);
            return null;
        }

        return null;
    }

    @Override
    protected Void doInBackground() throws Exception {
    	Thread.currentThread().setName("Open Image Thread");

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

            Node romNode = null;
            Document doc = null;

            try {
            	doc = createDocument(f);
				romNode = new DOMRomUnmarshaller().checkDefinitionMatch(doc.getDocumentElement(), input);
            }
            catch(Exception e) {
            	showExceptionPopup(e, f);
            }

            if(romNode != null) {
            	openRomWithDefinition(f, doc, romNode, input);
            	found = true;
            	break;
            }
         }

        if(!found) {
        	showNoDefinitionFoundPopup(input);
        }

		return null;
    }

    private void showNoDefinitionFoundPopup(byte[] input) {
    	// no ECU definitions configured - let user choose one
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

                Node romNode;
                Document doc = null;

				try {
					doc = createDocument(file);
					romNode = new DOMRomUnmarshaller().checkDefinitionMatch(doc.getDocumentElement(), input);
				} catch (Exception e) {
					showExceptionPopup(e, file);
					return;
				}

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
	                	Node n = DOMRomUnmarshaller.findFirstRomNode(doc.getDocumentElement());
	                	openRomWithDefinition(file, doc, n, input);
	                }
            	}
            	else {
                	openRomWithDefinition(file, doc, romNode, input);
            	}
            }
        }
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

        //Add the rom in the main thread
        if(rom != null) {
        	editor.addRom(rom);
        	rom = null;

		    editor.getStatusPanel().update(finalStatus, 0);
	        editor.setCursor(null);
	        editor.refreshAfterNewRom();
        }
        else {
        	editor.getStatusPanel().update(ECUEditor.rb.getString("STATUSREADY"), 0);
        }
    }
}