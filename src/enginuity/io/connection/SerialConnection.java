package enginuity.io.connection;

import java.io.IOException;

public interface SerialConnection {

    void write(byte[] bytes) throws IOException;

    int available() throws IOException;

    void read(byte[] bytes) throws IOException;

    byte[] readAvailable() throws IOException;

    void readStaleData() throws IOException;

    void close();

}
