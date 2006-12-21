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

package enginuity.util;

import enginuity.maps.DataCell;
import enginuity.maps.Table1D;

public final class TableAxisUtil {

    private TableAxisUtil() {
    }

    public static AxisRange getLiveDataRangeForAxis(Table1D axis) {
        int startIdx = 0;
        int endIdx = 0;
        double liveAxisValue = axis.getLiveValue();
        DataCell[] data = axis.getData();
        for (int i = 0; i < data.length; i++) {
            DataCell cell = data[i];
            double axisValue = cell.getValue();
            if (liveAxisValue == axisValue) {
                startIdx = i;
                endIdx = i;
                break;
            } else if (liveAxisValue < axisValue) {
                startIdx = i - 1;
                endIdx = i;
                break;
            } else {
                startIdx = i;
                endIdx = i + 1;
            }
        }
        if (startIdx < 0) {
            startIdx = 0;
        }
        if (startIdx >= data.length) {
            startIdx = data.length - 1;
        }
        if (endIdx < 0) {
            endIdx = 0;
        }
        if (endIdx >= data.length) {
            endIdx = data.length - 1;
        }
        return new AxisRange(startIdx, endIdx);
    }

}
