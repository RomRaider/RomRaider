package enginuity.logger.innovate.plugin;

import enginuity.logger.ecu.EcuLogger;
import enginuity.logger.ecu.external.ExternalDataItem;
import enginuity.logger.ecu.external.ExternalDataSource;
import enginuity.logger.ecu.ui.swing.menubar.action.GenericPluginMenuAction;
import enginuity.logger.innovate.io.InnovateRunnerImpl;
import static enginuity.util.ThreadUtil.runAsDaemon;
import org.apache.log4j.Logger;

import javax.swing.Action;
import static java.util.Arrays.asList;
import java.util.List;

public final class Lc1DataSource implements ExternalDataSource {
    private static final Logger LOGGER = Logger.getLogger(Lc1DataSource.class);
    private InnovateSettings settings = new InnovateSettingsImpl();
    private Lc1DataItem dataItem = new Lc1DataItem();
    private InnovateRunnerImpl runner;

    public String getName() {
        return "Innovate LC-1";
    }

    public String getVersion() {
        return "0.01";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return asList(dataItem);
    }

    public Action getMenuAction(EcuLogger logger) {
        return new GenericPluginMenuAction(logger, this);
    }

    public void setPort(String port) {
        settings.setPort(port);
    }

    public String getPort() {
        return settings.getPort();
    }

    public void connect() {
        try {
            runner = new InnovateRunnerImpl(settings, dataItem);
            runAsDaemon(runner);
        } catch (Exception e) {
            LOGGER.warn("Error starting Innovate runner", e);
        }
    }

    public void disconnect() {
        if (runner != null) {
            runner.stop();
        }
    }
}
