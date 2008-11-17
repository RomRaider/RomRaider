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
import com.romraider.logger.ecu.ui.swing.menubar.action.GenericPluginMenuAction;
import static com.romraider.util.ParamChecker.checkNotNull;
import com.romraider.util.Stoppable;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import javax.swing.Action;
import java.util.ArrayList;
import java.util.List;

public final class GenericDataSourceManager implements ExternalDataSource {
    private static final Logger LOGGER = getLogger(GenericDataSourceManager.class);
    private final List<Stoppable> connectors = new ArrayList<Stoppable>();
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
        return new GenericPluginMenuAction(logger, this);
    }

    public synchronized void setPort(String port) {
        String old = getPort();
        if (port == old || port != null && port.equals(old)) return;
        LOGGER.info(dataSource.getName() + ": port " + port + " selected");
        reconnect(port);
    }

    public String getPort() {
        return dataSource.getPort();
    }

    public synchronized void connect() {
        if (connectCount++ == 0) doConnect();
        LOGGER.trace("Connect count [" + dataSource.getName() + "]: " + connectCount);
    }

    public synchronized void disconnect() {
        if (connectCount-- == 1) doDisconnect();
        if (connectCount < 0) connectCount = 0;
        LOGGER.trace("Connect count [" + dataSource.getName() + "]: " + connectCount);
    }

    private void doConnect() {
        Stoppable connector = new GenericDataSourceConnector(dataSource);
        connectors.add(connector);
        runAsDaemon(connector);
    }

    private void doDisconnect() {
        try {
            LOGGER.info(dataSource.getName() + ": disconnecting");
            while (!connectors.isEmpty()) connectors.remove(0).stop();
            dataSource.disconnect();
        } catch (Exception e) {
            LOGGER.error("External Datasource [" + dataSource.getName() + "] disconnect error", e);
        }
    }

    private void reconnect(String port) {
        dataSource.setPort(port);
        doDisconnect();
        doConnect();
    }
}
