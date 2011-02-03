package com.romraider.metadata.rom;

public abstract class AbstractTableMetadata {
	
	public static int TABLEMETADATA_TYPE_1D 	= 1;
	public static int TABLEMETADATA_TYPE_2D 	= 2;
	public static int TABLEMETADATA_TYPE_3D 	= 3;
	public static int TABLEMETADATA_TYPE_XAXIS = 4;
	public static int TABLEMETADATA_TYPE_YAXIS = 5;
	
	private String 	id;
	private ScalingMetadata scalingMetadata;
	private String 	category;
	private String 	description;
	private int		type;
	
	public abstract int getSize(); // Size of table data in bytes
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
		if (scalingMetadata == null) return new ScalingMetadata();
		else return scalingMetadata;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getType() {
		return type;
	}
	
	public String toString() {
		return getId();
	}
	
}