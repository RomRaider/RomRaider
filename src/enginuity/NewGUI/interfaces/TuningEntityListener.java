package enginuity.NewGUI.interfaces;

import java.util.Vector;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import enginuity.NewGUI.tree.ETreeNode;

public interface TuningEntityListener {
	
	/**
	 * Change the tree structure base on the passed root node
	 * 
	 * @param rootNodeOfNewTree
	 */
	public void addNewTuningGroup(ETreeNode rootNodeOfNewTree);
	
	/**
	 * Removes a tuning group from the tree
	 * 
	 * @param tableGroup
	 */
	public void removeTuningGroup(String tableGroup);
	
	/**
	 * Prepends list of menu items to the menu bae
	 * 
	 * @param items
	 */
	public void rebuildJMenuBar(Vector<JMenu> items);
	
	/**
	 * If a tuning entity has a custom tool bar to add, call this method.
	 * 
	 * @param theToolBar
	 */
	public void setNewToolBar(JToolBar theToolBar);
	
	/**
	 * Return the number of maps that have changed belonging to the targeted tuning entity of the defined tableGroup
	 * 
	 * @param tuningEntity
	 * @param tableGroup
	 * @return
	 */
	public int getMapChangeCount(TuningEntity tuningEntity, String tableGroup);

	/**
	 * Has the main GUI kick off the process of saving table data.
	 * 
	 */
	public void saveMaps();
}
