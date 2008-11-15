package com.romraider.io.connection;

import com.romraider.io.j2534.api.J2534ConnectionManager;
import com.romraider.io.serial.connection.SerialConnectionManager;
import static com.romraider.util.proxy.Proxifier.proxy;
import com.romraider.util.proxy.TimerWrapper;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class ConnectionManagerFactory {
    private static final Logger LOGGER = getLogger(ConnectionManagerFactory.class);
    private static final boolean ENABLE_TIMER = false;

    private ConnectionManagerFactory() {
    }

    public static ConnectionManager getManager(String portName, ConnectionProperties connectionProperties) {
        ConnectionManager manager = manager(portName, connectionProperties);
        if (ENABLE_TIMER) return proxy(manager, TimerWrapper.class);
        return manager;
    }

    private static ConnectionManager manager(String portName, ConnectionProperties connectionProperties) {
        try {
            return new J2534ConnectionManager(connectionProperties);
        } catch (Throwable t) {
            LOGGER.info("J2534 connection not available [" + t.getClass().getName() + ": " + t.getMessage() + "], trying serial connection...");
            return new SerialConnectionManager(portName, connectionProperties);
        }
    }
}
