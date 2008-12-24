/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.romraider.logger.utec.commInterface;

import com.romraider.logger.utec.comm.UtecSerialConnectionManager;
import com.romraider.logger.utec.gui.mapTabs.UtecDataManager;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.IOException;

public class UtecSerialListener implements SerialPortEventListener {
    private static UtecSerialListener instance = null;
    private static String totalData = "";
    private static boolean isRegistered = false;

    public static UtecSerialListener getInstance() {
        if (instance == null) {
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
                        if (newData == 13 || newData == 10) {
                            //Dont append newlines or carriage returns
                        } else {
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
                    if (newData == 13) {
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
