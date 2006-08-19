package enginuity.logger.ui.handler;

import enginuity.logger.definition.EcuParameter;
import static enginuity.logger.ui.SpringUtilities.makeCompactGrid;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import static org.jfree.chart.plot.PlotOrientation.VERTICAL;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import static java.util.Collections.synchronizedMap;
import java.util.HashMap;
import java.util.Map;

public final class GraphUpdateHandler implements ParameterUpdateHandler {
    private final JPanel graphPanel;
    private final Map<EcuParameter, ChartPanel> chartMap = synchronizedMap(new HashMap<EcuParameter, ChartPanel>());
    private final Map<EcuParameter, XYSeries> seriesMap = synchronizedMap(new HashMap<EcuParameter, XYSeries>());
    private int loggerCount = 0;

    public GraphUpdateHandler(JPanel graphPanel) {
        this.graphPanel = graphPanel;
    }

    public void registerParam(EcuParameter ecuParam) {
        // add to charts
        final XYSeries series = new XYSeries(ecuParam.getName());
        //TODO: Make chart max item count configurable via settings
        series.setMaximumItemCount(1000);
        final XYDataset xyDataset = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory.createXYLineChart(ecuParam.getName(), "Time (sec)", ecuParam.getName()
                + " (" + ecuParam.getConvertor().getUnits() + ")", xyDataset, VERTICAL, false, true, false);
        ChartPanel chartPanel = new ChartPanel(chart, false, true, true, true, true);
        graphPanel.add(chartPanel);
        seriesMap.put(ecuParam, series);
        chartMap.put(ecuParam, chartPanel);
        makeCompactGrid(graphPanel, ++loggerCount, 1, 10, 10, 20, 20);
        repaintGraphPanel(2);
    }

    public void handleParamUpdate(EcuParameter ecuParam, byte[] value, long timestamp) {
        // update chart
        XYSeries series = seriesMap.get(ecuParam);
        series.add(timestamp / 1000.0, ecuParam.getConvertor().convert(value));
    }

    public void deregisterParam(EcuParameter ecuParam) {
        // remove from charts
        graphPanel.remove(chartMap.get(ecuParam));
        chartMap.remove(ecuParam);
        makeCompactGrid(graphPanel, --loggerCount, 1, 10, 10, 20, 20);
        repaintGraphPanel(1);
    }

    private void repaintGraphPanel(int parentRepaintLevel) {
        if (loggerCount < parentRepaintLevel) {
            graphPanel.doLayout();
            graphPanel.repaint();
        } else {
            if (loggerCount == 1) {
                graphPanel.doLayout();
            }
            graphPanel.getParent().doLayout();
            graphPanel.getParent().repaint();
        }
    }
}
