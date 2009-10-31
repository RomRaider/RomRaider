/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
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

package com.romraider.logger.innovate.generic.mts.io;

import static com.romraider.logger.innovate.generic.mts.io.MTSFactory.createMTS;
import com.romraider.logger.innovate.generic.mts.plugin.DataListener;
import static com.romraider.util.ThreadUtil.sleep;
import static java.lang.System.currentTimeMillis;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class MTSRunnerImpl implements MTSRunner {
    private static final Logger LOGGER = getLogger(MTSRunnerImpl.class);
    private final DataListener listener;
    private final int mtsInput = 0;
    private final int mtsPort;
    private boolean running;
    private boolean stop;

    public MTSRunnerImpl(int mtsPort, DataListener listener) {
        this.mtsPort = mtsPort;
        this.listener = listener;
    }

    public void run() {
        running = true;
        try {
            doRun();
        } finally {
            running = false;
        }
    }

    public void stop() {
        stop = true;

        // wait for it to stop running so mts can disconnect/dispose... timeout after 2secs
        long timeout = currentTimeMillis() + 2000L;
        while (running && currentTimeMillis() < timeout) sleep(50L);
    }

    private void doRun() {
        if (mtsPort < 0) return;

        MTS mts = createMTS();
        try {
            int portCount = mts.portCount();
            LOGGER.debug("MTS: found " + portCount + " ports.");

            mts.currentPort(mtsPort);
            String portName = mts.portName();
            LOGGER.debug("MTS: current port [" + mtsPort + "]: " + portName);

            // attempt to connect to the specified device
            mts.connect();
            try {

                // select the input
                mts.currentInput(mtsInput);

                // attempt to get data
                mts.startData();

                // wait a moment for data acquisition to begin
                sleep(1000L);

                // start collecting data
                while (!stop) {
                    int type = mts.inputType();
                    int function = mts.inputFunction();
                    int sample = mts.inputSample();

                    LOGGER.trace("MTS: type = " + type + ", function = " + function + ", sample = " + sample);

                    float data = 0f;

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
                    if (type == 1) {
                        // MTS_FUNC_LAMBDA
                        if (function == 0) {
                            float multiplier = mts.inputAFRMultiplier();
                            data = ((float) sample / 1000f + 0.5f) * multiplier;
                        }
                        // MTS_FUNC_O2
                        if (function == 1) {
                            data = ((float) sample / 10f);
                        }
                    }

                    // Lambda
                    // Identical to AFR, except we do not multiply for AFR.
                    if (type == 0) {
                        // MTS_FUNC_LAMBDA
                        if (function == 0) {
                            data = (float) sample / 1000f + 0.5f;
                        }
                    }

                    // report the result
                    listener.setData((double) data);

                    sleep(50L);
                }
            } finally {
                mts.disconnect();
            }
        } finally {
            mts.dispose();
        }
    }
}