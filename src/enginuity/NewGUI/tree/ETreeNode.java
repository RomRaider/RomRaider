package enginuity.NewGUI.tree;

import java.util.LinkedList;

import javax.swing.tree.DefaultMutableTreeNode;

public class ETreeNode extends DefaultMutableTreeNode{
	public static final int DATA1D = 0;
	public static final int DATA2D = 1;
	public static final int DATA3D = 3;
	public static final int CATEGORY = 4;
	public static final int RESERVED_ROOT = 5;
	public static final int USER_LEVEL_1 = 6;
	public static final int USER_LEVEL_2 = 7;
	public static final int USER_LEVEL_3 = 8;
	public static final int USER_LEVEL_4 = 9;
	public static final int USER_LEVEL_5 = 10;
	
	
	private int userLevel = ETreeNode.USER_LEVEL_1;
	private int nodeType;
	private String nodeName = "";
	
	
	public ETreeNode(int nodeType, String nodeName){
		super(nodeName);
		this.nodeType = nodeType;
		this.nodeName = nodeName;
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
}
