package enginuity.logger.utec.gui.mapTabs;

import javax.swing.table.AbstractTableModel;

public class UtecTableModel extends AbstractTableModel {

	private String[] columnNames = new String[11];

	private double[][] data = new double[11][40];
	
	public UtecTableModel() {
		for (int i = 0; i < columnNames.length; i++) {
			System.out.println("count: " + i);
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
        
        // Set new data in table
        double temp = data[col][row];
        
        try{
        	temp = Double.parseDouble((String)value);
        }catch (NumberFormatException e) {
        }
        data[col][row] = temp;
        fireTableCellUpdated(row, col);

    }

}