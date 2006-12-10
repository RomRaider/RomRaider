package enginuity.logger.definition;

import enginuity.logger.definition.xml.LoggerDefinitionHandler;
import enginuity.logger.exception.ConfigurationException;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;
import static enginuity.util.SaxParserFactory.getSaxParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class EcuDataLoaderImpl implements EcuDataLoader {
    private List<EcuParameter> ecuParameters = new ArrayList<EcuParameter>();
    private List<EcuSwitch> ecuSwitches = new ArrayList<EcuSwitch>();

    public void loadFromXml(String loggerConfigFilePath, String protocol) {
        checkNotNullOrEmpty(loggerConfigFilePath, "loggerConfigFilePath");
        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(new File(loggerConfigFilePath)));
            try {
                LoggerDefinitionHandler handler = new LoggerDefinitionHandler(protocol);
                getSaxParser().parse(inputStream, handler);
                ecuParameters = handler.getEcuParameters();
                ecuSwitches = handler.getEcuSwitches();
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    public List<EcuParameter> getEcuParameters() {
        return ecuParameters;
    }

    public List<EcuSwitch> getEcuSwitches() {
        return ecuSwitches;
    }

}
