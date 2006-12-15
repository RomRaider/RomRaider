package enginuity.rt.io;

public interface RTConnection {

    byte[] send(byte[] bytes);

    void close();
    
}
