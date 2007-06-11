package enginuity.logger.innovate.plugin;

import enginuity.logger.ecu.external.ExternalDataItem;
import static enginuity.util.ThreadUtil.runAsDaemon;

public final class InnovateControllerImpl implements InnovateController {
    private Lc1DataItem dataItem = new Lc1DataItem();

    public InnovateControllerImpl(InnovateConnection connection) {
        runAsDaemon(new InnovateRunnerImpl(connection, dataItem));
    }

    public synchronized ExternalDataItem getDataItem() {
        return dataItem;
    }
}
