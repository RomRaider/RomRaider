package com.romraider.metadata;

public abstract class AbstractMultiDimensionTableMetadata extends AbstractTableMetadata {
	
	public AbstractMultiDimensionTableMetadata() {
		super();
	}
	
	public AbstractMultiDimensionTableMetadata(boolean isStatic) {
		super(isStatic);
	}
	
	public abstract Table1DMetadata getAxisByType(String type);
	
	public abstract Table1DMetadata getAxisByName(String name);			

}