/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
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

package com.romraider.logger.ecu.ui.tab;

import static com.romraider.util.ParamChecker.checkNotNull;
import static java.awt.Color.BLACK;
import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.MAGENTA;
import static java.awt.Color.RED;
import static java.awt.Color.WHITE;
import static java.awt.Color.YELLOW;
import static org.apache.log4j.Logger.getLogger;
import static org.jfree.chart.ChartFactory.createScatterPlot;
import static org.jfree.chart.plot.PlotOrientation.VERTICAL;
import jamlab.Polyfit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYDrawableAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;

import com.romraider.logger.ecu.ui.handler.graph.SpringUtilities;

public final class DynoChartPanel extends JPanel {
    private static final long serialVersionUID = -6577979878171615665L;
    private static final Logger LOGGER = getLogger(DynoChartPanel.class);
    private static final Color DARK_GREY = new Color(80, 80, 80);
    private static final Color LIGHT_GREY = new Color(110, 110, 110);
    private static final String START_PROMPT = "Accelerate using WOT when ready!!";
    private final XYSeries data = new XYSeries("Raw HP");		// series for HorsePower
    private final XYSeries data1 = new XYSeries("Raw TQ");		// series for Torque
    private final XYTrendline trendline = new XYTrendline(data);
    private final XYTrendline trendline1 = new XYTrendline(data1);
    private final XYSeries logTime = new XYSeries("Logger Time");	// series for raw sample time
    private final XYSeries logRpm = new XYSeries("Logger RPM");		// series for raw sample RPM
    private final XYTrendline timeTrend = new XYTrendline(logTime);
    private final XYTrendline rpmTrend = new XYTrendline(logRpm);
    private final XYSeries hpRef = new XYSeries("HP Ref");		// series for reference HP
    private final XYSeries tqRef = new XYSeries("TQ Ref");		// series for reference TQ
    private final XYTrendline hpTrend = new XYTrendline(hpRef);
    private final XYTrendline tqTrend = new XYTrendline(tqRef);
    private final String labelX;
    private String labelY1 = null;
    private String labelY2 = null;
    private String refResults = " ";
    private StandardXYItemRenderer rendererY1 = new StandardXYItemRenderer();
    private StandardXYItemRenderer rendererY2 = new StandardXYItemRenderer();
    private NumberAxis hpAxis = new NumberAxis("pwr");
    private NumberAxis tqAxis = new NumberAxis("tq");
    private XYPlot plot;
    private final CircleDrawer cd = new CircleDrawer(RED, new BasicStroke(1.0f), null);
    private XYAnnotation bestHp; 
    private XYAnnotation bestTq; 
    private final XYPointerAnnotation hpPointer = new XYPointerAnnotation(
    										"Max HP", 1, 1, 3.0 * Math.PI / 6.0);
    private final XYPointerAnnotation tqPointer = new XYPointerAnnotation(
											"Max TQ", 1, 1, 3.0 * Math.PI / 6.0);
    private final XYTextAnnotation refStat = new XYTextAnnotation(refResults,0,0);
    
    public DynoChartPanel(String labelX, String labelY1, String labelY2) {
        super(new SpringLayout());
        checkNotNull(labelX, labelY1, labelY2);
        this.labelX = labelX;
        this.labelY1 = labelY1;
        this.labelY2 = labelY2;
        addChart();
    }

    public synchronized void addRawData(double x, double y, double y1) {
    	logTime.add(x, y);
    	logRpm.add(x, y1);
  }

    public synchronized void addData(double x, double y, double y1) {
        data.add(x, y);
        data1.add(x, y1);
    }

    public synchronized void setRefTrace(double x, double y1, double y2){
    	hpRef.add(x, y1);
    	tqRef.add(x, y2);
    }

    public void updateRefTrace(int order) {
    	if (hpRef.getItemCount() > 0) {
	    	hpTrend.update(order);
	    	tqTrend.update(order);
	    	refStat.setText(refResults);
    		refStat.setX(plot.getDomainAxis().getLowerBound() + 10);
    		refStat.setY(hpAxis.getUpperBound());
            plot.addAnnotation(refStat);
    	}
    }

