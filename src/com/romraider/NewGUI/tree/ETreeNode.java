package com.romraider.NewGUI.tree;

import com.romraider.NewGUI.data.ApplicationStateManager;
import com.romraider.NewGUI.data.TableMetaData;
import javax.swing.tree.DefaultMutableTreeNode;

public class ETreeNode extends DefaultMutableTreeNode {

    private int userLevel = ApplicationStateManager.USER_LEVEL_1;
    private String nodeName = "";

    private TableMetaData tableMetaData = null;

    public ETreeNode(String nodeName, TableMetaData tableMetaData) {
        super(nodeName);
        this.nodeName = nodeName;
        this.tableMetaData = tableMetaData;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String mapName) {
        this.nodeName = mapName;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }


    public TableMetaData getTableMetaData() {
        return tableMetaData;
    }
}
