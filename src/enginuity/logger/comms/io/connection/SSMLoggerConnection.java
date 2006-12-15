package enginuity.logger.comms.io.connection;

import enginuity.io.connection.SerialConnection;
import enginuity.io.connection.SerialConnectionImpl;
import enginuity.logger.comms.io.protocol.LoggerProtocol;
import enginuity.logger.comms.io.protocol.SSMLoggerProtocol;
import enginuity.logger.comms.query.RegisteredQuery;
import enginuity.logger.exception.SerialCommunicationException;
import static enginuity.util.HexUtil.asHex;
import static enginuity.util.ThreadUtil.sleep;

import java.util.Collection;

public final class SSMLoggerConnection implements LoggerConnection {
    private LoggerProtocol protocol;
    private SerialConnection serialConnection;

    public SSMLoggerConnection(String portName) {
        protocol = new SSMLoggerProtocol();
        serialConnection = new SerialConnectionImpl(protocol.getConnectionProperties(), portName);
    }

    public void sendAddressReads(Collection<RegisteredQuery> queries) {
        try {
            byte[] request = protocol.constructReadAddressRequest(queries);
            byte[] response = protocol.constructReadAddressResponse(queries);

            //System.out.println("Raw request        = " + asHex(request));

            serialConnection.readStaleData();
            serialConnection.write(request);
            int timeout = 1000;
            while (serialConnection.available() < response.length) {
                sleep(1);
                timeout -= 1;
                if (timeout <= 0) {
                    byte[] badBytes = new byte[serialConnection.available()];
                    serialConnection.read(badBytes);
                    System.out.println("Bad response (read timeout): " + asHex(badBytes));
                    break;
                }
            }
            serialConnection.read(response);

            //System.out.println("Raw response       = " + asHex(response));

            byte[] filteredResponse = new byte[response.length - request.length];
            System.arraycopy(response, request.length, filteredResponse, 0, filteredResponse.length);

            //System.out.println("Filtered response  = " + asHex(filteredResponse));
            //System.out.println();

            protocol.processReadAddressResponses(queries, filteredResponse);
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
    }

    public void close() {
        serialConnection.close();
    }

}
