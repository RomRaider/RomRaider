/*
 * Class defines an exception to be thrown in the event
 * of serial connection troubles
 */

package enginuity.logger.utec.comm;

public class SerialConnectionException extends Exception {

    /**
     * Constructs a <code>SerialConnectionException</code>
     * with the specified detail message.
     *
     * @param   s   the detail message.
     */
    public SerialConnectionException(String str) {
        super(str);
    }

    /**
     * Constructs a <code>SerialConnectionException</code>
     * with no detail message.
     */
    public SerialConnectionException() {
        super();
    }
}
