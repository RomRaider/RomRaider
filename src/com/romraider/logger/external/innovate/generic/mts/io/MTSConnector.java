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

import com.romraider.logger.external.innovate.lm2.mts.plugin.Lm2Sensor;

public final class MTSConnector {
    private static final Logger LOGGER = getLogger(MTSConnector.class);
    private final MTS mts;

    public MTSConnector(int mtsPort) {
        this.mts = mts(mtsPort);
    }

    public MTS getMts() {
    	return mts;
    }
    
    private MTS mts(int mtsPort) {
        // bail out early if we know specified mts port is invalid
        if (mtsPort < 0) throw new IllegalArgumentException("Bad MTS port: " + mtsPort);

        // create mts interface
        MTS mts = createMTS();
        mts.disconnect();

        try {
            // check there are ports available
            int portCount = mts.portCount();
            if (portCount <= 0) throw new IllegalStateException("No MTS ports found");
            LOGGER.info("MTS: found " + portCount + " ports.");

            // select the specified port
            mts.currentPort(mtsPort);
            String portName = mts.portName();
            LOGGER.info("MTS: current port [" + mtsPort + "]: " + portName);

            return mts;
        } catch (RuntimeException t) {
            // cleanup mts and rethrow exception
            if (mts != null) mts.dispose();
            throw t;
        }
    }

    public Set<Lm2Sensor> getSensors() {
    	Set<Lm2Sensor> sensors = new HashSet<Lm2Sensor>();
        try {
            // attempt to connect to the specified device
            mts.connect();

            try {
            	// get a count of available inputs
            	int inputCount = mts.inputCount();
            	LOGGER.info("MTS: found " + inputCount + " inputs.");

            	if (inputCount > 0) {
                	for (int i = 0; i < inputCount; i++) {
                        // report each input found
                        mts.currentInput(i);
                        Lm2Sensor sensor = new Lm2Sensor();
                        sensor.setInputNumber(i);
                        sensor.setInputName(mts.inputName());
                        sensor.setDeviceName(mts.inputDeviceName());
                        sensor.setDeviceChannel(mts.inputDeviceChannel());
                        sensor.setUnits(mts.inputUnit());
                        sensor.setMinValue(mts.inputMinValue());
                        sensor.setMaxValue(mts.inputMaxValue());
                        sensors.add(sensor);
                		LOGGER.debug(String.format(
                			"MTS: InputNo: %02d, InputName: %s, InputType: %d, DeviceName: %s, DeviceType: %d, DeviceChannel: %d, Units: %s, Multiplier: %f, MinValue: %f, MaxValue: %f",
                			i, mts.inputName(), mts.inputType(), mts.inputDeviceName(), mts.inputDeviceType(), mts.inputDeviceChannel(), mts.inputUnit(), mts.inputAFRMultiplier(), mts.inputMinValue(), mts.inputMaxValue()));
                	}
            	}
            	else {
                    LOGGER.error("MTS: Error - no input channels found to log from!");
            	}
            } finally {
                mts.disconnect();
            }
        } finally {
            mts.dispose();
        }
    	return sensors;
    }
}