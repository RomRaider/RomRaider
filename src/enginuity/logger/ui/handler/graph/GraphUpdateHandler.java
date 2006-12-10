package enginuity.logger.ui.handler.graph;

import enginuity.logger.definition.ConvertorUpdateListener;
import enginuity.logger.definition.EcuData;
import enginuity.logger.ui.handler.DataUpdateHandler;
import static enginuity.logger.ui.handler.graph.SpringUtilities.makeCompactGrid;
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

public final class GraphUpdateHandler implements DataUpdateHandler, ConvertorUpdateListener {
    private final JPanel graphPanel;
    private final Map<EcuData, ChartPanel> chartMap = synchronizedMap(new HashMap<EcuData, ChartPanel>());
    private final Map<EcuData, XYSeries> seriesMap = synchronizedMap(new HashMap<EcuData, XYSeries>());
    private int loggerCount = 0;

    public GraphUpdateHandler(JPanel graphPanel) {
        this.graphPanel = graphPanel;
    }

    public void registerData(EcuData ecuData) {
        // add to charts
        final XYSeries series = new XYSeries(ecuData.getName());
        //TODO: Make chart max item count configurable via settings
        series.setMaximumItemCount(200);
        final XYDataset xyDataset = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory.createXYLineChart(ecuData.getName(), "Time (sec)", buildRangeAxisTitle(ecuData), xyDataset,
                VERTICAL, false, true, false);
        ChartPanel chartPanel = new ChartPanel(chart, false, true, true, true, true);
        graphPanel.add(chartPanel);
        seriesMap.put(ecuData, series);
        chartMap.put(ecuData, chartPanel);
        makeCompactGrid(graphPanel, ++loggerCount, 1, 10, 10, 20, 20);
        repaintGraphPanel(2);
    }

    public synchronized void handleDataUpdate(EcuData ecuData, byte[] value, long timestamp) {
        // update chart
        XYSeries series = seriesMap.get(ecuData);
        if (series != null) {
            series.add(timestamp, ecuData.getSelectedConvertor().convert(value));
        }
    }

    public void deregisterData(EcuData ecuData) {
        // remove from charts
        graphPanel.remove(chartMap.get(ecuData));
        chartMap.remove(ecuData);
        makeCompactGrid(graphPanel, --loggerCount, 1, 10, 10, 20, 20);
        repaintGraphPanel(1);
    }

    public void cleanUp() {
    }

    public void notifyConvertorUpdate(EcuData updatedEcuData) {
        if (chartMap.containsKey(updatedEcuData)) {
            seriesMap.get(updatedEcuData).clear();
            JFreeChart chart = chartMap.get(updatedEcuData).getChart();
            chart.getXYPlot().getRangeAxis().setLabel(buildRangeAxisTitle(updatedEcuData));
        }
    }

    private String buildRangeAxisTitle(EcuData ecuData) {
        return ecuData.getName() + " (" + ecuData.getSelectedConvertor().getUnits() + ")";
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

