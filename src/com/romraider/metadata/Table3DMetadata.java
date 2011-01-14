package com.romraider.metadata;

public class Table3DMetadata extends AbstractTableMetadata {
	
	private Table1DMetadata xaxis;
	private Table1DMetadata yaxis;

	public Table1DMetadata getXaxis() {
		return xaxis;
	}

	public void setXaxis(Table1DMetadata xaxis) {
		this.xaxis = xaxis;
	}

	public Table1DMetadata getYaxis() {
		return yaxis;
	}

	public void setYaxis(Table1DMetadata yaxis) {
		this.yaxis = yaxis;
	}

	public void save() {
		
	}

	public void load() {
		
	}
	
	public int getSize() {
		return 0 + xaxis.getSize() + yaxis.getSize();
	}

}
