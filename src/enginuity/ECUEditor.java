package enginuity;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import enginuity.maps.Rom;
import enginuity.maps.Table;
import enginuity.swing.ECUEditorToolBar;
import enginuity.swing.ECUEditorMenuBar;
import enginuity.swing.MDIDesktopPane;
import enginuity.swing.RomTree;
import enginuity.swing.RomTreeNode;
import enginuity.swing.RomTreeRootNode;
import enginuity.swing.TableTreeNode;
import enginuity.swing.TableFrame;
import enginuity.net.URL;
import enginuity.swing.JProgressPane;
import enginuity.xml.DOMSettingsBuilder;
import enginuity.xml.DOMSettingsUnmarshaller;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class ECUEditor extends JFrame implements WindowListener {
    
    private RomTreeRootNode  imageRoot       = new RomTreeRootNode("Open Images");
    private RomTree          imageList       = new RomTree(imageRoot);
    private Vector<Rom>      images          = new Vector<Rom>();
    private Settings         settings        = new Settings();
    private String           version         = new String("0.3.0 Beta");
    private String           versionDate     = new String("7/2/2006");
    private String           titleText       = new String("Enginuity v" + version);
    private MDIDesktopPane   rightPanel      = new MDIDesktopPane();    
    private Rom              lastSelectedRom = null;
    private JSplitPane       splitPane       = new JSplitPane();
    private ECUEditorToolBar toolBar;
    private ECUEditorMenuBar menuBar;
    
    public ECUEditor() {
        
        // get settings from xml
        try {
            InputSource src = new InputSource(new FileInputStream(new File("./settings.xml")));                
            DOMSettingsUnmarshaller domUms = new DOMSettingsUnmarshaller();
            DOMParser parser = new DOMParser();                
            parser.parse(src);      
            Document doc = parser.getDocument();
            settings = domUms.unmarshallSettings(doc.getDocumentElement());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Settings file not found.\n" +
                    "A new file will be created.", "Error Loading Settings", JOptionPane.INFORMATION_MESSAGE);
        }
        
        setSize(getSettings().getWindowSize());
        setLocation(getSettings().getWindowLocation()); 
        if (getSettings().isWindowMaximized() == true) setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        JScrollPane rightScrollPane = new JScrollPane(rightPanel, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);   
        JScrollPane leftScrollPane = new JScrollPane(imageList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);   
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightScrollPane);
        splitPane.setDividerSize(4);
        splitPane.setDividerLocation(getSettings().getSplitPaneLocation());
        this.getContentPane().add(splitPane);
        rightPanel.setBackground(Color.BLACK);
        imageList.setScrollsOnExpand(true);
        imageList.setContainer(this);
        
        //create menubar and toolbar
        menuBar = new ECUEditorMenuBar(this);
        this.setJMenuBar(menuBar);    
        toolBar = new ECUEditorToolBar(this);
        this.add(toolBar, BorderLayout.NORTH);
                
        //set remaining window properties
        setIconImage(new ImageIcon("./graphics/enginuity-ico.gif").getImage());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(this);
        setTitle(titleText);
        setVisible(true);
    }

    public void windowClosing(WindowEvent e) {
        getSettings().setSplitPaneLocation(splitPane.getDividerLocation());
        if (getExtendedState() == JFrame.MAXIMIZED_BOTH) getSettings().setWindowMaximized(true);
        else {
            getSettings().setWindowMaximized(false);
            getSettings().setWindowSize(getSize());   
            getSettings().setWindowLocation(getLocation());         
        }
        
        DOMSettingsBuilder builder = new DOMSettingsBuilder();
        try {
            JProgressPane progress = new JProgressPane(this, "Saving settings...", "Saving settings...");
                
            builder.buildSettings(settings, new File("./settings.xml"), progress);
            
        } catch (IOException ex) { }
    }
    public void windowOpened(WindowEvent e) { }
    public void windowClosed(WindowEvent e) { }
    public void windowIconified(WindowEvent e) { }
    public void windowDeiconified(WindowEvent e) { }
    public void windowActivated(WindowEvent e) { }
    public void windowDeactivated(WindowEvent e) { }
    
    public static void main(String args[]) {
        new ECUEditor();
    }    

    public String getVersion() {
        return version;
    }

    public Settings getSettings() {
        return settings;
    }
    
    public void addRom(Rom input) {
        // add to ecu image list pane
        RomTreeNode romNode = new RomTreeNode(input, settings.getUserLevel());
        imageRoot.add(romNode);
        imageList.updateUI();    

        imageList.expandRow(imageList.getRowCount() - 1);
        imageList.updateUI();
        setLastSelectedRom(input);
        
        if (input.getRomID().isObsolete() && settings.isObsoleteWarning()) {
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new GridLayout(3, 1));
            infoPanel.add(new JLabel("A newer version of this ECU revision exists. " +
                    "Please visit the following link to download the latest revision:"));
            infoPanel.add(new URL(getSettings().getRomRevisionURL()));
            
            JCheckBox check = new JCheckBox("Always display this message", true);
            check.setHorizontalAlignment(JCheckBox.RIGHT);
            
            check.addActionListener( 
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        settings.setObsoleteWarning(((JCheckBox)e.getSource()).isSelected());            
                    }
                } 
            );
            
            infoPanel.add(check);
            JOptionPane.showMessageDialog(this, infoPanel, "ECU Revision is Obsolete", JOptionPane.INFORMATION_MESSAGE);
        }   
        input.setContainer(this); 
        imageList.updateUI();    
    }
    
    public void displayTable(TableFrame frame) {
        frame.setVisible(true);
        try {
            rightPanel.add(frame);
        } catch (IllegalArgumentException ex) {
            // table is already open, so set focus
            frame.requestFocus();
        }
        frame.setSize(frame.getTable().getFrameSize());
        rightPanel.repaint();
    }
    
    public void removeDisplayTable(TableFrame frame) {
        rightPanel.remove(frame);
        rightPanel.repaint();
    }
    
    public void closeImage() {
        for (int i = 0; i < imageRoot.getChildCount(); i++) {
        	RomTreeNode romTreeNode = (RomTreeNode)imageRoot.getChildAt(i);
        	Rom rom = romTreeNode.getRom();
            if (rom == lastSelectedRom) {
                imageRoot.remove(romTreeNode);
            	Vector<Table> romTables = rom.getTables();
            	for (Iterator j = romTables.iterator(); j.hasNext();) {
            		Table t = (Table)j.next();
            		rightPanel.remove(t.getFrame());
            		t.finalize();
            	}
                rom.finalize();
                romTreeNode.finalize();
                break;
            }
        }
        imageList.updateUI();
        if (imageRoot.getChildCount() > 0) {
			setLastSelectedRom(((RomTreeNode)imageRoot.getChildAt(0)).getRom());
		}
		else {
			// no other images open
			setLastSelectedRom(null);
		}
        rightPanel.repaint();
    }
    
    public void closeAllImages() {
        while (imageRoot.getChildCount() > 0) {
            ((RomTreeNode)imageRoot.getChildAt(0)).finalize();
            imageRoot.remove(0);
        }
        imageList.updateUI();
        setLastSelectedRom(null);
        rightPanel.removeAll();
        rightPanel.repaint();
    }

    public Rom getLastSelectedRom() {
        return lastSelectedRom;
    }

    public void setLastSelectedRom(Rom lastSelectedRom) {
        this.lastSelectedRom = lastSelectedRom;
        if (lastSelectedRom == null) {
            this.setTitle(titleText);
        } else {
            this.setTitle(titleText + " - " + lastSelectedRom.getFileName());
        }
        
        // update filenames
        for (int i = 0; i < imageRoot.getChildCount(); i++) {
            ((RomTreeNode)imageRoot.getChildAt(i)).updateFileName();
        }
        
        toolBar.updateButtons();
        menuBar.updateMenu();
        imageList.updateUI();
    }

    public ECUEditorToolBar getToolBar() {
        return toolBar;
    }

    public void setToolBar(ECUEditorToolBar toolBar) {
        this.toolBar = toolBar;
    }
    
    public void setSettings(Settings settings) {
        this.settings = settings;
        for (int i = 0; i < imageRoot.getChildCount(); i++) {
        	RomTreeNode rtn = (RomTreeNode)imageRoot.getChildAt(i);
            rtn.getRom().setContainer(this);
        }
    }
    
    public void repaintPanel() {
        rightPanel.repaint();
        rightPanel.update(rightPanel.getGraphics());
    }  
    
    public void setUserLevel(int userLevel) {
        settings.setUserLevel(userLevel);
        imageRoot.setUserLevel(userLevel);
        imageList.updateUI();
    }

    public Vector<Rom> getImages() {
        Vector<Rom> images = new Vector<Rom>();
        for (int i = 0; i < imageRoot.getChildCount(); i++) {
                RomTreeNode rtn = (RomTreeNode)imageRoot.getChildAt(i);
                images.add(rtn.getRom());
        }
        return images;
    }
}