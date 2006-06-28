package enginuity.swing;

import enginuity.maps.Table;
import javax.swing.tree.DefaultMutableTreeNode;

public class TableChooserTreeNode extends DefaultMutableTreeNode {
    
    private Table table;
    
    public TableChooserTreeNode(String text, Table table) {
        super(text);
        this.table = table;        
    }
    
    private TableChooserTreeNode() { }

    public Table getTable() {
        return table;
    }
}