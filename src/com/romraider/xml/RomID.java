package com.romraider.xml;

import java.io.File;

public final class RomID {
	
	private String	xmlid;
	private String	internalIDString;
	private int 	internalIDAddress;
	private String 	include;
	private File	file;

	public void 	setXmlID(String xmlid) { this.xmlid = xmlid; }
	public void 	setInternalIDString(String internalIDString) { this.internalIDString = internalIDString; }
	public void 	setInternalIDAddress(int internalIDAddress) { this.internalIDAddress = internalIDAddress; }
	public void 	setInclude(String include) { this.include = include; }
	public void 	setFile(File file) { this.file = file; }
	
	public String 	getXmlID() 				{ return xmlid; }
	public String 	getInternalIDString()	{ return internalIDString; }
	public int 		getInternalIDAddress() 	{ return internalIDAddress; }
	public String 	getInclude() 			{ return include; }
	public File		getFile()				{ return file; }
	
	public boolean	isReady() {
		// TODO: Validate romid is usable
		return true;
	}
	
	public String toString() {
		return 	"xmlid: " + xmlid +	
				"; internalidstring: " + internalIDString +	
				"; internalidaddress: " + internalIDAddress + 
				"; include: " + include +
				"; file: " + file.getAbsoluteFile() + 
				"; ready: " + isReady();
	}
	
}