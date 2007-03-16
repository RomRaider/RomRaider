package enginuity.NewGUI.interfaces;

import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JMenu;


public interface TuningEntity extends ActionListener{
	// Return name of tuning entity
	public String getName();
	
	// Method returns parent node of entire tree structure of maps available
	public TreeNode getJTreeNodeStructure();
	
	// Return all the menu items relevant to tuning entity
	public Vector<JMenu> getMenuItems();
	
	// Return double data based on passed table name
	public double[][] getTableData(String tableName);
	
	// Control methods
	public void init();
}
