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

package com.romraider.logger.external.txs.io;

import static com.romraider.util.ParamChecker.isNullOrEmpty;
import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.serial.connection.SerialConnection;
import com.romraider.io.serial.connection.SerialConnectionImpl;
import com.romraider.logger.external.core.Stoppable;
import com.romraider.logger.external.txs.plugin.TxsDataItem;

public final class TxsRunner implements Stoppable{

    private static final Logger LOGGER = getLogger(TxsRunner.class);
    private static final ConnectionProperties CONNECTION_PROPS =
            new TxsConnectionProperties();
    private static final String WHITESPACE_REGEX = "\\s+";
    private static final String SPLIT_DELIMITER = " ";
    private static final byte[] EXIT = new byte[]{24};

    private final ArrayList<TxsDataItem> dataItems;
    private final SerialConnection connection;

    private boolean stop;
    private String txsLogger;
    private String txsDevice;

    public TxsRunner(
            String port,
            ArrayList<TxsDataItem> dataItems,
            String logger,
            String device) {
        this.connection = new SerialConnectionImpl(port, CONNECTION_PROPS);
        this.dataItems = dataItems;
        this.txsLogger = logger;
        this.txsDevice = device;
    }

    public TxsRunner(ArrayList<TxsDataItem> dataItems)
    {
        this.connection = null;
        this.dataItems = dataItems;
    }

    @Override
    public void run() {
        try {

            if (LOGGER.isTraceEnabled())
                LOGGER.trace("TXS Runner Begin.");

            //Convert string into bytes[]
            byte[] device = txsDevice.getBytes();
            byte[] logger = this.txsLogger.getBytes();

            if (LOGGER.isTraceEnabled())
                LOGGER.trace("TXS Runner Send Exit to Main Screen.");

            //Exit to main screen
            connection.write(EXIT);

            if (LOGGER.isTraceEnabled())
                LOGGER.trace("TXS Runner Sleep 250 ms to exit to main.");
            //wait for exit to complete.
            Thread.sleep(250L);

            if (LOGGER.isTraceEnabled())
                LOGGER.trace("TXS Runner Readline 1.");
            String response = connection.readLine();

            if(response != null && response.trim().isEmpty() == false)
            {
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("TXS Runner Readline 1 Response: "+ response);
            }

            if (LOGGER.isTraceEnabled())
                LOGGER.trace("TXS Runner Switching to Device. " + txsDevice);
            //Send command to switch device: utec / tuner.
            connection.write(device);

            //Read and Trace response switching device.
            response = connection.readLine();

            if(response != null && response.trim().isEmpty() == false)
            {
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("TXS Runner Readline 2 Response: "+ response);
            }

            if (LOGGER.isTraceEnabled())
                LOGGER.trace("TXS Runner Start Logger.");

            //Start device logger
            connection.write(logger);

            while (!stop) {
                //Get Response from TXS Device
                response = connection.readLine();
                connection.write(logger);

                //Continue if no data was received.
                if (isNullOrEmpty(response)) {
                    continue;
                }

                //Trace response
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("TXS Runner Response: " + response);
                //Split Values for parsing
                String[] values = SplitUtecString(response);
                //Set Data Item Values
                SetDataItemValues(values);
            }
            connection.close();
        }
        catch (Throwable t) {
            LOGGER.error("Error occurred", t);
        }
        finally {
            connection.close();
        }
    }

    @Override
    public void stop() {
        stop = true;
    }

    String[] SplitUtecString(String value) {
        try {
            value = value.trim();
            value = value.replaceAll(WHITESPACE_REGEX, SPLIT_DELIMITER);
            String[] utecArray = value.split(SPLIT_DELIMITER);
            return utecArray;
        }
        catch (Exception e) {
            return new String[]{};
        }
    }

    void SetDataItemValues(String[] values) {
        for (TxsDataItem dataItem : dataItems)
        {
            if(dataItem != null)
            {
                //Set value to dataItem
                if(values.length <= dataItem.getItemIndex())
                {
                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace("TXS DataItem: " + dataItem.getName() +  " Index requested: " + dataItem.getItemIndex() + " TXS data size: " + values.length);
                    dataItem.setData(0);
                }
                else
                {
                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace("TXS Setting DataItem: " + dataItem.getName());
                    dataItem.setData(parseDouble(values[dataItem.getItemIndex()]));
                }
            }
        }
    }

    private double parseDouble(String value) {
        try {
            //try to parse value.
            return Double.parseDouble(value);
        }
        catch (Exception e) {
            //return 0 if value could not be parsed.
            return 0.0;
        }
    }
}
