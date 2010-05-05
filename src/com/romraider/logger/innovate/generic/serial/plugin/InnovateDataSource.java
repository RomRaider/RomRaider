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

package com.romraider.logger.innovate.generic.serial.plugin;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.ecu.external.ExternalDataSource;
import com.romraider.logger.innovate.generic.serial.io.InnovateRunner;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static java.util.Arrays.asList;
import javax.swing.Action;
import java.util.List;

public final class InnovateDataSource implements ExternalDataSource {
    private InnovateDataItem dataItem = new InnovateDataItem();
    private InnovateRunner runner;
    private String port;

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "Innovate Wideband AFR";
    }

    public String getVersion() {
        return "0.01";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return asList(dataItem);
    }

    public Action getMenuAction(EcuLogger logger) {
        return null;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    public void connect() {
        runner = new InnovateRunner(port, dataItem);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }
}
