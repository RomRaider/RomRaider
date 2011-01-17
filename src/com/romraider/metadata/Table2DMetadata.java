package com.romraider.metadata;

public class Table2DMetadata extends AbstractMultiDimensionTableMetadata {
	
	public Table2DMetadata() {
		setStatic(false);
	}
	
	public Table2DMetadata(boolean isStatic) {
		super(isStatic);
	}

	private Table1DMetadata axis;

	public Table1DMetadata getAxis() {
		if (axis == null) return new Table1DMetadata();
		else return axis;
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
	
	public Table1DMetadata getAxisByType(String type) {
		return axis;
	}
	
	public Table1DMetadata getAxisByName(String name) {
		try {
			if (getAxis().getName().equalsIgnoreCase(name)) return getAxis();
			else return new Table1DMetadata();
		} finally {
			return new Table1DMetadata();
		}
	}
	
}
