package com.romraider.maps;

import java.util.Vector;
import com.romraider.maps.DataCell;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import com.romraider.Settings;

public class TableDataSource implements TableModel{
		
    protected Scale curScale;
    protected int storageAddress;
    protected int storageType;
    protected boolean signed;
    protected Settings.Endian endian = Settings.Endian.BIG;
    protected boolean flip;
    protected boolean beforeRam = false;
    protected int ramOffset = 0;
    
    protected int columnCount;
    protected DataCell[] data = null;
    
	@Override
	public int getRowCount() {
		return (data.length / columnCount) + 1;
	}

	@Override
	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return null;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return DataCell.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return data[rowIndex * columnCount + columnIndex];
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		data[rowIndex * columnCount + columnIndex] = (DataCell) aValue;
		
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}
}
