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
import java.util.*;
//import javax.comm.CommPortIdentifier
import gnu.io.*;
import javax.swing.*;

import enginuity.logger.utec.gui.realtimeData.*;
import enginuity.logger.utec.mapData.GetMapFromUtecListener;
import enginuity.logger.utec.comm.*;
/**
 * @author emorgan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class UtecInterface{
	//Store string vector of known system comm ports
	private static Vector portChoices =  listPortChoices();
	
	//Basic connection settings, already tuned for a UTEC connection, baud, parity, etc
	private static SerialParameters parameters = new SerialParameters();
	
	//Actual connection entity
	private static UtecSerialConnection utecControl = new UtecSerialConnection(parameters);


	public static boolean ISOPEN = utecControl.isOpen();
	
	public static Vector getPortsVector(){
		return portChoices;
	}
	
	public static String getPortChoiceUsed(){
		return utecControl.parameters.getPortName();
	}
	
	public static void getMap(int mapNumber, GetMapFromUtecListener listener){
		openConnection();
		utecControl.pullMapData(mapNumber, listener);
	}
	
	/**
	 * Pass a command to the UTEC
	 * @param charValue
	 */
	public static void sendDataToUtec(int charValue){
		openConnection();
		utecControl.sendDataToUtec(charValue);
	}
	
	/**
	 * Open a port as per names defined in get port names method
	 * 
	 * @param portName
	 */
	 private static void openConnection(){
		 if(utecControl.isOpen()){
			 System.out.println("Port is already open.");
			 return;
		 }
		 
		//No port yet chosen
		if(utecControl.parameters.getPortName().equalsIgnoreCase("")){
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
	 		utecControl.openConnection();
	 	}catch(SerialConnectionException e){
	 		System.err.println("Error opening serial port connection");
	 		e.printStackTrace();
	 		return;
	 	}
	 	
	 	//Successful opening of port
	 	//ISOPEN=true;
	 }
	
	public static void closeConnection(){
		utecControl.closeConnection();	
	}
	
	public static void setPortChoice(String port){
		utecControl.closeConnection();
		utecControl.parameters.setPortName(port);
	}
	
	/**
	 * Resets UTEC to main screen
	 *
	 */
	public static void resetUtec(){
		openConnection();
		utecControl.resetUtec();	
	}
	
	/**
	 * Starts the #1 logger (SHIFT-1), ie !, ie CSV logger.
	 *
	 */
	public static void startDataLogFromUtec(){
		openConnection();
		utecControl.resetUtec();
		utecControl.startLoggerDataFlow();
	}
	
	
	/**
	 * Adds a listener for comm port events
	 * @param o
	 */
	public static void addListener(Object o){
		if(utecControl == null){
			System.err.println("No Serial Connection defined yet.");
			return;
		}
		
		utecControl.addListener(o);
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