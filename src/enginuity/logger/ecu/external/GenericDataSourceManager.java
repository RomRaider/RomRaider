package enginuity.logger.ecu.external;

import enginuity.logger.ecu.EcuLogger;
import static enginuity.util.ParamChecker.checkNotNull;
import org.apache.log4j.Logger;
import javax.swing.Action;
import java.util.List;

public final class GenericDataSourceManager implements ExternalDataSource {
    private static final Logger LOGGER = Logger.getLogger(GenericDataSourceManager.class);
    private final ExternalDataSource dataSource;
    private int connectCount;

    public GenericDataSourceManager(ExternalDataSource dataSource) {
        checkNotNull(dataSource, "dataSource");
        this.dataSource = dataSource;
    }

    public String getName() {
        return dataSource.getName();
    }

    public String getVersion() {
        return dataSource.getVersion();
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return dataSource.getDataItems();
    }

    public Action getMenuAction(EcuLogger logger) {
        return dataSource.getMenuAction(logger);
    }

    public void setPort(String port) {
        LOGGER.info(dataSource.getName() + ": port " + port + " selected");
        dataSource.setPort(port);
        reconnect();
    }

    public String getPort() {
        return dataSource.getPort();
    }

    public synchronized void connect() {
        try {
            if (connectCount == 0) {
                LOGGER.info(dataSource.getName() + ": connecting");
                dataSource.connect();
            }
            connectCount++;
            LOGGER.trace("Connect count [" + dataSource.getName() + "]: " + connectCount);
        } catch (Exception e) {
            LOGGER.error("External Datasource [" + dataSource.getName() + "] connect error", e);
        }
    }

    public synchronized void disconnect() {
        try {
            if (connectCount == 1) {
                LOGGER.info(dataSource.getName() + ": disconnecting");
                dataSource.disconnect();
            }
            connectCount = connectCount > 0 ? connectCount - 1 : 0;
            LOGGER.trace("Connect count [" + dataSource.getName() + "]: " + connectCount);
        } catch (Exception e) {
            LOGGER.error("External Datasource [" + dataSource.getName() + "] disconnect error", e);
        }
    }

    private synchronized void reconnect() {
        disconnect();
        connect();
    }
}
