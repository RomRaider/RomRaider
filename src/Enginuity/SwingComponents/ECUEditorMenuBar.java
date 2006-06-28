package Enginuity.SwingComponents;

import Enginuity.*;
import Enginuity.DefinitionBuilder.DefinitionBuilder;
import Enginuity.XML.RomNotFoundException;
import Enginuity.Maps.Rom;
import Enginuity.XML.DOMRomUnmarshaller;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
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
    private JMenuItem  refreshImage = new JMenuItem("Refresh Image");
    private JMenuItem  closeImage = new JMenuItem("Close Image");
    private JMenuItem  closeAll   = new JMenuItem("Close All Images");
    private JMenuItem  exit       = new JMenuItem("Exit");
    private JMenu      editMenu   = new JMenu("Edit");
    private JMenuItem  tableDef   = new JMenuItem("Table Definition Generator");
    private JMenu      viewMenu   = new JMenu("View");
    private JMenuItem  romProperties = new JMenuItem("ECU Image Properties");
    private ECUEditor  parent;
    
    public ECUEditorMenuBar(ECUEditor parent) {
        this.parent = parent;
        
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
        tableDef.addActionListener(this);
        
        add(editMenu);
        editMenu.setMnemonic('E');
        tableDef.setMnemonic('T');
        editMenu.add(tableDef);
        editMenu.addActionListener(this);
        
        add(viewMenu);
        viewMenu.setMnemonic('V');
        romProperties.setMnemonic('P');
        viewMenu.add(romProperties);
        romProperties.addActionListener(this);
        
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
                new JOptionPane().showMessageDialog(parent, ex.getStackTrace(), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == saveImage) {
            try {
                this.saveImage(parent.getLastSelectedRom());
            } catch (Exception ex) {
                new JOptionPane().showMessageDialog(parent, ex.getStackTrace(), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == closeImage) {
            this.closeImage();
        } else if (e.getSource() == closeAll) {
            this.closeAllImages();
        } else if (e.getSource() == exit) {
            System.exit(0);
        } else if (e.getSource() == romProperties) {
            new JOptionPane().showMessageDialog(parent, (Object)(new RomPropertyPanel(parent.getLastSelectedRom())),
                    parent.getLastSelectedRom().getRomIDString() + " Properties", JOptionPane.INFORMATION_MESSAGE);
        } else if (e.getSource() == refreshImage) {
            try {
                refreshImage();
            } catch (Exception ex) {
                new JOptionPane().showMessageDialog(parent, ex.getStackTrace(), "Exception", JOptionPane.ERROR_MESSAGE);              
            }
        } else if (e.getSource() == tableDef) {
            try {
                new DefinitionBuilder(parent.getLastSelectedRom(), parent.getSettings().getLastImageDir());
            } catch (NullPointerException ex) {
                //new DefinitionBuilder();
            }
        }
    }
    
    public void refreshImage() throws Exception {
        File file = parent.getLastSelectedRom().getFullFileName();
        parent.closeImage();
        openImage(file);
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

            if (fc.showSaveDialog(parent) == fc.APPROVE_OPTION) {
                boolean save = true;
                if (fc.getSelectedFile().exists()) {
                    if (new JOptionPane().showConfirmDialog(parent, fc.getSelectedFile().getName() + " already exists! Overwrite?") == JOptionPane.CANCEL_OPTION) {
                        save = false;
                    }                 
                }
                if (save) {
                    byte[] output = parent.getLastSelectedRom().saveFile();
                    FileOutputStream fos = new FileOutputStream(fc.getSelectedFile());
                    fos.write(output);
                    fos.close();

                    parent.closeImage();
                    openImage(fc.getSelectedFile());
                }
            }
        }
    }
    
    public void openImageDialog() throws XMLParseException, Exception {
        JFileChooser fc = new JFileChooser(parent.getSettings().getLastImageDir());
        fc.setFileFilter(new ECUImageFilter());        
        
        if (fc.showOpenDialog(parent) == fc.APPROVE_OPTION) {
            openImage(fc.getSelectedFile());
        }
        parent.getSettings().setLastImageDir(fc.getCurrentDirectory());
    }
    
    public void openImage(File inputFile) throws XMLParseException, Exception {            
        try {     
            InputSource src = new InputSource(new FileInputStream(parent.getSettings().getEcuDefinitionFile()));                
            DOMRomUnmarshaller domUms = new DOMRomUnmarshaller();
            DOMParser parser = new DOMParser();                
            parser.parse(src);                
            Document doc = parser.getDocument();
            FileInputStream fis = new FileInputStream(inputFile);
            byte[] input = new byte[fis.available()];                
            fis.read(input);     
            fis.close();

            Rom rom = domUms.unmarshallXMLDefinition(doc.getDocumentElement(), input);
            rom.populateTables(input);
            rom.setFileName(inputFile.getName());

            if (rom.getRomID().isObsolete()) {
                // insert JOptionPane with link to ECU revision wiki here
            }

            parent.addRom(rom);
            rom.setFullFileName(inputFile);

        } catch (RomNotFoundException ex) {
            new JOptionPane().showMessageDialog(parent, "ECU Definition Not Found", "Error Loading " + inputFile.getName(), JOptionPane.ERROR_MESSAGE);
        } catch (StackOverflowError ex) {
            new JOptionPane().showMessageDialog(parent, "Malformed \"base\" attribute in XML definitions.", "Error Loading ROM", JOptionPane.ERROR_MESSAGE);
        }         
    }       
}