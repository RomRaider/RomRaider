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

//This object defines the scaling factor and offset for calculating real values

package enginuity.maps;

import java.io.Serializable;

public class Scale implements Serializable {

    public static final int LINEAR = 1;
    public static final int INVERSE = 2;

    private String name = "Default";
    private String unit = "0x";
    private String expression = "x";
    private String byteExpression = "x";
    private String format = "#";
    private double coarseIncrement = 2;
    private double fineIncrement = 1;
    private double min = 0;
    private double max = 0;
    private Table table;

    public Scale() {
    }

    public String toString() {
        return "\n      ---- Scale ----" +
                "\n      Name: " + getName() +
                "\n      Expression: " + getExpression() +
                "\n      Unit: " + getUnit() +
                "\n      ---- End Scale ----";

    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public double getCoarseIncrement() {
        return coarseIncrement;
    }

    public void setCoarseIncrement(double increment) {
        this.coarseIncrement = increment;
    }

    public boolean isReady() {
        if (unit == null) {
            return false;
        } else if (expression == null) {
            return false;
        } else if (format == null) {
            return false;
        } else if (coarseIncrement < 1) {
            return false;
        }

        return true;
    }

    public String getByteExpression() {
        return byteExpression;
    }

    public void setByteExpression(String byteExpression) {
        this.byteExpression = byteExpression;
    }

    public double getFineIncrement() {
        return fineIncrement;
    }

    public void setFineIncrement(double fineIncrement) {
        this.fineIncrement = fineIncrement;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }
}