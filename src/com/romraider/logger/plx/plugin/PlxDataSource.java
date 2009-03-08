/*
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
 */

package com.romraider.logger.plx.plugin;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.ecu.external.ExternalDataSource;
import com.romraider.logger.plx.io.PlxRunnerImpl;
import com.romraider.logger.plx.io.PlxSensorType;
import static com.romraider.logger.plx.io.PlxSensorType.WIDEBAND_AFR;
import static com.romraider.logger.plx.io.PlxSensorUnits.WIDEBAND_AFR_GASOLINE147;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import javax.swing.Action;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PlxDataSource implements ExternalDataSource {
    private final Map<PlxSensorType, PlxDataItem> dataItems = new HashMap<PlxSensorType, PlxDataItem>();
    private final PlxSettings settings = new PlxSettingsImpl();
    private PlxRunnerImpl runner;

    {
        dataItems.put(WIDEBAND_AFR, new PlxDataItemImpl("Wideband AFR", "AFR", WIDEBAND_AFR, WIDEBAND_AFR_GASOLINE147));
//        dataItems.put(EXHAUST_GAS_TEMPERATURE, new PlxDataItemImpl("EGT", "C", EXHAUST_GAS_TEMPERATURE, EXHAUST_GAS_TEMPERATURE_CELSIUS));
    }

    public String getName() {
        return "PLX SM-AFR";
    }

    public String getVersion() {
        return "0.01";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return new ArrayList<ExternalDataItem>(dataItems.values());
    }

    public Action getMenuAction(EcuLogger logger) {
        throw new UnsupportedOperationException();
    }

    public void setPort(String port) {
        settings.setPort(port);
    }

    public String getPort() {
        return settings.getPort();
    }

    public void connect() {
        runner = new PlxRunnerImpl(settings, dataItems);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }
}