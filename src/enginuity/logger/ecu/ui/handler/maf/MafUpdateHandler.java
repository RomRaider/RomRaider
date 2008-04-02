package enginuity.logger.ecu.ui.handler.maf;

import enginuity.logger.ecu.comms.query.Response;
import enginuity.logger.ecu.definition.LoggerData;
import enginuity.logger.ecu.ui.handler.DataUpdateHandler;
import enginuity.logger.ecu.ui.tab.maf.MafTab;
import javax.swing.SwingUtilities;
import java.util.Set;

public final class MafUpdateHandler implements DataUpdateHandler {
    private static final String MAFV = "P18";
    private static final String AF_LEARNING_1 = "P4";
    private static final String AF_CORRECTION_1 = "P3";
    private MafTab mafTab;

    public synchronized void registerData(LoggerData loggerData) {
    }

    public synchronized void handleDataUpdate(Response response) {
        if (mafTab.isRecordData() && containsData(response, MAFV, AF_LEARNING_1, AF_CORRECTION_1)) {
            boolean valid = true;

            // cl/ol check
            if ((containsData(response, "E3") || containsData(response, "E27"))) {
                double clOl = -1;
                if (containsData(response, "E3")) {
                    clOl = (int) findValue(response, "E3");
                }
                if (containsData(response, "E27")) {
                    clOl = (int) findValue(response, "E27");
                }
                valid = mafTab.isValidClOl(clOl);
            }

            // afr check
            if (valid && containsData(response, "P58")) {
                double afr = findValue(response, "P58");
                valid = mafTab.isValidAfr(afr);
            }

            // rpm check
            if (valid && containsData(response, "P8")) {
                double rpm = findValue(response, "P8");
                valid = mafTab.isValidRpm(rpm);
            }

            // maf check
            if (valid && containsData(response, "P12")) {
                double maf = findValue(response, "P12");
                valid = mafTab.isValidMaf(maf);
            }

            // intake air temp check
            if (valid && containsData(response, "P11")) {
                double temp = findValue(response, "P11");
                valid = mafTab.isValidIntakeAirTemp(temp);
            }

            // coolant temp check
            if (valid && containsData(response, "P2")) {
                double temp = findValue(response, "P2");
                valid = mafTab.isValidCoolantTemp(temp);
            }

            // tip-in throttle check
            if (valid && (containsData(response, "E23") || containsData(response, "E49"))) {
                double tipIn = -1;
                if (containsData(response, "E23")) {
                    tipIn = findValue(response, "E23");
                }
                if (containsData(response, "E49")) {
                    tipIn = findValue(response, "E49");
                }
                valid = mafTab.isValidTipInThrottle(tipIn);
            }

            if (valid) {
                final double mafv = findValue(response, MAFV);
                final double learning = findValue(response, AF_LEARNING_1);
                final double correction = findValue(response, AF_CORRECTION_1);
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
        for (final LoggerData loggerData : response.getData()) {
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
