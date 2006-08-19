package enginuity.logger.ui.handler;

import enginuity.logger.definition.EcuParameter;
import enginuity.Settings;

public final class FileUpdateHandler implements ParameterUpdateHandler {
    private final Settings settings;

    public FileUpdateHandler(Settings settings) {
        this.settings = settings;
    }

    public void registerParam(EcuParameter ecuParam) {
    }

    public void handleParamUpdate(EcuParameter ecuParam, byte[] value, long timestamp) {
    }

    public void deregisterParam(EcuParameter ecuParam) {
    }

}
