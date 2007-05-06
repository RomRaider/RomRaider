package enginuity.logger.utec.impl;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import enginuity.NewGUI.data.ApplicationStateManager;
import enginuity.NewGUI.data.TableMetaData;
import enginuity.NewGUI.interfaces.TuningEntity;
import enginuity.NewGUI.interfaces.TuningEntityListener;
import enginuity.NewGUI.tree.ETreeNode;
import enginuity.logger.utec.commInterface.UtecInterface;
import enginuity.logger.utec.gui.JutecToolBar;
import enginuity.logger.utec.gui.mapTabs.UtecDataManager;
import enginuity.logger.utec.mapData.UtecMapData;
import enginuity.logger.utec.properties.UtecProperties;

public class UtecTuningEntityImpl implements TuningEntity{

	// Misc items
	private String currentPort = null;
	private int fileChosen;
	private JFileChooser fileChooser =  new JFileChooser();

	
	private Vector<JMenu> jMenuItems = new Vector<JMenu>();
	
	// Menu Items
	public JMenuItem saveItem = new JMenuItem("Save Log");
	public JMenuItem saveMapItem = new JMenuItem("Save Map To File");
	public JMenuItem exitItem = new JMenuItem("Exit");
	public JMenuItem resetUtec = new JMenuItem("Force Utec Reset");
	public JMenuItem startLogging = new JMenuItem("Start Logging");
	public JMenuItem closePort = new JMenuItem("Close Port");
	public JMenuItem loadMapOne = new JMenuItem("Load Map #1");
	public JMenuItem loadMapTwo = new JMenuItem("Load Map #2");
	public JMenuItem loadMapThree = new JMenuItem("Load Map #3");
	public JMenuItem loadMapFour = new JMenuItem("Load Map #4");
	public JMenuItem loadMapFive = new JMenuItem("Load Map #5");

	public JMenuItem saveMapOne = new JMenuItem("Save To Map #1");
	public JMenuItem saveMapTwo = new JMenuItem("Save To Map #2");
	public JMenuItem saveMapThree = new JMenuItem("Save To Map #3");
	public JMenuItem saveMapFour = new JMenuItem("Save To Map #4");
	public JMenuItem saveMapFive = new JMenuItem("Save To Map #5");
	
	private TuningEntityListener theTEL;
	
	public UtecTuningEntityImpl(){
		initJMenu();
	}
	
	private void initJMenu(){

		// *********************************************
		// Add a menu item for basic application actions
		// *********************************************
		// Define the menu system
		JMenu fileMenu = new JMenu("File");
		saveItem.addActionListener(this);
		saveMapItem.addActionListener(this);
		exitItem.addActionListener(this);
		fileMenu.add(saveItem);
		fileMenu.add(saveMapItem);
		fileMenu.add(exitItem);
		jMenuItems.add(fileMenu);
		
		// ******************************************
		// Add menu item to coordinate Utec operation
		// ******************************************
		JMenu actionMenu = new JMenu("Actions");
		this.resetUtec.addActionListener(this);
		this.startLogging.addActionListener(this);
		this.closePort.addActionListener(this);
		actionMenu.add(this.resetUtec);
		actionMenu.add(this.startLogging);
		actionMenu.add(this.closePort);
		jMenuItems.add(actionMenu);
		
		// ****************************************
		// Add menu item to pull maps from the utec
		// ****************************************
		JMenu getMapsMenu = new JMenu("Load Map");
		loadMapOne.addActionListener(this);
		loadMapTwo.addActionListener(this);
		loadMapThree.addActionListener(this);
		loadMapFour.addActionListener(this);
		loadMapFive.addActionListener(this);
		getMapsMenu.add(loadMapOne);
		getMapsMenu.add(loadMapTwo);
		getMapsMenu.add(loadMapThree);
		getMapsMenu.add(loadMapFour);
		getMapsMenu.add(loadMapFive);
		jMenuItems.add(getMapsMenu);
		

		// ****************************************
		// Add menu item to save maps to the utec
		// ****************************************
		JMenu setMapsMenu = new JMenu("Save Map");
		saveMapOne.addActionListener(this);
		saveMapTwo.addActionListener(this);
		saveMapThree.addActionListener(this);
		saveMapFour.addActionListener(this);
		saveMapFive.addActionListener(this);
		setMapsMenu.add(saveMapOne);
		setMapsMenu.add(saveMapTwo);
		setMapsMenu.add(saveMapThree);
		setMapsMenu.add(saveMapFour);
		setMapsMenu.add(saveMapFive);
		jMenuItems.add(setMapsMenu);

		// ***************************************
		// Add a menu item for comm port selection
		// ***************************************
		JMenu portsMenu = new JMenu("Select Port");

		// Gather list of ports from interface
		Vector portsVector = UtecInterface.getPortsVector();

		Iterator portsIterator = portsVector.iterator();
		int counter = 0;
		while (portsIterator.hasNext()) {
			counter++;
			Object o = portsIterator.next();
			String theName = (String) o;
			JMenuItem item = new JMenuItem(theName);
			item.setName(theName);
			item.addActionListener(this);
			portsMenu.add(item);
		}
		jMenuItems.add(portsMenu);
	}
	
