package enginuity.NewGUI.data;

import java.util.Iterator;
import java.util.Vector;

import enginuity.NewGUI.interfaces.TuningEntity;
import enginuity.NewGUI.interfaces.TuningEntityListener;

public class DataManager {
	private static Vector<TuningEntity> tuningEntities = new Vector<TuningEntity>();
	private static TuningEntity currentTuningEntity;
	
	public static Vector<TuningEntity> getTuningEntities() {
		return tuningEntities;
	}

	public static void addTuningEntity(TuningEntity tuningEntity) {
		tuningEntities.add(tuningEntity);
	}
	
	public static void setCurrentTuningEntity(String entityName, TuningEntityListener listener){
		// Do nothing if the same entity is specified
		if(currentTuningEntity != null && currentTuningEntity.getName().endsWith(entityName)){
			return;
		}
		
		Iterator iterator = tuningEntities.iterator();
		while(iterator.hasNext()){
			TuningEntity tuningEntity  = (TuningEntity)iterator.next();
			if(tuningEntity.getName().equalsIgnoreCase(entityName)){
				currentTuningEntity = tuningEntity;
				currentTuningEntity.init(listener);
				listener.rebuildJMenuBar(currentTuningEntity.getMenuItems());
			}
		}
	}
}
