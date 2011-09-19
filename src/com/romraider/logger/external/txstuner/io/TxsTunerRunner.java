
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

package com.romraider.logger.external.txstuner.io;

import com.romraider.io.serial.connection.SerialConnection;
import com.romraider.io.serial.connection.SerialConnectionImpl;
import com.romraider.logger.external.txstuner.plugin.TxsTunerDataItem;
import com.romraider.logger.external.core.Stoppable;

import static com.romraider.util.ParamChecker.isNullOrEmpty;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class TxsTunerRunner implements Stoppable {
    private static final Logger LOGGER = getLogger(TxsTunerRunner.class);
    private static final TxsTunerConnectionProperties CONNECTION_PROPS = new TxsTunerConnectionProperties();
    private static final String TXS_TUNER = "t";
    private static final String TXS_LOGGER = "1";
    private static final String SPLIT_DELIMITER = " ";
    private final SerialConnection connection;
    private final TxsTunerDataItem dataItem;
    private boolean stop;

    public TxsTunerRunner(String port, TxsTunerDataItem dataItem) {
        this.connection = new SerialConnectionImpl(port, CONNECTION_PROPS);
        this.dataItem = dataItem;
    }

    public void run() {
        try {

        	byte[] tuner = TXS_TUNER.getBytes();
        	byte[] logger = TXS_LOGGER.getBytes();
            
            //Send T to switch to tuner if connected to utec.
            connection.write(tuner);
            String response = connection.readLine();
            LOGGER.trace("TXS Tuner Response: " + response);
            
            //Start TXS tuner logger
            connection.write(logger);
            response = connection.readLine();
            LOGGER.trace("TXS Tuner Response: " + response);
                        
            while (!stop) {
                response = connection.readLine();
                LOGGER.trace("TXS Tuner Response: " + response);
                if (!isNullOrEmpty(response)) dataItem.setData(parseString(response));
            }
            
        } catch (Throwable t) {
            LOGGER.error("Error occurred", t);
        } finally {
            connection.close();
        }
    }

    public void stop() {
        stop = true;
        connection.close();
    }

    private double parseString(String value){
    	try{
    		value = value.trim();
    		String[] substr = value.split(SPLIT_DELIMITER);
    		return Double.parseDouble(substr[0]);    
        }
    	catch (Exception e){
    		return 0.0;
        }
	}
}
