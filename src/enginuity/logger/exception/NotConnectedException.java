package enginuity.logger.exception;

public final class NotConnectedException extends RuntimeException {
    public NotConnectedException() {
    }

    public NotConnectedException(String string) {
        super(string);
    }

    public NotConnectedException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public NotConnectedException(Throwable throwable) {
        super(throwable);
    }
}
