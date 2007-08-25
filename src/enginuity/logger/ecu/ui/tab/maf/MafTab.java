package enginuity.logger.ecu.ui.tab.maf;

import enginuity.logger.ecu.ui.tab.Tab;

public interface MafTab extends Tab {
    boolean isRecordData();

    boolean isValidClOl(double value);

    boolean isValidAfr(double value);

    boolean isValidRpm(double value);

    boolean isValidMaf(double value);

    boolean isValidCoolantTemp(double value);

    void addData(double mafv, double correction);
}
