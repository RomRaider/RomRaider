package com.romraider.metadata;

public abstract class AbstractTableMetadata {
	
	public static int TABLEMETADATA_TYPE_1D 	= 1;
	public static int TABLEMETADATA_TYPE_2D 	= 2;
	public static int TABLEMETADATA_TYPE_3D 	= 3;
	public static int TABLEMETADATA_TYPE_XAXIS = 4;
	public static int TABLEMETADATA_TYPE_YAXIS = 5;
	public static int TABLEMETADATA_TYPE_AXIS 	= 6;
	
	private String 	name;
	private ScalingMetadata scalingMetadata;
	private String 	category;
	private String 	description;
	private boolean isStatic;
	private int		type;
	
	public AbstractTableMetadata() {
		setStatic(false);
	}
	
	public AbstractTableMetadata (boolean isStatic) {
		this.isStatic = isStatic;
	}
	
	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

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
		if (scalingMetadata == null) return new ScalingMetadata();
		else return scalingMetadata;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getType() {
		return type;
	}
	
}