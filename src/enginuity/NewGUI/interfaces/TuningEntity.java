package enginuity.NewGUI.interfaces;

import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.tree.TreeModel;


public interface TuningEntity extends ActionListener{
	
	// Return name of tuning entity
	public String getName();
	
	// Return all the menu items relevant to tuning entity
	public Vector<JMenu> getMenuItems();
	
	// Return double data based on passed table name
	public double[][] getTableData(String tableName);
	
	// Control methods
	public void init(TuningEntityListener listener);
}
