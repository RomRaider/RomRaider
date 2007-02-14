package enginuity.logger.utec.comm;

//import javax.comm.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import enginuity.logger.utec.commEvent.*;
import enginuity.logger.utec.mapData.GetMapFromUtecListener;
import enginuity.logger.utec.mapData.UtecMapData;


/**
 * Class negotiates data to and from UTEC via the serial port
 * 
 * Please note that at this time ownership issues are not handled.
 * See commented out code pertaining to the parent entity
 * 
 * @author botman
 * 
 */
//public class SerialConnection implements SerialPortEventListener,
//		CommPortOwnershipListener {

import gnu.io.*;

public class UtecControl implements SerialPortEventListener{
	
	// Parent object organizing connections to and from UTEC
	//private JPanel parent;

	//Data to UTEC
	//private JTextArea messageAreaOut;

	//Data from UTEC
	//private JTextArea messageAreaIn;

	private GetMapFromUtecListener getMapFromUtecListener = null;
	
	// Parameters used to define serial connection
	public SerialParameters parameters = new SerialParameters();

	//Data from UTEC
	private OutputStream outputToUtecStream;

	//Data to UTEC
	private InputStream inputFromUtecStream;

	// Handler for keyboard input
	private KeyHandler keyHandler;

	// Defines named comport
	private CommPortIdentifier portId;

	// Serial port being accessed
	private SerialPort sPort;

	// Defin state of defined comport, open or closed.
	private boolean open;

	//Listeners
	private Vector portListeners = new Vector();
	
	//Define whether or not we are recieving a map from the UTEC
	private boolean isMapFromUtecPrep = false;
	private boolean isMapFromUtec = false;
	private UtecMapData currentMap = null;
	
	/**
	 * Public constructor
	 * 
	 * @param parent
	 * @param parameters
	 * @param messageAreaOut
	 * @param messageAreaIn
	 */
	//public SerialConnection(JPanel parent, SerialParameters parameters,
	//		TextArea messageAreaOut, TextArea messageAreaIn) {
	public UtecControl(SerialParameters parameters) {
		//this.parent = parent;
		//this.messageAreaOut = messageAreaOut;
		//this.messageAreaIn = messageAreaIn;
		open = false;
	}
	
	
	/**
	 * Get UTEC to send logger data data
	 *
	 */
	public void startLoggerDataFlow(){
		System.out.println("Starting data flow from UTEC");
		
		//OutPut a '!' to start basic data flow from UTEC
		this.sendDataToUtec(33);
	}
	
	/**
	 * Reset UTEC to main screen
	 *
	 */
	public void resetUtec(){
		//OutPut 2 ctrl-x to UTEC
		this.sendDataToUtec('\u0018');
		this.sendDataToUtec('\u0018');
		this.sendDataToUtec(33);
		this.sendDataToUtec(33);
	}
	
	/**
	 * Get map data from map number passed in
	 * 
	 * @param mapNumber
	 */
	public void pullMapData(int mapNumber, GetMapFromUtecListener listener){
		System.out.println("UtecControl, getting map:"+mapNumber);
		
		// Check bounds of map requested
		if(mapNumber < 1 || mapNumber > 5){
			return;
		}
		
		// Null out any previously loaded map
		this.currentMap = null;
		
		// Who will get this map in the end?
		this.getMapFromUtecListener = listener;
		
		// Setup map transfer prep state
		this.isMapFromUtecPrep = true;
		this.isMapFromUtec = false;
		
		// Reset the UTEC
		this.resetUtec();
		
		// Send an 'e' to enter map menu
		//this.sendDataToUtec('\u0065');
		this.sendDataToUtec('e');
		System.out.println("Sent an e");
		
		// Point UTEC menu to the appropriate map
		if(mapNumber == 1){
			//this.sendDataToUtec('\u0021');
			this.sendDataToUtec(33);
			System.out.println("Requested Map 1");
		}
		if(mapNumber == 2){
			//this.sendDataToUtec('\u0040');
			this.sendDataToUtec(64);
			System.out.println("Requested Map 2");
		}
		if(mapNumber == 3){
			//this.sendDataToUtec('\u0023');
			this.sendDataToUtec(35);
			System.out.println("Requested Map 3");
		}
		if(mapNumber == 4){
			//this.sendDataToUtec('\u0024');
			this.sendDataToUtec(36);
			System.out.println("Requested Map 4");
		}
		if(mapNumber == 5){
			//this.sendDataToUtec('\u0025');
			this.sendDataToUtec(37);
			System.out.println("Requested Map 5");
		}
		
		// Write first ctrl-s to init save state
		//this.sendDataToUtec('\u0013');
		this.sendDataToUtec(19);
		System.out.println("Sent crtl-s");
		
		// Make this class receptive to map transfer
		this.isMapFromUtec = true;
		
		// No longer map prep
		this.isMapFromUtecPrep = false;
		
		// Write second ctrl-s to start map data flow
		//this.sendDataToUtec('\u0013');
		this.sendDataToUtec(19);
		System.out.println("Sent crtl-s");
	}

