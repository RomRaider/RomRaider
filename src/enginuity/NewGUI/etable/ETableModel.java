package enginuity.NewGUI.etable;

import javax.swing.table.AbstractTableModel;

public class ETableModel extends AbstractTableModel {

	private String[] columnNames = new String[11];

	//private double[][] data = new double[11][40];
	private Double[][] data = new Double[11][40];
	String test = "";
	
	private String tableName;
	
	public ETableModel(String tableName, Double[][] initialData) {
		this.tableName = tableName;
		
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
       
        this.fireTableDataChanged();
    }
	
	public void setDoubleData(int row, int col, double value){
		this.data[col][row] = value;
	}
	
	
	public void replaceData(Double[][] newData){
		System.out.println("Model data being replaced in full.");
		if(this.data == newData){
			System.out.println("Same data");
		}
		
		this.data = newData;
		
		this.fireTableDataChanged();
	}

	public Double[][] getData() {
		return data;
	}
	
	public void refresh(){
		this.fireTableDataChanged();
	}
	
}