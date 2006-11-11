package enginuity.logger.exception;

public final class PortNotFoundException extends RuntimeException {

    public PortNotFoundException() {
    }

    public PortNotFoundException(String string) {
        super(string);
    }

    public PortNotFoundException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public PortNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
