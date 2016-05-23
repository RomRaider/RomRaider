/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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

package com.romraider.logger.ecu.ui.handler.dash;

import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.LoggerData;
import static com.romraider.util.ParamChecker.checkNotNull;
import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.ORANGE;
import static java.awt.Color.RED;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import static java.lang.Double.MAX_VALUE;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialFrame;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialRange;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.data.general.DefaultValueDataset;
import static org.jfree.ui.GradientPaintTransformType.VERTICAL;
import org.jfree.ui.StandardGradientPaintTransformer;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Point;
import java.text.DecimalFormat;

public class DialGaugeStyle implements GaugeStyle {
    protected final DefaultValueDataset current = new DefaultValueDataset(0.0);
    protected final DefaultValueDataset max = new DefaultValueDataset(0.0);
    protected final DefaultValueDataset min = new DefaultValueDataset(0.0);
    protected final DialTextAnnotation unitsLabel = new DialTextAnnotation("");
    protected final LoggerData loggerData;
    private double maxValue = MAX_VALUE * -1;
    private double minValue = MAX_VALUE;
    private JPanel panel;

    public DialGaugeStyle(LoggerData loggerData) {
        checkNotNull(loggerData);
        this.loggerData = loggerData;
    }

    public void apply(JPanel panel) {
        this.panel = panel;
        resetValue();
        refreshChart(panel);
    }

    private void refreshChart(final JPanel panel) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFreeChart chart = buildChart();
                ChartPanel chartPanel = new ChartPanel(chart);
                chartPanel.setPreferredSize(getChartSize());
                panel.removeAll();
                panel.add(chartPanel);
                panel.revalidate();
            }
        });
    }

    public void refreshTitle() {
        refreshChart(panel);
    }

    public void updateValue(final double value) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                current.setValue(value);
            }
        });
        updateMinMax(value);
    }

    private void updateMinMax(final double value) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (value > maxValue) {
                    maxValue = value;
                    max.setValue(value);
                }
                if (value < minValue) {
                    minValue = value;
                    min.setValue(value);
                }
            }
        });
    }

    public void resetValue() {
        EcuDataConvertor convertor = loggerData.getSelectedConvertor();
        GaugeMinMax minMax = convertor.getGaugeMinMax();
        double value = minMax.min;
        updateValue(value);
        resetMinMax(value);
    }

    private void resetMinMax(final double value) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                maxValue = MAX_VALUE * -1;
                minValue = MAX_VALUE;
                max.setValue(value);
                min.setValue(value);
            }
        });
    }

    protected Dimension getChartSize() {
        return new Dimension(250, 270);
    }

    protected JFreeChart buildChart() {
        DialPlot plot = new DialPlot();
        plot.setView(0.0, 0.0, 1.0, 1.0);
        plot.setDataset(0, current);
        plot.setDataset(1, max);
        plot.setDataset(2, min);
        DialFrame dialFrame = new StandardDialFrame();
        plot.setDialFrame(dialFrame);

        GradientPaint gp = new GradientPaint(new Point(),
                new Color(255, 255, 255), new Point(),
                new Color(170, 170, 220));
        DialBackground db = new DialBackground(gp);
        db.setGradientPaintTransformer(new StandardGradientPaintTransformer(VERTICAL));
        plot.setBackground(db);

        unitsLabel.setFont(new Font(Font.DIALOG, BOLD, 15));
        unitsLabel.setRadius(0.7);
        unitsLabel.setLabel(loggerData.getSelectedConvertor().getUnits());
        plot.addLayer(unitsLabel);

        DecimalFormat format = new DecimalFormat(loggerData.getSelectedConvertor().getFormat());

        DialValueIndicator dvi = new DialValueIndicator(0);
        dvi.setNumberFormat(format);
        plot.addLayer(dvi);

        EcuDataConvertor convertor = loggerData.getSelectedConvertor();
        GaugeMinMax minMax = convertor.getGaugeMinMax();
        StandardDialScale scale = new StandardDialScale(minMax.min, minMax.max, 225.0, -270.0, minMax.step, 5);
        scale.setTickRadius(0.88);
        scale.setTickLabelOffset(0.15);
        scale.setTickLabelFont(new Font(Font.DIALOG, PLAIN, 12));
        scale.setTickLabelFormatter(format);
        plot.addScale(0, scale);
        plot.addScale(1, scale);
        plot.addScale(2, scale);

        StandardDialRange range = new StandardDialRange(rangeLimit(minMax, 0.75), minMax.max, RED);
        range.setInnerRadius(0.52);
        range.setOuterRadius(0.55);
        plot.addLayer(range);

        StandardDialRange range2 = new StandardDialRange(rangeLimit(minMax, 0.5), rangeLimit(minMax, 0.75), ORANGE);
        range2.setInnerRadius(0.52);
        range2.setOuterRadius(0.55);
        plot.addLayer(range2);

        StandardDialRange range3 = new StandardDialRange(minMax.min, rangeLimit(minMax, 0.5), GREEN);
        range3.setInnerRadius(0.52);
        range3.setOuterRadius(0.55);
        plot.addLayer(range3);

        DialPointer needleCurrent = new DialPointer.Pointer(0);
        plot.addLayer(needleCurrent);

        DialPointer needleMax = new DialPointer.Pin(1);
        needleMax.setRadius(0.84);
        ((DialPointer.Pin) needleMax).setPaint(RED);
        ((DialPointer.Pin) needleMax).setStroke(new BasicStroke(1.5F));
        plot.addLayer(needleMax);

        DialPointer needleMin = new DialPointer.Pin(2);
        needleMin.setRadius(0.84);
        ((DialPointer.Pin) needleMin).setPaint(BLUE);
        ((DialPointer.Pin) needleMin).setStroke(new BasicStroke(1.5F));
        plot.addLayer(needleMin);

        DialCap cap = new DialCap();
        cap.setRadius(0.10);
        plot.setCap(cap);

        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(loggerData.getName());

        return chart;
    }

    private double rangeLimit(GaugeMinMax minMax, double fraction) {
        return minMax.min + (minMax.max - minMax.min) * fraction;
    }
}