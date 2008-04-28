package com.romraider.logger.ecu.ui.handler.maf;

import com.romraider.logger.ecu.comms.query.Response;
import com.romraider.logger.ecu.definition.LoggerData;
import com.romraider.logger.ecu.ui.handler.DataUpdateHandler;
import com.romraider.logger.ecu.ui.tab.maf.MafTab;
import org.apache.log4j.Logger;
import javax.swing.SwingUtilities;
import static java.lang.Math.abs;
import static java.lang.System.currentTimeMillis;
import java.util.Set;

public final class MafUpdateHandler implements DataUpdateHandler {
    private static final Logger LOGGER = Logger.getLogger(MafUpdateHandler.class);
    private static final String MAFV = "P18";
    private static final String AF_LEARNING_1 = "P4";
    private static final String AF_CORRECTION_1 = "P3";
    private MafTab mafTab;
    private double lastMafv;
    private long lastUpdate;

    public synchronized void registerData(LoggerData loggerData) {
    }

    public synchronized void handleDataUpdate(Response response) {
        if (mafTab.isRecordData() && containsData(response, MAFV, AF_LEARNING_1, AF_CORRECTION_1)) {
            boolean valid = true;

            // cl/ol check
            if ((containsData(response, "E3") || containsData(response, "E33"))) {
                double clOl = -1;
                if (containsData(response, "E3")) {
                    clOl = (int) findValue(response, "E3");
                    LOGGER.trace("MAF:[CL/OL:E3]:  " + clOl);
                }
                if (containsData(response, "E33")) {
                    clOl = (int) findValue(response, "E33");
                    LOGGER.trace("MAF:[CL/OL:E33]: " + clOl);
                }
                valid = mafTab.isValidClOl(clOl);
                LOGGER.trace("MAF:[CL/OL]:     " + valid);
            }

            // afr check
            if (valid && containsData(response, "P58")) {
                double afr = findValue(response, "P58");
                LOGGER.trace("MAF:[AFR:P58]: " + afr);
                valid = mafTab.isValidAfr(afr);
                LOGGER.trace("MAF:[AFR]:     " + valid);
            }

            // rpm check
            if (valid && containsData(response, "P8")) {
                double rpm = findValue(response, "P8");
                LOGGER.trace("MAF:[RPM:P8]: " + rpm);
                valid = mafTab.isValidRpm(rpm);
                LOGGER.trace("MAF:[RPM]:    " + valid);
            }

            // maf check
            if (valid && containsData(response, "P12")) {
                double maf = findValue(response, "P12");
                LOGGER.trace("MAF:[MAF:P12]: " + maf);
                valid = mafTab.isValidMaf(maf);
                LOGGER.trace("MAF:[MAF]:     " + valid);
            }

            // intake air temp check
            if (valid && containsData(response, "P11")) {
                double temp = findValue(response, "P11");
                LOGGER.trace("MAF:[IAT:P11]: " + temp);
                valid = mafTab.isValidIntakeAirTemp(temp);
                LOGGER.trace("MAF:[IAT]:     " + valid);
            }

            // coolant temp check
            if (valid && containsData(response, "P2")) {
                double temp = findValue(response, "P2");
                LOGGER.trace("MAF:[CT:P2]: " + temp);
                valid = mafTab.isValidCoolantTemp(temp);
                LOGGER.trace("MAF:[CT]:    " + valid);
            }

            // dMAFv/dt check
            if (valid && containsData(response, "P18")) {
                double mafv = findValue(response, "P18");
                long now = currentTimeMillis();
                double mafvChange = abs((mafv - lastMafv) / (now - lastUpdate) * 1000);
                LOGGER.trace("MAF:[dMAFv/dt]: " + mafvChange);
                valid = mafTab.isValidMafvChange(mafvChange);
                LOGGER.trace("MAF:[dMAFv/dt]: " + valid);
                lastMafv = mafv;
                lastUpdate = now;
            }

            // tip-in throttle check
            if (valid && (containsData(response, "E23") || containsData(response, "E54"))) {
                double tipIn = -1;
                if (containsData(response, "E23")) {
                    tipIn = findValue(response, "E23");
                    LOGGER.trace("MAF:[TIP:E23]: " + tipIn);
                }
                if (containsData(response, "E54")) {
                    tipIn = findValue(response, "E54");
                    LOGGER.trace("MAF:[TIP:E54]: " + tipIn);
                }
                valid = mafTab.isValidTipInThrottle(tipIn);
                LOGGER.trace("MAF:[TIP]:     " + valid);
            }

            if (valid) {
                final double mafv = findValue(response, MAFV);
                final double learning = findValue(response, AF_LEARNING_1);
                final double correction = findValue(response, AF_CORRECTION_1);
                LOGGER.trace("MAF Data: " + mafv + "v, " + correction + "%");
                SwingUtilities.invokeLater(new Runnable() {
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

    public synchronized void deregisterData(LoggerData loggerData) {
    }

    public synchronized void cleanUp() {
    }

    public synchronized void reset() {
    }

    public void setMafTab(MafTab mafTab) {
        this.mafTab = mafTab;
    }
}
