package com.romraider.logger.ecu.external;

import com.romraider.logger.ecu.EcuLogger;
import javax.swing.Action;
import java.util.List;

public interface ExternalDataSource {

    String getName();

    String getVersion();

    List<? extends ExternalDataItem> getDataItems();

    Action getMenuAction(EcuLogger logger);

    void setPort(String port);

    String getPort();

    public void connect();

    public void disconnect();
}
