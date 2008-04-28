package com.romraider.util;

import org.apache.log4j.PropertyConfigurator;

public final class LogManager {

    private LogManager() {
        throw new UnsupportedOperationException();
    }

    public static void initLogging() {
        PropertyConfigurator.configure("log4j.properties");
    }
}
