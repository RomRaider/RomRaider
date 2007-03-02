package enginuity.logger.utec.commInterface;

import java.io.IOException;
import java.util.Iterator;

import enginuity.logger.utec.comm.UtecSerialConnection;
import enginuity.logger.utec.commEvent.LoggerEvent;
import enginuity.logger.utec.commEvent.LoggerListener;
import enginuity.logger.utec.gui.mapTabs.DataManager;
import enginuity.logger.utec.mapData.UtecMapData;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class UtecSerialListener implements SerialPortEventListener{
	// Define whether or not we are recieving a map from the UTEC
	public boolean isMapFromUtecPrep = false;
	public boolean isMapFromUtec = false;
	public UtecMapData currentMap = null;
	public String totalDat = "";
	
	public UtecSerialListener(){
		System.out.println("Serial listener was instantiated.");
	}
	
	public void serialEvent(SerialPortEvent e) {
		System.out.println("Got serial event.");
		
		
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
					newData = UtecSerialConnection.getInputFromUtecStream().read();

					if (newData == -1) {
						break;
					}
					
					inputBuffer.append((char) newData);
					System.out.print((char)newData);
					this.totalDat += (char)newData;
					
				} catch (IOException ex) {
					System.err.println(ex);
					return;
				}
			}
			
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

					// Inform data manager of new map
					DataManager.setCurrentMap(this.currentMap);
					
					// Notify listner if available
					//System.out.println("Calling listeners.");
					//if (this.getMapFromUtecListener != null) {
					//	System.out.println("Listener called.");
					//	this.getMapFromUtecListener.mapRetrieved(this.currentMap);
					//}else{
					//	System.out.println("Calling listeners, but none found.");
					//}
					
					// Empty out map storage
					this.currentMap = null;
				}
			}

			// Logger data
			else {
				
				LoggerEvent loggerEvent = new LoggerEvent();
				loggerEvent.setLoggerData(new String(inputBuffer));
				loggerEvent.setLoggerData(true);

				Iterator portIterator = UtecInterface.getLoggerListeners().iterator();
				while (portIterator.hasNext()) {
					LoggerListener theListener = (LoggerListener) portIterator.next();
					if(loggerEvent.isValidData() == true){
						//System.out.println("Valid data");
						theListener.getCommEvent(loggerEvent);
					}else{
						//System.out.println("Invalid data");
					}
				}
				
				break;
			}

			// If break event append BREAK RECEIVED message.
		case SerialPortEvent.BI:
			//System.out.println("BREAK RECEIVED.");

		}
	}
}
