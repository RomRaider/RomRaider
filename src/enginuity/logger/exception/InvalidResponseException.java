package enginuity.logger.exception;

public final class InvalidResponseException extends RuntimeException {

    public InvalidResponseException() {
    }

    public InvalidResponseException(String string) {
        super(string);
    }

    public InvalidResponseException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public InvalidResponseException(Throwable throwable) {
        super(throwable);
    }

}
