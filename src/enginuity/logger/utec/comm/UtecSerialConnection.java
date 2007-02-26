package enginuity.logger.utec.comm;

//import javax.comm.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import enginuity.logger.utec.commEvent.*;
import enginuity.logger.utec.mapData.GetMapFromUtecListener;
import enginuity.logger.utec.mapData.UtecMapData;
import enginuity.logger.utec.properties.UtecProperties;

/**
 * Class negotiates data to and from UTEC via the serial port
 * 
 * Please note that at this time ownership issues are not handled. See commented
 * out code pertaining to the parent entity
 * 
 * @author botman
 * 
 */
// public class SerialConnection implements SerialPortEventListener,
// CommPortOwnershipListener {
import gnu.io.*;

public class UtecSerialConnection implements SerialPortEventListener {

	// Parent object organizing connections to and from UTEC
	// private JPanel parent;

	// Data to UTEC
	// private JTextArea messageAreaOut;

	// Data from UTEC
	// private JTextArea messageAreaIn;

	private GetMapFromUtecListener getMapFromUtecListener = null;

	// Parameters used to define serial connection
	public SerialParameters parameters = new SerialParameters();

	// Data from UTEC
	private OutputStream outputToUtecStream;

	// Data to UTEC
	private InputStream inputFromUtecStream;

	// Handler for keyboard input
	private KeyHandler keyHandler;

	// Defines named comport
	private CommPortIdentifier portId;

	// Serial port being accessed
	private SerialPort sPort;

	// Defin state of defined comport, open or closed.
	private boolean open;

	// Listeners
	private Vector portListeners = new Vector();

	// Define whether or not we are recieving a map from the UTEC
	private boolean isMapFromUtecPrep = false;

	private boolean isMapFromUtec = false;

	private UtecMapData currentMap = null;
	private String totalDat = "";
	/**
	 * Public constructor
	 * 
	 * @param parent
	 * @param parameters
	 * @param messageAreaOut
	 * @param messageAreaIn
	 */
	// public SerialConnection(JPanel parent, SerialParameters parameters,
	// TextArea messageAreaOut, TextArea messageAreaIn) {
	public UtecSerialConnection(SerialParameters parameters) {
		// this.parent = parent;
		// this.messageAreaOut = messageAreaOut;
		// this.messageAreaIn = messageAreaIn;
		open = false;
	}

	/**
	 * Get UTEC to send logger data data
	 * 
	 */
	public void startLoggerDataFlow() {
		System.out.println("Starting data flow from UTEC");
		
		String[] commandList = UtecProperties.getProperties("utec.startLogging");
		if(commandList == null){
			System.err.println("Command string in properties file for utec.startLogging not found.");
			return;
		}
		
		this.resetUtec();
		for(int i = 0; i < commandList.length ; i++){
			// Send parsed command to the utec
			this.sendCommandToUtec(Integer.parseInt(commandList[i]));
		}
	}

	/**
	 * Reset UTEC to main screen
	 * 
	 */
	public void resetUtec() {
		System.out.println("Utec reset called.");

		String[] commandList = UtecProperties.getProperties("utec.resetUtec");
		if(commandList == null){
			System.err.println("Command string in properties file for utec.resetUtec not found.");
			return;
		}
		
		for(int i = 0; i < commandList.length ; i++){
			// Send parsed command to the utec
			this.sendCommandToUtec(Integer.parseInt(commandList[i]));
		}
	}

	/**
	 * Get map data from map number passed in
	 * 
	 * @param mapNumber
	 */
	public void pullMapData(int mapNumber, GetMapFromUtecListener listener) {

		// Sanity check
		if (mapNumber < 1 || mapNumber > 5) {
			System.err.println("Map selection out of range.");
			return;
		}
		
		String[] commandList = UtecProperties.getProperties("utec.startMapDownload");
		if(commandList == null){
			System.err.println("Command string in properties file for utec.startMapDownload not found.");
			return;
		}
		
		this.resetUtec();
		System.out.println("UtecControl, getting map:" + mapNumber);

		// Null out any previously loaded map
		this.currentMap = null;

		// Who will get this map in the end?
		this.getMapFromUtecListener = listener;

		// Setup map transfer prep state
		this.isMapFromUtecPrep = true;
		this.isMapFromUtec = false;
		
		// Iterate through command string
		int starCounter = 0;
		for(int i = 0; i < commandList.length ; i++){
			if(commandList[i].equalsIgnoreCase("*")){
				if(starCounter == 0){
					// Select map
					
					if (mapNumber == 1) {
						this.sendCommandToUtec(33);
						System.out.println("Requested Map 1");
					}
					if (mapNumber == 2) {
						this.sendCommandToUtec(64);
						System.out.println("Requested Map 2");
					}
					if (mapNumber == 3) {
						this.sendCommandToUtec(35);
						System.out.println("Requested Map 3");
					}
					if (mapNumber == 4) {
						this.sendCommandToUtec(36);
						System.out.println("Requested Map 4");
					}
					if (mapNumber == 5) {
						this.sendCommandToUtec(37);
						System.out.println("Requested Map 5");
					}
				}else if(starCounter == 1){

					// Make this class receptive to map transfer
					this.isMapFromUtec = true;

					// No longer map prep
					this.isMapFromUtecPrep = false;
					
				}else{
					System.err.println("No operation supported for properties value '*'");
				}
				
				starCounter++;
			}else{
				// Send parsed command to the utec
				this.sendCommandToUtec(Integer.parseInt(commandList[i]));
			}
		}
		
	}

