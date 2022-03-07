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

package com.romraider.io.serial.port;

import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.fazecast.jSerialComm.SerialPort;

public final class SerialPortDiscovererImpl implements SerialPortDiscoverer {
    private static final Logger LOGGER = getLogger(SerialPortDiscovererImpl.class);

    public List<SerialPort> listPorts() {
        final List<SerialPort> serialPortIdentifiers = new ArrayList<SerialPort>();
        
        try {
	        for (final SerialPort port : SerialPort.getCommPorts()) {
	        	serialPortIdentifiers.add(port);
	        }
        }
        catch(NoClassDefFoundError e) {
        	LOGGER.error("Could not load jSerialComm library!");
        }
        catch(UnsatisfiedLinkError e) {
        	LOGGER.error("Could not load jSerialComm library!");
        }
        
        return serialPortIdentifiers;
    }
}
