/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2020 RomRaider.com
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
 */

package com.romraider.logger.ecu.ui.tab.dyno;

import static com.romraider.Settings.COMMA;

import com.romraider.logger.ecu.ui.handler.graph.SpringUtilities;
import com.romraider.logger.ecu.ui.tab.CircleDrawer;
import com.romraider.logger.ecu.ui.tab.XYTrendline;
import com.romraider.util.ResourceUtil;

import static com.romraider.Settings.SEMICOLON;
import static com.romraider.util.ParamChecker.checkNotNull;
import jamlab.Polyfit;
import static java.awt.Color.BLACK;
import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static java.awt.Color.WHITE;
import static java.awt.Color.YELLOW;
import static org.jfree.chart.ChartFactory.createScatterPlot;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYDrawableAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import static org.jfree.chart.plot.PlotOrientation.VERTICAL;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public final class DynoChartPanel extends JPanel {
    private static final long serialVersionUID = -6577979878171615665L;
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            DynoChartPanel.class.getName());
    private static final Color DARK_GREY = new Color(80, 80, 80);
    private static final Color LIGHT_GREY = new Color(110, 110, 110);
    private static final String START_PROMPT = rb.getString("STARTPROMPT");
    private static final String ET_PROMPT_I = rb.getString("ETPROMPTI");
    private static final String ET_PROMPT_M = rb.getString("ETPROMPTM");
    private final XYSeries data = new XYSeries("Raw HP");       // series for HorsePower/RPM
    private final XYSeries data1 = new XYSeries("Raw TQ");      // series for Torque/RPM
    private final XYSeries logRpm = new XYSeries("Logger RPM"); // series for raw sample time/RPM
    private final XYTrendline rpmTrend = new XYTrendline(logRpm);
    private final XYSeries hpRef = new XYSeries("HP Ref");      // series for reference HP/RPM
    private final XYSeries tqRef = new XYSeries("TQ Ref");      // series for reference TQ/RPM
    private final String labelX;
    private String labelY1 = null;
    private String labelY2 = null;
    private StandardXYItemRenderer rendererY1 = new StandardXYItemRenderer();
    private StandardXYItemRenderer rendererY2 = new StandardXYItemRenderer();
    private NumberAxis hpAxis = new NumberAxis("pwr");
    private NumberAxis tqAxis = new NumberAxis("tq");
    private XYPlot plot;
    private final CircleDrawer cd = new CircleDrawer(RED, new BasicStroke(1.0f), null);
    private final CircleDrawer cdGreen = new CircleDrawer(GREEN, new BasicStroke(1.0f), null);
    private XYAnnotation bestHp;
    private XYAnnotation bestTq;
    private final XYPointerAnnotation hpPointer = new XYPointerAnnotation(
            rb.getString("MAXHP"), 1, 1, 3.0 * Math.PI / 6.0);
    private final XYPointerAnnotation tqPointer = new XYPointerAnnotation(
            rb.getString("MAXTQ"), 1, 1, 3.0 * Math.PI / 6.0);
    private final XYTextAnnotation refStat = new XYTextAnnotation(" ", 0, 0);

    public DynoChartPanel(String labelX, String labelY1, String labelY2) {
        super(new SpringLayout());
        checkNotNull(labelX, labelY1, labelY2);
        this.labelX = labelX;
        this.labelY1 = labelY1;
        this.labelY2 = labelY2;
        addChart();
    }

    public void quietUpdate(boolean notify) {
        data.setNotify(notify);
        data1.setNotify(notify);
    }

    public synchronized void addRawData(double x, double y) {
        logRpm.add(x, y);
    }

    public synchronized void addData(double x, double y) {
        data.add(x, y);
    }

    public synchronized void addData(double x, double y1, double y2) {
        data.add(x, y1);
        data1.add(x, y2);
    }

    public synchronized void setRefTrace(double x, double y1, double y2) {
        hpRef.add(x, y1);
        tqRef.add(x, y2);
    }

    public void updateRefTrace(String[] line) {
        if (hpRef.getItemCount() > 0) {
            refStat.setText(MessageFormat.format(
                            rb.getString("REFERENCE"),
                            line[2],
                            Double.parseDouble(line[3]),
                            Double.parseDouble(line[4]),
                            Double.parseDouble(line[5]),
                            Double.parseDouble(line[6])));
            refStat.setX(plot.getDomainAxis().getLowerBound() + 10);
            refStat.setY(hpAxis.getUpperBound());
            plot.addAnnotation(refStat);
        }
    }

    public int getPwrTqCount() {
        return (int) data.getItemCount();
    }

    public String getPwrTq(int x) {
        String dataSet = data.getX(x) + COMMA + data.getY(x) + COMMA + data1.getY(x);
        return dataSet;
    }

    public void clearRefTrace() {
        refStat.setText(" ");
        plot.removeAnnotation(refStat);
        hpRef.clear();
        tqRef.clear();
    }

    public void clear() {
        logRpm.clear();
        rpmTrend.clear();
        clearGraph();
    }

    public long getTimeSample(int index) {
        return logRpm.getX(index).longValue();
    }

    public int getSampleCount() {
        clearGraph();
        return logRpm.getItemCount();
    }

    public void clearGraph() {
        data.clear();
        data1.clear();
        rendererY1.removeAnnotation(bestHp);
        rendererY2.removeAnnotation(bestTq);
        rendererY1.removeAnnotation(hpPointer);
        rendererY2.removeAnnotation(tqPointer);
        hpAxis.setAutoRange(true);
        tqAxis.setAutoRange(true);
        plot.clearAnnotations();
    }

    public double[] getRpmCoeff(int order) {
        rpmTrend.update(order);
        return getPolynomialCoefficients(rpmTrend);
    }

    public void interpolate(double[] results, String[] resultStrings) {
        hpAxis.setAutoRange(true);
        tqAxis.setAutoRange(true);
        double rangeMin = Math.min(tqAxis.getLowerBound(), hpAxis.getLowerBound());
        double yMin = Math.round(rangeMin);
        double ySpace = (hpAxis.getUpperBound() - hpAxis.getLowerBound()) / 25;
        double xMin = ((plot.getDomainAxis().getUpperBound() - plot.getDomainAxis().getLowerBound()) / 7) + plot.getDomainAxis().getLowerBound();
        hpAxis.setRange(Math.round(rangeMin), Math.round(hpAxis.getUpperBound() + ySpace));
        tqAxis.setRange(Math.round(rangeMin), Math.round(tqAxis.getUpperBound() + ySpace));
        bestHp = new XYDrawableAnnotation(results[1], results[0], 10, 10, cd);
        hpPointer.setX(results[1]);
        hpPointer.setY(results[0]);
        hpPointer.setArrowPaint(BLUE);
        hpPointer.setTipRadius(7.0);
        hpPointer.setBaseRadius(30.0);
        hpPointer.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        hpPointer.setPaint(BLUE);
        bestTq = new XYDrawableAnnotation(results[3], results[2], 10, 10, cd);
        tqPointer.setX(results[3]);
        tqPointer.setY(results[2]);
        tqPointer.setArrowPaint(YELLOW);
        tqPointer.setTipRadius(7.0);
        tqPointer.setBaseRadius(30.0);
        tqPointer.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        tqPointer.setPaint(YELLOW);
        final XYTextAnnotation dynoResults = new XYTextAnnotation(resultStrings[1], xMin, yMin + (ySpace * 5));
        dynoResults.setPaint(RED);
        dynoResults.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        dynoResults.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        final XYTextAnnotation carText = new XYTextAnnotation(resultStrings[0], xMin, yMin + (ySpace * 4));
        carText.setPaint(RED);
        carText.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        carText.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        final XYTextAnnotation stat1 = new XYTextAnnotation(resultStrings[2], xMin, yMin + (ySpace * 3));
        stat1.setPaint(RED);
        stat1.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        stat1.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        final XYTextAnnotation stat2 = new XYTextAnnotation(resultStrings[3], xMin, yMin + ySpace * 2);
        stat2.setPaint(RED);
        stat2.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        stat2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        final XYTextAnnotation stat3 = new XYTextAnnotation(resultStrings[4], xMin, yMin + ySpace);
        stat3.setPaint(RED);
        stat3.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        stat3.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        final XYTextAnnotation stat4 = new XYTextAnnotation(resultStrings[5], xMin, yMin);
        stat4.setPaint(RED);
        stat4.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        stat4.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        if (!refStat.equals(" ")) {
            refStat.setX(plot.getDomainAxis().getLowerBound() + 10);
            refStat.setY(hpAxis.getUpperBound());
            plot.addAnnotation(refStat);
        }
        rendererY1.addAnnotation(bestHp);
        rendererY2.addAnnotation(bestTq);
        rendererY1.addAnnotation(hpPointer);
        rendererY2.addAnnotation(tqPointer);
        plot.addAnnotation(dynoResults);
        plot.addAnnotation(carText);
        plot.addAnnotation(stat1);
        plot.addAnnotation(stat2);
        plot.addAnnotation(stat3);
        plot.addAnnotation(stat4);
    }

    public void updateEtResults(String carInfo, double[] etResults, String units) {
        String s60Text = rb.getString("S60TEXTI");
        String s330Text = rb.getString("S330TEXTI");
        String s660Text = rb.getString("S660TEXTI");
        String s1000Text = rb.getString("S1000TEXTI");
        String s1320Text = rb.getString("S1320TEXTI");
        String zTo60Text = rb.getString("ZTO60TEXTI");
        if (units.equalsIgnoreCase("km/h")) {
            s60Text = rb.getString("S60TEXTM");
            s330Text = rb.getString("S330TEXTM");
            s1000Text = rb.getString("S1000TEXTM");
            s1320Text = rb.getString("S1320TEXTM");
            zTo60Text = rb.getString("ZTO60TEXTM");
        }
        hpAxis.setLabel(MessageFormat.format(
                rb.getString("HPAXISLBL"), units));
        String[] car = carInfo.split(SEMICOLON);
        car[0] = MessageFormat.format(
                    rb.getString("CARTEXT"),
                    car[0].substring(0, car[0].length() - 3),
                    etResults[8],
                    etResults[9],
                    units);
        double ySpace = hpAxis.getUpperBound() / 25;
        double xMin = ((plot.getDomainAxis().getUpperBound() - plot.getDomainAxis().getLowerBound()) / 7) + plot.getDomainAxis().getLowerBound();
        tqAxis.setRange(hpAxis.getLowerBound(), hpAxis.getUpperBound());
        final XYAnnotation s60Marker = new XYDrawableAnnotation(etResults[0], etResults[1], 10, 10, cd);
        final XYTextAnnotation s60Label = new XYTextAnnotation(s60Text, etResults[0], (etResults[1] + ySpace));
        s60Label.setPaint(RED);
        s60Label.setTextAnchor(TextAnchor.TOP_RIGHT);
        s60Label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        final XYTextAnnotation s60Time = new XYTextAnnotation(String.format("%1.3f", etResults[0]) + "\" / " + String.format("%1.2f", etResults[1]), etResults[0], (etResults[1] - ySpace));
        s60Time.setPaint(RED);
        s60Time.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        s60Time.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        final XYAnnotation s330Marker = new XYDrawableAnnotation(etResults[2], etResults[3], 10, 10, cd);
        final XYTextAnnotation s330Label = new XYTextAnnotation(s330Text, etResults[2], (etResults[3] + ySpace));
        s330Label.setPaint(RED);
        s330Label.setTextAnchor(TextAnchor.TOP_RIGHT);
        s330Label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        final XYTextAnnotation s330Time = new XYTextAnnotation(String.format("%1.3f", etResults[2]) + "\" / " + String.format("%1.2f", etResults[3]), etResults[2], (etResults[3] - ySpace));
        s330Time.setPaint(RED);
        s330Time.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        s330Time.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        final XYAnnotation s660Marker = new XYDrawableAnnotation(etResults[4], etResults[5], 10, 10, cd);
        final XYTextAnnotation s660Label = new XYTextAnnotation(s660Text, etResults[4], (etResults[5] + ySpace));
        s660Label.setPaint(RED);
        s660Label.setTextAnchor(TextAnchor.TOP_RIGHT);
        s660Label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        final XYTextAnnotation s660Time = new XYTextAnnotation(String.format("%1.3f", etResults[4]) + "\" / " + String.format("%1.2f", etResults[5]), etResults[4], (etResults[5] - ySpace));
        s660Time.setPaint(RED);
        s660Time.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        s660Time.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        final XYAnnotation s1000Marker = new XYDrawableAnnotation(etResults[6], etResults[7], 10, 10, cd);
        final XYTextAnnotation s1000Label = new XYTextAnnotation(s1000Text, etResults[6], (etResults[7] + ySpace));
        s1000Label.setPaint(RED);
        s1000Label.setTextAnchor(TextAnchor.TOP_RIGHT);
        s1000Label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        final XYTextAnnotation s1000Time = new XYTextAnnotation(String.format("%1.3f", etResults[6]) + "\" / " + String.format("%1.2f", etResults[7]), etResults[6], (etResults[7] - ySpace));
        s1000Time.setPaint(RED);
        s1000Time.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        s1000Time.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        final XYAnnotation s1320Marker = new XYDrawableAnnotation(etResults[8], etResults[9], 10, 10, cd);
        final XYTextAnnotation s1320Label = new XYTextAnnotation(s1320Text, etResults[8], (etResults[9] - ySpace));
        s1320Label.setPaint(RED);
        s1320Label.setTextAnchor(TextAnchor.BOTTOM_CENTER);
        s1320Label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        final XYTextAnnotation s1320Time = new XYTextAnnotation(String.format("%1.3f", etResults[8]) + "\" / " + String.format("%1.2f", etResults[9]), (etResults[8] - 0.2), etResults[9]);
        s1320Time.setPaint(RED);
        s1320Time.setTextAnchor(TextAnchor.CENTER_RIGHT);
        s1320Time.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        final XYTextAnnotation carText = new XYTextAnnotation(car[0], (plot.getDomainAxis().getUpperBound() - 0.2), (hpAxis.getLowerBound() + ySpace));
        carText.setPaint(RED);
        carText.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
        carText.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        final XYAnnotation zTo60Marker = new XYDrawableAnnotation(etResults[10], etResults[11], 10, 10, cdGreen);
        final XYTextAnnotation zTo60Label = new XYTextAnnotation(zTo60Text, etResults[10], (etResults[11] + ySpace));
        zTo60Label.setPaint(GREEN);
        zTo60Label.setTextAnchor(TextAnchor.TOP_RIGHT);
        zTo60Label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        final XYTextAnnotation zTo60Time = new XYTextAnnotation((String.format("%1.3f", etResults[10]) + "\""), etResults[10], (etResults[11] - ySpace));
        zTo60Time.setPaint(GREEN);
        zTo60Time.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        zTo60Time.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        plot.addAnnotation(s60Marker);
        plot.addAnnotation(s60Label);
        plot.addAnnotation(s60Time);
        plot.addAnnotation(s330Marker);
        plot.addAnnotation(s330Label);
        plot.addAnnotation(s330Time);
        plot.addAnnotation(s660Marker);
        plot.addAnnotation(s660Label);
        plot.addAnnotation(s660Time);
        plot.addAnnotation(s1000Marker);
        plot.addAnnotation(s1000Label);
        plot.addAnnotation(s1000Time);
        plot.addAnnotation(s1320Marker);
        plot.addAnnotation(s1320Label);
        plot.addAnnotation(s1320Time);
        plot.addAnnotation(carText);
        plot.addAnnotation(zTo60Marker);
        plot.addAnnotation(zTo60Label);
        plot.addAnnotation(zTo60Time);
    }

    public double[] getPolynomialCoefficients(XYTrendline trendSeries) {
        Polyfit fit = trendSeries.getPolyFit();
        return fit.getPolynomialCoefficients();
    }

    private void addChart() {
        ChartPanel chartPanel = new ChartPanel(createChart(), false, true, true, true, true);
        chartPanel.setMinimumSize(new Dimension(400, 300));
        chartPanel.setPreferredSize(new Dimension(500, 400));
        add(chartPanel);
        SpringUtilities.makeCompactGrid(this, 1, 1, 2, 2, 2, 2);
    }

    private JFreeChart createChart() {
        JFreeChart chart = createScatterPlot(null, labelX, labelY1, null, VERTICAL, false, true, false);
        chart.setBackgroundPaint(BLACK);
        configurePlot(chart);
        addSeries1(chart, 0, data, BLUE);
        addSeries2(chart, 1, data1, YELLOW);
        addRef(chart, 2, hpRef, BLUE);
        addRef(chart, 3, tqRef, YELLOW);
        return chart;
    }

    private void configurePlot(JFreeChart chart) {
        plot = chart.getXYPlot();
        plot.setOutlinePaint(DARK_GREY);
        plot.setBackgroundPaint(BLACK);
        // X axis settings
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        plot.getDomainAxis().setLabelPaint(WHITE);
        plot.getDomainAxis().setTickLabelPaint(LIGHT_GREY);
        plot.setDomainGridlinePaint(DARK_GREY);
        // Y1 axis (left) settings
        hpAxis.setLabel(labelY1);
        hpAxis.setLabelPaint(BLUE);
        hpAxis.setTickLabelPaint(LIGHT_GREY);
        hpAxis.setAutoRangeIncludesZero(false);
        hpAxis.setAutoRange(true);
        plot.setRangeAxis(0, hpAxis);
        plot.setRangeAxisLocation(0, AxisLocation.TOP_OR_LEFT);
        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(2, 0);
        // Y2 axis (right) settings
        tqAxis.setLabel(labelY2);
        tqAxis.setLabelPaint(YELLOW);
        tqAxis.setTickLabelPaint(LIGHT_GREY);
        tqAxis.setAutoRangeIncludesZero(false);
        tqAxis.setAutoRange(true);
        plot.setRangeAxis(1, tqAxis);
        plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
        plot.mapDatasetToRangeAxis(1, 1);
        plot.mapDatasetToRangeAxis(3, 1);
        plot.setRangeGridlinePaint(DARK_GREY);
        refStat.setPaint(WHITE);
        refStat.setTextAnchor(TextAnchor.TOP_LEFT);
        refStat.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

    }

    public void setET() {
        clear();
        plot.getDomainAxis().setLabel(rb.getString("ETXAXISLBL"));
        hpAxis.setLabel(rb.getString("ETHPAXISLBL"));
        tqAxis.setLabel(" ");
    }

    public void setDyno() {
        clear();
        plot.getDomainAxis().setLabel(rb.getString("DYNOXAXISLBL"));
        hpAxis.setLabel(rb.getString("DYNOHPAXISLBL"));
        tqAxis.setLabel(rb.getString("DYNOTQAXISLBL"));
    }

    public void startPrompt(String select) {
        String startPrompt = START_PROMPT;
        if (select.equalsIgnoreCase("mph")) startPrompt = ET_PROMPT_I;
        if (select.equalsIgnoreCase("km/h")) startPrompt = ET_PROMPT_M;
        final double x = ((plot.getDomainAxis().getUpperBound() - plot.getDomainAxis().getLowerBound()) / 2) + plot.getDomainAxis().getLowerBound();
        final double y = ((hpAxis.getUpperBound() - hpAxis.getLowerBound()) / 2) + hpAxis.getLowerBound();
        final XYTextAnnotation startMessage = new XYTextAnnotation(startPrompt, x, y);
        startMessage.setPaint(GREEN);
        startMessage.setTextAnchor(TextAnchor.BOTTOM_CENTER);
        startMessage.setFont(new Font("Arial", Font.BOLD, 20));
        plot.addAnnotation(startMessage);
    }

    public void clearPrompt() {
        plot.clearAnnotations();
    }

    private void addSeries1(JFreeChart chart, int index, XYSeries series, Color color) {
        XYDataset dataset = new XYSeriesCollection(series);
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(index, dataset);
        plot.setRenderer(index, buildTrendLineRendererY1(color));
    }

    private void addSeries2(JFreeChart chart, int index, XYSeries series, Color color) {
        XYDataset dataset = new XYSeriesCollection(series);
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(index, dataset);
        plot.setRenderer(index, buildTrendLineRendererY2(color));
    }

    private void addRef(JFreeChart chart, int index, XYSeries series, Color color) {
        XYDataset dataset = new XYSeriesCollection(series);
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(index, dataset);
        plot.setRenderer(index, buildTrendLineRenderer(color));
    }

    private StandardXYItemRenderer buildTrendLineRenderer(Color color) {
        StandardXYItemRenderer renderer = new StandardXYItemRenderer();
        renderer.setSeriesPaint(0, color);
        float dash[] = {2.0f};
        renderer.setSeriesStroke(0, new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        return renderer;
    }

    private StandardXYItemRenderer buildTrendLineRendererY1(Color color) {
        rendererY1.setSeriesPaint(0, color);
        rendererY1.setSeriesStroke(0, new BasicStroke(1.6f));
        return rendererY1;
    }

    private StandardXYItemRenderer buildTrendLineRendererY2(Color color) {
        rendererY2.setSeriesPaint(0, color);
        rendererY2.setSeriesStroke(0, new BasicStroke(1.6f));
        return rendererY2;
    }
}