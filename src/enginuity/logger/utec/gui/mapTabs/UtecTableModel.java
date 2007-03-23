package enginuity.logger.utec.gui.mapTabs;

import javax.swing.table.AbstractTableModel;

public class UtecTableModel extends AbstractTableModel {

	private String[] columnNames = new String[11];

	private Double[][] data = new Double[11][40];
	
	String test = "";
	
	private int identifier = 0;
	
	public UtecTableModel(int identifier, Double[][] initialData) {
		this.identifier = identifier;
		
		this.data = initialData;
		
		for (int i = 0; i < columnNames.length; i++) {
			columnNames[i] = i + "";
		}
		
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return 40;
	}

	public Object getValueAt(int row, int col) {
		return data[col][row];
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public boolean isCellEditable(int row, int col) {
		return true;
	}
	
	
	public void setValueAt(Object value, int row, int col) {
        //System.out.print(" Updated:"+(String)value+": ");
        // Set new data in table
        double temp = data[col][row];
        
        if(value instanceof String){
        	 try{
             	temp = Double.parseDouble((String)value);
             }catch (NumberFormatException e) {
             	System.out.println("Not a valid number entered.");
             }
             data[col][row] = temp;
        }else if(value instanceof Double){
        	data[col][row] = (Double)value;
        }
       
        
        // Update current map in scope
        if(this.identifier == MapJPanel.FUELMAP){
        	UtecDataManager.setFuelMapValue(row, col, temp);
        } 
        else if(this.identifier == MapJPanel.TIMINGMAP){
        	UtecDataManager.setTimingMapValue(row, col, temp);
        }
        else if(this.identifier == MapJPanel.BOOSTMAP){
        	UtecDataManager.setBoostMapValue(row, col, temp);
        }
        
        this.fireTableDataChanged();
    }
	
	public void setDoubleData(int row, int col, double value){
		this.data[col][row] = value;
	}
	
	
	public void replaceData(Double[][] newData){
		System.out.println("Model data being replaced in full.");
		this.data = newData;
		this.fireTableDataChanged();
	}
	
}