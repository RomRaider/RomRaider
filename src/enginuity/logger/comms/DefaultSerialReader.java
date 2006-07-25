package enginuity.logger.comms;

import enginuity.logger.exception.SerialCommunicationException;
import static enginuity.util.ParamChecker.checkNotNull;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import static gnu.io.SerialPortEvent.DATA_AVAILABLE;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;

// TODO: Add a read timeout

public final class DefaultSerialReader implements SerialReader, SerialPortEventListener {

    private static final int BUFFER_SIZE = 32;
    private final SerialPort port;
    private boolean stop = false;
    private byte[] readBuffer = new byte[0];

    public DefaultSerialReader(SerialPort port) {
        checkNotNull(port, "port");
        this.port = port;
        initListener();
    }

    public void run() {
        System.out.println("SerialReader thread started.");
        while (!stop) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("SerialReader thread stopped.");
    }

    public byte[] read() {
        byte[] tmp = new byte[readBuffer.length];
        System.arraycopy(readBuffer, 0, tmp, 0, readBuffer.length);
        readBuffer = new byte[0];
        return tmp;
    }

    public void close() {
        stop = true;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public void serialEvent(SerialPortEvent serialPortEvent) {
        checkNotNull(serialPortEvent, "serialPortEvent");
        if (serialPortEvent.getEventType() == DATA_AVAILABLE) {
            readBuffer = new byte[0];
            try {
                InputStream is = port.getInputStream();
                while (is.available() > 0) {
                    byte[] tmp = new byte[BUFFER_SIZE];
                    int i = is.read(tmp);
                    byte[] tmp2 = new byte[readBuffer.length + i];
                    System.arraycopy(readBuffer, 0, tmp2, 0, readBuffer.length);
                    System.arraycopy(tmp, 0, tmp2, readBuffer.length, i);
                    readBuffer = tmp2;
                }
            } catch (IOException e) {
                throw new SerialCommunicationException(e);
            }
        }
    }

    private void initListener() {
        try {
            port.addEventListener(this);
        } catch (TooManyListenersException e) {
            throw new SerialCommunicationException(e);
        }
        port.notifyOnDataAvailable(true);
    }

}
