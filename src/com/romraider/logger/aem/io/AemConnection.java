package com.romraider.logger.aem.io;

public interface AemConnection {
    byte[] read();

    void close();
}
