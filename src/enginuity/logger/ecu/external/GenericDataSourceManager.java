package enginuity.logger.ecu.external;

import enginuity.logger.ecu.EcuLogger;
import static enginuity.util.ParamChecker.checkNotNull;

import javax.swing.Action;
import java.util.List;

public final class GenericDataSourceManager implements ExternalDataSource {
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
        reconnect();
        return dataSource.getDataItems();
    }

    public Action getMenuAction(EcuLogger logger) {
        return dataSource.getMenuAction(logger);
    }

    public void setPort(String port) {
        dataSource.setPort(port);
        reconnect();
    }

    public String getPort() {
        return dataSource.getPort();
    }

    public synchronized void connect() {
        dataSource.connect();
    }

    public synchronized void disconnect() {
        dataSource.disconnect();
    }

    private synchronized void reconnect() {
        disconnect();
        connect();
    }
}
