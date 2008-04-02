package enginuity.logger.ecu.ui.tab.maf;

import enginuity.logger.ecu.definition.EcuParameter;
import enginuity.logger.ecu.definition.EcuSwitch;
import enginuity.logger.ecu.definition.ExternalData;
import enginuity.logger.ecu.ui.tab.Tab;
import java.util.List;

public interface MafTab extends Tab {
    boolean isRecordData();

    boolean isValidClOl(double value);

    boolean isValidAfr(double value);

    boolean isValidRpm(double value);

    boolean isValidMaf(double value);

    boolean isValidCoolantTemp(double value);

    boolean isValidIntakeAirTemp(double value);

    boolean isValidTipInThrottle(double value);

    void addData(double mafv, double correction);

    void setEcuParams(List<EcuParameter> params);

    void setEcuSwitches(List<EcuSwitch> switches);

    void setExternalDatas(List<ExternalData> external);
}
