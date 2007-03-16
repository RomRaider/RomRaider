package enginuity.NewGUI.interfaces;

import java.util.LinkedList;

public interface TreeNode {
	public static final int DATA1D = 0;
	public static final int DATA2D = 1;
	public static final int DATA3D = 3;
	public static final int NODimension = 4;
	
	// Return the name of this node
	public String getName();
	public void setName(String name);
	
	// Return all the children nodes
	public LinkedList<TreeNode> getChildren();
	public void addChild(TreeNode child);
	
	// Return true if this node represents table data
	public boolean isTable();
	public void setIsTable(boolean isTable);
	
	// Retun the expected dimensionality of data
	public int getDimension();
	public void setDimension(int dimension);
	
	
}
