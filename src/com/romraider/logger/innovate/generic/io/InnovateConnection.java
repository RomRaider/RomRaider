package com.romraider.logger.innovate.generic.io;

public interface InnovateConnection {
    byte[] read();

    void close();
}
