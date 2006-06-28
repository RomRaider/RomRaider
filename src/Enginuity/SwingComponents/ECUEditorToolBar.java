package Enginuity.SwingComponents;

import Enginuity.ECUEditor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.management.modelmbean.XMLParseException;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

public class ECUEditorToolBar extends JToolBar implements ActionListener {
    
    private ECUEditor parent;
    private JButton openImage  = new JButton("Open");
    private JButton saveImage  = new JButton("Save");
    private JButton refreshImage  = new JButton("Refresh");
    private JButton closeImage = new JButton("Close");
    
    public ECUEditorToolBar(ECUEditor parent) {
        super();
        this.parent = parent;
        this.setFloatable(false);
        this.add(openImage);
        this.add(saveImage);
        this.add(closeImage);
        this.add(refreshImage);

        updateButtons();
        
        openImage.addActionListener(this);
        saveImage.addActionListener(this);
        closeImage.addActionListener(this);
        refreshImage.addActionListener(this);
    }

    public void updateButtons() {
        String file = "";
        try { 
            file = " " + parent.getLastSelectedRom().getFileName();
        } catch (NullPointerException ex) { }
        
        openImage.setToolTipText("Open Image");
        saveImage.setToolTipText("Save" + file);
        refreshImage.setToolTipText("Refresh" + file + " from saved copy");
        closeImage.setToolTipText("Close" + file);
        
        if (file.equals("")) {
            saveImage.setEnabled(false);
            refreshImage.setEnabled(false);
            closeImage.setEnabled(false);
        } else {
            saveImage.setEnabled(true);
            refreshImage.setEnabled(true);
            closeImage.setEnabled(true);
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openImage) {
            try { 
                ((ECUEditorMenuBar)parent.getJMenuBar()).openImageDialog();
            } catch (XMLParseException ex) {                
                new JOptionPane(ex, JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == saveImage) {
            try {
                ((ECUEditorMenuBar)parent.getJMenuBar()).saveImage(parent.getLastSelectedRom());
            } catch (XMLParseException ex) {                
                new JOptionPane(ex, JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == closeImage) {
            ((ECUEditorMenuBar)parent.getJMenuBar()).closeImage();
        } else if (e.getSource() == refreshImage) {
            ((ECUEditorMenuBar)parent.getJMenuBar()).refreshImage();
        }
    }    
}