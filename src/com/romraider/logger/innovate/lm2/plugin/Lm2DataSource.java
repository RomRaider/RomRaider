/*
 * Copyright (c) 2009. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.romraider.logger.innovate.lm2.plugin;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.ecu.external.ExternalDataSource;
import com.romraider.logger.innovate.generic.io.InnovateRunnerImpl;
import com.romraider.logger.innovate.generic.plugin.InnovateSettings;
import com.romraider.logger.innovate.generic.plugin.InnovateSettingsImpl;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import javax.swing.Action;
import static java.util.Arrays.asList;
import java.util.List;

public final class Lm2DataSource implements ExternalDataSource {
    private InnovateSettings settings = new InnovateSettingsImpl();
    private Lm2DataItem dataItem = new Lm2DataItem();
    private InnovateRunnerImpl runner;

    public String getName() {
        return "Innovate LM-2";
    }

    public String getVersion() {
        return "0.01";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return asList(dataItem);
    }

    public Action getMenuAction(EcuLogger logger) {
        return null;
    }

    public void setPort(String port) {
        settings.setPort(port);
    }

    public String getPort() {
        return settings.getPort();
    }

    public void connect() {
        runner = new InnovateRunnerImpl("LM-2", settings, dataItem, 18);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }
}