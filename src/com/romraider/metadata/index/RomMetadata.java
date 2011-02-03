package com.romraider.metadata.index;

import com.romraider.metadata.exception.ScalingMetadataNotFoundException;
import com.romraider.metadata.exception.TableMetadataNotFoundException;
import com.romraider.metadata.rom.AbstractTableMetadata;
import com.romraider.metadata.rom.ScalingMetadata;
import com.romraider.metadata.rom.ScalingMetadataList;
import com.romraider.metadata.rom.TableMetadataList;

public class RomMetadata {
	
	private TableMetadataList tableMetadata = new TableMetadataList();
	private ScalingMetadataList scalingMetadata = new ScalingMetadataList();
	private String id = "<null>";
	private String internalidstring = "<null>";
	private int    indernalidaddress = 0;
	private String year = "<null>";
	private String market = "<null>";
	private String make = "<null>";
	private String model = "<null>";
	private String submodel = "<null>";
	private String transmission = "<null>";
	private String memmodel = "<null>";
	private String flashmethod = "<null>";
	
	public TableMetadataList getTableMetadata() {
		return tableMetadata;
	}

	public void setTableMetadata(TableMetadataList tableMetadata) {
		this.tableMetadata = tableMetadata;
	}

	public ScalingMetadataList getScalingMetadata() {
		return scalingMetadata;
	}

	public void setScalingMetadata(ScalingMetadataList scalingMetadata) {
		this.scalingMetadata = scalingMetadata;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getInternalidstring() {
		return internalidstring;
	}

	public void setInternalidstring(String internalidstring) {
		this.internalidstring = internalidstring;
	}

	public int getInternalidaddress() {
		return indernalidaddress;
	}

	public void setInternalidaddress(int indernalidaddress) {
		this.indernalidaddress = indernalidaddress;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getSubmodel() {
		return submodel;
	}

	public void setSubmodel(String submodel) {
		this.submodel = submodel;
	}

	public String getTransmission() {
		return transmission;
	}

	public void setTransmission(String transmission) {
		this.transmission = transmission;
	}

	public String getMemmodel() {
		return memmodel;
	}

	public void setMemmodel(String memmodel) {
		this.memmodel = memmodel;
	}

	public String getFlashmethod() {
		return flashmethod;
	}

	public void setFlashmethod(String flashmethod) {
		this.flashmethod = flashmethod;
	}

	public int getIndernalidaddress() {
		return indernalidaddress;
	}

	public void setIndernalidaddress(int indernalidaddress) {
		this.indernalidaddress = indernalidaddress;
	}

	public void add(AbstractTableMetadata t) {
		tableMetadata.add(t);
	}
	
	public AbstractTableMetadata getTableMetadata(String n) throws TableMetadataNotFoundException {
		return tableMetadata.get(n);
	}
	
	public void add(ScalingMetadata s) {
		scalingMetadata.add(s);
	}
	
	public ScalingMetadata getScalingMetadata(String n) throws ScalingMetadataNotFoundException {
		return scalingMetadata.get(n);
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
	
	public String printTables() {
		StringBuffer output = new StringBuffer();
		for (AbstractTableMetadata t : tableMetadata) {
			output.append(t + "\n");
		}
		return new String(output);
	}
	
	public String printScaling() {
		StringBuffer output = new StringBuffer();
		for (ScalingMetadata t : scalingMetadata) {
			output.append(t + "\n");
		}
		return new String(output);
	}	
}