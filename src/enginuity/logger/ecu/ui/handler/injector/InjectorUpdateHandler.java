package enginuity.logger.ecu.ui.handler.injector;

import enginuity.logger.ecu.comms.query.Response;
import enginuity.logger.ecu.definition.LoggerData;
import enginuity.logger.ecu.ui.handler.DataUpdateHandler;
import enginuity.logger.ecu.ui.tab.injector.InjectorTab;
import javax.swing.SwingUtilities;
import java.util.Set;

public final class InjectorUpdateHandler implements DataUpdateHandler {
    private static final String PULSE_WIDTH = "P21";
    private static final String ENGINE_LOAD_16 = "E2";
    private static final String ENGINE_LOAD_32 = "E26";
    private InjectorTab injectorTab;

    public synchronized void registerData(LoggerData loggerData) {
    }

    public synchronized void handleDataUpdate(Response response) {
        if (injectorTab.isRecordData() && containsData(response, PULSE_WIDTH)
                && (containsData(response, ENGINE_LOAD_16) || containsData(response, ENGINE_LOAD_32))) {
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
                valid = injectorTab.isValidClOl(clOl);
            }

            // afr check
            if (valid && containsData(response, "P58")) {
                double afr = findValue(response, "P58");
                valid = injectorTab.isValidAfr(afr);
            }

            // rpm check
            if (valid && containsData(response, "P8")) {
                double rpm = findValue(response, "P8");
                valid = injectorTab.isValidRpm(rpm);
            }

            // maf check
            if (valid && containsData(response, "P12")) {
                double maf = findValue(response, "P12");
                valid = injectorTab.isValidMaf(maf);
            }

            // intake air temp check
            if (valid && containsData(response, "P11")) {
                double temp = findValue(response, "P11");
                valid = injectorTab.isValidIntakeAirTemp(temp);
            }

            // coolant temp check
            if (valid && containsData(response, "P2")) {
                double temp = findValue(response, "P2");
                valid = injectorTab.isValidCoolantTemp(temp);
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
                valid = injectorTab.isValidTipInThrottle(tipIn);
            }

            if (valid) {
                final double pulseWidth = findValue(response, PULSE_WIDTH);
                double load = containsData(response, ENGINE_LOAD_16) ? findValue(response, ENGINE_LOAD_16) : findValue(response, ENGINE_LOAD_32);
                double stoichAfr = injectorTab.getFuelStoichAfr();
                double density = injectorTab.getFuelDensity();
                final double fuelcc = load / 2 / stoichAfr * 1000 / density;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        injectorTab.addData(pulseWidth, fuelcc);
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

    public void setInjectorTab(InjectorTab injectorTab) {
        this.injectorTab = injectorTab;
    }
}