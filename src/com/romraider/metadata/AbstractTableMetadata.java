package com.romraider.metadata;

public abstract class AbstractTableMetadata {
	
	private String name;
	private ScalingMetadata scalingMetadata;
	private String category;
	private String description;
	
	public abstract int getSize(); // Size of table data in bytes
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public abstract void save();
	public abstract void load();
	
	public void setScalingMetadata(ScalingMetadata s) {
		this.scalingMetadata = s;
	}
	
	public ScalingMetadata getScalingMetadata() {
		return scalingMetadata;
	}
	
}