package enginuity.logger.utec.gui.mapTabs;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public class UtecJTable extends JTable{

	public UtecJTable(UtecTableModel theModel, int modelType, double minValue, double maxValue){
		super(theModel);
		//this.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		//this.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		//this.setRowSelectionAllowed(false);
		this.setCellSelectionEnabled(true);
		//this.setColumnSelectionAllowed(false);
		this.getSelectionModel().addListSelectionListener(new UtecSelectionListener(this));
		
		this.setDefaultRenderer(Object.class, new UtecTableCellRenderer(minValue, maxValue));
	
		
		// ************************
		// Utec Specific code below
		// ************************
		if(modelType == MapJPanel.FUELMAP){
			DataManager.setFuelListener(theModel);
		}
		else if(modelType == MapJPanel.BOOSTMAP){
			DataManager.setBoostListener(theModel);
		}
		else if(modelType == MapJPanel.TIMINGMAP){
			DataManager.setTimingListener(theModel);
		}
	}
	
	public void updateData(double[][] newData){
		((UtecTableModel)this.dataModel).replaceData(newData);
	}
}
