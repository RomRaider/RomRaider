package enginuity.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.border.LineBorder;

import enginuity.ECUEditor;

public class ECUEditorToolBar extends JToolBar implements ActionListener {
    
    private ECUEditor parent;
    private JButton openImage  = new JButton(new ImageIcon("./graphics/icon-open.png"));
    private JButton saveImage  = new JButton(new ImageIcon("./graphics/icon-save.png"));
    private JButton refreshImage = new JButton(new ImageIcon("./graphics/icon-refresh.png"));
    private JButton closeImage = new JButton(new ImageIcon("./graphics/icon-close.png"));
    
    public ECUEditorToolBar(ECUEditor parent) {
        super();
        this.parent = parent;
        this.setFloatable(false);
        this.add(openImage);
        this.add(saveImage);
        this.add(closeImage);
        this.add(refreshImage);
        
        openImage.setMaximumSize(new Dimension(58,50));
        openImage.setBorder(new LineBorder(new Color(150,150,150), 1));
        saveImage.setMaximumSize(new Dimension(50,50));
        saveImage.setBorder(new LineBorder(new Color(150,150,150), 1));
        closeImage.setMaximumSize(new Dimension(50,50));
        closeImage.setBorder(new LineBorder(new Color(150,150,150), 1));
        refreshImage.setMaximumSize(new Dimension(50,50));
        refreshImage.setBorder(new LineBorder(new Color(150,150,150), 1));

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
            } catch (Exception ex) {                
                JOptionPane.showMessageDialog(parent, new DebugPanel(ex,
                        parent.getSettings().getSupportURL()), "Exception", JOptionPane.ERROR_MESSAGE);   
            }
        } else if (e.getSource() == saveImage) {
            try {
                ((ECUEditorMenuBar)parent.getJMenuBar()).saveImage(parent.getLastSelectedRom());
            } catch (Exception ex) {               
                JOptionPane.showMessageDialog(parent, new DebugPanel(ex,
                        parent.getSettings().getSupportURL()), "Exception", JOptionPane.ERROR_MESSAGE); 
            }
        } else if (e.getSource() == closeImage) {
            ((ECUEditorMenuBar)parent.getJMenuBar()).closeImage();
        } else if (e.getSource() == refreshImage) {
            try {
                ((ECUEditorMenuBar)parent.getJMenuBar()).refreshImage();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, new DebugPanel(ex,
                        parent.getSettings().getSupportURL()), "Exception", JOptionPane.ERROR_MESSAGE);  
            }                
        }
    }    
}