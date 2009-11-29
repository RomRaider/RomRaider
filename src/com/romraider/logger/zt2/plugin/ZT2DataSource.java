/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
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

package com.romraider.logger.zt2.plugin;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.ecu.external.ExternalDataSource;
import com.romraider.logger.zt2.io.ZT2Runner;
import com.romraider.logger.zt2.io.ZT2RunnerImpl;
import com.romraider.logger.zt2.io.ZT2SensorType;
import static com.romraider.logger.zt2.io.ZT2SensorType.AFR;
import static com.romraider.logger.zt2.io.ZT2SensorType.EGT;
import static com.romraider.logger.zt2.io.ZT2SensorType.MAP;
import static com.romraider.logger.zt2.io.ZT2SensorType.RPM;
import static com.romraider.logger.zt2.io.ZT2SensorType.TPS;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static java.util.Collections.unmodifiableList;
import javax.swing.Action;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ZT2DataSource implements ExternalDataSource {
    private final Map<ZT2SensorType, ZT2DataItem> dataItems = new HashMap<ZT2SensorType, ZT2DataItem>();
    private ZT2Runner runner;
    private String port;

    {
        dataItems.put(AFR, new ZT2DataItemImpl("Wideband AFR", "AFR", AFR));
        dataItems.put(TPS, new ZT2DataItemImpl("TPS", "Percent", TPS));
        dataItems.put(RPM, new ZT2DataItemImpl("RPM", "RPM", RPM));
        dataItems.put(MAP, new ZT2DataItemImpl("MAP", "Vacuum(inHg)/Boost(PSI)", MAP));
        dataItems.put(EGT, new ZT2DataItemImpl("EGT", "Celsius", EGT));
    }

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "Zeitronix ZT-2";
    }

    public String getVersion() {
        return "0.01";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return unmodifiableList(new ArrayList<ZT2DataItem>(dataItems.values()));
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
        runner = new ZT2RunnerImpl(port, dataItems);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }
}
