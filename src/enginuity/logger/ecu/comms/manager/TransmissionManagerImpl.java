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

package enginuity.logger.ecu.comms.manager;

import enginuity.Settings;
import enginuity.logger.ecu.comms.io.connection.LoggerConnection;
import enginuity.logger.ecu.comms.io.connection.LoggerConnectionFactory;
import enginuity.logger.ecu.comms.query.EcuQuery;
import enginuity.logger.ecu.exception.NotConnectedException;
import enginuity.logger.ecu.exception.SerialCommunicationException;
import static enginuity.util.ParamChecker.checkNotNull;

import java.util.Collection;

public final class TransmissionManagerImpl implements TransmissionManager {
    private final Settings settings;
    private LoggerConnection connection;

    public TransmissionManagerImpl(Settings settings) {
        checkNotNull(settings, "settings");
        this.settings = settings;
    }

    public void start() {
        try {
            connection = LoggerConnectionFactory.getInstance().getLoggerConnection(settings.getLoggerProtocol(), settings.getLoggerPort(),
                    settings.getLoggerConnectionProperties());
            System.out.println("Connected to: " + settings.getLoggerPort() + "; using protocol: " + settings.getLoggerProtocol() + "; conn props: "
                    + settings.getLoggerConnectionProperties());
        } catch (Throwable e) {
            stop();
            throw new SerialCommunicationException("Unable to connect to port: " + settings.getLoggerPort() + ", with protocol: "
                    + settings.getLoggerProtocol(), e);
        }
    }

    public void sendQueries(Collection<EcuQuery> queries) {
        checkNotNull(queries, "queries");
        if (connection != null) {
            connection.sendAddressReads(queries);
        } else {
            throw new NotConnectedException("TransmissionManager must be started before queries can be sent!");
        }
    }

    public void stop() {
        if (connection != null) {
            connection.close();
        }
        System.out.println("Disconnected.");
    }

}
