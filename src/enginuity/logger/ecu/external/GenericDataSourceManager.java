package enginuity.logger.ecu.external;

import enginuity.logger.ecu.EcuLogger;
import static enginuity.util.ParamChecker.checkNotNull;
import org.apache.log4j.Logger;

import javax.swing.Action;
import java.util.List;

public final class GenericDataSourceManager implements ExternalDataSource {
    private static final Logger LOGGER = Logger.getLogger(GenericDataSourceManager.class);
    private final ExternalDataSource dataSource;

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
            LOGGER.info(dataSource.getName() + ": connecting");
            dataSource.connect();
        } catch (Exception e) {
            LOGGER.error("External Datasource connect error", e);
        }
    }

    public synchronized void disconnect() {
        try {
            LOGGER.info(dataSource.getName() + ": disconnecting");
            dataSource.disconnect();
        } catch (Exception e) {
            LOGGER.error("External datasource disconnect error", e);
        }
    }

    private synchronized void reconnect() {
        disconnect();
        connect();
    }
}
