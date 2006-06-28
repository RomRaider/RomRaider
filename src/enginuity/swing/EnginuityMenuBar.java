package Enginuity.SwingComponents;

import Enginuity.*;
import Enginuity.Exceptions.RomNotFoundException;
import Enginuity.Maps.ECUDefinitionCollection;
import Enginuity.Maps.Rom;
import Enginuity.Maps.XML.DOMRomUnmarshaller;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import javax.management.modelmbean.XMLParseException;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.xml.sax.SAXException;


public class EnginuityMenuBar extends JMenuBar implements ActionListener {
    
    private JMenu      fileMenu   = new JMenu("File");
    private JMenuItem  openImage  = new JMenuItem("Open Image");
    private JMenuItem  saveImage  = new JMenuItem("Save Image");
    private JMenuItem  closeImage = new JMenuItem("Close Image");
    private JMenuItem  exit       = new JMenuItem("Exit");
    private ECUEditor  parent;
    
    public EnginuityMenuBar(ECUEditor parent) {
        this.parent = parent;
        
        add(fileMenu);        
        fileMenu.setMnemonic('F');            
        openImage.setMnemonic('O');
        saveImage.setMnemonic('S');
        closeImage.setMnemonic('C');
        exit.setMnemonic('X');
        
        openImage.addActionListener(this);
        saveImage.addActionListener(this);
        closeImage.addActionListener(this);
        exit.addActionListener(this);   
        
        fileMenu.add(openImage);  
        fileMenu.add(saveImage);
        fileMenu.add(closeImage);
        fileMenu.add(new JSeparator());
        fileMenu.add(exit);       
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openImage) {
            openImage();
        } else if (e.getSource() == saveImage) {
            this.saveImage(parent.getLastSelectedRom());
        } else if (e.getSource() == closeImage) {
            // close image code here
        } else if (e.getSource() == exit) {
            System.exit(0);
        }
    }
    
    public void saveImage(Rom input) {
        JFileChooser fc = new JFileChooser(parent.getSettings().getLastImageDir());
        
        if (fc.showSaveDialog(parent) == fc.APPROVE_OPTION) {
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
        }
    }
    
    public void openImage() {        
        JFileChooser fc = new JFileChooser(parent.getSettings().getLastImageDir());
        if (fc.showOpenDialog(parent) == fc.APPROVE_OPTION) {
            
            try {     
                ECUDefinitionCollection roms = new ECUDefinitionCollection();
                InputSource src = new InputSource(new FileInputStream(parent.getSettings().getEcuDefinitionFile()));                
                DOMRomUnmarshaller domUms = new DOMRomUnmarshaller();
                DOMParser parser = new DOMParser();                
                parser.parse(src);                
                Document doc = parser.getDocument();
                
                roms = domUms.unmarshallXMLDefinition(doc.getDocumentElement());                
                Rom ecuImage = roms.parseRom(fc.getSelectedFile());
                ecuImage.setFileName(fc.getSelectedFile().getName());
                
                parent.addRom(ecuImage);
                parent.getSettings().setLastImageDir(fc.getCurrentDirectory());
                //System.out.println("File opened: " + ecuImage.getFileName());
                
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (XMLParseException ex) {
                ex.printStackTrace();    
            } catch (SAXException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (RomNotFoundException ex) {
                new JOptionPane().showMessageDialog(parent, "ECU Definition Not Found", fc.getSelectedFile().getName() + " - Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } else {
            // no file selected (do nothing)
        }           
    }       
}