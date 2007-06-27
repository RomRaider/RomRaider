package enginuity.logger.aem.io;

public interface AemConnection {
    byte[] read();

    void close();
}
