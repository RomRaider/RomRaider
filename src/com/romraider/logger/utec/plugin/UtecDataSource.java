package com.romraider.logger.utec.plugin;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.ecu.external.ExternalDataSource;
import com.romraider.logger.ecu.ui.swing.menubar.action.GenericPluginMenuAction;
import static com.romraider.logger.utec.commInterface.UtecInterface.closeConnection;
import static com.romraider.logger.utec.commInterface.UtecInterface.getPortChoiceUsed;
import static com.romraider.logger.utec.commInterface.UtecInterface.openConnection;
import static com.romraider.logger.utec.commInterface.UtecInterface.resetUtec;
import static com.romraider.logger.utec.commInterface.UtecInterface.setPortChoice;
import static com.romraider.logger.utec.commInterface.UtecInterface.startLoggerDataFlow;
import javax.swing.Action;
import java.util.ArrayList;
import java.util.List;

//NOTE: This class is instantiated via a no-args constructor.
public final class UtecDataSource implements ExternalDataSource {
    private ArrayList<ExternalDataItem> externalDataItems = new ArrayList<ExternalDataItem>();

    public UtecDataSource() {
        externalDataItems.add(new AfrExternalDataItem());
        externalDataItems.add(new PsiExternalDataItem());
        externalDataItems.add(new KnockExternalDataItem());
        externalDataItems.add(new LoadExternalDataItem());
    }

    public String getName() {
        return "UTEC";
    }

    public String getVersion() {
        return "0.01";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        System.out.println("External TXS data items requested.");
        return externalDataItems;
    }

    public Action getMenuAction(EcuLogger logger) {
        return new GenericPluginMenuAction(logger, this);
    }

    public void setPort(String port) {
        setPortChoice(port);
    }

    public String getPort() {
        return getPortChoiceUsed();
    }

    public void connect() {
        openConnection();
        startLoggerDataFlow();
    }

    public void disconnect() {
        resetUtec();
        closeConnection();
    }
}
