/*
 * Class holds serial port specific data
 */

package enginuity.logger.utec.comm;

//import javax.comm.*;
import gnu.io.*;

/**
 * A class that stores parameters for serial ports.
 */
public class SerialParameters {
	private String portName;
	private int baudRate;
	private int flowControlIn;
	private int flowControlOut;
	private int databits;
	private int stopbits;
	private int parity;
	

	/**
	 * Constructor. Settings tuned to connect to the UTEC
	 * 
	 */
	public SerialParameters() {
		this.portName = "";
		this.baudRate = 19200;
		this.flowControlIn = SerialPort.FLOWCONTROL_NONE;
		this.flowControlOut = SerialPort.FLOWCONTROL_NONE;
		this.databits = SerialPort.DATABITS_8;
		this.stopbits = SerialPort.STOPBITS_1;
		this.parity = SerialPort.PARITY_NONE;
	}

	   /**
    Sets port name.
    @param portName New port name.
    */
    public void setPortName(String portName) {
    	System.out.println("Set portName chosen: "+portName);
    	this.portName = portName;
    }

    /**
    Gets port name.
    @return Current port name.
    */
    public String getPortName() {
	return portName;
    }

    /**
    Sets baud rate.
    @param baudRate New baud rate.
    */
    public void setBaudRate(int baudRate) {
	this.baudRate = baudRate;
    }

    /**
    Sets baud rate.
    @param baudRate New baud rate.
    */
    public void setBaudRate(String baudRate) {
	this.baudRate = Integer.parseInt(baudRate);
    }

    /**
    Gets baud rate as an <code>int</code>.
    @return Current baud rate.
    */
    public int getBaudRate() {
	return baudRate;
    }

    /**
    Gets baud rate as a <code>String</code>.
    @return Current baud rate.
    */
    public String getBaudRateString() {
	return Integer.toString(baudRate);
    }

    /**
    Sets flow control for reading.
    @param flowControlIn New flow control for reading type.
    */
    public void setFlowControlIn(int flowControlIn) {
	this.flowControlIn = flowControlIn;
    }

    /**
    Sets flow control for reading.
    @param flowControlIn New flow control for reading type.
    */
    public void setFlowControlIn(String flowControlIn) {
	this.flowControlIn = stringToFlow(flowControlIn);
    }

    /** 
    Gets flow control for reading as an <code>int</code>.
    @return Current flow control type.
    */
    public int getFlowControlIn() {
	return flowControlIn;
    }

    /** 
    Gets flow control for reading as a <code>String</code>.
    @return Current flow control type.
    */
    public String getFlowControlInString() {
	return flowToString(flowControlIn);
    }

    /**
    Sets flow control for writing.
    @param flowControlIn New flow control for writing type.
    */
    public void setFlowControlOut(int flowControlOut) {
	this.flowControlOut = flowControlOut;
    }

    /**
    Sets flow control for writing.
    @param flowControlIn New flow control for writing type.
    */
    public void setFlowControlOut(String flowControlOut) {
	this.flowControlOut = stringToFlow(flowControlOut);
    }

    /** 
    Gets flow control for writing as an <code>int</code>.
    @return Current flow control type.
    */
    public int getFlowControlOut() {
	return flowControlOut;
    }

    /** 
    Gets flow control for writing as a <code>String</code>.
    @return Current flow control type.
    */
    public String getFlowControlOutString() {
	return flowToString(flowControlOut);
    }

    /** 
    Sets data bits.
    @param databits New data bits setting.
    */
    public void setDatabits(int databits) {
	this.databits = databits;
    }

    /** 
    Sets data bits.
    @param databits New data bits setting.
    */
    public void setDatabits(String databits) {
	if (databits.equals("5")) {
	    this.databits = SerialPort.DATABITS_5;
	}
	if (databits.equals("6")) {
	    this.databits = SerialPort.DATABITS_6;
	}
	if (databits.equals("7")) {
	    this.databits = SerialPort.DATABITS_7;
	}
	if (databits.equals("8")) {
	    this.databits = SerialPort.DATABITS_8;
	}
    }

    /**
    Gets data bits as an <code>int</code>.
    @return Current data bits setting.
    */
    public int getDatabits() {
	return databits;
    }

