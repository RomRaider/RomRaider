package enginuity.NewGUI.etable;

import javax.swing.table.AbstractTableModel;

public class ERowLabelTable extends AbstractTableModel{

	private int length;
	private String[] labels;
	private int counter = 0;
	
	public ERowLabelTable(int length, String[] labels){
		this.length = length;
		this.labels = labels;
	}
	
	public int getRowCount() {
		return  length;
	}

	public int getColumnCount() {
		return length;
	}

	public Object getValueAt(int arg0, int arg1) {
		if(this.labels == null){
			return  arg0;
		}
		
		return this.labels[arg1];
	}
	
	public String getColumnName(int col){
		if(this.labels == null){
			return counter++ + "";
		}
		
		return this.labels[col];
	}

}
