package enginuity.logger.exception;

public final class UnsupportedProtocolException extends RuntimeException {

    public UnsupportedProtocolException() {
    }

    public UnsupportedProtocolException(String string) {
        super(string);
    }

    public UnsupportedProtocolException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public UnsupportedProtocolException(Throwable throwable) {
        super(throwable);
    }

}
