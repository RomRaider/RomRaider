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

package com.romraider.logger.ecu.comms.manager;

import com.romraider.Settings;
import com.romraider.logger.ecu.comms.io.connection.LoggerConnection;
import static com.romraider.logger.ecu.comms.io.connection.LoggerConnectionFactory.getConnection;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.exception.NotConnectedException;
import static com.romraider.util.ParamChecker.checkNotNull;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import java.util.Collection;

public final class TransmissionManagerImpl implements TransmissionManager {
    private static final Logger LOGGER = getLogger(TransmissionManagerImpl.class);
    private final Settings settings;
    private LoggerConnection connection;

    public TransmissionManagerImpl(Settings settings) {
        checkNotNull(settings, "settings");
        this.settings = settings;
    }

    public void start() {
        try {
            connection = getConnection(settings.getLoggerProtocol(), settings.getLoggerPort(), settings.getLoggerConnectionProperties());
            LOGGER.info("TX Manager Started.");
        } catch (Throwable e) {
            stop();
        }
    }

    public void sendQueries(Collection<EcuQuery> queries, PollingState pollState) {
        checkNotNull(queries, "queries");
        checkNotNull(pollState, "pollState");
        if (connection == null) throw new NotConnectedException("TransmissionManager must be started before queries can be sent!");
        connection.sendAddressReads(queries, settings.getDestinationId(), pollState);
    }

    public void endQueries() {
        if (connection == null) throw new NotConnectedException("TransmissionManager must be started before ending queries!");
        connection.clearLine();
    }

    public void stop() {
        if (connection != null) {
        	endQueries();
        	connection.close();
        }
        LOGGER.info("TX Manager Stopped.");
    }
}
