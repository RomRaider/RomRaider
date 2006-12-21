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

package enginuity.logger.comms.io.connection;

import enginuity.io.connection.SerialConnection;
import enginuity.io.connection.SerialConnectionImpl;
import enginuity.logger.comms.io.protocol.LoggerProtocol;
import enginuity.logger.comms.io.protocol.SSMLoggerProtocol;
import enginuity.logger.comms.query.RegisteredQuery;
import enginuity.logger.exception.SerialCommunicationException;
import static enginuity.util.HexUtil.asHex;
import static enginuity.util.ThreadUtil.sleep;

import java.util.Collection;

public final class SSMLoggerConnection implements LoggerConnection {
    private LoggerProtocol protocol;
    private SerialConnection serialConnection;

    public SSMLoggerConnection(String portName) {
        protocol = new SSMLoggerProtocol();
        serialConnection = new SerialConnectionImpl(protocol.getConnectionProperties(), portName);
    }

    public void sendAddressReads(Collection<RegisteredQuery> queries) {
        try {
            byte[] request = protocol.constructReadAddressRequest(queries);
            byte[] response = protocol.constructReadAddressResponse(queries);

            //System.out.println("Raw request        = " + asHex(request));

            serialConnection.readStaleData();
            serialConnection.write(request);
            int timeout = 1000;
            while (serialConnection.available() < response.length) {
                sleep(1);
                timeout -= 1;
                if (timeout <= 0) {
                    byte[] badBytes = new byte[serialConnection.available()];
                    serialConnection.read(badBytes);
                    System.out.println("Bad response (read timeout): " + asHex(badBytes));
                    break;
                }
            }
            serialConnection.read(response);

            //System.out.println("Raw response       = " + asHex(response));

            byte[] filteredResponse = new byte[response.length - request.length];
            System.arraycopy(response, request.length, filteredResponse, 0, filteredResponse.length);

            //System.out.println("Filtered response  = " + asHex(filteredResponse));
            //System.out.println();

            protocol.processReadAddressResponses(queries, filteredResponse);
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
    }

    public void close() {
        serialConnection.close();
    }

}
