package enginuity.logger.ecu.ui.handler.graph;

import enginuity.logger.ecu.definition.ConvertorUpdateListener;
import enginuity.logger.ecu.definition.EcuData;
import enginuity.logger.ecu.ui.handler.DataUpdateHandler;
import static enginuity.logger.ecu.ui.handler.graph.SpringUtilities.makeCompactGrid;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import static org.jfree.chart.plot.PlotOrientation.VERTICAL;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import java.awt.Color;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.util.Collections.synchronizedMap;
import java.util.HashMap;
import java.util.Map;

public final class GraphUpdateHandler implements DataUpdateHandler, ConvertorUpdateListener {
    private static final Color DARK_GREY = new Color(80, 80, 80);
    private static final Color LIGHT_GREY = new Color(110, 110, 110);
    private final Map<EcuData, ChartPanel> chartMap = synchronizedMap(new HashMap<EcuData, ChartPanel>());
    private final Map<EcuData, XYSeries> seriesMap = synchronizedMap(new HashMap<EcuData, XYSeries>());
    private final Map<EcuData, Integer> datasetIndexes = synchronizedMap(new HashMap<EcuData, Integer>());
    private final JPanel graphPanel;
    private long startTime = System.currentTimeMillis();
    private boolean combinedChart = false;
    private boolean paused = false;
    private long pauseStartTime = System.currentTimeMillis();
    private ChartPanel combinedChartPanel = null;
    private int counter = 0;


