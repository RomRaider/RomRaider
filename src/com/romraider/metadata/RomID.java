package com.romraider.metadata;

import java.io.File;

public final class RomID {
	
	private String	xmlid;
	private String	internalIDString;
	private int 	internalIDAddress;
	private String 	include;
	private File	definitionFile;
	private String  ecuid;
	private String  year;
	private String  market;
	private String  make;
	private String  model;
	private String 	submodel;
	private String	transmission;
	private String 	memmodel;
	private String	flashMethod;	
	
	public String getXmlID() 			{ return xmlid; }
	public String getInternalIDString()	{ return internalIDString; }
	public int 	  getInternalIDAddress(){ return internalIDAddress; }
	public String getInclude() 			{ return include; }
	public File	  getDefinitionFile()	{ return definitionFile; }
	public String getXmlid() 			{ return xmlid; }
	public String getEcuid() 			{ return ecuid; }
	public String getYear() 			{ return year; }
	public String getMarket() 			{ return market; }
	public String getMake() 			{ return make; }
	public String getModel() 			{ return model; }
	public String getSubmodel() 		{ return submodel; }
	public String getTransmission() 	{ return transmission; }
	public String getMemmodel() 		{ return memmodel; }
	public String getFlashMethod() 		{ return flashMethod; }

	public void setXmlID(String xmlid) 						{ this.xmlid = xmlid; }
	public void setInternalIDString(String internalIDString){ this.internalIDString = internalIDString; }
	public void setInternalIDAddress(int internalIDAddress) { this.internalIDAddress = internalIDAddress; }
	public void setInclude(String include) 					{ this.include = include; }
	public void setDefinitionFile(File file) 				{ this.definitionFile = file; }	
	public void setXmlid(String xmlid) 						{ this.xmlid = xmlid; }
	public void setEcuid(String ecuid) 						{ this.ecuid = ecuid; }
	public void setYear(String year) 						{ this.year = year; }
	public void setMarket(String market) 					{ this.market = market; }
	public void setMake(String make) 						{ this.make = make; }
	public void setModel(String model)						{ this.model = model; }
	public void setSubmodel(String submodel) 				{ this.submodel = submodel; }
	public void setTransmission(String transmission) 		{ this.transmission = transmission; }
	public void setMemmodel(String memmodel) 				{ this.memmodel = memmodel; }
	public void setFlashMethod(String flashMethod) 			{ this.flashMethod = flashMethod; }
	
	public boolean	isReady() {
		// TODO: Validate romid is usable
		return true;
	}
	
	public String toString() {
		return 	"xmlid: " + xmlid +	
				"; internalidstring: " + internalIDString +	
				"; internalidaddress: " + internalIDAddress + 
				"; include: " + include +
				"; file: " + definitionFile.getAbsoluteFile() + 
				"; ready: " + isReady();
	}
	
}