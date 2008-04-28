package com.romraider.logger.innovate.lm1.plugin;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.ecu.external.ExternalDataSource;
import com.romraider.logger.ecu.ui.swing.menubar.action.GenericPluginMenuAction;
import com.romraider.logger.innovate.generic.io.InnovateRunnerImpl;
import com.romraider.logger.innovate.generic.plugin.InnovateSettings;
import com.romraider.logger.innovate.generic.plugin.InnovateSettingsImpl;
import static com.romraider.util.ThreadUtil.runAsDaemon;
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
