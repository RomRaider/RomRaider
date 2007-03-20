package enginuity.NewGUI.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import enginuity.NewGUI.data.ApplicationStateManager;
import enginuity.NewGUI.data.TableNodeMetaData;

public class ETreeNode extends DefaultMutableTreeNode{
	
	private int userLevel = ApplicationStateManager.USER_LEVEL_1;
	private String nodeName = "";
	
	private TableNodeMetaData tableMetaData = null;
	
	public ETreeNode(String nodeName, TableNodeMetaData tableMetaData){
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


	public TableNodeMetaData getTableMetaData() {
		return tableMetaData;
	}
}
