package enginuity.logger.innovate.plugin;

public interface InnovateConnection {
    byte[] read();

    void close();
}
