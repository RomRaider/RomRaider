package enginuity.logger.utec.gui.mapTabs;

import enginuity.logger.utec.commEvent.UtecAFRListener;
import enginuity.logger.utec.mapData.UtecMapData;

public class UtecDataManager {
	private static UtecMapData currentMapData = null;
	private static UtecTableModel fuelListener = null;
	private static UtecTableModel timingListener = null;
	private static UtecTableModel boostListener = null;
	private static UtecAFRListener utecAFRListener = null;

	// Set data values to initial state
	private static double afrData = 999.0;
	private static double psiData = 999.0;
	private static double knockData = 999.0;
	
	public static void setCurrentMap(UtecMapData newUtecMap){
		currentMapData = newUtecMap;
		
		// Call listeners
		fuelListener.replaceData(currentMapData.getFuelMap());
		boostListener.replaceData(currentMapData.getBoostMap());
		timingListener.replaceData(currentMapData.getTimingMap());
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
		return knockData;
	}


	public static void setKnockData(double knockData) {
		UtecDataManager.knockData = knockData;
	}


	public static double getPsiData() {
		return psiData;
	}


	public static void setPsiData(double psiData) {
		UtecDataManager.psiData = psiData;
	}
}
