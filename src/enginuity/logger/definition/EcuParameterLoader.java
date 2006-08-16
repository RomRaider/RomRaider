package enginuity.logger.definition;

import java.util.List;

public interface EcuParameterLoader {

    List<EcuParameter> loadFromXml(String loggerConfigFilePath, String protocol);

}
