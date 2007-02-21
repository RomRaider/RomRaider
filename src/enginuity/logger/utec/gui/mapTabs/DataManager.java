package enginuity.logger.utec.gui.mapTabs;

import enginuity.logger.utec.mapData.UtecMapData;

public class DataManager {
	private static UtecMapData currentData = null;
	
	
	private static UtecTableModel fuelListener = null;
	private static UtecTableModel timingListener = null;
	private static UtecTableModel boostListener = null;
	

	public static void setCurrentMap(UtecMapData newUtecMap){
		currentData = newUtecMap;
		fuelListener.replaceData(currentData.getFuelMap());
		boostListener.replaceData(currentData.getBoostMap());
		timingListener.replaceData(currentData.getTimingMap());
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


}
