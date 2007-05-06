package enginuity.logger.utec.gui.mapTabs;

import java.util.Iterator;
import java.util.Vector;

import enginuity.NewGUI.data.ApplicationStateManager;
import enginuity.NewGUI.data.TableMetaData;
import enginuity.NewGUI.interfaces.TuningEntity;
import enginuity.NewGUI.tree.ETreeNode;
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
		System.out.println("UtecDataManager:"+ApplicationStateManager.getCurrentTuningEntity().getName());
		ETreeNode rootNode = buildMapDataTreeNode(newUtecMap, ApplicationStateManager.getCurrentTuningEntity());
		ApplicationStateManager.getEnginuityInstance().addNewTuningGroup(rootNode);
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
	
	private static ETreeNode buildMapDataTreeNode(UtecMapData mapData, TuningEntity parentTuningEntity) {
		// Define columnLabels
		String[] columnLabels = new String[11];
		for(int i = 0; i < columnLabels.length ; i++){
			columnLabels[i] = (i * 10)+"";
		}

		String[] rowLabels = new String[40];
		for(int i = 0; i < rowLabels.length ; i++){
			rowLabels[i] = i+"";
		}
		
		
		// Initialise tree
		ETreeNode root = new ETreeNode("UTEC:"+mapData.getMapName()+", "+mapData.getMapComment(), new TableMetaData(TableMetaData.MAP_SET_ROOT,0.0,0.0,new Object[0],null,null,false,"","", "", "", mapData.getMapName(), parentTuningEntity));
		
		ETreeNode mapName = new ETreeNode("Map Name", new TableMetaData(TableMetaData.DATA_1D, Double.parseDouble(UtecProperties.getProperties("utec.fuelMapMin")[0]), Double.parseDouble(UtecProperties.getProperties("utec.fuelMapMax")[0]), null,columnLabels,rowLabels, false, "Map Name" , "", "", "MapName:"+mapData.getMapName(), mapData.getMapName(),parentTuningEntity));
		
		
		
		Object[] ignored = {new Double(-100.0)};
		ETreeNode fuel = new ETreeNode("Fuel", new TableMetaData(TableMetaData.DATA_3D, Double.parseDouble(UtecProperties.getProperties("utec.fuelMapMin")[0]), Double.parseDouble(UtecProperties.getProperties("utec.fuelMapMax")[0]), ignored,columnLabels,rowLabels, false, "Fuel" , "Load", "RPM", "Fuel:"+mapData.getMapName(), mapData.getMapName(),parentTuningEntity));
		
		Object[] ignored2 = {new Double(-100.0)};
		ETreeNode timing = new ETreeNode("Timing", new TableMetaData(TableMetaData.DATA_3D, Double.parseDouble(UtecProperties.getProperties("utec.timingMapMin")[0]), Double.parseDouble(UtecProperties.getProperties("utec.timingMapMax")[0]), ignored,columnLabels,rowLabels, false, "Timing" , "Load", "RPM",  "Timing:"+mapData.getMapName(), mapData.getMapName(),parentTuningEntity));
		
		Object[] ignored3 = {new Double(-100.0)};
		ETreeNode boost = new ETreeNode("Boost", new TableMetaData(TableMetaData.DATA_3D, Double.parseDouble(UtecProperties.getProperties("utec.boostMapMin")[0]), Double.parseDouble(UtecProperties.getProperties("utec.boostMapMax")[0]), ignored, columnLabels,rowLabels,false, "Boost", "Load", "RPM", "Boost:"+mapData.getMapName(), mapData.getMapName(), parentTuningEntity));
		
		root.add(mapName);
		root.add(fuel);
		root.add(timing);
		root.add(boost);
		
		return root;
	}
	
	/**
	 * Get serial data from a serial event
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
				
				
				// setCurrentMap(newMap);
				rawMapData = "";
				addMap(newMap);
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
	
	public static void removeTuningGroup(String tuningGroup){
		Iterator mapIterator = allMaps.iterator();
		UtecMapData newMapData = null;
		while(mapIterator.hasNext()){
			newMapData = (UtecMapData)mapIterator.next();
			if(newMapData.getMapName().equals(tuningGroup)){
				break;
			}
		}
		
		if(newMapData != null){
			allMaps.remove(newMapData);
		}
	}
}
