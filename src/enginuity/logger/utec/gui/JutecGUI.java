/*
 * Created on Jan 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package enginuity.logger.utec.gui;

import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

import java.awt.*;

import enginuity.Settings;
import enginuity.logger.utec.gui.realtimeData.*;
import enginuity.logger.utec.gui.bottomControl.*;
import enginuity.logger.utec.mapData.GetMapFromUtecListener;
import enginuity.logger.utec.mapData.UtecMapData;
import enginuity.tts.VoiceThread;
import enginuity.logger.utec.commInterface.UtecInterface;

/**
 * @author botman
 */
public class JutecGUI extends JFrame implements ActionListener, GetMapFromUtecListener{
	//Top level desktop pane
	public JLayeredPane desktop = null;
	
	//Top menu bar
	private JMenuBar menuBar;
	
	//A known existing port
	private String defaultPort = null;
	
	//Currently selected port
	private String currentPort = null;
	
	private BottomUtecControl bottomPanel = null;
	
	private JFileChooser fileChooser = null;
	private int fileChosen;
	private File selectedFile = null;
	
	
	//FileMenu Items
	public JMenuItem saveItem = new JMenuItem("Save Log");
	public JMenuItem saveMapItem = new JMenuItem("Save Map To File");
	public JMenuItem exitItem = new JMenuItem("Exit");
	
	public JMenuItem loadMapOne = new JMenuItem("Load Map #1");
	public JMenuItem loadMapTwo = new JMenuItem("Load Map #2");
	public JMenuItem loadMapThree = new JMenuItem("Load Map #3");
	public JMenuItem loadMapFour = new JMenuItem("Load Map #4");
	public JMenuItem loadMapFive = new JMenuItem("Load Map #5");
	
	private static JutecGUI instance = null;
	
	private UtecMapData currentMap = null;
	
	public static JutecGUI getInstance(){
		return instance;
	}
	
