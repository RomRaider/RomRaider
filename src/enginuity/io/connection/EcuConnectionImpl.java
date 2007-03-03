/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.io.connection;

import enginuity.logger.ecu.exception.SerialCommunicationException;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;
import static enginuity.util.ThreadUtil.sleep;

public final class EcuConnectionImpl implements EcuConnection {
    private final long sendTimeout;
    private final SerialConnection serialConnection;

    public EcuConnectionImpl(ConnectionProperties connectionProperties, String portName) {
        checkNotNull(connectionProperties, "connectionProperties");
        checkNotNullOrEmpty(portName, "portName");
        this.sendTimeout = connectionProperties.getSendTimeout();

        // Use TestSSMConnectionImpl for testing!!
        serialConnection = new SerialConnectionImpl(connectionProperties, portName);
//        serialConnection = new TestSSMConnectionImpl(connectionProperties, portName);
    }

    public byte[] send(byte[] bytes) {
        checkNotNull(bytes, "bytes");
        try {
            serialConnection.readStaleData();
            serialConnection.write(bytes);
            boolean keepLooking = true;
            int available = 0;
            long lastChange = System.currentTimeMillis();
            while (keepLooking) {
                sleep(5);
                if (serialConnection.available() != available) {
                    available = serialConnection.available();
                    lastChange = System.currentTimeMillis();
                }
                keepLooking = ((System.currentTimeMillis() - lastChange) < sendTimeout);
            }
            return serialConnection.readAvailable();
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
    }

    public void close() {
        serialConnection.close();
    }
}
