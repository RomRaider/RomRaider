package enginuity.logger.utec.gui.mapTabs;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class UtecSelectionListener implements ListSelectionListener{
	private JTable parentTable = null;
	
	public UtecSelectionListener(JTable parentTable){
		this.parentTable = parentTable;
	}
	public void valueChanged(ListSelectionEvent event) {
		//System.out.println("1: "+ event.getFirstIndex()+"     2: "+event.getLastIndex());
		
		int selRow[] = parentTable.getSelectedRows();
		int selCol[] = parentTable.getSelectedColumns();
		
		
		for(int i = 0; i < selRow.length; i++){
			//System.out.println("Row Value: "+selRow[i]);
		}
		
		for(int i = 0; i < selCol.length; i++){
			//System.out.println("Col Value: "+selCol[i]);
		}
		
		//System.out.println("---------------------------");
		Object[] selectedCells = new Object[selRow.length * selCol.length];
		
		
	}

}
