package com.romraider.metadata;

import java.util.ArrayList;
import com.romraider.metadata.exception.ScalingNotFoundException;

public class ScalingMetadataList<ScalingMetadata> extends ArrayList<ScalingMetadata> {

	public ScalingMetadata get(String name) {
		for (ScalingMetadata s : this) {
			if (s.getName().equalsIgnoreCase(name)) return s;
		}
		throw new ScalingNotFoundException();
	}
}