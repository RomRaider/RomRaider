package com.romraider.logger.innovate.generic.io;

import com.romraider.logger.innovate.generic.plugin.DataListener;
import com.romraider.logger.innovate.generic.plugin.InnovateSettings;

public final class InnovateRunnerImpl implements InnovateRunner {
    private final InnovateConnection connection;
    private final DataListener listener;
    private boolean stop;

    public InnovateRunnerImpl(String device, InnovateSettings innovateSettings, DataListener listener, int responseLength) {
        connection = new InnovateConnectionImpl(device, new InnovateConnectionProperties(), innovateSettings.getPort(), responseLength);
        this.listener = listener;
    }

    public void run() {
        try {
            while (!stop) {
                byte[] bytes = connection.read();
                listener.setBytes(bytes);
            }
        } finally {
            connection.close();
        }
    }

    public void stop() {
        stop = true;
    }
}