	/**
	 * Helper method to write chars to the UTEC
	 * 
	 * @param charValue
	 */
	private void sendDataToUtec(int charValue){
		try{
			outputToUtecStream.write(charValue);
		}
		catch(IOException e){
			System.err.println("Can't send char data to UTEC: "+charValue);
			e.getMessage();
		}
	}
	
	/**
	 * Opens a connection to the defined serial port If attempt fails,
	 * SerialConnectionException is thrown
	 * 
	 * @throws SerialConnectionException
	 */
	public void openConnection() throws SerialConnectionException {

		// Obtain a CommPortIdentifier object for the port you want to open.
		try {
			//System.out.println("PORT: "+parameters.getPortName());
			portId = CommPortIdentifier.getPortIdentifier(parameters
					.getPortName());
		} catch (NoSuchPortException e) {
			System.err.println("Can't get commport identifier");
			throw new SerialConnectionException(e.getMessage());
		}

		// Open the port represented by the CommPortIdentifier object. Give
		// the open call a relatively long timeout of 30 seconds to allow
		// a different application to reliquish the port if the user
		// wants to.
		try {
			sPort = (SerialPort) portId.open("SerialDemo", 30000);
		} catch (PortInUseException e) {
			System.err.println("Can't open serial port");
			throw new SerialConnectionException(e.getMessage());
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
		//keyHandler = new KeyHandler(outputToUtecStream);
		//messageAreaOut.addKeyListener(keyHandler);

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
		//portId.addPortOwnershipListener(this);

		open = true;
	}

	/**
	 * Sets the connection parameters to the setting in the parameters object.
	 * If set fails return the parameters object to origional settings and throw
	 * exception.
	 */
	public void setConnectionParameters() throws SerialConnectionException {

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
			return;
		}
		System.out.println("Closing connection to the currently targeted port");
		
		//Reset the UTEC first
		resetUtec();
		
		// Remove the key listener.
		//messageAreaOut.removeKeyListener(keyHandler);

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
			//portId.removePortOwnershipListener(this);
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
	 * @param o
	 */
	public void addListener(Object o){
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
			while (newData != -1) {
				try {
					newData = inputFromUtecStream.read();
					
					if (newData == -1) {
						break;
					}
					if ('\r' == (char) newData) {
						inputBuffer.append('\n');
					} else {
						inputBuffer.append((char) newData);
					}
					
					
				} catch (IOException ex) {
					System.err.println(ex);
					return;
				}
			}
			
			//Build new event with buffer data
			CommEvent commEvent = null; 
			if(this.isMapFromUtec || this.isMapFromUtecPrep){
				// See if we are finally recieving a map from the UTEC
				if(this.isMapFromUtec){
					System.out.println("Getting the map.");
					
					// If this is the start of map data flow, then create a new map data object
					if(this.currentMap == null){
						currentMap = new UtecMapData();
					}
					
					// Append byte data from the UTEC
					this.currentMap.addRawData(newData);
					System.out.println("Added:"+(char)newData);
					
					// Detect the end of the map recieving
					if(inputBuffer.indexOf("[EOF]") != -1){
						this.isMapFromUtecPrep = false;
						this.currentMap.populateMapDataStructures();
						
						// Notify listner if available
						if(this.getMapFromUtecListener != null){
							this.getMapFromUtecListener.mapRetrieved(this.currentMap);
						}
					}
				}
				
			}else{
				
			}
			
			commEvent = new CommEvent();
			commEvent.setLoggerData(new String(inputBuffer));
			commEvent.setLoggerData(true);
			
			//Send received data to listeners
			//if(commEvent != null){
				Iterator portIterator = portListeners.iterator();
				while(portIterator.hasNext()){
					CommListener theListener = (CommListener)portIterator.next();
					theListener.getCommEvent(commEvent);
				}
				break;
			//}
			

		// If break event append BREAK RECEIVED message.
		/*
		case SerialPortEvent.BI:
			messageAreaIn.append("\n--- BREAK RECEIVED ---\n");
		*/
		}
		
	}

	/**
	 * Handles ownership events. If a PORT_OWNERSHIP_REQUESTED event is received
	 * a dialog box is created asking the user if they are willing to give up
	 * the port. No action is taken on other types of ownership events.
	 */
	/*
	public void ownershipChange(int type) {
		if (type == CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED) {
			PortRequestedDialog prd = new PortRequestedDialog(parent);
		}
	}
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
