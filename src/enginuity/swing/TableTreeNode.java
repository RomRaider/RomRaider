package enginuity.swing;

import enginuity.maps.Rom;
import enginuity.maps.Table;
import javax.swing.tree.DefaultMutableTreeNode;

public class TableTreeNode extends DefaultMutableTreeNode {
    
    private String type;
    private Rom    rom;
    private Table  table;
    private String toolTip;
    private TableFrame frame;
    
    public TableTreeNode(Table table) {
        //super(table.getName() + " (" + table.getType() + "D)");
        super(table);
        this.table = table;
        this.frame = table.getFrame();
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
    
    public void setToolTipText(String input) {
        toolTip = input;
    }
    
    public String getToolTipText() {
        return toolTip;
    }

    public TableFrame getFrame() {
        return frame;
    }

    public void setFrame(TableFrame frame) {
        this.frame = frame;
    }
}