	/**
	 * Transmit a map to the utec
	 * 
	 * @param mapNumber
	 * @param listener
	 */
	public void sendMapData(int mapNumber, StringBuffer mapData) {

		// Sanity check
		if (mapNumber < 1 || mapNumber > 5) {
			System.err.println("Map selection out of range.");
			return;
		}
		
		String[] commandList = UtecProperties.getProperties("utec.startMapDownload");
		if(commandList == null){
			System.err.println("Command string in properties file for utec.startMapUpload not found.");
			return;
		}
		
		this.resetUtec();
		System.out.println("UtecControl, sending map:" + mapNumber);
		
		// Iterate through command string
		int starCounter = 0;
		for(int i = 0; i < commandList.length ; i++){
			if(commandList[i].equalsIgnoreCase("*")){
				if(starCounter == 0){
					// Select map
					
					if (mapNumber == 1) {
						this.sendCommandToUtec(33);
						System.out.println("Requested Map 1");
					}
					if (mapNumber == 2) {
						this.sendCommandToUtec(64);
						System.out.println("Requested Map 2");
					}
					if (mapNumber == 3) {
						this.sendCommandToUtec(35);
						System.out.println("Requested Map 3");
					}
					if (mapNumber == 4) {
						this.sendCommandToUtec(36);
						System.out.println("Requested Map 4");
					}
					if (mapNumber == 5) {
						this.sendCommandToUtec(37);
						System.out.println("Requested Map 5");
					}
				}else if(starCounter == 1){
					this.sendDataToUtec(mapData);
					
				}else{
					System.err.println("No operation supported for properties value '*'");
				}
				
				starCounter++;
			}else{
				// Send parsed command to the utec
				this.sendCommandToUtec(Integer.parseInt(commandList[i]));
			}
		}
	}
	
	
	/**
	 * Helper method to write chcommands to the UTEC. There is a pause after each command sent to ensure utec has time to respond.
	 * 
	 * @param charValue
	 */
	public void sendCommandToUtec(int charValue) {
		
		 if(this.sPort == null){ System.err.println("No Port Selected, therefore no interraction with Utec happening.");
		 	return; 
		 }
		 

		try {
			outputToUtecStream.write(charValue);

			System.out.println("Sending command to the ute:"+charValue);
		} catch (IOException e) {
			System.err.println("Can't send char command to UTEC: " + charValue);
			e.getMessage();
		}
		
		this.waitForIt();
	}
	
	/**
	 * Method sends string data to the utec
	 * @param mapData
	 */
	public void sendDataToUtec(StringBuffer mapData){
		 if(this.sPort == null){ System.err.println("No Port Selected, therefore no interraction with Utec happening.");
		 	return; 
		 }
		 
		for(int i = 0; i < mapData.length(); i++){
			try {
				outputToUtecStream.write(mapData.charAt(i));
			} catch (IOException e) {
				System.err.println("Can't send char data to UTEC: " + mapData.charAt(i));
				e.getMessage();
			}
			
			//Wait
			this.waitForIt();
		}
	}
	
