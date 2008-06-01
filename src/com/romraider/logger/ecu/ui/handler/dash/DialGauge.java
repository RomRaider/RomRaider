package com.romraider.logger.ecu.ui.handler.dash;

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

public final class DialGauge implements GaugeStyle {
    private final DefaultValueDataset dataset = new DefaultValueDataset(0.0);
    private final DialTextAnnotation unitsLabel = new DialTextAnnotation("");
    private final JFreeChart chart = buildChart(dataset, unitsLabel);
    private final LoggerData loggerData;

    public DialGauge(LoggerData loggerData) {
        checkNotNull(loggerData);
        this.loggerData = loggerData;
    }

    public void apply(JPanel panel) {
        refreshTitle();
        resetValue();
        int width = 200;
        int height = 200;
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(width, height));
        panel.setPreferredSize(new Dimension(width + 20, height + 20));
        panel.add(chartPanel);
    }


    public void refreshTitle() {
        chart.setTitle(loggerData.getName());
        unitsLabel.setLabel(loggerData.getSelectedConvertor().getUnits());
    }

    public void updateValue(double value) {
        dataset.setValue(value);
    }

    public void resetValue() {
        dataset.setValue(0);
    }

    private JFreeChart buildChart(DefaultValueDataset dataset, DialTextAnnotation unitsLabel) {
        DialPlot plot = new DialPlot(dataset);
        plot.setView(0.0, 0.0, 1.0, 1.0);
        plot.setDataset(this.dataset);
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
        plot.addLayer(unitsLabel);

        DialValueIndicator dvi = new DialValueIndicator(0);
        plot.addLayer(dvi);

        StandardDialScale scale = new StandardDialScale();
        scale.setTickRadius(0.88);
        scale.setTickLabelOffset(0.15);
        scale.setTickLabelFont(new Font("Dialog", PLAIN, 14));
        plot.addScale(0, scale);

        StandardDialRange range = new StandardDialRange(75.0, 100.0, RED);
        range.setInnerRadius(0.52);
        range.setOuterRadius(0.55);
        plot.addLayer(range);

        StandardDialRange range2 = new StandardDialRange(50.0, 75.0, ORANGE);
        range2.setInnerRadius(0.52);
        range2.setOuterRadius(0.55);
        plot.addLayer(range2);

        StandardDialRange range3 = new StandardDialRange(0.0, 50.0, GREEN);
        range3.setInnerRadius(0.52);
        range3.setOuterRadius(0.55);
        plot.addLayer(range3);

        DialPointer needle = new DialPointer.Pointer();
        plot.addLayer(needle);

        DialCap cap = new DialCap();
        cap.setRadius(0.10);
        plot.setCap(cap);

        return new JFreeChart(plot);
    }
}