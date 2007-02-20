package enginuity.logger.utec.gui.mapTabs;

import javax.swing.table.AbstractTableModel;

public class UtecTableModel extends AbstractTableModel {

	private String[] columnNames = new String[11];

	private double[][] data = new double[11][40];
	
	public UtecTableModel() {
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
        System.out.println("Updated:"+(String)value);
        // Set new data in table
        double temp = data[col][row];
        
        try{
        	temp = Double.parseDouble((String)value);
        }catch (NumberFormatException e) {
        	System.out.println("Not a valid number entered.");
        }
        data[col][row] = temp;
        fireTableCellUpdated(row, col);

    }
	
	public void replaceData(double[][] newData){
		System.out.println("Model data being replaced in full.");
		
			for(int j = 0; j < 40; j++){
				for(int i = 0; i < 11; i++){
					System.out.print(newData[i][j]+", ");
					//this.data[i][j] = newData[i][j];
					 this.setValueAt(newData[i][j]+"", j, i);
				}
				System.out.print("\n");
			}
			//this.fireTableDataChanged();
	}

}