	/**
	 * Helper method to pause application. Used to spread data transmission
	 *
	 */
	private void waitForIt(){
		String[] pauseString = UtecProperties.getProperties("utec.dataTransmissionPauseMS");
		
		int pauseCount = Integer.parseInt(pauseString[0]);
		try {
			Thread.currentThread().sleep(pauseCount);
			System.out.println("waiting for this many ms:"+pauseCount);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Opens a connection to the defined serial port If attempt fails,
	 * SerialConnectionException is thrown
	 * 
	 * @throws SerialConnectionException
	 */
	public void openConnection() throws SerialConnectionException {
		
		// if(sPort == null){ System.err.println("No port selected or available to open."); return; }
		 
		if(parameters == null){
			System.err.println("No port selected or available to open."); 
			return; 
		}
		// Obtain a CommPortIdentifier object for the port you want to open.
		try {
			// System.out.println("PORT: "+parameters.getPortName());
			portId = CommPortIdentifier.getPortIdentifier(parameters
					.getPortName());
		} catch (NoSuchPortException e) {
			System.err.println("Can't get commport identifier");
			return;
			//throw new SerialConnectionException(e.getMessage());
		}

		// Open the port represented by the CommPortIdentifier object. Give
		// the open call a relatively long timeout of 30 seconds to allow
		// a different application to reliquish the port if the user
		// wants to.
		try {
			sPort = (SerialPort) portId.open("SerialDemo", 30000);
		} catch (PortInUseException e) {
			System.err.println("Can't open serial port");
			// throw new SerialConnectionException(e.getMessage());
		}

		// Set the parameters of the connection. If they won't set, close the
		// port before throwing an exception.
		try {
			setConnectionParameters();
		} catch (SerialConnectionException e) {
			System.err.println("Can't set connection parameters");
			sPort.close();
			throw e;
		}

		// Open the input and output streams for the connection. If they won't
		// open, close the port before throwing an exception.
		try {
			outputToUtecStream = sPort.getOutputStream();
			inputFromUtecStream = sPort.getInputStream();
		} catch (IOException e) {
			System.err.println("Error opening IO streams");
			sPort.close();
			throw new SerialConnectionException("Error opening i/o streams");
		}

		// Create a new KeyHandler to respond to key strokes in the
		// messageAreaOut. Add the KeyHandler as a keyListener to the
		// messageAreaOut.
		// keyHandler = new KeyHandler(outputToUtecStream);
		// messageAreaOut.addKeyListener(keyHandler);

		// Add this object as an event listener for the serial port.
		try {
			sPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			System.err.println("Too Many listeners");
			sPort.close();
			throw new SerialConnectionException("too many listeners added");
		}

		// Set notifyOnDataAvailable to true to allow event driven input.
		sPort.notifyOnDataAvailable(true);

		// Set notifyOnBreakInterrup to allow event driven break handling.
		sPort.notifyOnBreakInterrupt(true);

		// Set receive timeout to allow breaking out of polling loop during
		// input handling.
		try {
			sPort.enableReceiveTimeout(30);
		} catch (UnsupportedCommOperationException e) {
			System.err.println("Time Out");
		}

		// Add ownership listener to allow ownership event handling.
		// portId.addPortOwnershipListener(this);

		open = true;
		System.out.println("Port opened with success.");
	}

	/**
	 * Sets the connection parameters to the setting in the parameters object.
	 * If set fails return the parameters object to origional settings and throw
	 * exception.
	 */
	public void setConnectionParameters() throws SerialConnectionException {

		if (sPort == null) {
			System.err.println("No port selected.");
			return;
		}
		// Save state of parameters before trying a set.
		int oldBaudRate = sPort.getBaudRate();
		int oldDatabits = sPort.getDataBits();
		int oldStopbits = sPort.getStopBits();
		int oldParity = sPort.getParity();
		int oldFlowControl = sPort.getFlowControlMode();

		// Set connection parameters, if set fails return parameters object
		// to original state.
		try {
			sPort.setSerialPortParams(parameters.getBaudRate(), parameters
					.getDatabits(), parameters.getStopbits(), parameters
					.getParity());
		} catch (UnsupportedCommOperationException e) {
			parameters.setBaudRate(oldBaudRate);
			parameters.setDatabits(oldDatabits);
			parameters.setStopbits(oldStopbits);
			parameters.setParity(oldParity);
			throw new SerialConnectionException("Unsupported parameter");
		}

		// Set flow control.
		try {
			sPort.setFlowControlMode(parameters.getFlowControlIn()
					| parameters.getFlowControlOut());
		} catch (UnsupportedCommOperationException e) {
			throw new SerialConnectionException("Unsupported flow control");
		}
	}

	/**
	 * Close the port and clean up associated elements.
	 */
	public void closeConnection() {
		// If port is alread closed just return.
		if (!open) {
			System.err.println("Attempting to close an already closed port.");
			return;
		}
		System.out.println("Closing connection to the currently targeted port");

		// Reset the UTEC first
		resetUtec();

		// Remove the key listener.
		// messageAreaOut.removeKeyListener(keyHandler);

		// Check to make sure sPort has reference to avoid a NPE.
		if (sPort != null) {
			try {
				// close the i/o streams.
				outputToUtecStream.close();
				inputFromUtecStream.close();
			} catch (IOException e) {
				System.err.println(e);
			}

			// Close the port.
			sPort.close();

			// Remove the ownership listener.
			// portId.removePortOwnershipListener(this);
		}

		open = false;
	}

	/**
	 * Send a one second break signal.
	 */
	public void sendBreak() {
		sPort.sendBreak(1000);
	}

	/**
	 * Reports the open status of the port.
	 * 
	 * @return true if port is open, false if port is closed.
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * Method adds a class as a listener
	 * 
	 * @param o
	 */
	public void addListener(Object o) {
		portListeners.add(o);
	}

	/**
	 * Handles SerialPortEvents. The two types of SerialPortEvents that this
	 * program is registered to listen for are DATA_AVAILABLE and BI. During
	 * DATA_AVAILABLE the port buffer is read until it is drained, when no more
	 * data is availble and 30ms has passed the method returns. When a BI event
	 * occurs the words BREAK RECEIVED are written to the messageAreaIn.
	 */

	public void serialEvent(SerialPortEvent e) {
		// Create a StringBuffer and int to receive input data.
		StringBuffer inputBuffer = new StringBuffer();
		int newData = 0;

		// Determine type of event.
		switch (e.getEventType()) {

		// Read data until -1 is returned. If \r is received substitute
		// \n for correct newline handling.
		case SerialPortEvent.DATA_AVAILABLE:

			// Append new output to buffer
			while (newData != -1) {
				try {
					newData = inputFromUtecStream.read();

					if (newData == -1) {
						break;
					}
					
					/*
					if ('\r' == (char) newData) {
						inputBuffer.append('\n');
					} else {
						inputBuffer.append((char) newData);
					}
					*/
					
					inputBuffer.append((char) newData);
					System.out.print((char)newData);
					this.totalDat += (char)newData;
					
				} catch (IOException ex) {
					System.err.println(ex);
					return;
				}
			}
//			 Ouput to console
			//System.out.println(inputBuffer);

			if (this.isMapFromUtecPrep == true) {
				System.out.println("Map Prep State.");
			}

			else if (this.isMapFromUtec == true) {
				//System.out.println("Map From Utec Data.");

				// If this is the start of map data flow, then create a new map
				// data object
				if (this.currentMap == null) {
					currentMap = new UtecMapData();
				}

				// Append byte data from the UTEC
				this.currentMap.addRawData(newData);
				//System.out.println("Added:" + (char) newData);

				// Detect the end of the map recieving
				if (inputBuffer.indexOf("[EOF]") != -1) {
					System.out.println("End of file detected.");
					
					this.currentMap.replaceRawData(new StringBuffer(this.totalDat));
					this.totalDat = "";
					
					System.out.println("hi 1");
					this.isMapFromUtec = false;
					System.out.println("hi 2");
					this.currentMap.populateMapDataStructures();
					System.out.println("hi 3");

					// Notify listner if available
					System.out.println("Calling listeners.");
					if (this.getMapFromUtecListener != null) {
						System.out.println("Listener called.");
						this.getMapFromUtecListener.mapRetrieved(this.currentMap);
					}else{
						System.out.println("Calling listeners, but none found.");
					}
					
					// Empty out map storage
					this.currentMap = null;
				}
			}

			// Logger data
			else {
				
				CommEvent commEvent = new CommEvent();
				commEvent.setLoggerData(new String(inputBuffer));
				commEvent.setLoggerData(true);

				Iterator portIterator = portListeners.iterator();
				while (portIterator.hasNext()) {
					CommListener theListener = (CommListener) portIterator.next();
					if(commEvent.isValidData() == true){
						theListener.getCommEvent(commEvent);
					}
				}
				
				break;
			}

			// If break event append BREAK RECEIVED message.
		case SerialPortEvent.BI:
			//System.out.println("BREAK RECEIVED.");

		}

	}

	/**
	 * Handles ownership events. If a PORT_OWNERSHIP_REQUESTED event is received
	 * a dialog box is created asking the user if they are willing to give up
	 * the port. No action is taken on other types of ownership events.
	 */
	/*
	 * public void ownershipChange(int type) { if (type ==
	 * CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED) { PortRequestedDialog
	 * prd = new PortRequestedDialog(parent); } }
	 */

	/**
	 * A class to handle <code>KeyEvent</code> s generated by the
	 * messageAreaOut. When a <code>KeyEvent</code> occurs the
	 * <code>char</code> that is generated by the event is read, converted to
	 * an <code>int</code> and writen to the <code>OutputStream</code> for
	 * the port.
	 */

	class KeyHandler extends KeyAdapter {
		OutputStream toUtec;

		/**
		 * Creates the KeyHandler.
		 * 
		 * @param toUtec
		 *            The OutputStream for the port.
		 */
		public KeyHandler(OutputStream toUtec) {
			super();
			this.toUtec = toUtec;
		}

		/**
		 * Handles the KeyEvent. Gets the
		 * <code>char</char> generated by the <code>KeyEvent</code>,
		 converts it to an <code>int</code>, writes it to the <code>
		 OutputStream</code> for the port.
		 */
		public void keyTyped(KeyEvent evt) {
			char newCharacter = evt.getKeyChar();
			try {
				toUtec.write((int) newCharacter);
			} catch (IOException e) {
				System.err.println("OutputStream write error: " + e);
			}
		}
	}

}
