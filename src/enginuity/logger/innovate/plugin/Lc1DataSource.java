package enginuity.logger.innovate.plugin;

import enginuity.logger.ecu.EcuLogger;
import enginuity.logger.ecu.external.ExternalDataItem;
import enginuity.logger.ecu.external.ExternalDataSource;

import javax.swing.Action;
import static java.util.Arrays.asList;
import java.util.List;

public final class Lc1DataSource implements ExternalDataSource {
    private String port;

    public String getName() {
        return "Innovate LC-1";
    }

    public String getVersion() {
        return "0.01";
    }

    public List<ExternalDataItem> getDataItems() {
//        Lc1Connection connection = new Lc1Connection(new InnovateConnectionProperties(), port);
//        InnovateController controller = new InnovateControllerImpl(connection);
        InnovateController controller = new InnovateControllerImpl();
        return asList(controller.getDataItem());
    }

    public Action getMenuAction(EcuLogger logger) {
        return new GenericPluginMenuAction(logger, this);
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    public void connect() {
    }

    public void disconnect() {
    }

    public void startLogging() {
    }

    public void stopLogging() {
    }
}
