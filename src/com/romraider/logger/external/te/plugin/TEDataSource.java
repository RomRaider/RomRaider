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

package com.romraider.logger.external.te.plugin;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.logger.external.te.io.TERunner;
import static com.romraider.logger.external.te.plugin.TESensorType.AFR;
import static com.romraider.logger.external.te.plugin.TESensorType.Lambda;
import static com.romraider.logger.external.te.plugin.TESensorType.USR1;
import static com.romraider.logger.external.te.plugin.TESensorType.USR2;
import static com.romraider.logger.external.te.plugin.TESensorType.USR3;
import static com.romraider.logger.external.te.plugin.TESensorType.TC1;
import static com.romraider.logger.external.te.plugin.TESensorType.TC2;
import static com.romraider.logger.external.te.plugin.TESensorType.TC3;
import static com.romraider.logger.external.te.plugin.TESensorType.TorVss;
import static com.romraider.logger.external.te.plugin.TESensorType.RPM;
import static com.romraider.logger.external.te.plugin.TESensorUnits.WIDEBAND_AFR_LAMBDA;
import static com.romraider.logger.external.te.plugin.TESensorUnits.WIDEBAND_AFR_GASOLINE147;
import static com.romraider.logger.external.te.plugin.TESensorUnits.VDC;
import static com.romraider.logger.external.te.plugin.TESensorUnits.RAW;
import static com.romraider.logger.external.te.plugin.TESensorUnits.ENGINE_SPEED;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static java.util.Collections.unmodifiableList;
import javax.swing.Action;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TEDataSource implements ExternalDataSource {
    private final Map<TESensorType, TEDataItem> dataItems = new HashMap<TESensorType, TEDataItem>();
    private TERunner runner;
    private String port;

    {
        dataItems.put(Lambda, new TEDataItemImpl("Wideband", new String[]{"Lambda", "AFR"}, Lambda, WIDEBAND_AFR_LAMBDA));
        dataItems.put(AFR, new TEDataItemImpl("Wideband", new String[]{"AFR"}, Lambda, WIDEBAND_AFR_GASOLINE147));
        dataItems.put(USR1, new TEDataItemImpl("User 1", new String[]{"VDC"}, USR1, VDC));
        dataItems.put(USR2, new TEDataItemImpl("User 2", new String[]{"VDC"}, USR2, VDC));
        dataItems.put(USR3, new TEDataItemImpl("User 3", new String[]{"VDC"}, USR3, VDC));
        dataItems.put(TC1, new TEDataItemImpl("Thermocouple 1", new String[]{"raw"}, TC1, RAW));
        dataItems.put(TC2, new TEDataItemImpl("Thermocouple 2", new String[]{"raw"}, TC2, RAW));
        dataItems.put(TC3, new TEDataItemImpl("Thermocouple 3", new String[]{"raw"}, TC3, RAW));
        dataItems.put(TorVss, new TEDataItemImpl("Thermistor or Vss", new String[]{"raw"}, TorVss, RAW));
        dataItems.put(RPM, new TEDataItemImpl("Engine Speed (4-cyl)", new String[]{"RPM"}, RPM, ENGINE_SPEED));
    }

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "Tech Edge (Format 2.0)";
    }

    public String getVersion() {
        return "0.02";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return unmodifiableList(new ArrayList<TEDataItem>(dataItems.values()));
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
        runner = new TERunner(port, dataItems);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }
}
