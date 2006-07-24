package enginuity.logger.comms;

import enginuity.logger.query.Query;
import enginuity.util.ParamChecker;

public final class DefaultSerialConnection implements SerialConnection {
    private SerialWriter serialWriter;
    private SerialReader serialReader;

    public DefaultSerialConnection(SerialWriter serialWriter, SerialReader serialReader) {
        this.serialWriter = serialWriter;
        this.serialReader = serialReader;
    }

    public byte[] transmit(Query query) {
        ParamChecker.checkNotNull(query, "query");
        return query.execute(serialWriter, serialReader);
    }

    public void close() {
        try {
            try {
                serialWriter.close();
            } finally {
                serialReader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
