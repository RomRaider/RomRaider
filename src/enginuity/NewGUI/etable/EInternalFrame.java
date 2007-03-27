package enginuity.NewGUI.etable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import enginuity.NewGUI.data.ApplicationStateManager;
import enginuity.NewGUI.data.TableMetaData;
import enginuity.NewGUI.tools.ClipBoardCopy;
import enginuity.NewGUI.tools.FitData;

public class EInternalFrame extends JInternalFrame implements InternalFrameListener, ActionListener{
	private Stack<ETableSaveState> savedData = new Stack<ETableSaveState>();
	
	private ETable eTable;
	private TableMetaData tableMetaData;
	private ClipBoardCopy excelCopy;
	
	public EInternalFrame(TableMetaData tableMetaData, Double[][] data, Dimension tableDimensions){
		super(tableMetaData.getTableName()+"   "+tableMetaData.getTableGroup(), true, true, true, true);
		this.tableMetaData = tableMetaData;
		
		// Save initial data
		this.savedData.push(new ETableSaveState(data));
		

		// Set the frame icon
		Image img = Toolkit.getDefaultToolkit().getImage("graphics/enginuity-ico.gif");
		ImageIcon imgIcon = new ImageIcon(img);
		this.setFrameIcon(imgIcon);
		
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
		eTable = new ETable(tableMetaData, data, columnModel);
		eTable.setBackground(Color.LIGHT_GRAY);
		excelCopy = new ClipBoardCopy(eTable);
		
		JTable headerColumn = new JTable(tableModel, rowHeaderModel);
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
		
        
        // Add the toolbar
        ETableToolBar toolBar = new ETableToolBar(tableMetaData, eTable);
		
        
    	// Add internal frame
		this.setLayout(new BorderLayout());
		this.setJMenuBar(new ETableMenuBar(this));
		this.add(toolBar, BorderLayout.NORTH);
		this.add(scrollPane, BorderLayout.CENTER);
		this.setSize(tableDimensions);
		this.setVisible(true);
		this.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		this.addInternalFrameListener(this);
	}
	
	public boolean dataChanged(){
		//if(this.eTable.getTheModel().getData() != this.savedData.get(savedData.size()-1).getData()){
		if(!compareMe(this.eTable.getTheModel().getData(), this.savedData.get(savedData.size()-1).getData())){
			System.out.println("Data not the same.");
			return true;
		}
		
		return false;
	}
	
	/**
	 * Helper method to compare data in two arrays. Must be a better wat to do this.
	 * .equals and == does not work for some reason.
	 * @param data1
	 * @param data2
	 * @return
	 */
	private boolean compareMe(Double[][] data1, Double[][] data2){
		int width = data1.length;
		int height = data1[0].length;
		
		
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				if((data1[i][j] - data2[i][j]) != 0){
					return false;
				}
			}
		}
		return true;
	}
	
	public Double[][] getTableData(){
		Double[][] data = this.eTable.getTheModel().getData();
		return data;
	}
	
	public void saveDataToParentTuningEntity(){
		this.tableMetaData.getParentTuningEntity().setTableData(this.tableMetaData.getTableIdentifier(), this.eTable.getTheModel().getData());
	}
	
	public void saveDataState(){
		this.savedData.push(new ETableSaveState(this.getTableData()));
	}
	
	public void revertDataState(){
		if(!this.savedData.isEmpty()){
			if(this.savedData.size() > 1){
				this.setTableData(this.savedData.pop().getData());
			}else if(savedData.size() == 1){
				this.setTableData(this.savedData.peek().getData());
			}
		}
	}
	
	
	public void setTableData(Double[][] data){
		this.eTable.getTheModel().replaceData(data);
	}
	
	public void internalFrameOpened(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void internalFrameClosing(InternalFrameEvent arg0) {
		this.setVisible(false);
		
	}

	public void internalFrameClosed(InternalFrameEvent arg0) {
	}

	public void internalFrameIconified(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void internalFrameDeiconified(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void internalFrameActivated(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void internalFrameDeactivated(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public TableMetaData getTableMetaData() {
		return tableMetaData;
	}

	public ETable getETable() {
		return eTable;
	}

	public ClipBoardCopy getExcelCopy() {
		return excelCopy;
	}
}
