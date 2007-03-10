/*
 * Created on May 28, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package enginuity.logger.utec.commEvent;



import java.util.*;

import enginuity.logger.utec.gui.mapTabs.UtecDataManager;
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
	private boolean isMapData = false;
	private UtecMapData mapData = null;
	private boolean isValidData = true;
	
	public LoggerEvent(String buffer){
		
		this.UtecBuffer = buffer;
		this.setLoggerData();
	}
	
	private void setLoggerData(){
		
		System.out.println("LoggerEvent:"+UtecBuffer+":");
		
		data = UtecBuffer.split(",");
		
		// Count the "," to ensure this is a line of logging data
		System.out.println("LoggerEvent: Checking data length");
		if(data.length < 4){
			this.isValidData = false;
			System.out.println("LoggerEvent: Too short returning, not valid data.");
			return;
		}
		System.out.println("LoggerEvent: Data length ok:"+data.length);
		
		doubleData = new double[data.length];
		
		System.out.println("LoggerEvent: Building double array.");
		for(int i = 0; i < data.length; i++){
			String theData = data[i];
			theData = theData.trim();
			if(theData.startsWith(">")){
				theData = "25.5";
			}
			if(theData.startsWith("--")){
				theData = "0.0";
			}
			if(theData.startsWith("ECU")){
				theData = "0.0";
			}
			
			try{
				doubleData[i] = Double.parseDouble(theData);
			}catch (NumberFormatException e) {
				System.out.println("LoggerEvent: Not valid data. Number error in commevent :"+theData+":");
				for(int k=0;k<theData.length();k++){
					System.out.println("-  LoggerEvent int values *****:"+(int)theData.charAt(k)+":");
				}
				this.isValidData = false;
				return;
	        }
		}
		
		System.out.println("LoggerEvent: Setting data manager afr, psi and knock data.");
		UtecDataManager.setAfrData(doubleData[Integer.parseInt(UtecProperties.getProperties("utec.afrIndex")[0])]);
		UtecDataManager.setPsiData(doubleData[Integer.parseInt(UtecProperties.getProperties("utec.psiIndex")[0])]);
		UtecDataManager.setKnockData(doubleData[Integer.parseInt(UtecProperties.getProperties("utec.knockIndex")[0])]);
		System.out.println("******* Logger event DONE ok***********");
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


	private void setUtecBuffer(String utecBuffer) {
		UtecBuffer = utecBuffer;
	}


	public boolean isValidData() {
		return isValidData;
	}
}
