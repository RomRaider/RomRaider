package enginuity.swing;

import javax.swing.tree.DefaultMutableTreeNode;

public class RomTreeRootNode extends DefaultMutableTreeNode {
    
    public RomTreeRootNode(String name) {
        super(name);
    }
    
    public void setUserLevel(int userLevel, boolean isDisplayHighTables) {
        for (int i = 0; i < getChildCount(); i++) {
            ((RomTreeNode)getChildAt(i)).refresh(userLevel, isDisplayHighTables);
        }
    }
}