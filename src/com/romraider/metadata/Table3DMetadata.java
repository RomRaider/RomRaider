package com.romraider.metadata;

public class Table3DMetadata extends AbstractMultiDimensionTableMetadata {
	
	public Table3DMetadata() {
		setStatic(false);
	}
	
	public Table3DMetadata(boolean isStatic) {
		super(isStatic);
	}

	private Table1DMetadata xaxis;
	private Table1DMetadata yaxis;

	public Table1DMetadata getXaxis() {
		if (xaxis == null) return new Table1DMetadata();
		else return xaxis;
	}

	public void setXaxis(Table1DMetadata xaxis) {
		this.xaxis = xaxis;
	}

	public Table1DMetadata getYaxis() {
		if (yaxis == null) return new Table1DMetadata();
		else return yaxis;
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

	public Table1DMetadata getAxisByType(String type) {
		if (type.equalsIgnoreCase("x axis") || type.equalsIgnoreCase("static x axis")) return getXaxis();
		else if (type.equalsIgnoreCase("y axis") || type.equalsIgnoreCase("static y axis")) return getXaxis();
		else return new Table1DMetadata();
	}
	
	public Table1DMetadata getAxisByName(String name) {
		if (getXaxis().getName().equalsIgnoreCase(name)) return getXaxis();
		if (getYaxis().getName().equalsIgnoreCase(name)) return getYaxis();
		else return new Table1DMetadata();
	}

}
