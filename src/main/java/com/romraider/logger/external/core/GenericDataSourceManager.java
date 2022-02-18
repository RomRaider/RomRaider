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

package com.romraider.logger.external.core;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.ui.swing.menubar.action.GenericPluginMenuAction;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import javax.swing.Action;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class GenericDataSourceManager implements ExternalDataSource {
    private static final Logger LOGGER = getLogger(GenericDataSourceManager.class);
    private final List<Stoppable> connectors = new ArrayList<Stoppable>();
    private final ExternalDataSource dataSource;
    private int connectCount;

    public GenericDataSourceManager(ExternalDataSource dataSource) {
        checkNotNull(dataSource, "dataSource");
        this.dataSource = dataSource;
    }

    @Override
    public String getId() {
        return dataSource.getId();
    }

    @Override
    public String getName() {
        return dataSource.getName();
    }

    @Override
    public String getVersion() {
        return dataSource.getVersion();
    }

    @Override
    public List<? extends ExternalDataItem> getDataItems() {
        return dataSource.getDataItems();
    }

    @Override
    public Action getMenuAction(EcuLogger logger) {
        Action action = dataSource.getMenuAction(logger);
        return action == null ? new GenericPluginMenuAction(logger, this) : action;
    }

    @Override
    public synchronized void setPort(String port) {
        if (port == null || port.length() == 0) return;
        if (port.equals(getPort())) return;
        LOGGER.info(dataSource.getName() + ": port " + port + " selected");
        doDisconnect();
        dataSource.setPort(port);
    }

    @Override
    public String getPort() {
        return dataSource.getPort();
    }

    @Override
    public void setProperties(Properties properties) {
    }

    @Override
    public synchronized void connect() {
        if (connectCount++ == 0) doConnect();
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Connect count [" + dataSource.getName() + "]: " + connectCount);
    }

    @Override
    public synchronized void disconnect() {
        if (connectCount-- == 1) doDisconnect();
        if (connectCount < 0) connectCount = 0;
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Connect count [" + dataSource.getName() + "]: " + connectCount);
    }

    private void doConnect() {
        Stoppable connector = new GenericDataSourceConnector(dataSource);
        connectors.add(connector);
        runAsDaemon(connector);
    }

    private void doDisconnect() {
        try {
            String message = String.format("%s: disconnecting port %s",
                    dataSource.getName(), dataSource.getPort());
            LOGGER.info(message);
            while (!connectors.isEmpty()) connectors.remove(0).stop();
            dataSource.disconnect();
            message = String.format("%s: disconnected", dataSource.getName());
            LOGGER.info(message);
        } catch (Exception e) {
            LOGGER.error("External Datasource [" + dataSource.getName() +
                    "] disconnect error", e);
        }
    }

    private void reconnect(String port) {
        doDisconnect();
        dataSource.setPort(port);
        doConnect();
    }
}
