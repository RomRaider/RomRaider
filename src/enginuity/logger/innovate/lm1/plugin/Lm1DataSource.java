package enginuity.logger.innovate.lm1.plugin;

import enginuity.logger.ecu.EcuLogger;
import enginuity.logger.ecu.external.ExternalDataItem;
import enginuity.logger.ecu.external.ExternalDataSource;
import enginuity.logger.ecu.ui.swing.menubar.action.GenericPluginMenuAction;
import enginuity.logger.innovate.generic.io.InnovateRunnerImpl;
import enginuity.logger.innovate.generic.plugin.InnovateSettings;
import enginuity.logger.innovate.generic.plugin.InnovateSettingsImpl;
import static enginuity.util.ThreadUtil.runAsDaemon;
import javax.swing.Action;
import static java.util.Arrays.asList;
import java.util.List;

public final class Lm1DataSource implements ExternalDataSource {
    private InnovateSettings settings = new InnovateSettingsImpl();
    private Lm1DataItem dataItem = new Lm1DataItem();
    private InnovateRunnerImpl runner;

    public String getName() {
        return "Innovate LM-1";
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
        runner = new InnovateRunnerImpl("LM-1", settings, dataItem, 18);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) {
            runner.stop();
        }
    }
}
