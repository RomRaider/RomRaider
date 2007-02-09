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
import enginuity.tts.VoiceThread;
import enginuity.logger.utec.commInterface.CommInterface;

/**
 * @author botman
 */
public class JutecGUI extends JFrame implements ActionListener{
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
	public JMenuItem saveItem = new JMenuItem("Save");
	public JMenuItem exitItem = new JMenuItem("Exit");
	
	private static JutecGUI instance = null;
	
	public static JutecGUI getInstance(){
		return instance;
	}
	
	private JutecGUI(int setDefaultCloseOperation) {
		
		// Main frame
		// Grid layout with a top and bottom, ie two rows
		super("-=<JUTEC Tuning Tool>=-  USE AT OWN RISK,NO WARRANTY");
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
				CommInterface.closeConnection();
				
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
		exitItem.addActionListener(this);
		fileMenu.add(saveItem);
		fileMenu.add(exitItem);
		menuBar.add(fileMenu);
		
		
		//----------------------------------
		//Add a menu item for comm port selection
		//----------------------------------
		JMenu portsMenu =new JMenu("Ports");
		
		//Gather list of ports from interface
		Vector portsVector =CommInterface.getPortsVector();
		
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
				CommInterface.setPortChoice(defaultPort);
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
		else if (cmd.equals("Save")) {
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
		
		//Stop Capture
		else if (cmd.equals("Exit")) {
			//Use interface to finally close the connection to the Utec
			CommInterface.closeConnection();
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
			CommInterface.setPortChoice(currentPort);
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
}
