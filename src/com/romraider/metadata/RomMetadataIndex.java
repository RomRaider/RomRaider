package com.romraider.metadata;

import com.romraider.metadata.exception.RomNotFoundException;


public class RomMetadataIndex {
	
	RomIndexID[] romid;
	int 	index = 0;

	@SuppressWarnings("unused")
	private RomMetadataIndex() {}
	
	public RomMetadataIndex (RomIndexID[] romid) {
		this.romid = romid;
	}
	
	public RomIndexID getRomIDByXmlID (String XmlID) throws RomNotFoundException {
		for (int i = 0; i < romid.length; i++) {
			if (romid[i].getXmlid().equalsIgnoreCase(XmlID)) return romid[i];
		}
		throw new RomNotFoundException();
	}
	
	public RomIndexID getRomID (int i) {
		return romid[i];
	}
	
	public RomIndexID getRomIDByIndex (int i) {
		return romid[i];
	}

	public int size() {
		return romid.length;
	}

}