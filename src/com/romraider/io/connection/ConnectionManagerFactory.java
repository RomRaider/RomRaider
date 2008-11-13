package com.romraider.io.connection;

import com.romraider.io.j2534.api.J2534ConnectionManager;
import com.romraider.io.serial.connection.SerialConnectionManager;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class ConnectionManagerFactory {
    private static final Logger LOGGER = getLogger(ConnectionManagerFactory.class);

    private ConnectionManagerFactory() {
    }

    public static ConnectionManager getManager(String portName, ConnectionProperties connectionProperties) {
        try {
            return new J2534ConnectionManager(connectionProperties);
        } catch (Throwable t) {
            LOGGER.info("J2534 connection not available [" + t.getClass().getName() + ": " + t.getMessage() + "], trying serial connection...");
            return new SerialConnectionManager(portName, connectionProperties);
        }
    }
}
