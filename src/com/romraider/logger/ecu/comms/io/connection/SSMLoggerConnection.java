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

package com.romraider.logger.ecu.comms.io.connection;

import com.romraider.io.connection.ConnectionManager;
import com.romraider.logger.ecu.comms.io.protocol.LoggerProtocol;
import com.romraider.logger.ecu.comms.io.protocol.SSMLoggerProtocol;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import java.util.Collection;

public final class SSMLoggerConnection implements LoggerConnection {
    private static final Logger LOGGER = getLogger(SSMLoggerConnection.class);
    private static final long SEND_TIMEOUT = 2000L;
    private final LoggerProtocol protocol = new SSMLoggerProtocol();
    private final ConnectionManager manager;

    public SSMLoggerConnection(ConnectionManager manager) {
        checkNotNull(manager, "manager");
        this.manager = manager;
    }

    public void ecuReset() {
        byte[] request = protocol.constructEcuResetRequest();
        LOGGER.debug("Ecu Reset Request  ---> " + asHex(request));
        byte[] response = manager.send(request, SEND_TIMEOUT);
        byte[] processedResponse = protocol.preprocessResponse(request, response);
        LOGGER.debug("Ecu Reset Response <--- " + asHex(processedResponse));
        protocol.processEcuResetResponse(processedResponse);
    }

    public void ecuInit(EcuInitCallback callback) {
        byte[] request = protocol.constructEcuInitRequest();
        LOGGER.debug("Ecu Init Request  ---> " + asHex(request));
        byte[] response = manager.send(request, SEND_TIMEOUT);
        byte[] processedResponse = protocol.preprocessResponse(request, response);
        LOGGER.debug("Ecu Init Response <--- " + asHex(processedResponse));
        protocol.processEcuInitResponse(callback, processedResponse);
    }

    public void sendAddressReads(Collection<EcuQuery> queries) {
        byte[] request = protocol.constructReadAddressRequest(queries);
        byte[] response = protocol.constructReadAddressResponse(queries);
        LOGGER.trace("ECU Request  ---> " + asHex(request));
        manager.send(request, response, SEND_TIMEOUT);
        byte[] processedResponse = protocol.preprocessResponse(request, response);
        LOGGER.trace("ECU Response <--- " + asHex(processedResponse));
        protocol.processReadAddressResponses(queries, processedResponse);
    }

    public void close() {
        manager.close();
    }

}
