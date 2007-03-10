package enginuity.logger.utec.commInterface;

import java.io.IOException;
import java.util.Iterator;

import enginuity.logger.utec.comm.UtecSerialConnectionManager;
import enginuity.logger.utec.commEvent.LoggerEvent;
import enginuity.logger.utec.commEvent.LoggerDataListener;
import enginuity.logger.utec.gui.mapTabs.UtecDataManager;
import enginuity.logger.utec.mapData.UtecMapData;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class UtecSerialListener implements SerialPortEventListener {
	private static UtecSerialListener instance = null;
	private static String totalData = "";
	private static boolean isRegistered = false;
	
	public static UtecSerialListener getInstance(){
		if(instance == null){
			instance = new UtecSerialListener();
		}
		System.out.println("Seria listener instance query.");
		return instance;
	}
	
	private UtecSerialListener() {
		totalData = "";
		System.out.println("Serial listener was instantiated.");
	}

	public void serialEvent(SerialPortEvent e) {
		
		int newData = 1;
		switch (e.getEventType()) {
		case SerialPortEvent.DATA_AVAILABLE:

			// Append new output to buffer
			while (newData != -1) {
				try {
					newData = UtecSerialConnectionManager.getInputFromUtecStream().read();

					// Invalid data
					if (newData == -1) {
						//totalData = "";
						//System.err.println("Invalid data at UtecSerialListener, breaking while loop.");
						break;
					}
					
					// Dont append new lines \r or \n
					if(newData == 13 || newData == 10){
						//Dont append newlines or carriage returns
					}else{
						totalData += (char) newData;
					}

					// Output all data received
					//System.out.print((char)newData);
					
				} catch (IOException ex) {
					System.err.println(ex);
					return;
				}
				
				// New line of data available now
				//if((this.totalData.indexOf("\r") != 1) || (this.totalData.indexOf("\n") != 1)){
				if(newData == 13){
					//System.out.println("USL Newline:"+newData);
					String tempData = totalData;
					totalData = "";
					//System.out.println("USL totalData:"+tempData+":");
					UtecDataManager.setSerialData(tempData);
					
				}
				
			}// End while loop
		}
	}

	public static boolean isRegistered() {
		return isRegistered;
	}

	public static void setRegistered(boolean isRegistered) {
		UtecSerialListener.isRegistered = isRegistered;
	}
}
