package enginuity.logger.ecu.external;

import enginuity.logger.ecu.EcuLogger;

import javax.swing.Action;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class TestExternalDataSource implements ExternalDataSource {

    public String getName() {
        return "Test Sensor";
    }

    public String getVersion() {
        return "0.0001";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        ExternalDataItem dataItem = new ExternalDataItem() {
            private Random random = new Random(System.currentTimeMillis());

            public String getName() {
                return "Test Data";
            }

            public String getDescription() {
                return "Test External Data Item";
            }

            public String getUnits() {
                return "?";
            }

            public double getData() {
                return random.nextDouble();
            }
        };
        List<ExternalDataItem> dataItems = new ArrayList<ExternalDataItem>();
        dataItems.add(dataItem);
        return dataItems;
    }

    public Action getMenuAction(EcuLogger logger) {
        return null;
    }

    public void setPort(String port) {
    }

    public String getPort() {
        return null;
    }

    // *****************************
    // Suggested Methods of interest
    // *****************************

    public void connect(){
    	
    }
    
    public void disconnect(){
    	
    }
    
    public void startLogging(){
    	
    }
    
    public void stopLogging(){
    	
    }
}
