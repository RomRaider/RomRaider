package com.romraider.metadata;

import com.romraider.xml.RomNotFoundException;

public class RomMetadataIndex {
	
	RomID[] romid;
	int 	index = 0;

	@SuppressWarnings("unused")
	private RomMetadataIndex() {}
	
	public RomMetadataIndex (RomID[] romid) {
		this.romid = romid;
	}
	
	public RomID getRomIDByXmlID (String XmlID) throws RomNotFoundException {
		for (int i = 0; i < romid.length; i++) {
			if (romid[i].getXmlid().equalsIgnoreCase(XmlID)) return romid[i];
		}
		throw new RomNotFoundException();
	}
	
	public RomID getRomID (int i) {
		return romid[i];
	}
	
	public RomID getRomIDByIndex (int i) {
		return romid[i];
	}

	public int size() {
		return romid.length;
	}

}