package enginuity.NewGUI.data;

import java.awt.Dimension;
import java.util.Iterator;
import java.util.Vector;

import enginuity.NewGUI.NewGUI;
import enginuity.NewGUI.etable.EInternalFrame;
import enginuity.NewGUI.interfaces.TuningEntity;
import enginuity.NewGUI.interfaces.TuningEntityListener;

public class ApplicationStateManager {

	public static final int USER_LEVEL_1 = 1;
	public static final int USER_LEVEL_2 = 2;
	public static final int USER_LEVEL_3 = 3;
	public static final int USER_LEVEL_4 = 4;
	public static final int USER_LEVEL_5 = 5;
	
	private static Vector<TuningEntity> tuningEntities = new Vector<TuningEntity>();
	private static TuningEntity currentTuningEntity;
	private static int currentUserLevel = ApplicationStateManager.USER_LEVEL_1;
	
	private static NewGUI enginuityInstance = null;
	
	public static Vector<EInternalFrame> eInternalFrames = new Vector<EInternalFrame>();
	
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
				listener.setNewToolBar(currentTuningEntity.getToolBar());
			}
		}
	}

	public static int getCurrentUserLevel() {
		return currentUserLevel;
	}

	public static void setCurrentUserLevel(int currentUserLevel) {
		ApplicationStateManager.currentUserLevel = currentUserLevel;
	}

	public static TuningEntity getCurrentTuningEntity() {
		return currentTuningEntity;
	}

	public static NewGUI getEnginuityInstance() {
		return enginuityInstance;
	}

	public static void setEnginuityInstance(NewGUI enginuityInstance) {
		ApplicationStateManager.enginuityInstance = enginuityInstance;
	}
	
	public static EInternalFrame addEInternalFrame(Double[][] data, TableMetaData tableMetaData){
		Iterator frameIterator = ApplicationStateManager.getEInternalFrames().iterator();
		EInternalFrame theFrame = null;
		
		boolean addFrameBoolean = true;
		while(frameIterator.hasNext()){
			theFrame = (EInternalFrame)frameIterator.next();
			if(theFrame.getTableMetaData().getTableIdentifier() == tableMetaData.getTableIdentifier()){
				System.out.println("Same frame opening again");
				
				theFrame.setVisible(true);
				addFrameBoolean = false;
			}
		}
		
		if(addFrameBoolean){
			theFrame = new EInternalFrame(tableMetaData, data, new Dimension(470, 450));
				
			ApplicationStateManager.eInternalFrames.add(theFrame);
			
		}else{
			theFrame = null;
		}
		
		return theFrame;
	}
	
	public static Vector<EInternalFrame> getEInternalFrames(){
		return ApplicationStateManager.eInternalFrames;
	}
}
