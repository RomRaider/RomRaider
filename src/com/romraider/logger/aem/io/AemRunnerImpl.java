package com.romraider.logger.aem.io;

import com.romraider.logger.aem.plugin.AemSettings;
import com.romraider.logger.aem.plugin.DataListener;

public final class AemRunnerImpl implements AemRunner {
    private final AemConnection connection;
    private final DataListener listener;
    private boolean stop;

    public AemRunnerImpl(AemSettings aemSettings, DataListener listener) {
        connection = new AemConnectionImpl(new AemConnectionProperties(), aemSettings.getPort());
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