	private JutecGUI(int setDefaultCloseOperation) {
		
		// Main frame
		// Grid layout with a top and bottom, ie two rows
		super("UTEC Loggers");
		this.setSize(800, 600);
		this.setResizable(false);
		this.setDefaultCloseOperation(setDefaultCloseOperation);

		//*************************
		//Voice the welcome message
		//*************************
		
		VoiceThread vc = new VoiceThread("Welcome to you teck logger! Use at your own risk.");
		vc.start();
		
		
		//Actions to take when window is closing
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("JUTEC Exiting");
				
				//Use interface to close the connecetion to the Utec
				UtecInterface.closeConnection();
				
			}
		});

		//-----------------------
		//Start Menu bar addtions
		//-----------------------
		menuBar = new JMenuBar();
		
		
		//*********************************************
		//Add a menu item for basic application actions
		//*********************************************
		// Define the menu system
		JMenu fileMenu = new JMenu("File");
		saveItem.addActionListener(this);
		saveMapItem.addActionListener(this);
		exitItem.addActionListener(this);
		fileMenu.add(saveItem);
		fileMenu.add(saveMapItem);
		fileMenu.add(exitItem);
		menuBar.add(fileMenu);
		
		
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
		menuBar.add(getMapsMenu);
		
		//----------------------------------
		//Add a menu item for comm port selection
		//----------------------------------
		JMenu portsMenu =new JMenu("Ports");
		
		//Gather list of ports from interface
		Vector portsVector =UtecInterface.getPortsVector();
		
		Iterator portsIterator = portsVector.iterator();
		int counter = 0;
		while(portsIterator.hasNext()){
			counter++;
			Object o = portsIterator.next();
			String theName = (String)o;
			JMenuItem item = new JMenuItem(theName);
			item.setName(theName);
			item.addActionListener(this);
			portsMenu.add(item);
			if(counter == 1){
				defaultPort = theName;
				UtecInterface.setPortChoice(defaultPort);
			}
		}
		menuBar.add(portsMenu);
		
		// Add menu item to the JFrame
		this.setJMenuBar(menuBar);
		
		
		//*********************************************
		//Start Adding GUI Elements to the Window
		//*********************************************
		
		bottomPanel = new BottomUtecControl();
		
		JTabbedPane topTabbedPane = new JTabbedPane();
		topTabbedPane.add("Graph Data",new RealTimeData());
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerLocation(435);
		
		splitPane.setTopComponent(topTabbedPane);
		splitPane.setBottomComponent(bottomPanel);
		splitPane.setPreferredSize(new Dimension(800,600));
		
		this.getContentPane().add(splitPane);
		
		
		//***********************
		//Define the file chooser
		//***********************
		fileChooser = new JFileChooser();
		
		
		//Save singleton
		instance = this;
	}

	/**
	 * Implements actionPerformed
	 * 
	 * Action listeners for buttons/menus that throw them
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		//Open Port
		if (cmd.equals("New")) {
			System.out.println("New action occuring");
		}
		
		//Close Port
		else if (cmd.equals("Open")) {
			System.out.println("Open action occuring");
		}
		
		//Start Capture
		else if (cmd.equals("Save Log")) {
			String saveFileName = null;
			System.out.println("Save action occuring");
			fileChosen = fileChooser.showSaveDialog(this);
			 if(fileChosen == JFileChooser.APPROVE_OPTION)
			 {
			 	saveFileName = fileChooser.getSelectedFile().getPath();
			 	//selectedFile = fileChooser.getSelectedFile();
			 	try {
				    File file = new File(saveFileName);
				    FileWriter out = new FileWriter(file);
				    out.write(bottomPanel.totalLog);
				    out.close();
				    bottomPanel.totalLog = "";
				}
				catch (IOException e2) {
				    System.out.println("Couldn't save file " + saveFileName);
				    e2.printStackTrace();
				}
		 	 }	
			
		}
		
		else if (cmd.equals("Save Map To File")) {
			System.out.println("Saving map to file.");
			if(this.currentMap != null){

				String saveFileName = null;
				System.out.println("Save map now.");
				fileChosen = fileChooser.showSaveDialog(this);
				 if(fileChosen == JFileChooser.APPROVE_OPTION)
				 {
				 	saveFileName = fileChooser.getSelectedFile().getPath();
				 	this.currentMap.writeMapToFile(saveFileName);
				 	
			 	 }
			}else{
				System.out.println("Map is null.");
			}
		}
		
		else if (cmd.equals("Load Map #1")) {
			System.out.println("Starting to get map 1");
			UtecInterface.openConnection();
			UtecInterface.getMap(1, this);
		}
		
		else if (cmd.equals("Load Map #2")) {
			System.out.println("Starting to get map 2");
			UtecInterface.openConnection();
			UtecInterface.getMap(2, this);
		}
		
		else if (cmd.equals("Load Map #3")) {
			System.out.println("Starting to get map 3");
			UtecInterface.openConnection();
			UtecInterface.getMap(3, this);
		}
		
		else if (cmd.equals("Load Map #4")) {
			System.out.println("Starting to get map 4");
			UtecInterface.openConnection();
			UtecInterface.getMap(4, this);
		}
		
		else if (cmd.equals("Load Map #5")) {
			System.out.println("Starting to get map 5");
			UtecInterface.openConnection();
			UtecInterface.getMap(5, this);
		}
		
		//Stop Capture
		else if (cmd.equals("Exit")) {
			//Use interface to finally close the connection to the Utec
			UtecInterface.closeConnection();
			System.out.println("Exit action occuring");
			
			//Close out the application
			System.exit(0);
		}
		
		//Only non explicitly defined actions are those generated by ports.
		//Since an arbitrary machine could have any number of serial ports
		//its impossible to hard code choices based on menu items generated on the fly.
		//Must pull the calling object and interrogate
		else{
			JMenuItem theItem = (JMenuItem)e.getSource();
			String portChoice = theItem.getName();
			System.out.println("Port chosen: "+portChoice);
			currentPort = portChoice;
			UtecInterface.setPortChoice(currentPort);
			bottomPanel.setEnabled(true);
			//Notify the infoPane of the current port choice
			//infoPane.setPort(currentPort);
		}
	}
	
	
	public static void startLogger(final int defaultCloseOperation, final Settings settings) {
	        SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	JutecGUI application = new JutecGUI(defaultCloseOperation);
	        		application.setVisible(true);
	            }
	        });
	}

	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args) {
		JutecGUI application = new JutecGUI(0);
		application.setVisible(true);

	}

	public void mapRetrieved(UtecMapData theMap) {
		System.out.println("Got a map from the utec:"+theMap.getMapName());
		this.currentMap = theMap;	
	}
}
