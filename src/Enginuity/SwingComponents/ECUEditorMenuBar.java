package Enginuity.SwingComponents;

import Enginuity.*;
import Enginuity.Exceptions.RomNotFoundException;
import Enginuity.Maps.ECUDefinitionCollection;
import Enginuity.Maps.Rom;
import Enginuity.Maps.XML.DOMRomUnmarshaller;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.management.modelmbean.XMLParseException;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ECUEditorMenuBar extends JMenuBar implements ActionListener {
    
    private JMenu      fileMenu   = new JMenu("File");
    private JMenuItem  openImage  = new JMenuItem("Open Image");
    private JMenuItem  saveImage  = new JMenuItem("Save Image");
    private JMenuItem  closeImage = new JMenuItem("Close Image");
    private JMenuItem  closeAll   = new JMenuItem("Close All Images");
    private JMenuItem  exit       = new JMenuItem("Exit");
    private ECUEditor  parent;
    
    public ECUEditorMenuBar(ECUEditor parent) {
        this.parent = parent;
        
        add(fileMenu);        
        fileMenu.setMnemonic('F');            
        openImage.setMnemonic('O');
        saveImage.setMnemonic('S');
        closeImage.setMnemonic('C');
        closeAll.setMnemonic('A');
        exit.setMnemonic('X');
        
        openImage.addActionListener(this);
        saveImage.addActionListener(this);
        closeImage.addActionListener(this);
        closeAll.addActionListener(this);
        exit.addActionListener(this);   
        
        fileMenu.add(openImage);  
        fileMenu.add(saveImage);
        fileMenu.add(closeImage);
        fileMenu.add(closeAll);
        fileMenu.add(new JSeparator());
        fileMenu.add(exit);  
        
        this.updateMenu();          
    }
    
    public void updateMenu() {
        String file = "";
        try { 
            file = " " + parent.getLastSelectedRom().getFileName();
        } catch (NullPointerException ex) { }
        if (file.equals("")) {
            saveImage.setEnabled(false);
            closeImage.setEnabled(false);
            closeAll.setEnabled(false);
        } else {
            saveImage.setEnabled(true);
            closeImage.setEnabled(true);
            closeAll.setEnabled(true);            
        }
        
        saveImage.setText("Save" + file);
        closeImage.setText("Close" + file);
    }

    public void actionPerformed(ActionEvent e)  {
        if (e.getSource() == openImage) {
            try {
                this.openImageDialog();
            } catch (XMLParseException ex) {
                new JOptionPane().showMessageDialog(parent, ex, "XML Parse Exception", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == saveImage) {
            try {
                this.saveImage(parent.getLastSelectedRom());
            } catch (XMLParseException ex) {
                new JOptionPane().showMessageDialog(parent, ex, "XML Parse Exception", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == closeImage) {
            this.closeImage();
        } else if (e.getSource() == closeAll) {
            this.closeAllImages();
        } else if (e.getSource() == exit) {
            System.exit(0);
        }
    }
    
    public void closeImage() {
        parent.closeImage();
    }
    
    public void closeAllImages() {
        parent.closeAllImages();
    }
    
    public void saveImage(Rom input) throws XMLParseException {
        if (parent.getLastSelectedRom() != null) {
            JFileChooser fc = new JFileChooser(parent.getSettings().getLastImageDir());

            if (fc.showSaveDialog(parent) == fc.APPROVE_OPTION) {
                boolean save = true;
                if (fc.getSelectedFile().exists()) {
                    if (new JOptionPane().showConfirmDialog(parent, fc.getSelectedFile().getName() + " already exists! Overwrite?") == JOptionPane.CANCEL_OPTION) {
                        save = false;
                    }                 
                }
                if (save) {
                    try {
                        byte[] output = parent.getLastSelectedRom().saveFile();
                        FileOutputStream fos = new FileOutputStream(fc.getSelectedFile());
                        fos.write(output);
                        fos.close();

                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }  
                    parent.closeImage();
                    openImage(fc.getSelectedFile());
                }
            }
        }
    }
    
    public void openImageDialog() throws XMLParseException {
        JFileChooser fc = new JFileChooser(parent.getSettings().getLastImageDir());
        if (fc.showOpenDialog(parent) == fc.APPROVE_OPTION) {
            openImage(fc.getSelectedFile());
        }
        parent.getSettings().setLastImageDir(fc.getCurrentDirectory());
    }
    
    public void openImage(File input) throws XMLParseException {            
            try {     
                ECUDefinitionCollection roms = new ECUDefinitionCollection();
                InputSource src = new InputSource(new FileInputStream(parent.getSettings().getEcuDefinitionFile()));                
                DOMRomUnmarshaller domUms = new DOMRomUnmarshaller();
                DOMParser parser = new DOMParser();                
                parser.parse(src);                
                Document doc = parser.getDocument();
                
                roms = domUms.unmarshallXMLDefinition(doc.getDocumentElement());                
                Rom ecuImage = roms.parseRom(input);
                ecuImage.setFileName(input.getName());
                
                parent.addRom(ecuImage);
                //System.out.println("File opened: " + ecuImage.getFileName());
                
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (SAXException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (RomNotFoundException ex) {
                new JOptionPane().showMessageDialog(parent, "ECU Definition Not Found", input.getName() + " - Error", JOptionPane.ERROR_MESSAGE);
            }
         
    }       
}