package enginuity.swing;

import enginuity.maps.Rom;
import enginuity.maps.Table;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

public class RomTreeNode extends DefaultMutableTreeNode {
    
    private Rom rom = new Rom();
    
    public RomTreeNode(Rom rom, int userLevel) {
        setRom(rom);
        refresh(userLevel);
        updateFileName();
    }
        
    public void refresh(int userLevel) {
        removeAllChildren();
        Vector<Table> tables = rom.getTables();
        
        for (int i = 0; i < tables.size(); i++) {
            Table table = tables.get(i);
            add(table);
            
            if (userLevel >= table.getUserLevel()) {
                boolean categoryExists = false;

                for (int j = 0; j < getChildCount(); j++) {
                    if (getChildAt(j).toString().equals(table.getCategory())) {

                        // add to appropriate category
                        TableTreeNode tableNode = new TableTreeNode(table);                        
                        getChildAt(j).add(tableNode);
                        categoryExists = true;
                        break;
                    }
                }         

                if (!categoryExists) { // if category does not already exist, create it
                    add(new CategoryTreeNode(table.getCategory(), table.getRom()));            
                    TableTreeNode tableNode = new TableTreeNode(table);
                    getLastChild().add(tableNode);           
                }  
            } else {
                table.getFrame().setVisible(false);
            }
        }
    }
    
    public void updateFileName() {
        /*JPanel panel = new JPanel();
        JLabel fileName = new JLabel(rom.getFileName());
        JLabel info = new JLabel("info");
        panel.add(fileName);
        panel.add(info);
        setUserObject(panel);*/
        
        setUserObject(rom.getFileName());
    }
    
    public void add(Table table) {
        TableFrame frame = new TableFrame(table);
        table.setFrame(frame); 
    }
    
    public DefaultMutableTreeNode getChildAt(int i) {
        return (DefaultMutableTreeNode)super.getChildAt(i);
    }
    
    public DefaultMutableTreeNode getLastChild() {
        return (DefaultMutableTreeNode)super.getLastChild();
    }    

    public Rom getRom() {
        return rom;
    }
    
    public void setRom(Rom rom) {
        this.rom = rom;
    }
    
    public void finalize() {
        try {
            rom = null;
            removeAllChildren();
        }
        catch (Throwable t) {}
    }    
}