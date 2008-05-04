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
