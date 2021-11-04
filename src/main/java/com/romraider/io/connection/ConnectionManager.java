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

package com.romraider.io.connection;

import com.romraider.logger.ecu.comms.manager.PollingState;

public interface ConnectionManager {
    /**
     * Use the open method when communications to a Module requires a
     * StartCommunication sequence, such as Five Baud or Fast Init.
     * Include the StopCommunication sequence in the open so that
     * if something fails, the stop sequence is pre-loaded for use.
     * @param start - the byte sequence used to start comms
     * @param stop - the byte sequence used to stop comss
     */
    void open(byte[] start, byte[] stop);

    /**
     * Use this send method to send a request to a Module and return the
     * Module's reply in response.  Polling state can be slow or fast as
     * provided by the PollingState parameter.
     * @param request - the bytes to send to the Module
     * @param response - a byte array sized to contain the Module's response
     * @param pollState - polling state, State_0 (slow) or State_1 (fast)
     */
    void send(byte[] request, byte[] response, PollingState pollState);

    /**
     * Use this send method to send bytes to a Module and return the
     * Module's reply.
     * @param bytes - the bytes to send to the Module
     * @return A byte array of the Module's response, variable sized
     */
    byte[] send(byte[] bytes);

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
}
