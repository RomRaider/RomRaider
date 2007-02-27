package enginuity.logger.utec.gui.mapTabs;

import enginuity.logger.utec.commEvent.UtecAFRListener;
import enginuity.logger.utec.mapData.UtecMapData;

public class DataManager {
	private static UtecMapData currentMapData = null;
	
	
	private static UtecTableModel fuelListener = null;
	private static UtecTableModel timingListener = null;
	private static UtecTableModel boostListener = null;
	
	private static UtecAFRListener utecAFRListener = null;

	public static void setCurrentMap(UtecMapData newUtecMap){
		currentMapData = newUtecMap;
		
		// Call listeners
		fuelListener.replaceData(currentMapData.getFuelMap());
		boostListener.replaceData(currentMapData.getBoostMap());
		timingListener.replaceData(currentMapData.getTimingMap());
	}
	
	
	public static void setBoostListener(UtecTableModel boostListener) {
		DataManager.boostListener = boostListener;
	}


	public static void setFuelListener(UtecTableModel fuelListener) {
		DataManager.fuelListener = fuelListener;
	}


	public static void setTimingListener(UtecTableModel timingListener) {
		DataManager.timingListener = timingListener;
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
		DataManager.utecAFRListener = utecAFRListener;
	}
}
