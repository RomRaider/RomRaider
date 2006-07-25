package enginuity.logger.comms;

import enginuity.logger.exception.SerialCommunicationException;
import static enginuity.util.ParamChecker.checkNotNull;
import gnu.io.SerialPort;

import java.io.IOException;

public final class DefaultSerialWriter implements SerialWriter {
    private final SerialPort port;
    private boolean stop = false;

    public DefaultSerialWriter(SerialPort port) {
        checkNotNull(port, "port");
        this.port = port;
    }

    public void run() {
        System.out.println("SerialWriter thread started.");
        while (!stop) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("SerialWriter thread stopped.");
    }

    public void write(byte[] bytes) {
        try {
            port.getOutputStream().write(bytes);
        } catch (IOException e) {
            throw new SerialCommunicationException(e);
        }
    }

    public void close() {
        stop = true;
    }

}
