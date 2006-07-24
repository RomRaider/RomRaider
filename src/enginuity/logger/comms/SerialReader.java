package enginuity.logger.comms;

public interface SerialReader extends Runnable {

    byte[] read();

    void close();

}
