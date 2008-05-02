package com.romraider.util;

import static org.apache.log4j.PropertyConfigurator.configureAndWatch;

public final class LogManager {

    private LogManager() {
        throw new UnsupportedOperationException();
    }

    public static void initDebugLogging() {
        configureAndWatch("log4j.properties");
    }
}
