/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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
    private boolean pollMode;

    /**
     * MTSRunner contains the work-horse methods to process the MTS stream
     * data and update the appropriate sensor result.  Once started this class
     * listens to NewData Events.
     * If Event processing appears problematic set the pollmode option to true
     * in the lm2_mts.plugin file to enable reading data from the MTS stream
     * rather than waiting for NewData events.
     */
    public MTSRunner(int mtsPort, Map<Integer,
            Lm2MtsDataItem> dataItems,
            boolean pollMode) {
        MTSConnector connection = new MTSConnector(mtsPort);
        this.mts = connection.getMts();
        this.dataItems = dataItems;
        this.pollMode = pollMode;
    }

    @Override
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

    @Override
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
                        if (pollMode) readData();
                        sleep(40L);
                    }
                }
                else {
                    LOGGER.error("Innovate MTS: Error while reading data - no input channels found to log from!");
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

    @Override
    public void connectionEvent(int result) {
        if (result == 0) {
            mts.startData();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Innovate MTS connection success, pollmode:" + pollMode);
        }
        else if (result == -1) {
            throw new IllegalStateException("No Innovate MTS Data detected");
        }
        else {
            throw new IllegalStateException("Innovate MTS Connect Error: " + result);
        }
    }

    @Override
    public void connectionError() {
        stop();
        throw new IllegalStateException("Innovate MTS Connection Timeout");
    }

    @Override
    public void newData() {
        if(!pollMode) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Innovate MTS newData event");
            readData();
        }
    }

    public void readData() {
        Lm2MtsDataItem dataItem;
        int type;
        int function;
        int sample;
        float data = 0.0f;
        float min;
        float max;
        float multiplier;
        try {
            for (int i = 0; i < mts.inputCount(); i++) {
                // select the input
                dataItem = dataItems.get(i);
                mts.currentInput(i);
                type = mts.inputType();
                function = mts.inputFunction();
                sample = mts.inputSample();

                // 5V channel
                // Determine the range between min and max,
                // calculate what percentage of that our sample represents,
                // shift back to match our offset from 0.0 for min
                if (type == MTS_TYPE_VDC.getType()) {
                    if (function == MTS_FUNC_NOTLAMBDA.getFunction()) {
                        min = dataItem.getMinValue();
                        max = dataItem.getMaxValue();
                        data = ((max - min) * (sample / 1024f)) + min;
                    }
                    else {
                        // this will report other functions, such as ERROR states
                        // as a negative constant value
                        data = function * -1f;
                    }
                }

                // AFR
                // Take each sample step as .001 Lambda,
                // add 0.5 (so our range is 0.5 to 1.523 for our 1024 steps),
                // then multiply by the AFR multiplier
                if (type == MTS_TYPE_AFR.getType()) {
                    if (function == MTS_FUNC_LAMBDA.getFunction()) {
                        multiplier = dataItem.getMultiplier();
                        data = (sample / 1000f + 0.5f) * multiplier;
                    }
                    else if (function == MTS_FUNC_O2.getFunction()) {
                        data = (sample / 10f);
                    }
                    else {
                        // this will report other functions, such as ERROR states
                        // as a negative constant value
                        data = function * -1f;
                    }
                }

                // LAMBDA
                // Identical to AFR, except we do not multiply for AFR.
                if (type == MTS_TYPE_LAMBDA.getType()) {
                    if (function == MTS_FUNC_LAMBDA.getFunction()) {
                        data = sample / 1000f + 0.5f;
                    }
                    else {
                        // this will report other functions, such as ERROR states
                        // as a negative constant value
                        data = function * -1f;
                    }
                }
                // set data for this sensor based on inputNumber
                if (dataItem != null) dataItem.setData(data);
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace(String.format(
                            "Innovate MTS input: %d, type: %d, function: %d, sample: %d, result: %f",
                            i, type, function, sample, data));
            }
        }
        catch (Exception e) {
            LOGGER.error(String.format(
                    "Innovate MTS read data error: %s", e.toString()));
        }
    }
}
