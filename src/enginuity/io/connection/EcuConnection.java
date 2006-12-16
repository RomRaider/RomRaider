package enginuity.io.connection;

public interface EcuConnection {

    byte[] send(byte[] bytes);

    void close();
    
}
