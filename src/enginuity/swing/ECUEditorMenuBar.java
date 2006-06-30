package enginuity.swing;

import enginuity.xml.RomNotFoundException;
import enginuity.maps.Rom;
import enginuity.xml.DOMRomUnmarshaller;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.management.modelmbean.XMLParseException;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import enginuity.ECUEditor;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class ECUEditorMenuBar extends JMenuBar implements ActionListener {
    
    private JMenu      fileMenu   = new JMenu("File");
    private JMenuItem  openImage  = new JMenuItem("Open Image");
    private JMenuItem  saveImage  = new JMenuItem("Save Image");
    private JMenuItem  refreshImage = new JMenuItem("Refresh Image");
    private JMenuItem  closeImage = new JMenuItem("Close Image");
    private JMenuItem  closeAll   = new JMenuItem("Close All Images");
    private JMenuItem  exit       = new JMenuItem("Exit");
    private JMenu      editMenu   = new JMenu("Edit");
    private JMenuItem  settings   = new JMenuItem("Settings");
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
        
        add(editMenu);
        editMenu.setMnemonic('E');
        settings.setMnemonic('S');
        editMenu.add(new JSeparator());
        editMenu.add(settings);
        settings.addActionListener(this);
        
        
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

            if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                boolean save = true;
                if (fc.getSelectedFile().exists()) {
                    if (JOptionPane.showConfirmDialog(parent, fc.getSelectedFile().getName() + " already exists! Overwrite?") == JOptionPane.CANCEL_OPTION) {
                        save = false;
                    }                 
                }
                if (save) {
                    byte[] output = parent.getLastSelectedRom().saveFile();
                    FileOutputStream fos = new FileOutputStream(fc.getSelectedFile());
                    fos.write(output);
                    fos.close();
                    fos = null;

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
        try {     
            parent.repaintPanel();
            JProgressPane progress = new JProgressPane(parent, "Opening file...", "Parsing ECU definitions...");
            progress.update("Parsing ECU definitions...", 0);
            
            InputSource src = new InputSource(new FileInputStream(parent.getSettings().getEcuDefinitionFile()));                
            DOMRomUnmarshaller domUms = new DOMRomUnmarshaller();
            DOMParser parser = new DOMParser();                
            parser.parse(src);      
            Document doc = parser.getDocument();
            FileInputStream fis = new FileInputStream(inputFile);
            byte[] input = new byte[fis.available()];                
            fis.read(input);     
            fis.close();
                        
            progress.update("Finding ECU definition...", 10);
            
            Rom rom = domUms.unmarshallXMLDefinition(doc.getDocumentElement(), input, progress);
            progress.update("Populating tables...", 50);
            rom.populateTables(input, progress);
            rom.setFileName(inputFile.getName());
            
            progress.update("Finalizing...", 90);     
            parent.addRom(rom);                   
            rom.setFullFileName(inputFile);            
            
            progress.dispose();
            
        } catch (RomNotFoundException ex) {
            JOptionPane.showMessageDialog(parent, "ECU Definition Not Found", "Error Loading " + inputFile.getName(), JOptionPane.ERROR_MESSAGE);
            
        } catch (StackOverflowError ex) {
            JOptionPane.showMessageDialog(parent, "Looped \"base\" attribute in XML definitions.", "Error Loading ROM", JOptionPane.ERROR_MESSAGE);
            
        }         
    }       
}