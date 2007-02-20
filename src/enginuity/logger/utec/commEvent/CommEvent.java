/*
 * Created on May 28, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package enginuity.logger.utec.commEvent;



import java.util.*;
import enginuity.logger.utec.mapData.UtecMapData;


/**
 * @author emorgan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CommEvent {
	private String UtecBuffer = null;
	private String[] data = new String[6];
	private double[] doubleData = new double[6];
	
	private boolean isLoggerData = false;
	private boolean isMapData = false;
	
	private UtecMapData mapData = null;
	
	public void setLoggerData(String buffer){
		UtecBuffer = buffer;
		StringTokenizer st = new StringTokenizer(UtecBuffer, ",");
		int counter = 0;
		while(st.hasMoreTokens()){
			String theData = st.nextToken();
			
			//RPM
			if(counter == 0){
				data[0] = theData;
			}
			
			//PSI
			if(counter == 1){
				data[1] = theData;
			}
			
			//KNOCK
			if(counter == 5){
				data[2] = theData;
			}
			
			//IGN
			if(counter == 6){
				data[3] = theData;
			}
			
			//DUTY
			if(counter == 7){
				data[4] = theData;
			}
			
			//AFR
			if(counter == 13){
				data[5] = theData;
			}
			
			counter++;
		}
		
		for(int i = 0; i < 6; i++){
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
				return;
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
}
