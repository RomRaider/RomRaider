package Enginuity.SwingComponents;

import Enginuity.Maps.Rom;
import Enginuity.Maps.Table;
import javax.swing.tree.DefaultMutableTreeNode;

public class RomTreeNode extends DefaultMutableTreeNode {
    
    private String type;
    private Rom    rom;
    private Table  table;
    
    public RomTreeNode(Rom rom) {
        super(rom.getFileName());
        this.rom = rom;
    }
    
    public RomTreeNode(Table table) {
        super(table.getName() + " (" + table.getType() + "D)");
        this.table = table;
    }
    
    public String getType() {
        return type;
    }

    public Rom getRom() {
        return rom;
    }

    public void setRom(Rom rom) {
        this.rom = rom;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }    
}