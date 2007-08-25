package enginuity.logger.ecu.ui.tab.maf;

import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.WEST;
import javax.swing.JPanel;
import enginuity.ECUEditor;
import enginuity.logger.ecu.ui.handler.maf.XYTrendline;
import org.jfree.data.xy.XYSeries;

public final class MafTabImpl extends JPanel implements MafTab {
    private final XYSeries series = new XYSeries("MAF Analysis");
    private final XYTrendline trendline = new XYTrendline();
    private final MafControlPanel controlPanel;

    public MafTabImpl(ECUEditor ecuEditor) {
        super(new BorderLayout(2, 2));
        controlPanel = buildControlPanel(ecuEditor);
        add(controlPanel, WEST);
        add(buildGraphPanel(), CENTER);
    }

    private MafControlPanel buildControlPanel(ECUEditor ecuEditor) {
        return new MafControlPanel(this, trendline, series, ecuEditor);
    }

    private MafChartPanel buildGraphPanel() {
        return new MafChartPanel(trendline, series);
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

    public void addData(double mafv, double correction) {
        series.add(mafv, correction);
    }

    public JPanel getPanel() {
        return this;
    }
}
