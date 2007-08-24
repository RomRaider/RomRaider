package enginuity.logger.ecu.ui.handler.maf;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.WEST;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import enginuity.ECUEditor;
import enginuity.logger.ecu.comms.query.Response;
import enginuity.logger.ecu.definition.LoggerData;
import enginuity.logger.ecu.ui.handler.DataUpdateHandler;
import enginuity.logger.ecu.ui.tab.maf.MafChartPanel;
import enginuity.logger.ecu.ui.tab.maf.MafControlPanel;
import org.jfree.data.xy.XYSeries;

public final class MafUpdateHandler implements DataUpdateHandler {
    private static final String MAFV = "P18";
    private static final String AF_LEARNING_1 = "P4";
    private static final String AF_CORRECTION_1 = "P3";
    private final XYSeries series = new XYSeries("MAF Analysis");
    private final XYTrendline trendline = new XYTrendline();
    private final JPanel mafPanel;
    private final ECUEditor ecuEditor;
    private final MafControlPanel controlPanel;

    public MafUpdateHandler(JPanel mafPanel, ECUEditor ecuEditor) {
        this.mafPanel = mafPanel;
        this.ecuEditor = ecuEditor;
        controlPanel = buildControlPanel();
        mafPanel.add(controlPanel, WEST);
        mafPanel.add(buildGraphPanel(), CENTER);
    }

    private MafControlPanel buildControlPanel() {
        return new MafControlPanel(mafPanel, trendline, series, ecuEditor);
    }

    private MafChartPanel buildGraphPanel() {
        return new MafChartPanel(trendline, series);
    }

    public synchronized void registerData(LoggerData loggerData) {
    }

    public synchronized void handleDataUpdate(Response response) {
        if (controlPanel.isRecordData() && containsData(response, MAFV, AF_LEARNING_1, AF_CORRECTION_1)) {
            boolean valid = true;

            // cl/ol check
            if (valid && (containsData(response, "E3") || containsData(response, "E27"))) {
                double clOl = -1;
                if (containsData(response, "E3")) {
                    clOl = (int) findValue(response, "E3");
                }
                if (containsData(response, "E27")) {
                    clOl = (int) findValue(response, "E27");
                }
                valid = controlPanel.isValidClOl(clOl);
            }

            // afr check
            if (valid && containsData(response, "P58")) {
                double afr = findValue(response, "P58");
                valid = controlPanel.isValidAfr(afr);
            }

            // rpm check
            if (valid && containsData(response, "P8")) {
                double rpm = findValue(response, "P8");
                valid = controlPanel.isValidRpm(rpm);
            }

            // maf check
            if (valid && containsData(response, "P12")) {
                double maf = findValue(response, "P12");
                valid = controlPanel.isValidMaf(maf);
            }

            // coolant temp check
            if (valid && containsData(response, "P2")) {
                double temp = findValue(response, "P2");
                valid = controlPanel.isValidCoolantTemp(temp);
            }

            if (valid) {
                final double mafv = findValue(response, MAFV);
                final double learning = findValue(response, AF_LEARNING_1);
                final double correction = findValue(response, AF_CORRECTION_1);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        series.add(mafv, learning + correction);
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
}
