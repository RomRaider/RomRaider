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
