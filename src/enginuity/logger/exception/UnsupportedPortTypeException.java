package enginuity.logger.exception;

public final class UnsupportedPortTypeException extends RuntimeException {

    public UnsupportedPortTypeException() {
    }

    public UnsupportedPortTypeException(String string) {
        super(string);
    }

    public UnsupportedPortTypeException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public UnsupportedPortTypeException(Throwable throwable) {
        super(throwable);
    }
}
