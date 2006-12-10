package enginuity.logger.ui.handler.file;

public interface FileLogger {
    void start();

    void stop();

    void writeLine(String line);

    boolean isStarted();
}