	public void removeTuningGroup(String tuningGroup){
		System.out.println("UTE removing tuning group:"+tuningGroup);
		
		UtecDataManager.removeTuningGroup(tuningGroup);
	}
	
	public Object[][] getTableData(String tableIdentifier) {
		System.out.println("UTEC getTableData Impl tablename:"+tableIdentifier);
		Object[][] data = null;
		
		if(UtecDataManager.getAllMaps().size() == 0){
			System.out.println("UTEC Impl Current map data is null");
			return new Double[0][0];
		}
		
		Iterator mapIterator = UtecDataManager.getAllMaps().iterator();
		while(mapIterator.hasNext()){
			UtecMapData newMapData = (UtecMapData)mapIterator.next();
			String mapName = newMapData.getMapName();
			
			if(tableIdentifier.equals("Fuel:"+mapName)){
				System.out.println("UTE Fuel");
				data = newMapData.getFuelMap();
				return data;
			}
			else if(tableIdentifier.equals("Timing:"+mapName)){
				System.out.println("UTE Timing");
				data = newMapData.getTimingMap();
				return data;
			}
			else if(tableIdentifier.equals("Boost:"+mapName)){
				System.out.println("UTE Boost");
				data = newMapData.getBoostMap();
				return data;
			}else if(tableIdentifier.equals("MapName:"+mapName)){
				System.out.println("UTE MapName");
				data = new Object[1][1];
				data[0][0] = newMapData.getMapName();
				return data;
			}else{
				System.out.println("UTE returning empty data");
				data = new Double[0][0];
			}
			
		}
		return data;
	}

	public void init(TuningEntityListener theTEL) {
		this.theTEL = theTEL;

		// Initialise tree
		// ETreeNode root = new ETreeNode("UTEC: No map selected....", new TableMetaData(TableMetaData.CATEGORY,0.0,0.0,new Object[0], null, null, false,"","","", this));
		
		
		// Inform main GUI of initial tree
		//this.theTEL.addNewTuningGroup(root);
	}

	public String getName() {
		return "UTEC Tuning Entity";
	}

	public Vector<JMenu> getMenuItems() {
		
		return jMenuItems;
	}


