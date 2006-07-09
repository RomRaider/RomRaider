package enginuity.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.management.modelmbean.XMLParseException;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import enginuity.ECUEditor;
import enginuity.definitions.DefinitionEditor;
import enginuity.maps.Rom;
import enginuity.xml.DOMRomUnmarshaller;
import enginuity.xml.RomNotFoundException;

public class ECUEditorMenuBar extends JMenuBar implements ActionListener {
    
    private JMenu       fileMenu        = new JMenu("File");
    private JMenuItem   openImage       = new JMenuItem("Open Image");
    private JMenuItem   saveImage       = new JMenuItem("Save Image");
    private JMenuItem   refreshImage    = new JMenuItem("Refresh Image");
    private JMenuItem   closeImage      = new JMenuItem("Close Image");
    private JMenuItem   closeAll        = new JMenuItem("Close All Images");
    private JMenuItem   exit            = new JMenuItem("Exit");
    
    private JMenu       editMenu        = new JMenu("Edit");
    private JMenuItem   settings        = new JMenuItem("Settings");
    private JMenuItem   editDefinition  = new JMenuItem("Edit ECU Definitions");
    
    private JMenu       viewMenu        = new JMenu("View");
    private JMenuItem   romProperties   = new JMenuItem("ECU Image Properties");
    private ButtonGroup levelGroup      = new ButtonGroup();
    private JMenu       levelMenu       = new JMenu("User Level");
    private JRadioButtonMenuItem level1 = new JRadioButtonMenuItem("1 Beginner");
    private JRadioButtonMenuItem level2 = new JRadioButtonMenuItem("2 Intermediate");
    private JRadioButtonMenuItem level3 = new JRadioButtonMenuItem("3 Advanced");
    private JRadioButtonMenuItem level4 = new JRadioButtonMenuItem("4 Highest");
    private JRadioButtonMenuItem level5 = new JRadioButtonMenuItem("5 Debug Mode");
    
    private JMenu       helpMenu        = new JMenu("Help");
    private JMenuItem   about           = new JMenuItem("About Enginuity");
    
    private ECUEditor   parent;
    
    public ECUEditorMenuBar(ECUEditor parent) {
        this.parent = parent;
        
        // file menu items
        add(fileMenu);
        fileMenu.setMnemonic('F');            
        openImage.setMnemonic('O');
        saveImage.setMnemonic('S');
        refreshImage.setMnemonic('R');
        closeImage.setMnemonic('C');
        closeAll.setMnemonic('A');
        exit.setMnemonic('X');
        
        fileMenu.add(openImage);  
        fileMenu.add(saveImage);
        fileMenu.add(refreshImage);
        fileMenu.add(new JSeparator());
        fileMenu.add(closeImage);
        fileMenu.add(closeAll);
        fileMenu.add(new JSeparator());
        fileMenu.add(exit);  
        
        openImage.addActionListener(this);
        saveImage.addActionListener(this);
        refreshImage.addActionListener(this);
        closeImage.addActionListener(this);
        closeAll.addActionListener(this);
        exit.addActionListener(this);   
        
        // edit menu items
        add(editMenu);
        editMenu.setMnemonic('E');
        settings.setMnemonic('S');
        editDefinition.setMnemonic('E');
        editMenu.add(new JSeparator());
        editMenu.add(settings);
        editMenu.add(editDefinition);
        settings.addActionListener(this);
        editDefinition.addActionListener(this);
        
        // view menu items
        add(viewMenu);
        viewMenu.setMnemonic('V');
        romProperties.setMnemonic('P');
        levelMenu.setMnemonic('U');
        level1.setMnemonic('1');
        level2.setMnemonic('2');
        level3.setMnemonic('3');
        level4.setMnemonic('4');
        level5.setMnemonic('5');
        viewMenu.add(romProperties);
        viewMenu.add(levelMenu);
        levelMenu.add(level1);
        levelMenu.add(level2);
        levelMenu.add(level3);
        levelMenu.add(level4);
        levelMenu.add(level5);
        romProperties.addActionListener(this);
        level1.addActionListener(this);
        level2.addActionListener(this);
        level3.addActionListener(this);
        level4.addActionListener(this);
        level5.addActionListener(this);
        levelGroup.add(level1);
        levelGroup.add(level2);
        levelGroup.add(level3);
        levelGroup.add(level4);
        levelGroup.add(level5);
        // select correct userlevel button
        if      (parent.getSettings().getUserLevel() == 1) level1.setSelected(true);
        else if (parent.getSettings().getUserLevel() == 2) level2.setSelected(true);
        else if (parent.getSettings().getUserLevel() == 3) level3.setSelected(true);
        else if (parent.getSettings().getUserLevel() == 4) level4.setSelected(true);
        else if (parent.getSettings().getUserLevel() == 5) level5.setSelected(true);
        
        // help menu stuff
        add(helpMenu);
        helpMenu.setMnemonic('H');
        about.setMnemonic('A');
        helpMenu.add(about);
        about.addActionListener(this);               
        
        // disable unused buttons! 0.3.1
        about.setEnabled(false);
        editDefinition.setEnabled(false);
        
        this.updateMenu();          
    }
    
