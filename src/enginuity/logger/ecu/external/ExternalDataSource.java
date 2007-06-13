package enginuity.logger.ecu.external;

import enginuity.logger.ecu.EcuLogger;

import javax.swing.Action;
import java.util.List;

public interface ExternalDataSource {

    String getName();
    
    String getVersion();

    List<? extends ExternalDataItem> getDataItems();
    
    Action getMenuAction(EcuLogger logger);

    void setPort(String port);

    String getPort();

    // *****************************
    // Suggested Methods of interest
    // *****************************

    public void connect();

    public void disconnect();

    public void startLogging();

    public void stopLogging();
}
