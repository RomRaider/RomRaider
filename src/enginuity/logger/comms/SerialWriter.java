package enginuity.logger.comms;

public interface SerialWriter extends Runnable {

    void write(byte[] bytes);

    void close();

}
