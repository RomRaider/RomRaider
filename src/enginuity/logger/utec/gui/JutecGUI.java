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
import enginuity.logger.utec.gui.mapTabs.UtecDataManager;
import enginuity.logger.utec.gui.mapTabs.MapJPanel;
import enginuity.logger.utec.gui.realtimeData.*;
import enginuity.logger.utec.gui.bottomControl.*;
import enginuity.logger.utec.mapData.GetMapFromUtecListener;
import enginuity.logger.utec.mapData.UtecMapData;
import enginuity.logger.utec.properties.UtecProperties;
import enginuity.tts.SpeakString;
import enginuity.logger.utec.commInterface.UtecInterface;

/**
 * @author botman
 */
public class JutecGUI extends JFrame implements ActionListener, KeyListener {
	
	// Top level desktop pane
	public JLayeredPane desktop = null;

	// Top menu bar
	private JMenuBar menuBar;

	// A known existing port
	private String defaultPort = null;

	// Currently selected port
	private String currentPort = null;

	private BottomUtecControl bottomPanel = null;

	private JFileChooser fileChooser = null;

	private int fileChosen;

	private File selectedFile = null;

	// Tabbed Panes
	public JTabbedPane topTabbedPane = new JTabbedPane();
	public JTabbedPane timingTabbedPane = new JTabbedPane();
	public JTabbedPane fuelTabbedPane = new JTabbedPane();
	public JTabbedPane boostTabbedPane = new JTabbedPane();
	
	// FileMenu Items
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
	public JMenuItem loadMapFile = new JMenuItem("Load Map File");

	public JMenuItem saveMapOne = new JMenuItem("Save To Map #1");
	public JMenuItem saveMapTwo = new JMenuItem("Save To Map #2");
	public JMenuItem saveMapThree = new JMenuItem("Save To Map #3");
	public JMenuItem saveMapFour = new JMenuItem("Save To Map #4");
	public JMenuItem saveMapFive = new JMenuItem("Save To Map #5");
	
	
	private static JutecGUI instance = null;


	// Text input field for sending commands directly to the UTEC
	private JTextField textInput = new JTextField();

	private JLabel utecInputLabel = new JLabel("   Send Single Char Commands to UTEC: ");
	
	private JProgressBar jProgressBar = new JProgressBar();

	public JProgressBar getJProgressBar() {
		return jProgressBar;
	}

	public static JutecGUI getInstance() {
		return instance;
	}

