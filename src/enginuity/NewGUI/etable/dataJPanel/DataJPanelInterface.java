package enginuity.NewGUI.etable.dataJPanel;

import javax.swing.JMenuBar;
import javax.swing.JToolBar;

public interface DataJPanelInterface {
	public JToolBar getToolBar();
	
	public JMenuBar getMenuBar();
	
	public boolean dataChanged();
	
	public void copySelectedTableData();
	
	public void copyEntireTable();
	
	public void pasteTableData();
	
	public void setClosed(boolean value);
	
	public void revertDataState();
	
	public void saveDataState();
	
	public void replaceData(Object[][] newData);
	
	public Object[][] getData();
}
