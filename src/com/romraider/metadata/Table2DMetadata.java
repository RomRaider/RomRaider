package com.romraider.metadata;

public class Table2DMetadata extends AbstractTableMetadata {
	
	private Table1DMetadata axis;

	public Table1DMetadata getAxis() {
		return axis;
	}

	public void setAxis(Table1DMetadata axis) {
		this.axis = axis;
	}

	public void save() {
		
	}

	public void load() {
		
	}
	
	public int getSize() {
		return 0 + axis.getSize();
	}

}
