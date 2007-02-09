/*
 * Created on May 30, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package enginuity.logger.utec.commInterface;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.*;
//import javax.comm.CommPortIdentifier
import gnu.io.*;
import javax.swing.*;

import enginuity.logger.utec.gui.realtimeData.*;
import enginuity.logger.utec.comm.*;
/**
 * @author emorgan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CommInterface{
	//Store string vector of known system comm ports
	private static Vector portChoices =  listPortChoices();
	
	//Basic connection settings, already tuned for a UTEC connection, baud, parity, etc
	private static SerialParameters parameters = new SerialParameters();
	
	//Actual connection entity
	private static SerialConnection connection = new SerialConnection(parameters);


	public static boolean ISOPEN = connection.isOpen();
	
	public static Vector getPortsVector(){
		return portChoices;
	}
	
	public static String getPortChoiceUsed(){
		return connection.parameters.getPortName();
	}
	
	/**
	 * Open a port as per names defined in get port names method
	 * 
	 * @param portName
	 */
	 public static void openConnection(){
		//No port yet chosen
		if(connection.parameters.getPortName().equalsIgnoreCase("")){
					System.err.println("No Port Yet Chosen, nothing to open");
					return;
		}
	 	
	 	//Port is already opened, any port is open
	 	/*
	 	if(ISOPEN){
	 		System.err.println("Port already opened");
	 		return;
	 	}
	 	*/
	 	
	 	//Attempt to make connection
	 	try{
	 		connection.openConnection();
	 	}catch(SerialConnectionException e){
	 		System.err.println("Error opening serial port connection");
	 		e.printStackTrace();
	 		return;
	 	}
	 	
	 	//Successful opening of port
	 	//ISOPEN=true;
	 }
	
	public static void closeConnection(){
		connection.closeConnection();	
	}
	
	public static void setPortChoice(String port){
		connection.parameters.setPortName(port);
	}
	
	/**
	 * Resets UTEC to main screen
	 *
	 */
	public static void resetUtec(){
		connection.resetUtec();	
	}
	
	/**
	 * Starts the #1 logger (SHIFT-1), ie !, ie CSV logger.
	 *
	 */
	public static void startDataLogFromUtec(){
		connection.startDataFlow();
	}
	
	
	/**
	 * Adds a listener for comm port events
	 * @param o
	 */
	public static void addListener(Object o){
		if(connection == null){
			System.err.println("No Serial Connection defined yet.. DIZZoGG!");
			return;
		}
		
		connection.addListener(o);
	}
	
	/**
	 * Method returns a vector of all available serial ports found
	 * 
	 * @return
	 */
	private static Vector listPortChoices() {
		Vector theChoices = new Vector();
		CommPortIdentifier portId;
		
		Enumeration en = CommPortIdentifier.getPortIdentifiers();
		if (!en.hasMoreElements()) {
			System.err.println("No Valid ports found, check Java installation");
		}
			
		//Iterate through the ports
		while (en.hasMoreElements()) {
			portId = (CommPortIdentifier) en.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				System.out.println("Port found on system: "+portId.getName());
				theChoices.addElement(portId.getName());
			}
		}
		return theChoices;
	}
}