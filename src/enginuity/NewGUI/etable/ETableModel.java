package enginuity.NewGUI.etable;

import javax.swing.table.AbstractTableModel;

import enginuity.NewGUI.data.TableMetaData;

public class ETableModel extends AbstractTableModel {

	private String[] columnNames = new String[11];
	private Double[][] data = new Double[11][40];
	private TableMetaData tableMetaData;
	
	public ETableModel(TableMetaData metaData, Double[][] initialData) {
		this.tableMetaData = metaData;
		this.data = initialData;
		
		if(metaData.getColumnLabels() == null){
			for (int i = 0; i < columnNames.length; i++) {
				columnNames[i] = i + "";
			}
		}else{
			this.columnNames = metaData.getColumnLabels();
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