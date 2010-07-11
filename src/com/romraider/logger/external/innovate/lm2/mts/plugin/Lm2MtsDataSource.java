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

package com.romraider.logger.external.innovate.lm2.mts.plugin;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.logger.external.innovate.generic.mts.io.MTSRunner;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import javax.swing.Action;
import java.util.List;

public final class Lm2MtsDataSource implements ExternalDataSource {
    private static final Logger LOGGER = getLogger(Lm2MtsDataSource.class);
    private final Lm2MtsDataItem dataItem = new Lm2MtsDataItem();
    private MTSRunner runner;
    private int mtsPort = -1;

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "Innovate LM-2 [mts]";
    }

    public String getVersion() {
        return "0.03";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return asList(dataItem);
    }

    public Action getMenuAction(EcuLogger logger) {
        return new Lm2MtsPluginMenuAction(logger, this);
    }

    public void setPort(String port) {
        mtsPort = mtsPort(port);
    }

    public String getPort() {
        return "" + mtsPort;
    }

    public void connect() {
        runner = new MTSRunner(mtsPort, dataItem);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }

    private int mtsPort(String port) {
        try {
            return parseInt(port);
        } catch (Exception e) {
            LOGGER.warn("Bad MTS port: " + port);
            return -1;
        }
    }
}