/*
 *
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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

package com.romraider.logger.ecu.comms.io.connection;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.connection.SerialConnectionManager;
import com.romraider.io.connection.SerialConnectionManagerImpl;
import com.romraider.logger.ecu.comms.io.protocol.LoggerProtocol;
import com.romraider.logger.ecu.comms.io.protocol.SSMLoggerProtocol;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import java.util.Collection;

public final class SSMLoggerConnection implements LoggerConnection {
    private static final long SEND_TIMEOUT = 2000L;
    private final LoggerProtocol protocol = new SSMLoggerProtocol();
    private final SerialConnectionManager manager;

    public SSMLoggerConnection(String portName, ConnectionProperties connectionProperties) {
        checkNotNullOrEmpty(portName, "portName");
        checkNotNull(connectionProperties);
        this.manager = new SerialConnectionManagerImpl(portName, connectionProperties);
    }

    public void sendAddressReads(Collection<EcuQuery> queries) {
        byte[] request = protocol.constructReadAddressRequest(queries);
        byte[] response = protocol.constructReadAddressResponse(queries);

        manager.send(request, response, SEND_TIMEOUT);

        byte[] processedResponse = protocol.preprocessResponse(request, response);

//            LOGGER.trace("ECU Request  ---> " + asHex(request));
//            LOGGER.trace("ECU Response <--- " + asHex(processedResponse));

        protocol.processReadAddressResponses(queries, processedResponse);
    }

    public void close() {
        manager.close();
    }

}