    public GraphUpdateHandler(final JPanel panel) {
        this.graphPanel = new JPanel(new SpringLayout());
        JCheckBox combinedCheckbox = new JCheckBox("Combine Graphs", combinedChart);
        combinedCheckbox.addActionListener(new CombinedActionListener(combinedCheckbox));
        JToggleButton playPauseButton = new JToggleButton("Pause Graphs");
        playPauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                paused = !paused;
                if (paused) {
                    pauseStartTime = System.currentTimeMillis();
                } else {
                    startTime = startTime + (System.currentTimeMillis() - pauseStartTime);
                }
            }
        });
        JPanel controlPanel = new JPanel();
        controlPanel.add(combinedCheckbox);
        controlPanel.add(playPauseButton);
        panel.add(controlPanel, NORTH);
        panel.add(this.graphPanel, CENTER);
    }

    public synchronized void registerData(EcuData ecuData) {
        // add to charts
        registerSeries(ecuData);
        if (combinedChart) {
            addToCombined(ecuData);
            layoutForCombined();
        } else {
            addToPanel(ecuData);
            layoutForPanel();
        }
        graphPanel.updateUI();
    }

    private synchronized void addToPanel(EcuData ecuData) {
        XYSeries series = seriesMap.get(ecuData);
        ChartPanel chartPanel = new ChartPanel(createXYLineChart(ecuData, new XYSeriesCollection(series), false), false, true, true, true, true);
        chartPanel.setMinimumSize(new Dimension(600, 200));
        chartPanel.setMaximumSize(new Dimension(10000, 200));
        chartPanel.setPreferredSize(new Dimension(600, 200));
        chartMap.put(ecuData, chartPanel);
        graphPanel.add(chartPanel);
    }

    private void layoutForPanel() {
        makeCompactGrid(graphPanel, seriesMap.size(), 1, 2, 2, 2, 2);
    }

    private synchronized void addToCombined(EcuData ecuData) {
        if (combinedChartPanel == null) {
            combinedChartPanel = new ChartPanel(createXYLineChart(ecuData, null, true), false, true, true, true, true);
            LegendTitle legendTitle = new LegendTitle(combinedChartPanel.getChart().getXYPlot());
            legendTitle.setItemPaint(WHITE);
            combinedChartPanel.getChart().addLegend(legendTitle);
            combinedChartPanel.setMinimumSize(new Dimension(500, 400));
            combinedChartPanel.setPreferredSize(new Dimension(500, 400));
            graphPanel.add(combinedChartPanel);
        }
        XYPlot plot = combinedChartPanel.getChart().getXYPlot();
        plot.setDataset(counter, new XYSeriesCollection(seriesMap.get(ecuData)));
        plot.setRenderer(counter, new StandardXYItemRenderer());
        datasetIndexes.put(ecuData, counter++);
    }

    private void layoutForCombined() {
        makeCompactGrid(graphPanel, 1, 1, 2, 2, 2, 2);
    }

    public synchronized void handleDataUpdate(EcuData ecuData, final double value, final long timestamp) {
        // update chart
        final XYSeries series = seriesMap.get(ecuData);
        if (series != null && !paused) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    series.add((timestamp - startTime) / 1000.0, value);
                }
            });
        }
    }

    public synchronized void deregisterData(EcuData ecuData) {
        seriesMap.remove(ecuData);
        if (combinedChart) {
            removeFromCombined(ecuData);
        } else {
            removeFromPanel(ecuData);
            layoutForPanel();
        }
        graphPanel.updateUI();
    }

    private void removeFromCombined(EcuData ecuData) {
        // remove from charts
        if (datasetIndexes.containsKey(ecuData)) {
            combinedChartPanel.getChart().getXYPlot().setDataset(datasetIndexes.get(ecuData), null);
        }
        datasetIndexes.remove(ecuData);
        chartMap.remove(ecuData);
        if (datasetIndexes.isEmpty()) {
            graphPanel.remove(combinedChartPanel);
            combinedChartPanel = null;
        }
    }

    private void removeFromPanel(EcuData ecuData) {
        // remove from charts
        graphPanel.remove(chartMap.get(ecuData));
        datasetIndexes.remove(ecuData);
        chartMap.remove(ecuData);
    }

    public synchronized void cleanUp() {
    }

    public synchronized void reset() {
        for (XYSeries series : seriesMap.values()) {
            series.clear();
        }
    }

    public synchronized void notifyConvertorUpdate(EcuData updatedEcuData) {
        if (chartMap.containsKey(updatedEcuData)) {
            seriesMap.get(updatedEcuData).clear();
            JFreeChart chart = chartMap.get(updatedEcuData).getChart();
            chart.getXYPlot().getRangeAxis().setLabel(buildRangeAxisTitle(updatedEcuData));
        }
    }

    private void registerSeries(EcuData ecuData) {
        final XYSeries series = new XYSeries(ecuData.getName());
        series.setMaximumItemCount(200);
        seriesMap.put(ecuData, series);
    }

    private JFreeChart createXYLineChart(EcuData ecuData, XYDataset dataset, boolean combined) {
        String title = combined ? "Combined Data" : ecuData.getName();
        String rangeAxisTitle = combined ? "Data" : buildRangeAxisTitle(ecuData);
        JFreeChart chart = ChartFactory.createXYLineChart(title, "Time (sec)", rangeAxisTitle, dataset, VERTICAL, false, true, false);
        chart.setBackgroundPaint(BLACK);
        chart.getTitle().setPaint(WHITE);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(BLACK);
        plot.getDomainAxis().setLabelPaint(WHITE);
        plot.getRangeAxis().setLabelPaint(WHITE);
        plot.getDomainAxis().setTickLabelPaint(LIGHT_GREY);
        plot.getRangeAxis().setTickLabelPaint(LIGHT_GREY);
        plot.setDomainGridlinePaint(DARK_GREY);
        plot.setRangeGridlinePaint(DARK_GREY);
        plot.setOutlinePaint(DARK_GREY);
        return chart;
    }

    private String buildRangeAxisTitle(EcuData ecuData) {
        return ecuData.getName() + " (" + ecuData.getSelectedConvertor().getUnits() + ")";
    }

    
    private final class CombinedActionListener implements ActionListener {
        private final JCheckBox combinedCheckbox;

        private CombinedActionListener(JCheckBox combinedCheckbox) {
            this.combinedCheckbox = combinedCheckbox;
        }

        public void actionPerformed(ActionEvent actionEvent) {
            combinedChart = combinedCheckbox.isSelected();
            if (combinedChart) {
                removeAllFromPanel();
                addAllToCombined();
                layoutForCombined();
            } else {
                removeAllFromCombined();
                addAllToPanel();
                layoutForPanel();
            }
            graphPanel.updateUI();
        }

        private void addAllToCombined() {
            for (EcuData ecuData : seriesMap.keySet()) {
                addToCombined(ecuData);
            }
        }

        private void removeAllFromPanel() {
            for (EcuData ecuData : seriesMap.keySet()) {
                removeFromPanel(ecuData);
            }
        }

        private void addAllToPanel() {
            for (EcuData ecuData : seriesMap.keySet()) {
                addToPanel(ecuData);
            }
        }

        private void removeAllFromCombined() {
            for (EcuData ecuData : seriesMap.keySet()) {
                removeFromCombined(ecuData);
            }
        }

    }
}
