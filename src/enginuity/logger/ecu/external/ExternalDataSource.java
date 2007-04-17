package enginuity.logger.ecu.external;

import java.util.List;

public interface ExternalDataSource {

    String getName();
    
    String getVersion();

    List<ExternalDataItem> getDataItems();
    
    
    // *****************************
    // Suggested Methods of interest
    // *****************************
    
    public void setCommPortChoice(String commPort);
    
    public void connect();
    
    public void disconnect();
    
    public void startLogging();
    
    public void stopLogging();
}
