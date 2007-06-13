package enginuity.logger.utec.plugin;

import enginuity.logger.ecu.EcuLogger;
import enginuity.logger.ecu.external.ExternalDataItem;
import enginuity.logger.ecu.external.ExternalDataSource;
import enginuity.logger.ecu.ui.swing.menubar.action.GenericPluginMenuAction;
import enginuity.logger.utec.commInterface.UtecInterface;

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
        UtecInterface.setPortChoice(port);
    }

    public String getPort() {
        return UtecInterface.getPortChoiceUsed();
    }

    // *****************************
    // Suggested Methods of interest
    // *****************************

    public void connect() {
        UtecInterface.openConnection();
    }

    public void disconnect() {
        UtecInterface.closeConnection();
    }

    public void startLogging() {
        UtecInterface.startLoggerDataFlow();
    }

    public void stopLogging() {
        UtecInterface.resetUtec();
    }
}
