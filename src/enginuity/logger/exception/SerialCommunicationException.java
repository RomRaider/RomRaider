package enginuity.logger.exception;

public final class SerialCommunicationException extends RuntimeException {

    public SerialCommunicationException() {
    }

    public SerialCommunicationException(String msg) {
        super(msg);
    }

    public SerialCommunicationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SerialCommunicationException(Throwable cause) {
        super(cause);
    }

}
