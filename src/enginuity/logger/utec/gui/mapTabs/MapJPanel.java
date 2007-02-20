package enginuity.logger.utec.gui.mapTabs;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import enginuity.logger.utec.mapData.UtecMapData;

public class MapJPanel extends JPanel{

	public static int FUELMAP = 0;
	public static int TIMINGMAP = 1;
	public static int BOOSTMAP = 2;
	
	private int mapType = 0;
	
	private UtecTableModel tableModel = new UtecTableModel();
	
	
	public MapJPanel(int mapType){
		super(new BorderLayout());
		
		this.mapType = mapType;
		
		init();
		
	}
	
	public void init(){	
        JTable table = new JTable(tableModel);
        //table.setPreferredScrollableViewportSize(new Dimension(500, 70));

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        //Add the scroll pane to this panel.
        this.add(scrollPane, BorderLayout.CENTER);

	}
	
	public void updateData(UtecMapData utecMapData){
		if(this.mapType == MapJPanel.FUELMAP){
			System.out.println("Updating fuel map now.");
			this.tableModel.replaceData(utecMapData.getFuelMap());
		}
		
		if(this.mapType == MapJPanel.TIMINGMAP){
			System.out.println("Updating timing map now.");
			this.tableModel.replaceData(utecMapData.getTimingMap());
		}
		
		if(this.mapType == MapJPanel.BOOSTMAP){
			System.out.println("Updating boost map now.");
			this.tableModel.replaceData(utecMapData.getBoostMap());
		}
		
	}
	
	
}
