/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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

package com.romraider.logger.ecu.comms.io.connection;

import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.definition.Module;

import java.util.Collection;
import java.util.Map;

public interface LoggerConnection {

    /**
     * Use the open method when communications to a Module requires a
     * StartCommunication sequence, such as Five Baud or Fast Init.
     * @param module - the Module to open
     */
    void open(Module module);

    /**
     * Use this method to reset the module.
     * @param module - the Module to reset
     * @param resetCode - the reset procedure to activate
     */
    void ecuReset(Module module, int resetCode);

    /**
     * Use this method to get the identity the Module communicating with.
     * @param callback - callback which will identify the Module
     * @param module - the Module to identify
     */
    void ecuInit(EcuInitCallback callback, Module module);

    /**
     * Use this method to query the Module for the parameters included as queries. 
     * @param queries - the Collection of EcuQuery items
     * @param module - the Module to query
     * @param pollState - the PollingState to use
     */
    void sendAddressReads(Collection<EcuQuery> queries, Module module, PollingState pollState);

    /**
     * Use this method to clear the communications line of any erroneous data.
     * It can be called before closing off communications to clear buffers
     * of stale data, or when changing polling modes.
     */
    void clearLine();

    /**
     * Use this method to close communications with the Module.
     */
    void close();

    /**
     * Use this method to write to a Module. 
     * @param writeQueries - a Map of EcuQuery items to write
     * @param module - the Module to write to
     */
    void sendAddressWrites(Map<EcuQuery, byte[]> writeQueries, Module module);
}
