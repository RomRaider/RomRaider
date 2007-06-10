package enginuity.logger.innovate.plugin;

import enginuity.logger.ecu.external.ExternalDataItem;
import static enginuity.util.ThreadUtil.runAsDaemon;

public final class InnovateControllerImpl implements InnovateController, DataListener {
    private Lc1Convertor convertor = new Lc1ConvertorImpl();
    private byte[] bytes;

    public InnovateControllerImpl(InnovateConnection connection) {
        runAsDaemon(new InnovateRunnerImpl(connection, this));
    }

    public synchronized ExternalDataItem getDataItem() {
        if (bytes != null) {
            return new Lc1DataItem(convertor.convert(bytes));
        } else {
            return new Lc1DataItem(0.0);
        }
    }

    public synchronized void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
