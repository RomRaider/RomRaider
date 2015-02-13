/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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

package com.romraider.logger.external.plx.plugin;

import static com.romraider.logger.external.core.SensorConversionsOther.AIR_ABS_KPA;
import static com.romraider.logger.external.core.SensorConversionsOther.AIR_ABS_KPA2BAR;
import static com.romraider.logger.external.core.SensorConversionsOther.AIR_ABS_KPA2KGCM2;
import static com.romraider.logger.external.core.SensorConversionsOther.AIR_ABS_KPA2PSI;
import static com.romraider.logger.external.core.SensorConversionsOther.AIR_DEG_C;
import static com.romraider.logger.external.core.SensorConversionsOther.AIR_DEG_C2F;
import static com.romraider.logger.external.core.SensorConversionsOther.EXHAUST_DEG_C;
import static com.romraider.logger.external.core.SensorConversionsOther.EXHAUST_DEG_C2F;
import static com.romraider.logger.external.core.SensorConversionsOther.FLUID_DEG_C;
import static com.romraider.logger.external.core.SensorConversionsOther.FLUID_DEG_C2F;
import static com.romraider.logger.external.core.SensorConversionsOther.PERCENT;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.AFR_146;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.AFR_147;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.AFR_155;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.AFR_172;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.AFR_34;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.AFR_64;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.AFR_90;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.BATTERY;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.BOOST_BAR;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.BOOST_KGCM2;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.BOOST_KPA;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.BOOST_PSI;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.DC_NEG;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.DC_POS;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.DEGREES;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.FLUID_BAR;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.FLUID_KGCM2;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.FLUID_KPA;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.FLUID_PSI;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.FUEL_TRIM;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.KNOCK_VDC;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.KPH;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.LAMBDA;
import static com.romraider.logger.external.core.SensorConversionsOther.MAF_GS;
import static com.romraider.logger.external.core.SensorConversionsOther.MAF_GS2LB;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.MPH;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.NB_P;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.NB_V;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.RPM;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.VACUUM_IN;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.VACUUM_MM;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.WB_HEALTH;
import static com.romraider.logger.external.plx.plugin.PlxSensorConversions.WB_REACT;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.AIR_INTAKE_TEMPERATURE;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.BOOST;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.DUTY_CYCLE;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.ENGINE_LOAD;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.ENGINE_SPEED;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.EXHAUST_GAS_TEMPERATURE;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.FLUID_PRESSURE;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.FLUID_TEMPERATURE;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.FUEL_LEVEL;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.KNOCK;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.LONG_TERM_FUEL_TRIM;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.MANIFOLD_ABSOLUTE_PRESSURE;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.MASS_AIR_FLOW;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.NARROWBAND_AFR;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.SHORT_TERM_FUEL_TRIM;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.THROTTLE_POSITION;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.TIMING;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.VACUUM;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.VEHICLE_SPEED;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.VOLTAGE;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.WIDEBAND;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.WIDEBAND_HEALTH;
import static com.romraider.logger.external.plx.plugin.PlxSensorType.WIDEBAND_REACTION;
import static com.romraider.util.ThreadUtil.runAsDaemon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Action;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.logger.external.plx.io.PlxRunner;

public final class PlxDataSource implements ExternalDataSource {
    private final Map<PlxSensorType, PlxDataItem> dataItems = new HashMap<PlxSensorType, PlxDataItem>();
    private PlxRunner runner;
    private String port;

    {
        dataItems.put(WIDEBAND, new PlxDataItemImpl("O2 - Wideband", 0, AFR_147, LAMBDA, AFR_90, AFR_146, AFR_64, AFR_155, AFR_172, AFR_34));
        dataItems.put(EXHAUST_GAS_TEMPERATURE, new PlxDataItemImpl("Temperature - Exhaust Gas", 0, EXHAUST_DEG_C, EXHAUST_DEG_C2F));
        dataItems.put(FLUID_TEMPERATURE, new PlxDataItemImpl("Temperature - Oil/H20", 0, FLUID_DEG_C, FLUID_DEG_C2F));
        dataItems.put(VACUUM, new PlxDataItemImpl("Manifold Vaccum", 0, VACUUM_IN, VACUUM_MM)); 
        dataItems.put(BOOST, new PlxDataItemImpl("Manifold Boost", 0, BOOST_PSI, BOOST_BAR, BOOST_KPA, BOOST_KGCM2));
        dataItems.put(AIR_INTAKE_TEMPERATURE, new PlxDataItemImpl("Temperature - Intake Air", 0, AIR_DEG_C, AIR_DEG_C2F));
        dataItems.put(ENGINE_SPEED, new PlxDataItemImpl("Engine Speed", 0, RPM));
        dataItems.put(VEHICLE_SPEED, new PlxDataItemImpl("Vehicle Speed", 0, MPH, KPH));
        dataItems.put(THROTTLE_POSITION, new PlxDataItemImpl("Throttle Position", 0, PERCENT));
        dataItems.put(ENGINE_LOAD, new PlxDataItemImpl("Engine Load", 0, PERCENT));
        dataItems.put(FLUID_PRESSURE, new PlxDataItemImpl("Fuel/0il Pressure", 0, FLUID_PSI, FLUID_BAR, FLUID_KPA, FLUID_KGCM2));
        dataItems.put(TIMING, new PlxDataItemImpl("Engine Timing", 0, DEGREES));
        dataItems.put(MANIFOLD_ABSOLUTE_PRESSURE, new PlxDataItemImpl("Manifold Absolute Pressure", 0, AIR_ABS_KPA2PSI, AIR_ABS_KPA2BAR, AIR_ABS_KPA, AIR_ABS_KPA2KGCM2));
        dataItems.put(MASS_AIR_FLOW, new PlxDataItemImpl("Mass Air Flow", 0, MAF_GS, MAF_GS2LB));
        dataItems.put(SHORT_TERM_FUEL_TRIM, new PlxDataItemImpl("Fuel Trim - Short Term", 0, FUEL_TRIM));
        dataItems.put(LONG_TERM_FUEL_TRIM, new PlxDataItemImpl("Fuel Trim - Long Term", 0, FUEL_TRIM));
        dataItems.put(NARROWBAND_AFR, new PlxDataItemImpl("O2 - Narrowband", 0, NB_P, NB_V));
        dataItems.put(FUEL_LEVEL, new PlxDataItemImpl("Fuel Level", 0, PERCENT));
        dataItems.put(VOLTAGE, new PlxDataItemImpl("Battery Voltage", 0, BATTERY));
        dataItems.put(KNOCK, new PlxDataItemImpl("Knock", 0, KNOCK_VDC));
        dataItems.put(DUTY_CYCLE, new PlxDataItemImpl("Duty Cycle", 0, DC_POS, DC_NEG));
        dataItems.put(WIDEBAND_HEALTH, new PlxDataItemImpl("Wideband Health", 0, WB_HEALTH));
        dataItems.put(WIDEBAND_REACTION, new PlxDataItemImpl("Wideband Reaction Time", 0, WB_REACT));
    }

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "PLX SM-AFR";
    }

    public String getVersion() {
        return "0.05";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return new ArrayList<ExternalDataItem>(dataItems.values());
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

    public void setProperties(Properties properties) {
    }

    public void connect() {
        runner = new PlxRunner(port, dataItems);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }
}