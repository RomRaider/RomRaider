package com.romraider.metadata.rom;

public class TableAxisMetadata extends Table1DMetadata {

	private boolean isStatic;
	private int axisType;

	private TableAxisMetadata(){}
	
	public TableAxisMetadata(int axisType, boolean isStatic) {
		this.axisType = axisType;
		this.isStatic = isStatic;
	}
	
	public TableAxisMetadata(int axisType) {
		this.axisType = axisType;
		isStatic = false;
	}

	public int getAxisType() {
		return axisType;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}
	
	public boolean isStatic() {
		return isStatic;
	}
}
