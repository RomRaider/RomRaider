package enginuity.logger.innovate.plugin;

import enginuity.logger.ecu.external.ExternalDataItem;
import enginuity.logger.ecu.external.ExternalDataSource;
import enginuity.logger.innovate.io.InnovateConnectionProperties;

import static java.util.Arrays.asList;
import java.util.List;

public final class Lc1DataSource implements ExternalDataSource {
    private InnovateController controller;
    private List<ExternalDataItem> dataItems;

    public Lc1DataSource() {
        Lc1Connection connection = new Lc1Connection(new InnovateConnectionProperties(), "COM6");
        controller = new InnovateControllerImpl(connection);
        dataItems = asList(controller.getDataItem());
    }

    public String getName() {
        return "Innovate LC-1 Datasource";
    }

    public String getVersion() {
        return "0.01";
    }

    public List<ExternalDataItem> getDataItems() {
        return dataItems;
    }

    public void setCommPortChoice(String commPort) {
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
