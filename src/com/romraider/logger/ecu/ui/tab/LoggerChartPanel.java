/*
 *
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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

package com.romraider.logger.ecu.ui.tab;

import com.romraider.logger.ecu.ui.handler.graph.SpringUtilities;
import static com.romraider.util.ParamChecker.checkNotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import static org.jfree.chart.plot.PlotOrientation.VERTICAL;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import java.awt.Color;
import java.awt.Dimension;

public final class LoggerChartPanel extends JPanel {
    private static final Color DARK_GREY = new Color(80, 80, 80);
    private static final Color LIGHT_GREY = new Color(110, 110, 110);
    private final XYTrendline trendline;
    private final XYSeries series;
    private final String labelX;
    private final String labelY;

    public LoggerChartPanel(XYTrendline trendline, XYSeries series, String labelX, String labelY) {
        super(new SpringLayout());
        checkNotNull(trendline, series, labelX, labelY);
        this.trendline = trendline;
        this.series = series;
        this.labelX = labelX;
        this.labelY = labelY;
        addChart();
    }

    private void addChart() {
        ChartPanel chartPanel = new ChartPanel(createChart(), false, true, true, true, true);
        chartPanel.setMinimumSize(new Dimension(400, 300));
        chartPanel.setPreferredSize(new Dimension(500, 400));
        add(chartPanel);
        SpringUtilities.makeCompactGrid(this, 1, 1, 2, 2, 2, 2);
    }

    private JFreeChart createChart() {
        XYDataset dataset = buildDataset();
        return buildChart(dataset);
    }

    private XYSeriesCollection buildDataset() {
        return new XYSeriesCollection(series);
    }

    private JFreeChart buildChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createScatterPlot(null, labelX, labelY, dataset, VERTICAL, false, true, false);
        chart.setBackgroundPaint(Color.BLACK);
        configurePlot(chart);
        addTrendLine(chart);
        return chart;
    }

    private void configurePlot(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.BLACK);
        plot.getDomainAxis().setLabelPaint(Color.WHITE);
        plot.getRangeAxis().setLabelPaint(Color.WHITE);
        plot.getDomainAxis().setTickLabelPaint(LIGHT_GREY);
        plot.getRangeAxis().setTickLabelPaint(LIGHT_GREY);
        plot.setDomainGridlinePaint(DARK_GREY);
        plot.setRangeGridlinePaint(DARK_GREY);
        plot.setOutlinePaint(DARK_GREY);
        plot.setRenderer(buildScatterRenderer());
    }

    private XYDotRenderer buildScatterRenderer() {
        XYDotRenderer renderer = new XYDotRenderer();
        renderer.setDotHeight(2);
        renderer.setDotWidth(2);
        return renderer;
    }

    private void addTrendLine(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(1, trendline);
        plot.setRenderer(1, buildTrendLineRenderer());
    }

    private StandardXYItemRenderer buildTrendLineRenderer() {
        return new StandardXYItemRenderer();
    }
}