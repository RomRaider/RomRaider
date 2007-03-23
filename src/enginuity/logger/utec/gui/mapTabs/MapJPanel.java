package enginuity.logger.utec.gui.mapTabs;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import enginuity.logger.utec.mapData.UtecMapData;
import enginuity.logger.utec.properties.UtecProperties;

public class MapJPanel extends JPanel{

	public static int FUELMAP = 0;
	public static int TIMINGMAP = 1;
	public static int BOOSTMAP = 2;
	
	private int mapType = 0;
	
	private UtecTableModel tableModel = null; 
	
	private UtecJTable table = null;
	
	public MapJPanel(int mapType){
		super(new BorderLayout());
		
		this.mapType = mapType;
		
		Double[][] initialData = new Double[11][40];
		for(int i=0; i < 40; i++){
			for(int j = 0; j < 11 ; j++){
				initialData[j][i] = 0.0;
			}
		}
		this.tableModel = new UtecTableModel(this.mapType, initialData);
	
		if(this.mapType == MapJPanel.FUELMAP){
			Object[] ignored = {new Double(-100.0)};
			init(Double.parseDouble(UtecProperties.getProperties("utec.fuelMapMin")[0]), Double.parseDouble(UtecProperties.getProperties("utec.fuelMapMax")[0]), ignored, false);
		}
		
		if(this.mapType == MapJPanel.TIMINGMAP){
			Object[] ignored = {new Double(-100.0)};
			init(Double.parseDouble(UtecProperties.getProperties("utec.timingMapMin")[0]), Double.parseDouble(UtecProperties.getProperties("utec.timingMapMax")[0]), ignored, true);
		}
		
		if(this.mapType == MapJPanel.BOOSTMAP){
			Object[] ignored = {new Double(-100.0)};
			init(Double.parseDouble(UtecProperties.getProperties("utec.boostMapMin")[0]), Double.parseDouble(UtecProperties.getProperties("utec.boostMapMax")[0]), ignored, false);
		}
		
		
	}
	
	public void init(double min, double max, Object[] ignoredValues, boolean isInvertedColoring){

		
		// ************************
		// Utec Specific code below
		// ************************
		if(mapType == MapJPanel.FUELMAP){
			UtecDataManager.setFuelListener(tableModel);
		}
		else if(mapType == MapJPanel.BOOSTMAP){
			UtecDataManager.setBoostListener(tableModel);
		}
		else if(mapType == MapJPanel.TIMINGMAP){
			UtecDataManager.setTimingListener(tableModel);
		}
		
		table = new UtecJTable(tableModel, min, max, ignoredValues, isInvertedColoring);
        //table.setPreferredScrollableViewportSize(new Dimension(500, 70));

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        //Add the scroll pane to this panel.
        this.add(scrollPane, BorderLayout.CENTER);

	}
	
	public void updateDaa(UtecMapData utecMapData){
		if(this.mapType == MapJPanel.FUELMAP){
			System.out.println("Updating fuel map now.");
			//this.table.setModel(new UtecTableModel(this.mapType, utecMapData));
			//this.tableModel.replaceData(utecMapData.getFuelMap());
		}
		
		if(this.mapType == MapJPanel.TIMINGMAP){
			System.out.println("Updating timing map now.");
			//this.tableModel.replaceData(utecMapData.getTimingMap());
		}
		
		if(this.mapType == MapJPanel.BOOSTMAP){
			System.out.println("Updating boost map now.");
			//this.tableModel.replaceData(utecMapData.getBoostMap());
		}
		
	}
	
	
}
