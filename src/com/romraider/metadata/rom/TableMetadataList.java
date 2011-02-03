package com.romraider.metadata.rom;

import java.util.ArrayList;
import com.romraider.metadata.exception.TableMetadataNotFoundException;

public class TableMetadataList extends ArrayList<AbstractTableMetadata> {

	public AbstractTableMetadata get(String id) throws TableMetadataNotFoundException {
		for (AbstractTableMetadata t : this) {
			if (t.getId().equalsIgnoreCase(id)) return t;
		}
		throw new TableMetadataNotFoundException();
	}
	
	public boolean add(AbstractTableMetadata table) {
		for (AbstractTableMetadata t : this) {
			if (t.getId().equalsIgnoreCase(table.getId())) super.remove(t);
		}
		super.add(table);
		return true;
	}
}