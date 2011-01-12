package com.romraider.metadata;

import java.util.Vector;

public class RomMetadata {
	
	private RomID 					romid;
	private Vector<TableMetadata> 	tableMetadata;

	public RomID getRomid() {
		return romid;
	}

	public void setRomID(RomID romid) {
		this.romid = romid;
	}

	public void addTableMetadata(TableMetadata t) {
		tableMetadata.add(t);
	}
	
	public TableMetadata getTableMetadata(String n) {
		for (TableMetadata t : tableMetadata) {
			// TODO: Get table metadata by name
		}
		return null;
	}
	
	public TableMetadata getTableMetadata(int i) {
		return tableMetadata.get(i);
	}
}