	/**
	 * Implements actionPerformed
	 * 
	 * Action listeners for buttons/menus that throw them
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();

			// Open Port
			if (cmd.equals("New")) {
				System.out.println("New action occuring");
			}

			// Close Port
			else if (cmd.equals("Open")) {
				System.out.println("Open action occuring");
			}

			// Start Capture
			else if (cmd.equals("Save Log")) {
				String saveFileName = null;
				System.out.println("Save action occuring");
				fileChosen = fileChooser.showSaveDialog(null);
				if (fileChosen == JFileChooser.APPROVE_OPTION) {
					saveFileName = fileChooser.getSelectedFile().getPath();
					// selectedFile = fileChooser.getSelectedFile();
					try {
						File file = new File(saveFileName);
						FileWriter out = new FileWriter(file);
						//out.write(bottomPanel.totalLog);
						out.close();
						//bottomPanel.totalLog = "";
					} catch (IOException e2) {
						System.out
								.println("Couldn't save file " + saveFileName);
						e2.printStackTrace();
					}
				}

			}

			else if (cmd.equals("Save Map To File")) {
				System.out.println("Saving map to file.");

				if (UtecDataManager.getCurrentMapData() != null) {

					String saveFileName = null;
					System.out.println("Save map now.");
					fileChosen = fileChooser.showSaveDialog(null);
					if (fileChosen == JFileChooser.APPROVE_OPTION) {
						saveFileName = fileChooser.getSelectedFile().getPath();
						UtecDataManager.getCurrentMapData().writeMapToFile(saveFileName);

					}
				} else {
					System.out.println("Map is null.");
				}
			}

			else if (cmd.equals("Load Map #1")) {
				System.out.println("Starting to get map 1");
				UtecInterface.pullMapData(1);
			}

			else if (cmd.equals("Load Map #2")) {
				System.out.println("Starting to get map 2");
				UtecInterface.pullMapData(2);
			}

			else if (cmd.equals("Load Map #3")) {
				System.out.println("Starting to get map 3");
				UtecInterface.pullMapData(3);
			}

			else if (cmd.equals("Load Map #4")) {
				System.out.println("Starting to get map 4");
				UtecInterface.pullMapData(4);
			}

			else if (cmd.equals("Load Map #5")) {
				System.out.println("Starting to get map 5");
				UtecInterface.pullMapData(5);
			}
			
			else if (cmd.equals("Save To Map #1")) {
				System.out.println("Starting to save map #1");
				Iterator mapIterator = UtecDataManager.getAllMaps().iterator();
				while(mapIterator.hasNext()){
					UtecMapData mapData = (UtecMapData)mapIterator.next();
					if(mapData.getMapName().equals(ApplicationStateManager.getSelectedTuningGroup())){
						UtecInterface.sendMapData(1, mapData);
					}
				}
			}

			else if (cmd.equals("Save To Map #2")) {
				System.out.println("Starting to save map #2");
				Iterator mapIterator = UtecDataManager.getAllMaps().iterator();
				while(mapIterator.hasNext()){
					UtecMapData mapData = (UtecMapData)mapIterator.next();
					if(mapData.getMapName().equals(ApplicationStateManager.getSelectedTuningGroup())){
						UtecInterface.sendMapData(2, mapData);
					}
				}
			}

			else if (cmd.equals("Save To Map #3")) {
				System.out.println("Starting to save map #3");
				Iterator mapIterator = UtecDataManager.getAllMaps().iterator();
				while(mapIterator.hasNext()){
					UtecMapData mapData = (UtecMapData)mapIterator.next();
					if(mapData.getMapName().equals(ApplicationStateManager.getSelectedTuningGroup())){
						UtecInterface.sendMapData(3, mapData);
					}
				}
			}

			else if (cmd.equals("Save To Map #4")) {
				System.out.println("Starting to save map #4");
				Iterator mapIterator = UtecDataManager.getAllMaps().iterator();
				while(mapIterator.hasNext()){
					UtecMapData mapData = (UtecMapData)mapIterator.next();
					if(mapData.getMapName().equals(ApplicationStateManager.getSelectedTuningGroup())){
						UtecInterface.sendMapData(4, mapData);
					}
				}
			}

			else if (cmd.equals("Save To Map #5")) {
				System.out.println("Starting to save map #5");
				Iterator mapIterator = UtecDataManager.getAllMaps().iterator();
				while(mapIterator.hasNext()){
					UtecMapData mapData = (UtecMapData)mapIterator.next();
					if(mapData.getMapName().equals(ApplicationStateManager.getSelectedTuningGroup())){
						UtecInterface.sendMapData(5, mapData);
					}
				}
			}
			else if (cmd.equals("Exit")) {
				// Use interface to finally close the connection to the Utec
				UtecInterface.closeConnection();
				System.out.println("Exit action occuring");

				// Close out the application
				System.exit(0);
			}
			
			else if(cmd.equals("Force Utec Reset")){
				System.out.println("Resetting the Utec");
				UtecInterface.resetUtec();
			}
			
			else if(cmd.equals("Start Logging")){
				System.out.println("Kicking off the logging.");
				UtecInterface.startLoggerDataFlow();
			}
			
			else if(cmd.equals("Close Port")){
				System.out.println("Closing access to the currently opened port (if any).");
				UtecInterface.closeConnection();
			}
			

			// Only non explicitly defined actions are those generated by ports.
			// Since an arbitrary machine could have any number of serial ports
			// its impossible to hard code choices based on menu items generated
			// on the fly.
			// Must pull the calling object and interrogate
			else {
				JMenuItem theItem = (JMenuItem) e.getSource();
				String portChoice = theItem.getName();
				System.out.println("Port chosen: " + portChoice);
				currentPort = portChoice;
				UtecInterface.setPortChoice(currentPort);
				UtecInterface.openConnection();
			}
	}

	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return new JutecToolBar(this.theTEL, this);
	}

	public void setTableData(String tableIdentifier, Object[][] data) {
		System.out.println("utec save data requested:"+tableIdentifier);
		
		Iterator mapIterate = UtecDataManager.getAllMaps().iterator();
		while(mapIterate.hasNext()){
			UtecMapData mapData = (UtecMapData)mapIterate.next();
			String[] split = tableIdentifier.split(":");
			String mapType = split[0];
			String tableName = split[1];
			if(mapData.getMapName().equals(tableName)){
				if(mapType.equals("Fuel")){
					System.out.println("UTE: Fuel Set");
					mapData.setFuelMap(convertObjToDouble(data));
				}
				else if(mapType.equals("Boost")){
					System.out.println("UTE: Boost Set");
					mapData.setBoostMap(convertObjToDouble(data));
				}
				else if(mapType.equals("Timing")){
					System.out.println("UTE: Timing Set");
					mapData.setTimingMap(convertObjToDouble(data));
				}
				else if(mapType.equals("MapName")){
					System.out.println("UTE: MapName Set");
					mapData.setMapName((String)data[0][0]);
				}
			}
		}
	}
	
	/**
	 * Helper method for when dealing with fuel timing and boost tables
	 * @param objData
	 * @return
	 */
	private Double[][] convertObjToDouble(Object[][] objData){
		int length = objData.length;
		int width = objData[0].length;
		
		Double[][] newData = new Double[length][width];
		
		for(int i = 0 ; i < length ; i++){
			for(int j = 0 ; j < width ; j++){
				newData[i][j] = (Double)objData[i][j];
			}
		}
		
		return newData;
	}
}