    public int getPwrTqCount() {
    	return (int) data.getItemCount();
    }

    public String getPwrTq(int x){
    	String set = data.getX(x) + "," + data.getY(x) + "," + data1.getY(x);
    	return set;
    }

    public void setRefResults(String line) {
    	refResults = "Reference: " + line;
    }

    public void clearRefTrace() {
    	refResults = " ";
    	plot.removeAnnotation(refStat);
    	hpRef.clear();
    	tqRef.clear();
    	hpTrend.clear();
    	tqTrend.clear();
    }

    public void clear() {
    	logTime.clear();
    	logRpm.clear();
        timeTrend.clear();
        rpmTrend.clear();
        clearGraph();
    }

    public int getSampleCount() {
    	clearGraph();
    	return (int) logTime.getItemCount();
    }
    
    public void clearGraph(){
        trendline.clear();
        trendline1.clear();
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

    public double[] getTimeCoeff(int order) {
        timeTrend.update(order);
        return getPolynomialCoefficients(timeTrend);
    }
    
    public double[] getRpmCoeff(int order) {
        rpmTrend.update(order);
        return getPolynomialCoefficients(rpmTrend);
    }

    public String interpolate(int order, double min, double max, String carInfo, double fToE, double sToE, double tToS, String units) {
//    	updateRefTrace(order);
    	hpAxis.setAutoRange(true);
        tqAxis.setAutoRange(true);
    	trendline.update(order);
    	trendline1.update(order);
    	double[] hPCoeff = getPolynomialCoefficients(trendline);
    	double[] TqCoeff = getPolynomialCoefficients(trendline1);
//    	LOGGER.trace(Arrays.toString(hPCoeff));
//    	LOGGER.trace(Arrays.toString(TqCoeff));
//    	LOGGER.trace("order: " + order + " samples: " + samples); 
    	double minHp = 200;
    	double minTq = 200;
    	double maxHp = 0;
        double maxHpRpm = 0;
        double maxTq = 0;
        double maxTqRpm = 0;
        for (double x = min; x <= max; x=x+1) {
            double nowHp = 0;
            double nowTq = 0;
        	for (int i = 0; i < order + 1; i++) {
//        		int pwr = order - i;
        		nowHp = nowHp + (Math.pow(x,order-i) * hPCoeff[i]);
        		nowTq = nowTq + (Math.pow(x,order-i) * TqCoeff[i]);
//        		LOGGER.trace("sample: " + x + " i " + i + " pwr " + pwr + " coeff_i " + hPCoeff[i] + " time " + timeSample);
        	}
//    		LOGGER.trace("HP: " + nowHp + " TQ: " + nowTq);
            if ((nowHp > 0) && (nowHp < minHp)){
            	minHp = nowHp;
            }
            if ((nowTq > 0) && (nowTq < minTq)){
            	minTq = nowTq;
            }
            if (nowHp > maxHp){
            	maxHp = nowHp;
            	maxHpRpm = x;
            }
            if (nowTq > maxTq){
            	maxTq = nowTq;
            	maxTqRpm = x;
            }
//            LOGGER.trace("minHp:" + minHp + " minTq:" + minTq + " maxHp:" + maxHp + " maxTq:" + maxTq);
        }
//        double yMax = Math.max(maxTq, maxHp);
        double rangeMin = Math.min(tqAxis.getLowerBound(), hpAxis.getLowerBound());
        double yMin = Math.round(rangeMin);
        double ySpace = (hpAxis.getUpperBound() - hpAxis.getLowerBound()) / 25;
        double xMin = ((plot.getDomainAxis().getUpperBound() - plot.getDomainAxis().getLowerBound()) / 7) + plot.getDomainAxis().getLowerBound();
//    	double xMin = (max < 5000) ? 2100 : 2550;
        hpAxis.setRange(Math.round(rangeMin), Math.round(hpAxis.getUpperBound() + ySpace));
        tqAxis.setRange(Math.round(rangeMin), Math.round(tqAxis.getUpperBound() + ySpace));
//        LOGGER.trace("yMin:" + yMin + " yMax:" + yMax + " ySpace:" + ySpace);

        String hpUnits = " hp(I)";
        String tqUnits = " lbf-ft";
        String stat1Text = "50-80 MPH: " + fToE + " secs";
        String stat2Text = "60-80 MPH: " + sToE + " secs";
        if (units.equalsIgnoreCase("Metric")) {
        	hpUnits = " kW";
        	tqUnits = " N-m";
            stat1Text = "80-130 km/h: " + fToE + " secs";
            stat2Text = "100-130 km/h: " + sToE + " secs";
        }

        String stat3Text = "3000-6000 RPM: " + tToS + " secs";
        String resultsText = "Max Pwr: " + String.format("%1.1f", maxHp) + hpUnits +
        						  " @ " + String.format("%1.0f",maxHpRpm) + 
        						  " RPM / Max TQ: " + String.format("%1.1f", maxTq) + tqUnits +
        						  " @ " + String.format("%1.0f", maxTqRpm) + " RPM";
        LOGGER.info("DYNO Results: " + carInfo);
        LOGGER.info("DYNO Results: " + resultsText);
        LOGGER.info("DYNO Results: " + stat1Text);
        LOGGER.info("DYNO Results: " + stat2Text);
        LOGGER.info("DYNO Results: " + stat3Text);
        bestHp = new XYDrawableAnnotation(maxHpRpm, maxHp, 10, 10, cd);
        hpPointer.setX(maxHpRpm);
        hpPointer.setY(maxHp);
        hpPointer.setArrowPaint(BLUE);
        hpPointer.setTipRadius(7.0);
        hpPointer.setBaseRadius(30.0);
        hpPointer.setFont(new Font("SansSerif",Font.BOLD,10));
        hpPointer.setPaint(BLUE);
        bestTq = new XYDrawableAnnotation(maxTqRpm, maxTq, 10, 10, cd);
        tqPointer.setX(maxTqRpm);
        tqPointer.setY(maxTq);
        tqPointer.setArrowPaint(YELLOW);
        tqPointer.setTipRadius(7.0);
        tqPointer.setBaseRadius(30.0);
        tqPointer.setFont(new Font("SansSerif",Font.BOLD,10));
        tqPointer.setPaint(YELLOW);
        final XYTextAnnotation dynoResults = new XYTextAnnotation(resultsText, xMin, yMin+(ySpace*4));
        dynoResults.setPaint(RED);
        dynoResults.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        dynoResults.setFont(new Font("SansSerif", Font.BOLD,14));
        final XYTextAnnotation carText = new XYTextAnnotation(carInfo, xMin, yMin+(ySpace*3));
        carText.setPaint(RED);
        carText.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        carText.setFont(new Font("SansSerif", Font.BOLD,12));
        final XYTextAnnotation stat1 = new XYTextAnnotation(stat1Text, xMin, yMin+(ySpace*2));
        stat1.setPaint(RED);
        stat1.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        stat1.setFont(new Font("SansSerif", Font.PLAIN,12));
        final XYTextAnnotation stat2 = new XYTextAnnotation(stat2Text, xMin, yMin+ySpace);
        stat2.setPaint(RED);
        stat2.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        stat2.setFont(new Font("SansSerif", Font.PLAIN,12));
        final XYTextAnnotation stat3 = new XYTextAnnotation(stat3Text, xMin, yMin);
        stat3.setPaint(RED);
        stat3.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        stat3.setFont(new Font("SansSerif", Font.PLAIN,12));
        if (!refResults.equals(" ")){
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
        return resultsText;
    }

    public double[] calculate(XYTrendline trendSeries, double[] x) {
        return trendSeries.calculate(x);
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
        addTrendLineY1(chart, 0, trendline, BLUE);
        addTrendLineY2(chart, 1, trendline1, YELLOW);
        addSeries(chart, 2, data, 1, RED);
        addSeries(chart, 3, data1, 1, MAGENTA);
        addSeries(chart, 4, hpRef, 1, BLACK);
        addSeries(chart, 5, tqRef, 1, BLACK);
        addTrendLine(chart, 6, hpTrend, BLUE);
        addTrendLine(chart, 7, tqTrend, YELLOW);

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
        plot.setRangeAxis(0,hpAxis);
        plot.setRangeAxisLocation(0, AxisLocation.TOP_OR_LEFT);
        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(2, 0);
        plot.mapDatasetToRangeAxis(4, 0);
        plot.mapDatasetToRangeAxis(6, 0);
        // Y2 axis (right) settings
        tqAxis.setLabel(labelY2);
        tqAxis.setLabelPaint(YELLOW);
        tqAxis.setTickLabelPaint(LIGHT_GREY);
        tqAxis.setAutoRangeIncludesZero(false);
        tqAxis.setAutoRange(true);
        plot.setRangeAxis(1,tqAxis);
        plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
        plot.mapDatasetToRangeAxis(1, 1);
        plot.mapDatasetToRangeAxis(3, 1);
        plot.mapDatasetToRangeAxis(5, 1);
        plot.mapDatasetToRangeAxis(7, 1);
        plot.setRangeGridlinePaint(DARK_GREY);
	    refStat.setPaint(WHITE);
	    refStat.setTextAnchor(TextAnchor.TOP_LEFT);
	    refStat.setFont(new Font("SansSerif", Font.BOLD,12));

    }

    public void startPrompt() {
        final double x = ((plot.getDomainAxis().getUpperBound() - plot.getDomainAxis().getLowerBound()) / 2) + plot.getDomainAxis().getLowerBound();  
        final double y = ((hpAxis.getUpperBound() - hpAxis.getLowerBound()) / 2) + hpAxis.getLowerBound();
    	final XYTextAnnotation startMessage = new XYTextAnnotation(START_PROMPT, x, y);
	    startMessage.setPaint(GREEN);
	    startMessage.setTextAnchor(TextAnchor.BOTTOM_CENTER);
	    startMessage.setFont(new Font("Arial", Font.BOLD,20));
	    plot.addAnnotation(startMessage);
    }

    public void clearPrompt() {
    	plot.clearAnnotations();
    }

    private XYDotRenderer buildScatterRenderer(int size, Color color) {
        XYDotRenderer renderer = new XYDotRenderer();
        renderer.setDotHeight(size);
        renderer.setDotWidth(size);
        renderer.setSeriesPaint(0, color);
        return renderer;
    }

    private void addTrendLine(JFreeChart chart, int index, XYTrendline trendline, Color color) {
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(index, trendline);
        plot.setRenderer(index, buildTrendLineRenderer(color));
    }

    private void addTrendLineY1(JFreeChart chart, int index, XYTrendline trendline, Color color) {
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(index, trendline);
        plot.setRenderer(index, buildTrendLineRendererY1(color));
    }

    private void addTrendLineY2(JFreeChart chart, int index, XYTrendline trendline, Color color) {
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(index, trendline);
        plot.setRenderer(index, buildTrendLineRendererY2(color));
    }

    private void addSeries(JFreeChart chart, int index, XYSeries series, int size, Color color) {
        XYDataset dataset = new XYSeriesCollection(series);
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(index, dataset);
        plot.setRenderer(index, buildScatterRenderer(size, color));
    }

    private StandardXYItemRenderer buildTrendLineRenderer(Color color) {
    	StandardXYItemRenderer renderer = new StandardXYItemRenderer();
        renderer.setSeriesPaint(0, color);
        float dash[] = { 2.0f };
        renderer.setSeriesStroke(0, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dash, 0.0f));
        return renderer;
    }

    private StandardXYItemRenderer buildTrendLineRendererY1(Color color) {
        rendererY1.setSeriesPaint(0, color);
        return rendererY1;
    }

    private StandardXYItemRenderer buildTrendLineRendererY2(Color color) {
        rendererY2.setSeriesPaint(0, color);
        return rendererY2;
    }
}