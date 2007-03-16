package enginuity.logger.utec.impl;

import java.util.LinkedList;

import enginuity.NewGUI.interfaces.TreeNode;

public class UtecTreeNode implements TreeNode{
	private String name;
	private LinkedList<TreeNode> children = new LinkedList<TreeNode>();
	private int dimension = -1;
	private boolean isTable = false;
	
	public UtecTreeNode(String name, int dimension){
		this.name = name;
		this.dimension = dimension;
		if(dimension != TreeNode.NODimension){
			isTable = true;
		}
	}
	
	public String getName() {
		return name;
	}

	public LinkedList<TreeNode> getChildren() {
		return children;
	}

	public boolean isTable() {
		return this.isTable;
	}

	public int getDimension() {
		return dimension;
	}

	public void addChild(TreeNode child) {
		this.children.add(child);	
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIsTable(boolean isTable) {
		this.isTable = isTable;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

}
