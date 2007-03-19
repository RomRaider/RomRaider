package enginuity.NewGUI.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import enginuity.NewGUI.data.ApplicationStateManager;
import enginuity.NewGUI.data.TableNodeMetaData;

public class ETreeNode extends DefaultMutableTreeNode{
	public static final int DATA1D = 0;
	public static final int DATA2D = 1;
	public static final int DATA3D = 3;
	public static final int CATEGORY = 4;
	public static final int RESERVED_ROOT = 5;
	
	
	private int userLevel = ApplicationStateManager.USER_LEVEL_1;
	private int nodeType;
	private String nodeName = "";
	
	private TableNodeMetaData tableMetaData = null;
	
	public ETreeNode(int nodeType, String nodeName, TableNodeMetaData tableMetaData){
		super(nodeName);
		this.nodeType = nodeType;
		this.nodeName = nodeName;
		this.tableMetaData = tableMetaData;
	}
	
	
	public int getNodeType() {
		return nodeType;
	}
	public void setNodeType(int nodeType) {
		this.nodeType = nodeType;
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
