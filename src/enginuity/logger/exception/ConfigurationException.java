package enginuity.logger.exception;

public final class ConfigurationException extends RuntimeException {

    public ConfigurationException() {
    }

    public ConfigurationException(String string) {
        super(string);
    }

    public ConfigurationException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public ConfigurationException(Throwable throwable) {
        super(throwable);
    }

}
