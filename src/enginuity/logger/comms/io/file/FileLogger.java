package enginuity.logger.comms.io.file;

public interface FileLogger {
    void start();

    void stop();

    void writeLine(String line);

    boolean isStarted();
}
