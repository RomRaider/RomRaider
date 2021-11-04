package com.romraider.xml.ConversionLayer;

import java.io.File;

public class ConversionLayerFactory {
    private final static ConversionLayer[] convLayers = {new BMWCodingConversionLayer()};
    
	//Check if its an .xml file
	public static boolean requiresConversionLayer(File f) {
		return !f.getName().matches("^.*\\.(xml|XML)$");
	}
	
	public static ConversionLayer getConversionLayerForFile(File f) {
        for(ConversionLayer l: convLayers) {
        	if(l.isFileSupported(f)) {
    			return l;
        	}
        }
        
        return null;
	}
}