	private JutecGUI(int setDefaultCloseOperation) {
		
		// Main frame
		// Grid layout with a top and bottom, ie two rows
		super("UTEC Logger");
		this.setSize(800, 640);
		this.setResizable(false);
		this.setDefaultCloseOperation(setDefaultCloseOperation);
		
		this.jProgressBar.setSize(790,20);
		
		// *************************
		// Voice the welcome message
		// *************************

		SpeakString vc = new SpeakString("Welcome to you teck logger! Use at your own risk.");
		System.out.println("UTEC Gui is loading now.");
		
		// Actions to take when window is closing
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("JUTEC Exiting");

				// Use interface to close the connecetion to the Utec
				UtecInterface.closeConnection();
			}
		});

		// -----------------------
		// Start Menu bar addtions
		// -----------------------
		menuBar = new JMenuBar();

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
		menuBar.add(fileMenu);
		
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
		menuBar.add(actionMenu);
		
		// ****************************************
		// Add menu item to pull maps from the utec
		// ****************************************
		JMenu getMapsMenu = new JMenu("Load Map");
		loadMapOne.addActionListener(this);
		loadMapTwo.addActionListener(this);
		loadMapThree.addActionListener(this);
		loadMapFour.addActionListener(this);
		loadMapFive.addActionListener(this);
		loadMapFile.addActionListener(this);
		getMapsMenu.add(loadMapOne);
		getMapsMenu.add(loadMapTwo);
		getMapsMenu.add(loadMapThree);
		getMapsMenu.add(loadMapFour);
		getMapsMenu.add(loadMapFive);
		getMapsMenu.add(loadMapFile);
		menuBar.add(getMapsMenu);
		

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
		menuBar.add(setMapsMenu);

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
			/*
			if (counter == 1) {
				defaultPort = theName;
				UtecInterface.setPortChoice(defaultPort);
			}
			*/
		}
		menuBar.add(portsMenu);
		

		// Add menu item to the JFrame
		this.setJMenuBar(menuBar);

		// *********************************************
		// Start Adding GUI Elements to the Window
		// *********************************************
		JPanel totalPanel = new JPanel(new BorderLayout());
		JPanel commandPanel = new JPanel(new BorderLayout());
		this.textInput.addKeyListener(this);
		
		commandPanel.add(this.jProgressBar, BorderLayout.NORTH);
		commandPanel.add(this.utecInputLabel, BorderLayout.WEST);
		commandPanel.add(this.textInput, BorderLayout.CENTER);

		bottomPanel = new BottomUtecControl();

		
		this.topTabbedPane.add("Dashboard", new RealTimeData());
		this.topTabbedPane.add("Timing Data", new MapJPanel(MapJPanel.TIMINGMAP));
		this.topTabbedPane.add("Fuel Data", new MapJPanel(MapJPanel.FUELMAP));
		this.topTabbedPane.add("Boost Data", new MapJPanel(MapJPanel.BOOSTMAP));

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerLocation(440);

		splitPane.setTopComponent(topTabbedPane);
		splitPane.setBottomComponent(bottomPanel);
		splitPane.setPreferredSize(new Dimension(800, 700));

		totalPanel.add(commandPanel, BorderLayout.NORTH);
		totalPanel.add(splitPane, BorderLayout.CENTER);

		this.getContentPane().add(totalPanel);

		// ***********************
		// Define the file chooser
		// ***********************
		fileChooser = new JFileChooser();

		// Save singleton
		instance = this;
	}

	/**
	 * Implements actionPerformed
	 * 
	 * Action listeners for buttons/menus that throw them
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		// Detect whether or not this is a command to be sent to the UTEC
		if (e.getSource() == this.textInput) {
			System.out.println("Text Input received:"+this.textInput.getText());
			String tempString = this.textInput.getText();
			tempString = tempString.substring(0, 1);
			
			System.out.println("Reduce Input:"+tempString+":");
			this.textInput.setText("");
			
			
		} 
		
		// Else this is either a port event or other
		else {
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
				fileChosen = fileChooser.showSaveDialog(this);
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
					fileChosen = fileChooser.showSaveDialog(this);
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

			else if (cmd.equals("Load Map File")) {
				System.out.println("Load Map From File");

				String saveFileName = null;
				fileChosen = fileChooser.showSaveDialog(this);
				if (fileChosen == JFileChooser.APPROVE_OPTION) {
					saveFileName = fileChooser.getSelectedFile().getPath();
					UtecMapData mapData = new UtecMapData(saveFileName);
					UtecDataManager.setCurrentMap(mapData);
				}
			}
			
			else if (cmd.equals("Save To Map #1")) {
				System.out.println("Starting to save map #1");
				//UtecInterface.sendMapData(1);
			}

			else if (cmd.equals("Save To Map #2")) {
				System.out.println("Starting to save map #2");
				//UtecInterface.sendMapData(2);
			}

			else if (cmd.equals("Save To Map #3")) {
				System.out.println("Starting to save map #3");
				//UtecInterface.sendMapData(3);
			}

			else if (cmd.equals("Save To Map #4")) {
				System.out.println("Starting to save map #4");
				//UtecInterface.sendMapData(4);
			}

			else if (cmd.equals("Save To Map #5")) {
				System.out.println("Starting to save map #5");
				//UtecInterface.sendMapData(5);
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
				bottomPanel.setEnabled(true);
				// Notify the infoPane of the current port choice
				// infoPane.setPort(currentPort);
			}
		}
	}

	public static void startLogger(final int defaultCloseOperation,
			final Settings settings) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JutecGUI application = new JutecGUI(defaultCloseOperation);
				application.setVisible(true);
			}
		});
	}

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		JutecGUI application = new JutecGUI(0);
		application.setVisible(true);
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}

	public void keyPressed(KeyEvent arg0) {
		//System.out.println("Key Pressed");
		// System.out.println("Key Pressed:"+arg0.getKeyCode()+"   :"+arg0.getKeyChar()+"   :"+arg0.getModifiers()+"     :"+(int)arg0.getKeyChar());
		
		if((int)arg0.getKeyChar() != 65535){
			int charCodeInt = (int)arg0.getKeyChar();
			System.out.println("Key Code entered:"+charCodeInt);
			
			//Pass along command to the UTEC
			UtecInterface.sendCommandToUtec(charCodeInt);
		}
		
		// Empty out the text field
		this.textInput.setText("");
		
	}

	public void keyReleased(KeyEvent arg0) {
		//System.out.println("Key Released");
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(KeyEvent arg0) {
		//System.out.println("Key Typed:"+arg0.getKeyCode()+"   :"+arg0.getKeyChar()+"   :"+arg0.getModifiers());
		// TODO Auto-generated method stub
		
	}

}
