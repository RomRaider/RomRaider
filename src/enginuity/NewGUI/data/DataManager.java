package enginuity.NewGUI.data;

import java.util.Iterator;
import java.util.Vector;

import enginuity.NewGUI.interfaces.TuningEntity;

public class DataManager {
	private static Vector<TuningEntity> tuningEntities = new Vector<TuningEntity>();
	private static TuningEntity currentTuningEntity;
	
	public static Vector<TuningEntity> getTuningEntities() {
		return tuningEntities;
	}

	public static void addTuningEntity(TuningEntity tuningEntity) {
		tuningEntities.add(tuningEntity);
	}
	
	public static void setCurrentTuningEntity(String entityName){
		Iterator iterator = tuningEntities.iterator();
		while(iterator.hasNext()){
			TuningEntity tuningEntity  = (TuningEntity)iterator.next();
			if(tuningEntity.getName().equalsIgnoreCase(entityName)){
				currentTuningEntity = tuningEntity;
			}
		}
	}
	
	public static TuningEntity getCurrentTuningEntity(){
		return currentTuningEntity;
	}
	
}
