package enginuity.logger.ui;

import enginuity.logger.definition.EcuData;

import java.util.Comparator;

public final class EcuDataComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        EcuData ecuData1 = (EcuData) o1;
        EcuData ecuData2 = (EcuData) o2;
        return ecuData1.getName().compareTo(ecuData2.getName());
    }

}
