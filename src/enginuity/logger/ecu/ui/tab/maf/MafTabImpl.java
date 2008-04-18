package enginuity.logger.ecu.ui.tab.maf;

import enginuity.ECUEditor;
import enginuity.logger.ecu.definition.EcuParameter;
import enginuity.logger.ecu.definition.EcuSwitch;
import enginuity.logger.ecu.definition.ExternalData;
import enginuity.logger.ecu.ui.DataRegistrationBroker;
import enginuity.logger.ecu.ui.tab.LoggerChartPanel;
import enginuity.logger.ecu.ui.tab.XYTrendline;
import org.jfree.data.xy.XYSeries;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.WEST;
import java.util.List;

public final class MafTabImpl extends JPanel implements MafTab {
    private final XYSeries series = new XYSeries("MAF Analysis");
    private final XYTrendline trendline = new XYTrendline();
    private final MafControlPanel controlPanel;

    public MafTabImpl(DataRegistrationBroker broker, ECUEditor ecuEditor) {
        super(new BorderLayout(2, 2));
        controlPanel = buildControlPanel(broker, ecuEditor);
        add(controlPanel, WEST);
        add(buildGraphPanel(), CENTER);
    }

    private MafControlPanel buildControlPanel(DataRegistrationBroker broker, ECUEditor ecuEditor) {
        return new MafControlPanel(this, trendline, series, broker, ecuEditor);
    }

    private LoggerChartPanel buildGraphPanel() {
        return new LoggerChartPanel(trendline, series, "MAF (v)", "Total Correction (%)");
    }

    public boolean isRecordData() {
        return controlPanel.isRecordData();
    }

    public boolean isValidClOl(double value) {
        return controlPanel.isValidClOl(value);
    }

    public boolean isValidAfr(double value) {
        return controlPanel.isValidAfr(value);
    }

    public boolean isValidRpm(double value) {
        return controlPanel.isValidRpm(value);
    }

    public boolean isValidMaf(double value) {
        return controlPanel.isValidMaf(value);
    }

    public boolean isValidCoolantTemp(double value) {
        return controlPanel.isValidCoolantTemp(value);
    }

    public boolean isValidIntakeAirTemp(double value) {
        return controlPanel.isValidIntakeAirTemp(value);
    }

    public boolean isValidMafvChange(double value) {
        return controlPanel.isValidMafvChange(value);
    }

    public boolean isValidTipInThrottle(double value) {
        return controlPanel.isValidTipInThrottle(value);
    }

    public void addData(double mafv, double correction) {
        series.add(mafv, correction);
    }

    public void setEcuParams(List<EcuParameter> params) {
        controlPanel.setEcuParams(params);
    }

    public void setEcuSwitches(List<EcuSwitch> switches) {
        controlPanel.setEcuSwitches(switches);
    }

    public void setExternalDatas(List<ExternalData> external) {
        controlPanel.setExternalDatas(external);
    }

    public JPanel getPanel() {
        return this;
    }
}