    /**
    Gets data bits as a <code>String</code>.
    @return Current data bits setting.
    */
    public String getDatabitsString() {
	switch(databits) {
	    case SerialPort.DATABITS_5:
		return "5";
	    case SerialPort.DATABITS_6:
		return "6";
	    case SerialPort.DATABITS_7:
		return "7";
	    case SerialPort.DATABITS_8:
		return "8";
	    default:
		return "8";
	}
    }

    /**
    Sets stop bits.
    @param stopbits New stop bits setting.
    */
    public void setStopbits(int stopbits) {
	this.stopbits = stopbits;
    }

    /**
    Sets stop bits.
    @param stopbits New stop bits setting.
    */
    public void setStopbits(String stopbits) {
	if (stopbits.equals("1")) {
	    this.stopbits = SerialPort.STOPBITS_1;
	}
	if (stopbits.equals("1.5")) {
	    this.stopbits = SerialPort.STOPBITS_1_5;
	}
	if (stopbits.equals("2")) {
	    this.stopbits = SerialPort.STOPBITS_2;
	}
    }

    /**
    Gets stop bits setting as an <code>int</code>.
    @return Current stop bits setting.
    */
    public int getStopbits() {
	return stopbits;
    }

    /**
    Gets stop bits setting as a <code>String</code>.
    @return Current stop bits setting.
    */
    public String getStopbitsString() {
	switch(stopbits) {
	    case SerialPort.STOPBITS_1:
		return "1";
	    case SerialPort.STOPBITS_1_5:
		return "1.5";
	    case SerialPort.STOPBITS_2:
		return "2";
	    default:
		return "1";
	}
    }

    /**
    Sets parity setting.
    @param parity New parity setting.
    */
    public void setParity(int parity) {
	this.parity = parity;
    }

    /**
    Sets parity setting.
    @param parity New parity setting.
    */
    public void setParity(String parity) {
	if (parity.equals("None")) {
	    this.parity = SerialPort.PARITY_NONE;
	}
	if (parity.equals("Even")) {
	    this.parity = SerialPort.PARITY_EVEN;
	}
	if (parity.equals("Odd")) {
	    this.parity = SerialPort.PARITY_ODD;
	}
    }

    /**
    Gets parity setting as an <code>int</code>.
    @return Current parity setting.
    */
    public int getParity() {
	return parity;
    }

    /**
    Gets parity setting as a <code>String</code>.
    @return Current parity setting.
    */
    public String getParityString() {
	switch(parity) {
	    case SerialPort.PARITY_NONE:
		return "None";
 	    case SerialPort.PARITY_EVEN:
		return "Even";
	    case SerialPort.PARITY_ODD:
		return "Odd";
	    default:
		return "None";
	}
    }

    /**
    Converts a <code>String</code> describing a flow control type to an
    <code>int</code> type defined in <code>SerialPort</code>.
    @param flowControl A <code>string</code> describing a flow control type.
    @return An <code>int</code> describing a flow control type.
    */
    private int stringToFlow(String flowControl) {
	if (flowControl.equals("None")) {
	    return SerialPort.FLOWCONTROL_NONE;
	}
	if (flowControl.equals("Xon/Xoff Out")) {
	    return SerialPort.FLOWCONTROL_XONXOFF_OUT;
	}
	if (flowControl.equals("Xon/Xoff In")) {
	    return SerialPort.FLOWCONTROL_XONXOFF_IN;
	}
	if (flowControl.equals("RTS/CTS In")) {
	    return SerialPort.FLOWCONTROL_RTSCTS_IN;
	}
	if (flowControl.equals("RTS/CTS Out")) {
	    return SerialPort.FLOWCONTROL_RTSCTS_OUT;
	}
	return SerialPort.FLOWCONTROL_NONE;
    }

    /**
    Converts an <code>int</code> describing a flow control type to a 
    <code>String</code> describing a flow control type.
    @param flowControl An <code>int</code> describing a flow control type.
    @return A <code>String</code> describing a flow control type.
    */
    String flowToString(int flowControl) {
	switch(flowControl) {
	    case SerialPort.FLOWCONTROL_NONE:
		return "None";
	    case SerialPort.FLOWCONTROL_XONXOFF_OUT:
		return "Xon/Xoff Out";
	    case SerialPort.FLOWCONTROL_XONXOFF_IN:
		return "Xon/Xoff In";
	    case SerialPort.FLOWCONTROL_RTSCTS_IN:
		return "RTS/CTS In";
	    case SerialPort.FLOWCONTROL_RTSCTS_OUT:
		return "RTS/CTS Out";
	    default:
		return "None";
	}
    }
}
