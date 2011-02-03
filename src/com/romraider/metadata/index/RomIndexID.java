package com.romraider.metadata.index;

import java.io.File;

public final class RomIndexID {
	
	private String	xmlid;
	private String	internalIDString;
	private int 	internalIDAddress = -1; // Default to -1 to identify abstract roms
	private String 	include;
	private File	definitionFile;
	
	public String getInternalIDString()	{ return internalIDString; }
	public int 	  getInternalIDAddress(){ return internalIDAddress; }
	public String getInclude() 			{ return include; }
	public File	  getDefinitionFile()	{ return definitionFile; }
	public String getXmlid() 			{ return xmlid; }

	public void setInternalIDString(String internalIDString){ this.internalIDString = internalIDString; }
	public void setInternalIDAddress(int internalIDAddress) { this.internalIDAddress = internalIDAddress; }
	public void setInclude(String include) 					{ this.include = include; }
	public void setDefinitionFile(File file) 				{ this.definitionFile = file; }	
	public void setXmlid(String xmlid) 						{ this.xmlid = xmlid; }
	
	public boolean isReady() {
		// TODO: Validate romid is usable
		return true;
	}
	
	public boolean isAbstract() {
		if (internalIDString == null || internalIDAddress == -1) return true;
		else return false;
	}
	
	public String toString() {
		return 	"xmlid:" + xmlid + 
		"internalidstring:" + internalIDString + 
		"internalidaddress:" + internalIDAddress + 
		"definitionfile:" + definitionFile + 
		"include:" + include;
	}
	
}