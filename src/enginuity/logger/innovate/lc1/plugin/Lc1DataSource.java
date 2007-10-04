package enginuity.logger.innovate.lc1.plugin;

import static java.util.Arrays.asList;
import java.util.List;
import javax.swing.Action;
import enginuity.logger.ecu.EcuLogger;
import enginuity.logger.ecu.external.ExternalDataItem;
import enginuity.logger.ecu.external.ExternalDataSource;
import enginuity.logger.ecu.ui.swing.menubar.action.GenericPluginMenuAction;
import enginuity.logger.innovate.generic.io.InnovateRunnerImpl;
import enginuity.logger.innovate.generic.plugin.InnovateSettings;
import enginuity.logger.innovate.generic.plugin.InnovateSettingsImpl;
import static enginuity.util.ThreadUtil.runAsDaemon;

public final class Lc1DataSource implements ExternalDataSource {
    private InnovateSettings settings = new InnovateSettingsImpl();
    private Lc1DataItem dataItem = new Lc1DataItem();
    private InnovateRunnerImpl runner;

    public String getName() {
        return "Innovate LC-1";
    }

    public String getVersion() {
        return "0.02";
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
        runner = new InnovateRunnerImpl(settings, dataItem);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) {
            runner.stop();
        }
    }
}
