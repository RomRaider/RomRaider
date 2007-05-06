package enginuity.NewGUI.etable.dataJPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Stack;

import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import enginuity.NewGUI.data.TableMetaData;
import enginuity.NewGUI.etable.ETable;
import enginuity.NewGUI.etable.ETableMenuBar;
import enginuity.NewGUI.etable.ETableRowLabel;
import enginuity.NewGUI.etable.ETableSaveState;
import enginuity.NewGUI.etable.ETableToolBar;
import enginuity.NewGUI.etable.text.RotatedLabel;
import enginuity.NewGUI.tools.ClipBoardCopy;

public class DataJPanel3DDouble extends JPanel implements DataJPanelInterface{
	private Stack<ETableSaveState> savedData = new Stack<ETableSaveState>();
	private ETable eTable;
	private TableMetaData tableMetaData;
	private ClipBoardCopy excelCopy;
	private ETableMenuBar eTableMenuBar = new ETableMenuBar(this);
	private ETableToolBar toolBar = null;
	
	public DataJPanel3DDouble(TableMetaData tableMetaData, Object[][] data) {
		
		Double[][] newData = null;
		if(data[0][0] instanceof Double){
			// Typical 2D & 3D data
			newData = (Double[][])data;
		}
		
		// Save initial data
		this.savedData.push(new ETableSaveState(newData));
		
		
		TableColumnModel columnModel = new DefaultTableColumnModel(){
			boolean first = true;
			public void addColumn(TableColumn tc){
				if(first){
					first = false;
					return;
				}
				//tc.setMinWidth(30);
				//tc.setMaxWidth(30);
				super.addColumn(tc);
			}
		};
		
		TableColumnModel rowHeaderModel = new DefaultTableColumnModel(){
			boolean first = true;
			public void addColumn(TableColumn tc){
				if(first){
					tc.setMaxWidth(35);
					super.addColumn(tc);
					first = false;
				}
			}
		};
		
		TableModel tableModel = new ETableRowLabel(data[0].length, tableMetaData.getRowLabels());
		eTable = new ETable(tableMetaData, newData, columnModel);
		this.toolBar = new ETableToolBar(tableMetaData, eTable);
		
		
		eTable.setBackground(Color.LIGHT_GRAY);
		excelCopy = new ClipBoardCopy(eTable);
		
		JTable headerColumn = new JTable(tableModel, rowHeaderModel);
		headerColumn.setBackground(new Color(236, 233, 216));
		eTable.createDefaultColumnsFromModel();
		headerColumn.createDefaultColumnsFromModel();
		eTable.setSelectionModel(headerColumn.getSelectionModel());
		
		headerColumn.setMaximumSize(new Dimension(40, 10000));
		headerColumn.setColumnSelectionAllowed(false);
		headerColumn.setCellSelectionEnabled(false);
		
		JViewport jv = new JViewport();
		jv.setView(headerColumn);
		jv.setPreferredSize(headerColumn.getMaximumSize());
		
		headerColumn.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		JScrollPane scrollPane = new JScrollPane(eTable);
		scrollPane.setRowHeader(jv);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		
		// Table Title
		JLabel titleJLabel = new JLabel(tableMetaData.getTableName());
		titleJLabel.setHorizontalAlignment(JLabel.CENTER);
		
		// X Label
		JLabel xAxisJLabel = new JLabel(tableMetaData.getXAxisLabel());
		xAxisJLabel.setHorizontalAlignment(JLabel.CENTER);
		
		// Y Label
		RotatedLabel yAxisJLabel = new RotatedLabel(tableMetaData.getYAxisLabel());
		yAxisJLabel.setHorizontalAlignment(JLabel.CENTER);
		
		this.setLayout(new BorderLayout());
		this.add(titleJLabel, BorderLayout.NORTH);
		this.add(xAxisJLabel, BorderLayout.SOUTH);
		this.add(yAxisJLabel, BorderLayout.WEST);
		
		this.add(scrollPane, BorderLayout.CENTER);
	}
	
	public JToolBar getToolBar(){
		return this.toolBar;
	}
	
	public JMenuBar getMenuBar(){
		return this.eTableMenuBar;
	}
	
	/**
	 * Check to see if data relevant to this frame has changed
	 * 
	 * @return
	 */
	public boolean dataChanged(){
		//if(this.eTable.getTheModel().getData() != this.savedData.get(savedData.size()-1).getData()){
		if(!compareMe(this.eTable.getTheModel().getData(), this.savedData.get(savedData.size()-1).getData())){
			System.out.println("Data not the same.");
			return true;
		}
		
		return false;
	}
	

	/**
	 * Helper method to compare data in two arrays. Must be a better way to do this.
	 * .equals and == does not work for some reason.
	 * @param data1
	 * @param data2
	 * @return
	 */
	private boolean compareMe(Double[][] data1, Object[][] data2){
		int width = data1.length;
		int height = data1[0].length;
		
		
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				if((data1[i][j] - (Double)data2[i][j]) != 0){
					return false;
				}
			}
		}
		return true;
	}

	public void copySelectedTableData() {
		// TODO Auto-generated method stub
		
	}

	public void copyEntireTable() {
		// TODO Auto-generated method stub
		
	}

	public void pasteTableData() {
		// TODO Auto-generated method stub
		
	}

	public void setClosed(boolean value) {
		// TODO Auto-generated method stub
		
	}

	public void revertDataState() {
		// TODO Auto-generated method stub
		
	}

	public void saveDataState() {
		// TODO Auto-generated method stub
		
	}

	public void replaceData(Object[][] newData) {
		// TODO Auto-generated method stub
		
	}

	public Object[][] getData() {
		// TODO Auto-generated method stub
		return null;
	}
}
