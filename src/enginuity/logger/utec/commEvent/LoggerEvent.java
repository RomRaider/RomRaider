/*
 * Created on May 28, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package enginuity.logger.utec.commEvent;



import java.util.*;

import enginuity.logger.utec.gui.mapTabs.DataManager;
import enginuity.logger.utec.mapData.UtecMapData;
import enginuity.logger.utec.properties.UtecProperties;


/**
 * @author emorgan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class LoggerEvent {
	private String UtecBuffer = null;
	private String[] data = new String[6];
	private double[] doubleData = null; //new double[6];
	
	private boolean isLoggerData = false;
	private boolean isMapData = false;
	
	private UtecMapData mapData = null;
	
	private boolean isValidData = true;
	
	public void setLoggerData(String buffer){
		UtecBuffer = buffer;
		
		data = UtecBuffer.split(",");
		doubleData = new double[data.length];
		
		for(int i = 0; i < data.length; i++){
			String theData = data[i];
			theData = theData.trim();
			if(theData.startsWith(">")){
				theData = "25.5";
			}
			if(theData.startsWith("--")){
				theData = "0.0";
			}
			
			try{
				doubleData[i] = Double.parseDouble(theData);
			}catch (NumberFormatException e) {
				System.out.println("Number error in commevent.");
				this.isValidData = false;
				return;
	        }
			
			
			// Valid data found
			String[] afrIndex = UtecProperties.getProperties("utec.afrIndex");
			if(afrIndex == null || afrIndex[0] == null || afrIndex[0].length() < 1){
				// No afr data available
			}else{
				System.out.println("AFR Data available.");
				UtecAFRListener utecAFRListener = DataManager.getUtecAFRListener();
				if(utecAFRListener != null){
					utecAFRListener.receivedUtecAFRData(Double.parseDouble(afrIndex[0]));
				}
			}
		}
		
	}


	public boolean isLoggerData() {
		return isLoggerData;
	}


	public void setLoggerData(boolean isLoggerData) {
		this.isLoggerData = isLoggerData;
	}


	public boolean isMapData() {
		return isMapData;
	}


	public void setMapData(boolean isMapData) {
		this.isMapData = isMapData;
	}


	public UtecMapData getMapData() {
		return mapData;
	}


	public void setMapData(UtecMapData mapData) {
		this.mapData = mapData;
	}


	public String[] getData() {
		return data;
	}


	public void setData(String[] data) {
		this.data = data;
	}


	public double[] getDoubleData() {
		return doubleData;
	}


	public void setDoubleData(double[] doubleData) {
		this.doubleData = doubleData;
	}


	public String getUtecBuffer() {
		return UtecBuffer;
	}


	public void setUtecBuffer(String utecBuffer) {
		UtecBuffer = utecBuffer;
	}


	public boolean isValidData() {
		return isValidData;
	}
}
