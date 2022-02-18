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

package com.romraider.logger.ecu.ui.handler.maf;

import com.romraider.logger.ecu.comms.query.Response;
import com.romraider.logger.ecu.definition.LoggerData;
import com.romraider.logger.ecu.ui.handler.DataUpdateHandler;
import com.romraider.logger.ecu.ui.tab.maf.MafTab;
import static java.lang.Math.abs;
import static java.lang.System.currentTimeMillis;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import javax.swing.SwingUtilities;
import java.util.Set;

public final class MafUpdateHandler implements DataUpdateHandler {
    private static final Logger LOGGER = getLogger(MafUpdateHandler.class);
    private static final String MAFV = "P18";
    private static final String AF_LEARNING_1 = "P4";
    private static final String AF_CORRECTION_1 = "P3";
    private MafTab mafTab;
    private double lastMafv;
    private long lastUpdate;

    @Override
    public synchronized void registerData(LoggerData loggerData) {
    }

    @Override
    public synchronized void handleDataUpdate(Response response) {
        if (mafTab!= null && mafTab.isRecordData() && containsData(response, MAFV, AF_LEARNING_1, AF_CORRECTION_1)) {
            boolean valid = true;

            // cl/ol check
            if ((containsData(response, "E3") || containsData(response, "E33"))) {
                double clOl = -1;
                if (containsData(response, "E3")) {
                    clOl = (int) findValue(response, "E3");
                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace("MAF:[CL/OL:E3]:  " + clOl);
                }
                if (containsData(response, "E33")) {
                    clOl = (int) findValue(response, "E33");
                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace("MAF:[CL/OL:E33]: " + clOl);
                }
                valid = mafTab.isValidClOl(clOl);
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MAF:[CL/OL]:     " + valid);
            }

            // afr check
            if (valid && containsData(response, "P58")) {
                double afr = findValue(response, "P58");
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MAF:[AFR:P58]: " + afr);
                valid = mafTab.isValidAfr(afr);
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MAF:[AFR]:     " + valid);
            }

            // rpm check
            if (valid && containsData(response, "P8")) {
                double rpm = findValue(response, "P8");
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MAF:[RPM:P8]: " + rpm);
                valid = mafTab.isValidRpm(rpm);
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MAF:[RPM]:    " + valid);
            }

            // maf check
            if (valid && containsData(response, "P12")) {
                double maf = findValue(response, "P12");
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MAF:[MAF:P12]: " + maf);
                valid = mafTab.isValidMaf(maf);
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MAF:[MAF]:     " + valid);
            }

            // intake air temp check
            if (valid && containsData(response, "P11")) {
                double temp = findValue(response, "P11");
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MAF:[IAT:P11]: " + temp);
                valid = mafTab.isValidIntakeAirTemp(temp);
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MAF:[IAT]:     " + valid);
            }

            // coolant temp check
            if (valid && containsData(response, "P2")) {
                double temp = findValue(response, "P2");
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MAF:[CT:P2]: " + temp);
                valid = mafTab.isValidCoolantTemp(temp);
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MAF:[CT]:    " + valid);
            }

            // dMAFv/dt check
            if (valid && containsData(response, "P18")) {
                double mafv = findValue(response, "P18");
                long now = currentTimeMillis();
                double mafvChange = abs((mafv - lastMafv) / (now - lastUpdate) * 1000);
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MAF:[dMAFv/dt]: " + mafvChange);
                valid = mafTab.isValidMafvChange(mafvChange);
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MAF:[dMAFv/dt]: " + valid);
                lastMafv = mafv;
                lastUpdate = now;
            }

            // tip-in throttle check
            if (valid && (containsData(response, "E23") || containsData(response, "E54"))) {
                double tipIn = -1;
                if (containsData(response, "E23")) {
                    tipIn = findValue(response, "E23");
                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace("MAF:[TIP:E23]: " + tipIn);
                }
                if (containsData(response, "E54")) {
                    tipIn = findValue(response, "E54");
                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace("MAF:[TIP:E54]: " + tipIn);
                }
                valid = mafTab.isValidTipInThrottle(tipIn);
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MAF:[TIP]:     " + valid);
            }

            if (valid) {
                final double mafv = findValue(response, MAFV);
                final double learning = findValue(response, AF_LEARNING_1);
                final double correction = findValue(response, AF_CORRECTION_1);
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MAF Data: " + mafv + "v, " + correction + "%");
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        mafTab.addData(mafv, learning + correction);
                    }
                });
            }
        }
    }

    private boolean containsData(Response response, String... ids) {
        Set<LoggerData> datas = response.getData();
        for (String id : ids) {
            boolean found = false;
            for (LoggerData data : datas) {
                if (data.getId().equals(id)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private double findValue(Response response, String id) {
        for (LoggerData loggerData : response.getData()) {
            if (id.equals(loggerData.getId())) {
                return response.getDataValue(loggerData);
            }
        }
        throw new IllegalStateException("Expected data item " + id + " not in response.");
    }

    @Override
    public synchronized void deregisterData(LoggerData loggerData) {
    }

    @Override
    public synchronized void cleanUp() {
    }

    @Override
    public synchronized void reset() {
    }

    public void setMafTab(MafTab mafTab) {
        this.mafTab = mafTab;
    }
}
