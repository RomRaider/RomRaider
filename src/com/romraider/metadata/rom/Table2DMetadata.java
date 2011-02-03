package com.romraider.metadata.rom;

public class Table2DMetadata extends AbstractTableMetadata {

	private TableAxisMetadata axis = new TableAxisMetadata(TableAxisMetadata.TABLEMETADATA_TYPE_YAXIS);

	public TableAxisMetadata getYaxis() {
		return axis;
	}

	public void setYAxis(TableAxisMetadata axis) {
		this.axis = axis;
	}

	public void save() {
		
	}

	public void load() {
		
	}
	
	public int getSize() {
		return 0 + axis.getSize();
	}
	
	public String toString() {
		return "table:" + getId() + " scaling:" + getScalingMetadata() + " axis:" + getYaxis();
	}
	
}
