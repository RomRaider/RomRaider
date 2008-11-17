package com.romraider.logger.ecu.external;

import static com.romraider.util.ParamChecker.checkNotNull;
import com.romraider.util.Stoppable;
import static com.romraider.util.ThreadUtil.sleep;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class GenericDataSourceConnector implements Stoppable {
    private static final Logger LOGGER = getLogger(GenericDataSourceConnector.class);
    private final ExternalDataSource dataSource;
    private boolean stop;

    public GenericDataSourceConnector(ExternalDataSource dataSource) {
        checkNotNull(dataSource);
        this.dataSource = dataSource;
    }

    public void run() {
        LOGGER.info(dataSource.getName() + ": connecting...");
        while (!stop) {
            try {
                dataSource.connect();
                LOGGER.info(dataSource.getName() + ": connected.");
                break;
            } catch (Exception e) {
                LOGGER.error(dataSource.getName() + ": connect error", e);
                sleep(500L);
            }
        }
    }

    public void stop() {
        stop = true;
    }
}
