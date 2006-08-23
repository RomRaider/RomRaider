package enginuity.logger.exception;

public final class FileLoggerException extends RuntimeException {

    public FileLoggerException() {
    }

    public FileLoggerException(String string) {
        super(string);
    }

    public FileLoggerException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public FileLoggerException(Throwable throwable) {
        super(throwable);
    }

}
