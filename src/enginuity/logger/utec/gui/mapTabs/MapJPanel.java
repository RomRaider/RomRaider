package enginuity.logger.utec.gui.mapTabs;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import enginuity.logger.utec.mapData.UtecMapData;

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
		
		double[][] initialData = new double[11][40];
		for(int i=0; i < 40; i++){
			for(int j = 0; j < 11 ; j++){
				initialData[j][i] = 0.0;
			}
		}
		this.tableModel = new UtecTableModel(this.mapType, initialData);
	
		if(this.mapType == MapJPanel.FUELMAP){
			init(-8.0, 8.0);
		}
		
		if(this.mapType == MapJPanel.TIMINGMAP){
			init(-1.0, 5.0);
		}
		
		if(this.mapType == MapJPanel.BOOSTMAP){
			init(0.0, 500.0);
		}
		
		
	}
	
	public void init(double min, double max){	
		table = new UtecJTable(tableModel, mapType, min, max);
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
