package enginuity.logger.utec.gui.mapTabs;

import javax.swing.table.AbstractTableModel;

public class UtecTableModel extends AbstractTableModel {

	private String[] columnNames = new String[11];

	private double[][] data = new double[11][40];
	
	String test = "";
	
	public UtecTableModel(int identifier, double[][] initialData) {
		
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
		System.out.println(test+"->("+row+","+col+") "+data[col][row]);
		return data[col][row];
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public boolean isCellEditable(int row, int col) {
		return true;
	}
	
	
	public void setValueAt(Object value, int row, int col) {
        System.out.print(" Updated:"+(String)value+": ");
        // Set new data in table
        double temp = data[col][row];
        
        try{
        	temp = Double.parseDouble((String)value);
        }catch (NumberFormatException e) {
        	System.out.println("Not a valid number entered.");
        }
        data[col][row] = temp;
        this.fireTableDataChanged();
    }
	
	
	public void replaceData(double[][] newData){
		System.out.println("Model data being replaced in full.");
		this.data = newData;
		this.fireTableDataChanged();
	}
	
}