package com.romraider.metadata;

import java.util.ArrayList;

import com.romraider.metadata.exception.TableNotFoundException;

public class TableMetadataList<TableMetadata> extends ArrayList<AbstractTableMetadata> {
	
	public AbstractTableMetadata get(String name) throws TableNotFoundException {
		for (AbstractTableMetadata t : this) {
			if (t.getName().equalsIgnoreCase(name)) return t;
		}
		throw new TableNotFoundException();
	}
}