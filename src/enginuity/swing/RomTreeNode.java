package enginuity.swing;

import enginuity.maps.Rom;
import enginuity.maps.Table;
import javax.swing.tree.DefaultMutableTreeNode;

public class RomTreeNode extends DefaultMutableTreeNode {
    
    private Rom rom = new Rom();
    
    public RomTreeNode(Rom rom) {
        super(rom.getFileName());
        this.setRom(rom);
    }
    
    public void updateFileName() {
        this.setUserObject(rom.getFileName());
    }
    
    public void add(Table table) {
        boolean categoryExists = false;
        for (int i = 0; i < this.getChildCount(); i++) {
            if (this.getChildAt(i).toString().equals(table.getCategory())) {
                
                TableFrame frame = new TableFrame(table);
                table.setFrame(frame); 
                TableTreeNode tableNode = new TableTreeNode(table);
                        
                this.getChildAt(i).add(tableNode);
                categoryExists = true;
                break;
            }
        }         
        if (!categoryExists) {
            this.add(new CategoryTreeNode(table.getCategory(), table.getRom()));
            
            TableFrame frame = new TableFrame(table);
            table.setFrame(frame); 
            TableTreeNode tableNode = new TableTreeNode(table);
            this.getLastChild().add(tableNode);           
        }
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
}