package enginuity.logger.ecu.ui.tab;

import jamlab.Polyfit;
import jamlab.Polyval;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

public final class XYTrendline extends AbstractXYDataset {
    private List<XYDataItem> items = new ArrayList<XYDataItem>();
    private double[] xVals = new double[0];
    private double[] yPoly = new double[0];
    private Polyfit polyfit;

    public int getSeriesCount() {
        return 1;
    }

    public Comparable getSeriesKey(int seriesIndex) {
        return "foo";
    }

    public synchronized int getItemCount(int seriesIndex) {
        return yPoly.length;
    }

    public synchronized Number getY(int seriesIndex, int item) {
        return yPoly[item];
    }

    public synchronized Number getX(int seriesIndex, int item) {
        return xVals[item];
    }

    public synchronized void update(XYSeries series, int order) {
        if (series.getItemCount() <= order) return;
        items = new ArrayList<XYDataItem>(series.getItems());
        xVals = new double[items.size()];
        double[] yVals = new double[items.size()];
        for (int i = 0; i < items.size(); i++) {
            XYDataItem dataItem = items.get(i);
            xVals[i] = dataItem.getX().doubleValue();
            yVals[i] = dataItem.getY().doubleValue();
        }
        try {
            polyfit = new Polyfit(xVals, yVals, order);
            yPoly = calculate(xVals);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    public synchronized double[] calculate(double[] x) {
        if (polyfit == null) throw new IllegalStateException("Interpolation required");
        Polyval polyval = new Polyval(x, polyfit);
        return polyval.getYout();
    }

    public synchronized Polyfit getPolyFit() {
        if (polyfit == null) throw new IllegalStateException("Interpolation required");
        return polyfit;
    }

    public synchronized void clear() {
        items.clear();
        xVals = new double[0];
        yPoly = new double[0];
        polyfit = null;
    }
}
