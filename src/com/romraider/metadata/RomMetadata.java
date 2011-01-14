package com.romraider.metadata;

import java.util.Vector;

public class RomMetadata {
	
	private RomID 							romid;
	private Vector<AbstractTableMetadata> 	tableMetadata   = new Vector<AbstractTableMetadata>();
	private Vector<ScalingMetadata>			scalingMetadata = new Vector<ScalingMetadata>();

	public RomID getRomid() {
		return romid;
	}

	public void setRomID(RomID romid) {
		this.romid = romid;
	}

	public void add(AbstractTableMetadata t) {
		// If table name already exists, replace
		for (AbstractTableMetadata x : tableMetadata) {
			if (x.getName().equalsIgnoreCase(t.getName())) {
				x = t;
				return;
			}
		}
		// If table was not found, add it
		tableMetadata.add(t);
	}
	
	public AbstractTableMetadata getTableMetadata(String n) throws TableNotFoundException {
		for (AbstractTableMetadata t : tableMetadata) {
			if (t.getName().equalsIgnoreCase(n)) return t;
		}
		throw new TableNotFoundException();
	}
	
	public void add(ScalingMetadata s) {
		// If scaling name already exists, replace
		for (ScalingMetadata x : scalingMetadata) {
			if (x.getName().equalsIgnoreCase(s.getName())) {
				x = s;
				return;
			}
		}
		// If scaling was not found, add it
		scalingMetadata.add(s);
	}
	
	public ScalingMetadata getScalingMetadata(String n) throws ScalingMetadataNotFoundException {
		for (ScalingMetadata s: scalingMetadata) {
			if (s.getName().equalsIgnoreCase(n)) return s;
		}
		throw new ScalingMetadataNotFoundException();
	}
	
	public ScalingMetadata getScalingMetadata(int i) {
		return scalingMetadata.get(i);
	}
	
	public AbstractTableMetadata getTableMetadata(int i) {
		return tableMetadata.get(i);
	}

	public int scalingMetadataSize() {
		return scalingMetadata.size();
	}
	
	public int tableMetadataSize() {
		return tableMetadata.size();
	}
	
}