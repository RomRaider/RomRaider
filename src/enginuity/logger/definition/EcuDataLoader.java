package enginuity.logger.definition;

import java.util.List;

public interface EcuDataLoader {

    void loadFromXml(String loggerConfigFilePath, String protocol);

    List<EcuParameter> getEcuParameters();

    List<EcuSwitch> getEcuSwitches();

}
