package com.romraider.logger.ecu.ui.tab.injector;

import com.romraider.logger.ecu.definition.EcuParameter;
import com.romraider.logger.ecu.definition.EcuSwitch;
import com.romraider.logger.ecu.definition.ExternalData;
import com.romraider.logger.ecu.ui.tab.Tab;
import java.util.List;

public interface InjectorTab extends Tab {

    double getFuelStoichAfr();

    double getFuelDensity();

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