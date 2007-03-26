package enginuity.logger.utec.gui.mapTabs;

import java.util.Iterator;
import java.util.Vector;

import enginuity.logger.utec.commEvent.LoggerDataListener;
import enginuity.logger.utec.commEvent.UtecAFRListener;
import enginuity.logger.utec.mapData.UtecMapData;
import enginuity.logger.utec.properties.UtecProperties;

public class UtecDataManager {
	private static UtecMapData currentMapData = null;
	private static UtecTableModel fuelListener = null;
	private static UtecTableModel timingListener = null;
	private static UtecTableModel boostListener = null;
	private static UtecAFRListener utecAFRListener = null;
	private static Vector<LoggerDataListener> loggerListener = new Vector<LoggerDataListener>();
	private static Vector<LoggerDataListener> generalListener = new Vector<LoggerDataListener>();
	private static boolean isExpectingMap = false;
	
	private static String rawMapData = "";
	
	// Loggers
	private static int afrIndex = Integer.parseInt(UtecProperties.getProperties("utec.afrIndex")[0]);
	private static int psiIndex = Integer.parseInt(UtecProperties.getProperties("utec.psiIndex")[0]);
	private static int knockIndex = Integer.parseInt(UtecProperties.getProperties("utec.knockIndex")[0]);
	
	// Set data values to initial state
	private static double afrData = 999.0;
	private static double psiData = 999.0;
	private static double knockData = 0.0;
	
	private static int lineCounter = 0;
	
	private static Vector<UtecMapData> allMaps = new Vector<UtecMapData>();
	
	public static void addMap(UtecMapData newUtecMap){
		allMaps.add(newUtecMap);
		setCurrentMap(newUtecMap);
	}
	
	public static void setCurrentMap(UtecMapData newUtecMap){
		currentMapData = newUtecMap;
		
		// Call listeners
		System.out.println("Calling map listeners.");
		if(fuelListener != null){
			fuelListener.replaceData(currentMapData.getFuelMap());
		}
		
		if(boostListener != null){
			boostListener.replaceData(currentMapData.getBoostMap());
		}
		
		if(timingListener != null){
			timingListener.replaceData(currentMapData.getTimingMap());
		}
		
		
		
		System.out.println("Done calling map listeners.");
	}
	
	/**
	 * Set serial data from a serial event
	 * 
	 * @param serialData
	 */
	public static void setSerialData(String serialData){
		
		if(isExpectingMap){
			lineCounter++;
			System.out.println("Line:"+lineCounter);
			//System.out.println("Map:"+serialData+":");
			rawMapData += serialData+"\n";
			
			// Detect End of Map
			if(lineCounter == 128){
				rawMapData += "[END][MAPGROUP][0B05E][EOF]";
				lineCounter = 0;
				System.out.println("Map EOF");
				UtecMapData newMap = new UtecMapData();
				newMap.replaceRawData(new StringBuffer(rawMapData));
				newMap.populateMapDataStructures();
				setCurrentMap(newMap);
				rawMapData = "";
				setExpectingMap(false);
			}
		}else{
			pullLoggerData(serialData);
		}
		
		
	}

	private static void pullLoggerData(String serialData) {
		String[] data = serialData.split(",");
		
		// Count the "," to ensure this is a line of logging data
		//System.out.println("DM LoggerEvent: Checking data length");
		if(data.length < 4){
			
			// **********************************
			// Call out to general data listeners
			// **********************************
			
			
			return;
		}
		
		double[] doubleData = new double[data.length];
		
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
				for(int k=0;k<theData.length();k++){
					//System.out.println("--  DM LoggerEvent int values *****:"+(int)theData.charAt(k)+":");
				}
				
				// **********************************
				// Call out to general data listeners
				// **********************************
				
	        }
		}
		
		// ********************************************************
		// If we make it this far we know we have valid logger data
		// ********************************************************
		UtecDataManager.notifyLoggerDataListeners(doubleData);
	}
	
	/**
	 * Helper method to dole out data to logger data listeners
	 * @param doubleData
	 */
	private static void notifyLoggerDataListeners(double[] doubleData){
		
		setAfrData(doubleData[afrIndex]);
		setPsiData(doubleData[psiIndex]);
		setKnockData(doubleData[knockIndex]);
		
		Iterator iterator = UtecDataManager.getLoggerListeners().iterator();
		while(iterator.hasNext()){
			LoggerDataListener loggerListener = (LoggerDataListener)iterator.next();
			loggerListener.getCommEvent(doubleData);
		}
	}
	
	
	public static void setBoostListener(UtecTableModel boostListener) {
		UtecDataManager.boostListener = boostListener;
	}


	public static void setFuelListener(UtecTableModel fuelListener) {
		UtecDataManager.fuelListener = fuelListener;
	}


	public static void setTimingListener(UtecTableModel timingListener) {
		UtecDataManager.timingListener = timingListener;
	}

	public static void setFuelMapValue(int row, int col, double value){
		if(currentMapData != null){
			currentMapData.setFuelMapValue(row, col, value);
		}
	}
	
	public static void setBoostMapValue(int row, int col, double value){
		if(currentMapData != null){
			currentMapData.setBoostMapValue(row, col, value);
		}
	}
	
	public static void setTimingMapValue(int row, int col, double value){
		if(currentMapData != null){
			currentMapData.setTimingMapValue(row, col, value);
		}
	}


	public static UtecMapData getCurrentMapData() {
		return currentMapData;
	}


	public static UtecAFRListener getUtecAFRListener() {
		return utecAFRListener;
	}


	public static void setUtecAFRListener(UtecAFRListener utecAFRListener) {
		UtecDataManager.utecAFRListener = utecAFRListener;
	}


	public static double getAfrData() {
		return afrData;
	}


	public static void setAfrData(double afrData) {
		UtecDataManager.afrData = afrData;
	}


	public static double getKnockData() {
		// Zero out knock data when pulled
		double temp = UtecDataManager.knockData;
		UtecDataManager.knockData = 0;
		return temp;
	}


	public static void setKnockData(double knockData) {
		// Save highest counted knock events
		if(knockData > UtecDataManager.knockData){
			UtecDataManager.knockData = knockData;
		}
		
	}


	public static double getPsiData() {
		return psiData;
	}


	public static void setPsiData(double psiData) {
		UtecDataManager.psiData = psiData;
	}

	public static Vector<LoggerDataListener> getLoggerListeners() {
		return loggerListener;
	}

	public static void addLoggerListener(LoggerDataListener loggerListener) {
		UtecDataManager.loggerListener.add(loggerListener);
	}
	
	public static void addMapDataListener(LoggerDataListener loggerListener) {
		UtecDataManager.loggerListener.add(loggerListener);
	}

	public static boolean isExpectingMap() {
		return isExpectingMap;
	}

	public static void setExpectingMap(boolean isExpectingMap) {
		UtecDataManager.isExpectingMap = isExpectingMap;
	}

	public static Vector<UtecMapData> getAllMaps() {
		return allMaps;
	}
}
