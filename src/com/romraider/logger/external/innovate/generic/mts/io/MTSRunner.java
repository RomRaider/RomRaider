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

import static com.romraider.logger.external.innovate.generic.mts.io.MTSSensorInputFunction.MTS_FUNC_LAMBDA;
import static com.romraider.logger.external.innovate.generic.mts.io.MTSSensorInputFunction.MTS_FUNC_NOTLAMBDA;
import static com.romraider.logger.external.innovate.generic.mts.io.MTSSensorInputFunction.MTS_FUNC_O2;
import static com.romraider.logger.external.innovate.generic.mts.io.MTSSensorInputType.MTS_TYPE_AFR;
import static com.romraider.logger.external.innovate.generic.mts.io.MTSSensorInputType.MTS_TYPE_LAMBDA;
import static com.romraider.logger.external.innovate.generic.mts.io.MTSSensorInputType.MTS_TYPE_VDC;
import static com.romraider.util.ThreadUtil.sleep;
import static java.lang.System.currentTimeMillis;
import static org.apache.log4j.Logger.getLogger;

import java.util.Map;

import org.apache.log4j.Logger;

import com.romraider.logger.external.core.Stoppable;
import com.romraider.logger.external.innovate.lm2.mts.plugin.Lm2MtsDataItem;
import com4j.EventCookie;

public final class MTSRunner implements MTSEvents, Stoppable {
    private static final Logger LOGGER = getLogger(MTSRunner.class);
    private final Map<Integer, Lm2MtsDataItem> dataItems;
    private EventCookie connectionEventCookie;
    private final MTS mts;
    private boolean running;
    private boolean stop;

    /**
     * MTSRunner contains the work-horse methods to process the MTS stream
     * data and update the appropriate sensor result.  Once started this class
     * listens for events to process, typically with the newData() method after
     * a successful connection is made and data collection started. 
     */
    public MTSRunner(int mtsPort, Map<Integer, Lm2MtsDataItem> dataItems) {
    	MTSConnector connection = new MTSConnector(mtsPort);
    	this.mts = connection.getMts();
        this.dataItems = dataItems;
    }

    public void run() {
        running = true;
        try {
            doRun();
        } catch (Throwable t) {
            LOGGER.error("Innovate MTS error occurred", t);
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

    private void doRun() {
        try {
            // attempt to connect to the specified device
            connectionEventCookie = mts.advise(MTSEvents.class, this);
            mts.connect();

            try {
                if (mts.inputCount() > 0) {
                    while (!stop) {
                        // wait for newData() event to occur
                        sleep(100L);
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
            connectionEventCookie.close();
            mts.dispose();
        }
    }

    public void connectionEvent(int result) {
        if (result == 0) {
            mts.startData();
        }
        else if (result == -1) {
        	throw new IllegalStateException("No Innovate MTS Data detected");
        }
        else {
        	throw new IllegalStateException("Innovate MTS Connect Error: " + result);
        }
    }
      
    public void connectionError() {
        mts.disconnect();
    	throw new IllegalStateException("Innovate MTS Connection Timeout");
    }

    public void newData() {
        for (int i = 0; i < mts.inputCount(); i++) {
            float data = 0f;

            // select the input
            mts.currentInput(i);
            int type = mts.inputType();
            int function = mts.inputFunction();
            int sample = mts.inputSample();
            LOGGER.trace("Innovate MTS input = " + i + ", type = " + type + ", function = " + function + ", sample = " + sample);

            // 5V channel
            // Determine the range between min and max,
            // calculate what percentage of that our sample represents,
            // shift back to match our offset from 0.0 for min
            if (type == MTS_TYPE_VDC.getType()) {
                if (function == MTS_FUNC_NOTLAMBDA.getFunction()) {
                    float min = mts.inputMinValue();
                    float max = mts.inputMaxValue();
                    data = ((max - min) * ((float) sample / 1024f)) + min;
                }
                else {
                	// this will report other functions, such as ERROR states
                	// as a negative constant value
                	data = (float)function * -1f;
                }
            }

            // AFR
            // Take each sample step as .001 Lambda,
            // add 0.5 (so our range is 0.5 to 1.523 for our 1024 steps),
            // then multiply by the AFR multiplier
            if (type == MTS_TYPE_AFR.getType()) {
                if (function == MTS_FUNC_LAMBDA.getFunction()) {
                    float multiplier = mts.inputAFRMultiplier();
                    data = ((float) sample / 1000f + 0.5f) * multiplier;
                }
                else if (function == MTS_FUNC_O2.getFunction()) {
                    data = ((float) sample / 10f);
                }
                else {
                	// this will report other functions, such as ERROR states
                	// as a negative constant value
                	data = (float)function * -1f;
                }
            }

            // LAMBDA
            // Identical to AFR, except we do not multiply for AFR.
            if (type == MTS_TYPE_LAMBDA.getType()) {
                if (function == MTS_FUNC_LAMBDA.getFunction()) {
                    data = (float) sample / 1000f + 0.5f;
                }
                else {
                	// this will report other functions, such as ERROR states
                	// as a negative constant value
                	data = (float)function * -1f;
                }
            }
            // set data for this sensor based on inputNumber
            Lm2MtsDataItem dataItem = dataItems.get(i);
            if (dataItem != null) dataItem.setData(data);
        }
    }
}
