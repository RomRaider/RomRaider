package enginuity.logger.ui;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.Timeline;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ChartScrollBar extends JScrollBar implements AdjustmentListener, AxisChangeListener, MouseListener, DatasetChangeListener {
    private JFreeChart chart;
    private XYPlot plot;
    private double ratio;
    private boolean updating = false;

    public ChartScrollBar(int orientation, JFreeChart chart) {
        this(orientation, chart, null);
    }

    public ChartScrollBar(int orientation, JFreeChart chart, XYPlot plot) {
        super(orientation);
        this.chart = chart;
        if (plot == null) {
            this.plot = chart.getXYPlot();
        } else {
            this.plot = plot;
        }
        if (getXYPlot() != null && getValueAxis() != null) {
            getValueAxis().addChangeListener(this);
            addAdjustmentListener(this);
            if (getXYPlot().getDataset() != null) {
                getXYPlot().getDataset().addChangeListener(this);
            }
            axisUpdate();
            addMouseListener(this);
        }
    }

    public XYPlot getXYPlot() {
        return plot;
    }

    public ValueAxis getValueAxis() {
        if (orientation == VERTICAL) {
            return getXYPlot().getRangeAxis();
        }
        return getXYPlot().getDomainAxis();
    }

    public Dataset getDataset() {
        return getXYPlot().getDataset();
    }

    public Range getDataRange() {
        return getXYPlot().getDataRange(getValueAxis());
    }

    public double getDataMinimum() {
        return getDataRange().getLowerBound();
    }

    public double getDataMaximum() {
        return getDataRange().getUpperBound();
    }

    public double getViewMinimum() {
        return getValueAxis().getLowerBound();
    }

    public double getViewMaximum() {
        return getValueAxis().getUpperBound();
    }

    public double getViewLength() {
        return getValueAxis().getRange().getLength();
    }

    public double getDisplayMaximum() {
        return getDataMaximum();//Math.max(getDataMaximum(), getViewMaximum());
    }

    public double getDisplayMinimum() {
        return getDataMinimum();//Math.min(getDataMinimum(), getViewMinimum());
    }

    private double displayMin;
    private double displayMax;
    private double viewLength;
    private static int STEPS = 100000; // 1000000 could be Integer.MAX_VALUE if you like, but this makes debugging a little easier
    Color oldColor;

    public void axisUpdate() {
        ValueAxis va = getValueAxis();
        if (va.isAutoRange()) {
            if (oldColor == null) {
                oldColor = getBackground();
            }
            setBackground(oldColor.brighter());
        } else if (oldColor != null) {
            setBackground(oldColor);
            oldColor = null;
        }
        if (updating) {
            return;
        }
        updating = true;
        displayMin = 0;
        displayMax = 0;
        viewLength = 0;
        double viewMin = 0;
        double viewMax = 0;
        ratio = 1;
        Range dataRange = getDataRange();
        if (dataRange != null) {
            displayMin = getDisplayMinimum();
            displayMax = getDisplayMaximum();
            viewMin = getViewMinimum();
            viewMax = getViewMaximum();
            //ValueAxis va = getValueAxis();
            if (va instanceof DateAxis) {
                Timeline tl = ((DateAxis) va).getTimeline();
                displayMin = tl.toTimelineValue((long) displayMin);
                displayMax = tl.toTimelineValue((long) displayMax);
                viewMin = tl.toTimelineValue((long) viewMin);
                viewMax = tl.toTimelineValue((long) viewMax);
            }
            viewLength = viewMax - viewMin;
            ratio = STEPS / (displayMax - displayMin);
        }

        int newMin = 0;
        int newMax = STEPS;
        int newExtent = (int) (viewLength * ratio);
        int newValue;
        if (orientation == VERTICAL) {
            newValue = (int) ((displayMax - viewMax) * ratio);
        } else {
            newValue = (int) ((viewMin - displayMin) * ratio);
        }
        //System.out.println("ChartScrollBar.axisUpdate(): newValue: " + newValue + " newExtent: " + newExtent + " newMin: " + newMin + " newMax: " + newMax);
        setValues(newValue, newExtent, newMin, newMax);
        updating = false;
    }

    public void axisChanged(AxisChangeEvent event) {
        //System.out.println("ChartScrollBar.axisChanged()");
        axisUpdate();
    }

    public void datasetChanged(DatasetChangeEvent event) {
        //System.out.println("ChartScrollBar.datasetChanged()");
        axisUpdate();
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (updating) {
            return;
        }
        updating = true;
        double start, end;
        if (orientation == VERTICAL) {
            end = displayMax - (getValue() / ratio);
            start = end - viewLength;
        } else {
            start = getValue() / ratio + displayMin;
            end = start + viewLength;
        }

        if (end > start) {
            ValueAxis va = getValueAxis();
            if (va instanceof DateAxis) {
                Timeline tl = ((DateAxis) va).getTimeline();
                start = tl.toMillisecond((long) start);
                end = tl.toMillisecond((long) end);
                //System.out.println("********** converting start=" + new java.util.Date((long)start) + " end=" + new java.util.Date((long)end) + " **********");
            }
            getValueAxis().setRange(start, end);
        }
        updating = false;
    }

    public void zoomFull() {
        getValueAxis().setAutoRange(true);
        //getValueAxis().autoAdjustRange();
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            zoomFull();
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
}