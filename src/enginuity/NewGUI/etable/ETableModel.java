package enginuity.NewGUI.etable;

import java.text.DecimalFormat;

import javax.swing.table.AbstractTableModel;

import enginuity.NewGUI.data.TableMetaData;

public class ETableModel extends AbstractTableModel {

	private String[] columnNames = new String[11];
	private Double[][] data = new Double[11][40];
	private TableMetaData tableMetaData;
	private DecimalFormat formatter = new DecimalFormat( "#.#" );
	
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
        		 System.out.println("value:"+value+":");
        		//String tempString = formatter.format(value);
             	//temp = Double.parseDouble(tempString);
        		 temp = Double.parseDouble((String)value);
             }catch (NumberFormatException e) {
             	System.out.println("Not a valid number entered.");
             }
             data[col][row] = temp;
        }else if(value instanceof Double){
        	String tempString = formatter.format(value);
        	data[col][row] = Double.parseDouble(tempString);
        }
       
        this.fireTableDataChanged();
    }
	
	public void setDoubleData(int row, int col, double value){
		String tempString = formatter.format(value);
		this.data[col][row] = Double.parseDouble(tempString);
	}
	
	
	public void replaceData(Double[][] newData){
		
		this.copyData(newData);
		
		this.fireTableDataChanged();
		this.fireTableStructureChanged();
	}
	
	/**
	 * ARG Why????
	 * 
	 * Seem to be getting some pass by refence issues.
	 * 
	 * @param data
	 */
	private void copyData(Double[][] data){
		int width = data.length;
		int height = data[0].length;
		
		for(int i = 0; i < width; i ++){
			for(int j=0; j < height; j++){
				double tempData = data[i][j];
				String tempString = formatter.format(tempData);
				this.data[i][j] = Double.parseDouble(tempString);
			}
		}
	}

	public Double[][] getData() {
		return data;
	}
	
	public void refresh(){
		this.fireTableDataChanged();
	}
	
}