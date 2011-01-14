package com.romraider.metadata;

public class Table1DMetadata extends AbstractTableMetadata {

	public Table1DMetadata() {
		setStatic(false);
	}
	
	public Table1DMetadata(boolean isStatic) {
		super(isStatic);
	}

	public void save() {

	}

	public void load() {

	}
	
	public int getSize() {
		return 0;
	}

}
