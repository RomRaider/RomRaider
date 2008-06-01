package com.romraider.logger.ecu.ui.handler.dash;

import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.LoggerData;
import static com.romraider.util.ParamChecker.checkNotNull;
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
import java.awt.Color;
import static java.awt.Color.GREEN;
import static java.awt.Color.ORANGE;
import static java.awt.Color.RED;
import java.awt.Dimension;
import java.awt.Font;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import java.awt.GradientPaint;
import java.awt.Point;

public final class DialGaugeStyle implements GaugeStyle {
    private final DefaultValueDataset dataset = new DefaultValueDataset(0.0);
    private final DialTextAnnotation unitsLabel = new DialTextAnnotation("");
    private final LoggerData loggerData;
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
                JFreeChart chart = buildChart(dataset, unitsLabel);
                ChartPanel chartPanel = new ChartPanel(chart);
                chartPanel.setPreferredSize(new Dimension(200, 220));
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
                dataset.setValue(value);
            }
        });
    }

    public void resetValue() {
        EcuDataConvertor convertor = loggerData.getSelectedConvertor();
        GaugeMinMax minMax = convertor.getGaugeMinMax();
        double min = minMax.min;
        updateValue(min);
    }

    private JFreeChart buildChart(DefaultValueDataset dataset, DialTextAnnotation unitsLabel) {
        DialPlot plot = new DialPlot(dataset);
        plot.setView(0.0, 0.0, 1.0, 1.0);
        plot.setDataset(dataset);
        DialFrame dialFrame = new StandardDialFrame();
        plot.setDialFrame(dialFrame);

        GradientPaint gp = new GradientPaint(new Point(),
                new Color(255, 255, 255), new Point(),
                new Color(170, 170, 220));
        DialBackground db = new DialBackground(gp);
        db.setGradientPaintTransformer(new StandardGradientPaintTransformer(VERTICAL));
        plot.setBackground(db);

        unitsLabel.setFont(new Font("Dialog", BOLD, 14));
        unitsLabel.setRadius(0.7);
        unitsLabel.setLabel(loggerData.getSelectedConvertor().getUnits());
        plot.addLayer(unitsLabel);

        DialValueIndicator dvi = new DialValueIndicator(0);
        plot.addLayer(dvi);

        EcuDataConvertor convertor = loggerData.getSelectedConvertor();
        GaugeMinMax minMax = convertor.getGaugeMinMax();
        StandardDialScale scale = new StandardDialScale(minMax.min, minMax.max, 210.0, -240.0, minMax.step, 5);
        scale.setTickRadius(0.88);
        scale.setTickLabelOffset(0.15);
        scale.setTickLabelFont(new Font("Dialog", PLAIN, 14));
        plot.addScale(0, scale);

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

        DialPointer needle = new DialPointer.Pointer();
        plot.addLayer(needle);

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