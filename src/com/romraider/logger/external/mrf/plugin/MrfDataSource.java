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

package com.romraider.logger.external.mrf.plugin;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.logger.external.mrf.io.MrfRunner;
import static com.romraider.logger.external.mrf.plugin.MrfSensorType.AFR;
import static com.romraider.logger.external.mrf.plugin.MrfSensorType.EGT;
import static com.romraider.logger.external.mrf.plugin.MrfSensorType.FUEL_PRESS;
import static com.romraider.logger.external.mrf.plugin.MrfSensorType.MANIFOLD_TEMP;
import static com.romraider.logger.external.mrf.plugin.MrfSensorType.MAP;
import static com.romraider.logger.external.mrf.plugin.MrfSensorType.OIL_PRESS;
import static com.romraider.logger.external.mrf.plugin.MrfSensorType.OIL_TEMP;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static java.util.Collections.unmodifiableList;
import javax.swing.Action;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MrfDataSource implements ExternalDataSource {
    private final Map<MrfSensorType, MrfDataItem> dataItems = new HashMap<MrfSensorType, MrfDataItem>();
    private MrfRunner runner;
    private String port;

    {
        dataItems.put(AFR, new MrfDataItem("AFR", "AFR"));
        dataItems.put(MAP, new MrfDataItem("MAP", "Vacuum(inHg)/Boost(PSI)"));
        dataItems.put(EGT, new MrfDataItem("EGT", "F"));
        dataItems.put(OIL_TEMP, new MrfDataItem("Oil Temp", "F"));
        dataItems.put(OIL_PRESS, new MrfDataItem("Oil Press", "PSI"));
        dataItems.put(FUEL_PRESS, new MrfDataItem("Fuel Press", "PSI"));
        dataItems.put(MANIFOLD_TEMP, new MrfDataItem("Manifold Temp", "F"));
    }

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "MRF Stealth Gauge";
    }

    public String getVersion() {
        return "0.01";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return unmodifiableList(new ArrayList<MrfDataItem>(dataItems.values()));
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
        runner = new MrfRunner(port, dataItems);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }
}