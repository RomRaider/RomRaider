package enginuity.logger.utec.gui.mapTabs;

import javax.swing.JTable;

public class UtecJTable extends JTable{

	public UtecJTable(UtecTableModel theModel, int modelType){
		super(theModel);
		if(modelType == MapJPanel.FUELMAP){
			System.out.println("Setting the fuel listener");
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
		System.out.println("Hi ya spanky");
		((UtecTableModel)this.dataModel).replaceData(newData);
	}
}
