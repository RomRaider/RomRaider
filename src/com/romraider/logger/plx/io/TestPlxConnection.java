package com.romraider.logger.plx.io;

import com.romraider.io.serial.connection.SerialConnection;
import static com.romraider.util.ThreadUtil.sleep;

public final class TestPlxConnection implements SerialConnection {
    private final byte[] data = {(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x01, 0x00, 0x00, 0x0f, 0x40};
    private int index;

    public void write(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    public int available() {
        return 1;
    }

    public void read(byte[] bytes) {
        if (bytes.length != 1) throw new IllegalArgumentException();
        if (index >= data.length) index = 0;
        bytes[0] = data[index++];
        sleep(10);
    }

    public byte[] readAvailable() {
        throw new UnsupportedOperationException();
    }

    public void readStaleData() {
        throw new UnsupportedOperationException();
    }

    public void close() {
        index = 0;
    }
}