    public void updateMenu() {
        String file = "";
        try { 
            file = " " + parent.getLastSelectedRom().getFileName() + " ";
        } catch (NullPointerException ex) { }
        if (file.equals("")) {
            saveImage.setEnabled(false);
            closeImage.setEnabled(false);
            closeAll.setEnabled(false);
            romProperties.setEnabled(false);
        } else {
            saveImage.setEnabled(true);
            closeImage.setEnabled(true);
            closeAll.setEnabled(true);   
            romProperties.setEnabled(true);         
        }
        
        saveImage.setText("Save" + file);
        refreshImage.setText("Refresh" + file);
        closeImage.setText("Close" + file);
        romProperties.setText(file + "Properties");
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openImage) {
            try {
                this.openImageDialog();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, new DebugPanel(ex,
                        parent.getSettings().getSupportURL()), "Exception", JOptionPane.ERROR_MESSAGE);
            }
            
        } else if (e.getSource() == saveImage) {
            try {
                this.saveImage(parent.getLastSelectedRom());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, new DebugPanel(ex,
                        parent.getSettings().getSupportURL()), "Exception", JOptionPane.ERROR_MESSAGE);
            }
            
        } else if (e.getSource() == closeImage) {
            this.closeImage();
            
        } else if (e.getSource() == closeAll) {
            this.closeAllImages();
            
        } else if (e.getSource() == exit) {
            System.exit(0);
            
        } else if (e.getSource() == romProperties) {
            JOptionPane.showMessageDialog(parent, (Object)(new RomPropertyPanel(parent.getLastSelectedRom())),
                    parent.getLastSelectedRom().getRomIDString() + " Properties", JOptionPane.INFORMATION_MESSAGE);
            
        } else if (e.getSource() == refreshImage) {
            try {
                refreshImage();
                
            } catch (Exception ex) {            
                JOptionPane.showMessageDialog(parent, new DebugPanel(ex,
                        parent.getSettings().getSupportURL()), "Exception", JOptionPane.ERROR_MESSAGE);
            }
            
        } else if (e.getSource() == settings) {
            SettingsForm form = new SettingsForm(parent);
            form.setLocationRelativeTo(parent);
            form.setVisible(true);
            
        } else if (e.getSource() == level1) {
            parent.setUserLevel(1);
            
        } else if (e.getSource() == level2) {
            parent.setUserLevel(2);
            
        } else if (e.getSource() == level3) {
            parent.setUserLevel(3);
            
        } else if (e.getSource() == level4) {
            parent.setUserLevel(4);
            
        } else if (e.getSource() == level5) {
            parent.setUserLevel(5);
            
        } else if (e.getSource() == editDefinition) {
            new DefinitionEditor(parent);
            
        }
    }
    
    public void refreshImage() throws Exception {
		if (parent.getLastSelectedRom() != null) {
			File file = parent.getLastSelectedRom().getFullFileName();
			parent.closeImage();
			openImage(file);
		}
	}
    
    public void closeImage() {
        parent.closeImage();
    }
    
    public void closeAllImages() {
        parent.closeAllImages();
    }
    
    public void saveImage(Rom input) throws XMLParseException, Exception {
        if (parent.getLastSelectedRom() != null) {
            JFileChooser fc = new JFileChooser(parent.getSettings().getLastImageDir());
            fc.setFileFilter(new ECUImageFilter());

            if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                boolean save = true;
                if (fc.getSelectedFile().exists()) {
                    if (JOptionPane.showConfirmDialog(parent, fc.getSelectedFile().getName() + " already exists! Overwrite?") == JOptionPane.CANCEL_OPTION) {
                        save = false;
                    }                 
                }
                if (save) {
                    byte[] output = parent.getLastSelectedRom().saveFile();
                    FileOutputStream fos = null;
                    try {
                    	fos = new FileOutputStream(fc.getSelectedFile());
                        fos.write(output);
                    }
                    finally {
                    	if (fos != null) {
                    		fos.close();
                    	}
                    }

                    parent.getLastSelectedRom().setFullFileName(fc.getSelectedFile().getAbsoluteFile());
                    parent.setLastSelectedRom(parent.getLastSelectedRom());
                }
            }
        }
    }
    
    public void openImageDialog() throws XMLParseException, Exception {
        JFileChooser fc = new JFileChooser(parent.getSettings().getLastImageDir());
        fc.setFileFilter(new ECUImageFilter());        
        
        if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            openImage(fc.getSelectedFile());
            parent.getSettings().setLastImageDir(fc.getCurrentDirectory());
        }
    }
    
    public void openImage(File inputFile) throws XMLParseException, Exception {        
        JProgressPane progress = new JProgressPane(parent, "Opening file...", "Parsing ECU definitions...");
        try {     
            parent.repaintPanel();
            progress.update("Parsing ECU definitions...", 0);
            
            InputSource src = new InputSource(new FileInputStream(parent.getSettings().getEcuDefinitionFile()));                
            DOMRomUnmarshaller domUms = new DOMRomUnmarshaller();
            DOMParser parser = new DOMParser();                
            parser.parse(src);      
            Document doc = parser.getDocument();
            byte[] input = readFile(inputFile);
                        
            progress.update("Finding ECU definition...", 10);
            
            Rom rom = domUms.unmarshallXMLDefinition(doc.getDocumentElement(), input, progress);
            progress.update("Populating tables...", 50);
            rom.populateTables(input, progress);
            rom.setFileName(inputFile.getName());
            
            progress.update("Finalizing...", 90);     
            parent.addRom(rom);                   
            rom.setFullFileName(inputFile);
            
        } catch (RomNotFoundException ex) {
            JOptionPane.showMessageDialog(parent, "ECU Definition Not Found", "Error Loading " + inputFile.getName(), JOptionPane.ERROR_MESSAGE);
            
        } catch (StackOverflowError ex) {
            JOptionPane.showMessageDialog(parent, "Looped \"base\" attribute in XML definitions.", "Error Loading ROM", JOptionPane.ERROR_MESSAGE);
            
        } finally {
            progress.dispose();
        }
    }
    
    private byte[] readFile(File inputFile) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(inputFile);
			byte[] buf = new byte[8192];
			int bytesRead = 0;
			while ((bytesRead = fis.read(buf)) != -1) {
				baos.write(buf, 0, bytesRead);
			}
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
		return baos.toByteArray();
	}
}