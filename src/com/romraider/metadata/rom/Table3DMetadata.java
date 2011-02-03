package com.romraider.metadata.rom;

public class Table3DMetadata extends AbstractTableMetadata {

	private TableAxisMetadata xaxis = new TableAxisMetadata(TableAxisMetadata.TABLEMETADATA_TYPE_XAXIS);
	private TableAxisMetadata yaxis = new TableAxisMetadata(TableAxisMetadata.TABLEMETADATA_TYPE_YAXIS);

	public TableAxisMetadata getXaxis() {
		return xaxis;
	}

	public void setXaxis(TableAxisMetadata xaxis) {
		this.xaxis = xaxis;
	}

	public TableAxisMetadata getYaxis() {
		return yaxis;
	}

	public void setYaxis(TableAxisMetadata yaxis) {
		this.yaxis = yaxis;
	}

	public void save() {
		
	}

	public void load() {
		
	}
	
	public int getSize() {
		return 0 + xaxis.getSize() + yaxis.getSize();
	}

	public Table1DMetadata getXAxis() {
		return xaxis;
	}

	public Table1DMetadata getYAxis() {
		return yaxis;
	}
	
	public String toString() {
		return "table:" + getId() +" scaling:" + getScalingMetadata() + " xaxis:" + getXaxis() + " yaxis:" + getYaxis();
	}

}
