package com.romraider.maps;

import com.romraider.Settings;

public class RawDataSource {
    protected Scale curScale;
    protected int storageAddress;
    protected int storageType;
    protected boolean signed;
    protected Settings.Endian endian = Settings.Endian.BIG;
    protected boolean flip;
    protected boolean beforeRam = false;
    protected int ramOffset = 0;
    
    protected double[] data;
    protected byte[] input;
    
    public RawDataSource(byte[] input) {
    	this.input = input;
    }
}
