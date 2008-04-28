package com.romraider.logger.aem.plugin;

import com.romraider.logger.aem.io.AemRunnerImpl;
import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.ecu.external.ExternalDataSource;
import com.romraider.logger.ecu.ui.swing.menubar.action.GenericPluginMenuAction;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import javax.swing.Action;
import static java.util.Arrays.asList;
import java.util.List;

public final class AemDataSource implements ExternalDataSource {
    private AemSettings settings = new AemSettingsImpl();
    private AemDataItem dataItem = new AemDataItem();
    private AemRunnerImpl runner;

    public String getName() {
        return "AEM UEGO";
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
        runner = new AemRunnerImpl(settings, dataItem);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) {
            runner.stop();
        }
    }
}
