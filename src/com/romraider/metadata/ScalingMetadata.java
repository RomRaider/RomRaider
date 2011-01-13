package com.romraider.metadata;

public class ScalingMetadata {
	
	public static int ENDIAN_LITTLE = 0;
	public static int ENDIAN_BIG = 1;
	private String name			= "Name";
	private String units		= "Unit";
	private String toexpr		= "x";
	private String frexpr		= "x";
	private String format		= "0.0";
	private double min			= 0;
	private double max			= 255;
	private double inc			= 1;
	private String storageType	= "uint16";
	private int	   endian		= ENDIAN_LITTLE;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUnits() {
		return units;
	}
	public void setUnits(String units) {
		this.units = units;
	}
	public String getToexpr() {
		return toexpr;
	}
	public void setToexpr(String toexpr) {
		this.toexpr = toexpr;
	}
	public String getFrexpr() {
		return frexpr;
	}
	public void setFrexpr(String frexpr) {
		this.frexpr = frexpr;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public double getMin() {
		return min;
	}
	public void setMin(double d) {
		this.min = d;
	}
	public double getMax() {
		return max;
	}
	public void setMax(double max) {
		this.max = max;
	}
	public double getInc() {
		return inc;
	}
	public void setInc(double inc) {
		this.inc = inc;
	}
	public String getStorageType() {
		return storageType;
	}
	public void setStorageType(String storageType) {
		this.storageType = storageType;
	}
	public int getEndian() {
		return endian;
	}
	public void setEndian(int endian) {
		this.endian = endian;
	}

}
