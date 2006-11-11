package enginuity.logger.ui;

public interface MessageListener {

    void reportMessage(String message);

    void reportError(String error);

    void reportError(Exception e);
}
