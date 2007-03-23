package enginuity.logger.utec.gui.mapTabs;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public class UtecJTable extends JTable{

	private UtecTableModel theModel;
	
	public UtecJTable(UtecTableModel theModel, double minValue, double maxValue, Object[] ignoredValues, boolean isInvertedColoring){
		super(theModel);
		
		this.theModel = theModel;
		
		//this.setSelectionModel(ListSelectionModel.)
		
		this.setCellSelectionEnabled(true);
		this.getSelectionModel().addListSelectionListener(new UtecSelectionListener(this));
		this.setDefaultRenderer(Object.class, new UtecTableCellRenderer(minValue, maxValue, ignoredValues, isInvertedColoring));
	
		//this.setSelectedQuadrilateral(2,4,4,7);
		//this.incSelectedCells(22);
		this.setSelected(0,0);
		this.setSelected(1,1);
		this.incSelectedCells(22);
		//this.setSelected(3,5);
	}
	
	
	/**
	 * Zero based row and columns, inclusive
	 * 
	 * @param rowStart
	 * @param rowEnd
	 * @param colStart
	 * @param colEnd
	 */
	public void setSelectedQuadrilateral(int rowStart, int rowEnd, int colStart, int colEnd){
		for(int i = rowStart; i < rowEnd + 1; i++){
			for(int j = colStart; j < colEnd + 1; j++){
				this.changeSelection(i, j, false, true);
			}
		}
	}
	
	
	/**
	 * Set a cell as being selected.
	 * 
	 * @param rowIndex
	 * @param colIndex
	 */
	public void setSelected(int rowIndex, int colIndex){
		this.changeSelection(rowIndex, colIndex, false, true);
	}
	
	/**
	 * Increment cell values by passed double amount.
	 * @param amount
	 */
	public void incSelectedCells(double amount){
		
		int rowStart = this.getSelectedRow();
		int rowEnd = this.getSelectionModel().getMaxSelectionIndex();
		
		int colStart = this.getSelectedColumn();
		int colEnd = this.getColumnModel().getSelectionModel().getMaxSelectionIndex();
		
		for(int i = rowStart; i <= rowEnd; i++){
			for(int j = colStart; j <= colEnd; j++){
				if(this.isCellSelected(i,j)){
					// The cell is selected
					Object value = theModel.getValueAt(i, j);
					System.out.println("Selection found at:"+i+"    :"+j);
					
					if(value instanceof Double){
						Double temp = (Double)value + amount;
						//theModel.setValueAt(temp, i, j);
						theModel.setDoubleData(i,j,temp);
					}
				}
			}
		}
	}
	
	/**
	 * Decrement cell values by passed double amount.
	 * @param amount
	 */
	public void decSelectedCells(double amount){
		
		int rowStart = this.getSelectedRow();
		int rowEnd = this.getSelectionModel().getMaxSelectionIndex();
		
		int colStart = this.getSelectedColumn();
		int colEnd = this.getColumnModel().getSelectionModel().getMaxSelectionIndex();
		
		for(int i = rowStart; i <= rowEnd; i++){
			for(int j = colStart; j <= colEnd; j++){
				if(this.isCellSelected(i,j)){
					// The cell is selected
					Object value = theModel.getValueAt(i, j);
					if(value instanceof Double){
						Double temp = (Double)value - amount;
						//theModel.setValueAt(temp, i, j);
						theModel.setDoubleData(i,j,temp);
					}
				}
			}
		}
	}
	
	/**
	 * Replace all table data with passed data.
	 * @param newData
	 */
	public void replaceAlltableData(Double[][] newData){
		((UtecTableModel)this.dataModel).replaceData(newData);
	}
}
