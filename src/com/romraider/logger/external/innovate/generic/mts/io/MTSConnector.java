/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2010 RomRaider.com
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

package com.romraider.logger.external.innovate.generic.mts.io;

import static com.romraider.logger.external.innovate.generic.mts.io.MTSFactory.createMTS;
import static org.apache.log4j.Logger.getLogger;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;


public final class MTSConnector {
    private static final Logger LOGGER = getLogger(MTSConnector.class);
    private MTS mts;
    private int[] ports;
    {
    	createMts();
    }
    
    /**
     * MTS Connector is a set of methods to create the MTS connection,
     * retrieve a set of available ports and the sensor inputs available
     * across all the found ports.
     */
    public MTSConnector() {
        setMtsPorts();
    }

    public MTSConnector(int mtsPort) {
        if (mtsPort != -1) mts(mtsPort);
    }

    public MTS getMts() {
    	return mts;
    }
    
    public int[] getMtsPorts() {
    	return ports;
    }
    
    public void usePort(int mtsPort) {
    	mts(mtsPort);
    }

    public void dispose() {
    	mts.disconnect();
    	mts.dispose();
    }

    private void createMts() {
      // create mts interface
      this.mts = createMTS();
      mts.disconnect();
    }

    private void setMtsPorts() {

        try {
            // check there are ports available
            int portCount = mts.portCount();
            if (portCount <= 0) throw new IllegalStateException("No Innovate MTS ports found");
            ports = new int[portCount];
            String names = "";
            for (int i = 0; i < portCount; i++) {
            	ports[i] = i;
            	mts.currentPort(i);
                names = names + " " + mts.portName();
            }
        	LOGGER.info("Innovate MTS: found " + portCount + " ports," + names);
        }
        catch (RuntimeException t) {
            // cleanup mts and rethrow exception
            if (mts != null) mts.dispose();
            throw t;
        }
    }
    
    public void mts(int mtsPort) {
        // bail out early if we know specified mts port is invalid
        if (mtsPort < 0) throw new IllegalArgumentException("Bad Innovate MTS port: " + mtsPort);

        try {
            int portCount = mts.portCount();
            if (portCount <= 0) throw new IllegalStateException("No Innovate MTS ports found");
        	
            // select the specified port
            mts.currentPort(mtsPort);
            String portName = mts.portName();
            LOGGER.info("Innovate MTS: current port [" + mtsPort + "]: " + portName);

        } catch (RuntimeException t) {
            // cleanup mts and rethrow exception
            if (mts != null) mts.dispose();
            throw t;
        }
    }

    public Set<MTSSensor> getSensors() {
    	Set<MTSSensor> sensors = new HashSet<MTSSensor>();
        try {
            // attempt to connect to the specified device
            mts.connect();

            try {
            	// get a count of available inputs
            	int inputCount = mts.inputCount();
            	LOGGER.info("Innovate MTS: found " + inputCount + " inputs.");

            	if (inputCount > 0) {
                	for (int i = 0; i < inputCount; i++) {
                        // report each input found
                        mts.currentInput(i);
                        MTSSensor sensor = new MTSSensor();
                        sensor.setInputNumber(i);
                        sensor.setInputName(mts.inputName());
                        sensor.setDeviceName(mts.inputDeviceName());
                        sensor.setDeviceChannel(mts.inputDeviceChannel());
                        sensor.setUnits(mts.inputUnit());
                        sensor.setMinValue(mts.inputMinValue());
                        sensor.setMaxValue(mts.inputMaxValue());
                        sensors.add(sensor);
                		LOGGER.debug(String.format(
                			"Innovate MTS: InputNo: %02d, InputName: %s, InputType: %d, DeviceName: %s, DeviceType: %d, DeviceChannel: %d, Units: %s, Multiplier: %f, MinValue: %f, MaxValue: %f",
                			i, mts.inputName(), mts.inputType(), mts.inputDeviceName(), mts.inputDeviceType(), mts.inputDeviceChannel(), mts.inputUnit(), mts.inputAFRMultiplier(), mts.inputMinValue(), mts.inputMaxValue()));
                	}
            	}
            	else {
                    LOGGER.error("Innovate MTS: Error - no input channels found to log from!");
            	}
            }
            finally {
                mts.disconnect();
            }
        }
        finally {
        }
    	return sensors;
    }
}