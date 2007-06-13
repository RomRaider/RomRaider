package enginuity.logger.innovate.io;

import enginuity.logger.innovate.plugin.DataListener;
import enginuity.logger.innovate.plugin.InnovateSettings;

public final class InnovateRunnerImpl implements InnovateRunner {
    private final InnovateConnection connection;
    private final DataListener listener;
    private boolean stop;

    public InnovateRunnerImpl(InnovateSettings innovateSettings, DataListener listener) {
        connection = new Lc1Connection(new InnovateConnectionProperties(), innovateSettings.getPort());
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
