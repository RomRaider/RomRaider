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
import static com.romraider.util.ThreadUtil.sleep;
import static java.lang.System.currentTimeMillis;
import static org.apache.log4j.Logger.getLogger;

import java.util.Map;

import org.apache.log4j.Logger;

import com.romraider.logger.external.core.Stoppable;
import com.romraider.logger.external.innovate.lm2.mts.plugin.Lm2MtsDataItem;
import com.romraider.logger.external.innovate.lm2.mts.plugin.Lm2SensorType;

public final class MTSRunner implements Stoppable {
    private static final Logger LOGGER = getLogger(MTSRunner.class);
    private final Map<Lm2SensorType, Lm2MtsDataItem> dataItems;
    private final MTS mts;
    private boolean running;
    private boolean stop;

    public MTSRunner(int mtsPort, Map<Lm2SensorType, Lm2MtsDataItem> dataItems) {
        this.mts = mts(mtsPort);
        this.dataItems = dataItems;
    }

    public void run() {
        running = true;
        try {
            doRun();
        } catch (Throwable t) {
            LOGGER.error("Error occurred", t);
        } finally {
            running = false;
        }
    }

    public void stop() {
        stop = true;

        // wait for it to stop running so mts can disconnect/dispose... timeout after 5secs
        long timeout = currentTimeMillis() + 5000L;
        while (running && currentTimeMillis() < timeout) sleep(100L);
    }

    private MTS mts(int mtsPort) {
        // bail out early if we know specified mts port is invalid
        if (mtsPort < 0) throw new IllegalArgumentException("Bad MTS port: " + mtsPort);

        // create mts interface
        MTS mts = createMTS();

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

    private void doRun() {
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
                		LOGGER.info(String.format(
                			"MTS: InputNo: %02d, InputName: %s, InputType: %d, DeviceName: %s, DeviceType: %d, DeviceChannel: %d, Units: %s, Multiplier: %f, MinValue: %f, MaxValue: %f, MinVolts: %f, MaxVolts: %f",
                			i, mts.inputName(), mts.inputType(), mts.inputDeviceName(), mts.inputDeviceType(), mts.inputDeviceChannel(), mts.inputUnit(), mts.inputAFRMultiplier(), mts.inputMinValue(), mts.inputMaxValue(), mts.inputMinVolt(), mts.inputMaxVolt()));
                	}
	                // attempt to get data
	                mts.startData();

	                // for each input get the sample
	            	while (!stop) {
	            		for (int i = 0; i < inputCount; i++) {
	            			mts.currentInput(i);
		                    int type = mts.inputType();
		                    int function = mts.inputFunction();
		                    int sample = mts.inputSample();
		
		                    LOGGER.trace("MTS: type = " + type + ", function = " + function + ", sample = " + sample);
		
		                    float data = 0f;
	
		                    // Input Types
		                    // 0 = Lambda 
		                    // 1 = AFR 
		                    // 2 = 5V
	
		                    // 5V channel
		                    // Determine the range between min and max,
		                    // calculate what percentage of that our sample represents,
		                    // shift back to match our offset from 0.0 for min
		                    if (type == 2) {
		                        // MTS_FUNC_NOTLAMBDA
		                        if (function == 9) {
		                            float min = mts.inputMinValue();
		                            float max = mts.inputMaxValue();
		                            data = ((max - min) * ((float) sample / 1024f)) + min;
		                        }
		                    }
		
		                    // AFR
		                    // Take each sample step as .001 Lambda,
		                    // add 0.5 (so our range is 0.5 to 1.523 for our 1024 steps),
		                    // then multiply by the AFR multiplier
		                    if (type == 0 || type == 1) {
		                        // MTS_FUNC_LAMBDA
		                        if (function == 0) {
		                            //float multiplier = mts.inputAFRMultiplier();
		                            //data = ((float) sample / 1000f + 0.5f) * multiplier;
		                            data = ((float) sample / 1000f + 0.5f);
		                        }
		                        // MTS_FUNC_O2
		                        if (function == 1) {
		                            data = ((float) sample / 10f);
		                        }
		                    }
	                        Lm2SensorType sensor = Lm2SensorType.valueOf(function, mts.inputDeviceChannel());
	                        Lm2MtsDataItem dataItem = dataItems.get(sensor);
	                        if (dataItem != null) dataItem.setData(data);
		                }
	                    sleep(10L);
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
    }
}