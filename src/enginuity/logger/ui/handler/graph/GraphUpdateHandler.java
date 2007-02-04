/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.logger.ui.handler.graph;

import enginuity.logger.definition.ConvertorUpdateListener;
import enginuity.logger.definition.EcuData;
import enginuity.logger.ui.handler.DataUpdateHandler;
import static enginuity.logger.ui.handler.graph.SpringUtilities.makeCompactGrid;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import static org.jfree.chart.plot.PlotOrientation.VERTICAL;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.util.Collections.synchronizedMap;
import java.util.HashMap;
import java.util.Map;

public final class GraphUpdateHandler implements DataUpdateHandler, ConvertorUpdateListener {
    private static final Color RED = new Color(190, 30, 30);
    private static final Color DARK_GREY = new Color(80, 80, 80);
    private static final Color LIGHT_GREY = new Color(110, 110, 110);
    private final Map<EcuData, ChartPanel> chartMap = synchronizedMap(new HashMap<EcuData, ChartPanel>());
    private final Map<EcuData, XYSeries> seriesMap = synchronizedMap(new HashMap<EcuData, XYSeries>());
    private final long startTime = System.currentTimeMillis();
    private final JPanel graphPanel;
    private int loggerCount = 0;

    public GraphUpdateHandler(JPanel graphPanel) {
        this.graphPanel = graphPanel;
    }

    public synchronized void registerData(EcuData ecuData) {
        // add to charts
        final XYSeries series = new XYSeries(ecuData.getName());
        //TODO: Make chart max item count configurable via settings
        series.setMaximumItemCount(200);
        ChartPanel chartPanel = new ChartPanel(createXYLineChart(series, ecuData), false, true, true, true, true);
        graphPanel.add(chartPanel);
        seriesMap.put(ecuData, series);
        chartMap.put(ecuData, chartPanel);
        makeCompactGrid(graphPanel, ++loggerCount, 1, 10, 10, 20, 20);
        repaintGraphPanel(2);
    }

    public synchronized void handleDataUpdate(EcuData ecuData, final double value, final long timestamp) {
        // update chart
        final XYSeries series = seriesMap.get(ecuData);
        if (series != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    series.add((timestamp - startTime) / 1000.0, value);
                }
            });
        }
    }

    public synchronized void deregisterData(EcuData ecuData) {
        // remove from charts
        graphPanel.remove(chartMap.get(ecuData));
        chartMap.remove(ecuData);
        seriesMap.remove(ecuData);
        makeCompactGrid(graphPanel, --loggerCount, 1, 10, 10, 20, 20);
        repaintGraphPanel(1);
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

    private JFreeChart createXYLineChart(XYSeries series, EcuData ecuData) {
        final XYDataset xyDataset = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory.createXYLineChart(ecuData.getName(), "Time (sec)", buildRangeAxisTitle(ecuData), xyDataset,
                VERTICAL, false, true, false);
        chart.setBackgroundPaint(BLACK);
        chart.getTitle().setPaint(WHITE);
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(BLACK);
        plot.getRenderer().setPaint(RED);
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

    private void repaintGraphPanel(final int parentRepaintLevel) {
        new Thread(new Runnable() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
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
                });
            }
        }).start();
    }
}

