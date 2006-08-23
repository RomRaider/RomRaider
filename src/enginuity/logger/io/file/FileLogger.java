package enginuity.logger.io.file;

public interface FileLogger {
    void start();

    void stop();

    void writeLine(String line);

    boolean isStarted();
}
