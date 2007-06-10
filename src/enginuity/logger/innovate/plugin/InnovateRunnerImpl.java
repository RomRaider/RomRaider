package enginuity.logger.innovate.plugin;

import static enginuity.util.HexUtil.asHex;

public final class InnovateRunnerImpl implements InnovateRunner {
    private final InnovateConnection connection;
    private final DataListener listener;
    private boolean stop;

    public InnovateRunnerImpl(InnovateConnection connection, DataListener listener) {
        this.connection = connection;
        this.listener = listener;
    }

    public void run() {
        try {
            while (!stop) {
                byte[] bytes = connection.read();
                System.out.println("Read bytes = " + asHex(bytes));
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
