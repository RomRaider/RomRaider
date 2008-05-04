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

package com.romraider.logger.ecu.external;

import com.romraider.logger.ecu.EcuLogger;
import static com.romraider.util.ParamChecker.checkNotNull;
import org.apache.log4j.Logger;
import javax.swing.Action;
import java.util.List;

public final class GenericDataSourceManager implements ExternalDataSource {
    private static final Logger LOGGER = Logger.getLogger(GenericDataSourceManager.class);
    private final ExternalDataSource dataSource;
    private int connectCount;

    public GenericDataSourceManager(ExternalDataSource dataSource) {
        checkNotNull(dataSource, "dataSource");
        this.dataSource = dataSource;
    }

    public String getName() {
        return dataSource.getName();
    }

    public String getVersion() {
        return dataSource.getVersion();
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return dataSource.getDataItems();
    }

    public Action getMenuAction(EcuLogger logger) {
        return dataSource.getMenuAction(logger);
    }

    public void setPort(String port) {
        LOGGER.info(dataSource.getName() + ": port " + port + " selected");
        dataSource.setPort(port);
        reconnect();
    }

    public String getPort() {
        return dataSource.getPort();
    }

    public synchronized void connect() {
        try {
            if (connectCount == 0) {
                LOGGER.info(dataSource.getName() + ": connecting");
                dataSource.connect();
            }
            connectCount++;
            LOGGER.trace("Connect count [" + dataSource.getName() + "]: " + connectCount);
        } catch (Exception e) {
            LOGGER.error("External Datasource [" + dataSource.getName() + "] connect error", e);
        }
    }

    public synchronized void disconnect() {
        try {
            if (connectCount == 1) {
                LOGGER.info(dataSource.getName() + ": disconnecting");
                dataSource.disconnect();
            }
            connectCount = connectCount > 0 ? connectCount - 1 : 0;
            LOGGER.trace("Connect count [" + dataSource.getName() + "]: " + connectCount);
        } catch (Exception e) {
            LOGGER.error("External Datasource [" + dataSource.getName() + "] disconnect error", e);
        }
    }

    private synchronized void reconnect() {
        disconnect();
        connect();
    }
}
