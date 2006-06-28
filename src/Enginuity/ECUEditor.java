package Enginuity;

import Enginuity.Maps.Rom;
import Enginuity.Maps.Table;
import Enginuity.SwingComponents.ECUEditorToolBar;
import Enginuity.SwingComponents.ECUEditorMenuBar;
import Enginuity.SwingComponents.RomTree;
import Enginuity.SwingComponents.RomTreeNode;
import Enginuity.SwingComponents.TableFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;

public class ECUEditor extends JFrame implements WindowListener {
    
    private JScrollPane  imagePane           = new JScrollPane();
    private DefaultMutableTreeNode imageRoot = new DefaultMutableTreeNode("ECU Images");
    private RomTree      imageList           = new RomTree(imageRoot);
    private Vector<Rom>  images              = new Vector<Rom>();
    private Settings     settings            = new Settings();
    private String       version             = new String("0.2.5 Beta");
    private String       titleText           = new String("Enginuity v" + version);
    private JDesktopPane rightPanel          = new JDesktopPane();
    private Rom          lastSelectedRom     = null;
    private ECUEditorToolBar toolBar;
    private ECUEditorMenuBar menuBar;
    
    public ECUEditor() {
        //load settings from disk
        try {            
            ObjectInputStream in = new ObjectInputStream(
                                new FileInputStream(new File("./settings.dat")));
            settings = (Settings)in.readObject();
            in.close();
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        } catch (ClassNotFoundException ex) {
        }        
        setSize(getSettings().getWindowSize()[0], getSettings().getWindowSize()[1]);
        setLocation(getSettings().getWindowLocation()[0], getSettings().getWindowLocation()[1]);   
        
        JScrollPane rightScrollPane = new JScrollPane(rightPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);   
        JScrollPane leftScrollPane = new JScrollPane(imageList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);   
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightScrollPane);
        splitPane.setDividerSize(4);
        splitPane.setDividerLocation(150);
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
        getSettings().setWindowSize(getSize().width, getSize().height);
        getSettings().setWindowLocation(getLocation().x, getLocation().y);
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                                    new FileOutputStream(new File("./settings.dat")));
            out.writeObject(getSettings());
            out.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }       
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
        // add to image vector
        images.add(input);
        input.setContainer(this);
        
        // add to ecu image list pane
        RomTreeNode node = new RomTreeNode(input);
        imageRoot.add(node);
        imageList.updateUI();
        
        // add tables
        Vector<Table> tables = input.getTables();
        RomTreeNode[] tableNodes = new RomTreeNode[tables.size()];
        for (int i = 0; i < tables.size(); i++) {
            tableNodes[i] = new RomTreeNode(tables.get(i));
            node.add(tableNodes[i]);
            
            TableFrame frame = new TableFrame(tables.get(i));
            rightPanel.add(frame);
        }
        imageList.expandRow(imageList.getRowCount() - 1);
        imageList.updateUI();
        this.setLastSelectedRom(input);
    }
    
    public void closeImage() {
        for (int i = 0; i < imageRoot.getChildCount(); i++) {
            if (((RomTreeNode)imageRoot.getChildAt(i)).getRom() == lastSelectedRom) {
                ((Rom)images.get(i)).closeImage();
                imageRoot.remove((DefaultMutableTreeNode)imageRoot.getChildAt(i));
                images.remove(i);
            }
        }
        imageList.updateUI();
        setLastSelectedRom(null);
    }
    
    public void closeAllImages() {
        while (imageRoot.getChildCount() > 0) {
            ((Rom)images.get(0)).closeImage();
            imageRoot.remove(0);
            images.remove(0);
        }
        imageList.updateUI();
        setLastSelectedRom(null);
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
        toolBar.updateButtons();
        menuBar.updateMenu();
    }

    public ECUEditorToolBar getToolBar() {
        return toolBar;
    }

    public void setToolBar(ECUEditorToolBar toolBar) {
        this.toolBar = toolBar;
    }
}