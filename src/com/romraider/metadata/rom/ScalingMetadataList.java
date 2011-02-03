package com.romraider.metadata.rom;

import java.util.ArrayList;

import com.romraider.metadata.exception.ScalingMetadataNotFoundException;

public class ScalingMetadataList extends ArrayList<ScalingMetadata> {

	public ScalingMetadata get(String id) throws ScalingMetadataNotFoundException {
		for (ScalingMetadata s : this) {
			if (s.getId().equalsIgnoreCase(id)) return s;
		}
		throw new ScalingMetadataNotFoundException();
	}
	
	public boolean add(ScalingMetadata scaling) {
		for (ScalingMetadata s : this) {
			if (s.getId().equalsIgnoreCase(scaling.getId())) super.remove(s);
		}
		super.add(scaling);
		return true;
	}
}