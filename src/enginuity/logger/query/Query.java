package enginuity.logger.query;

import enginuity.logger.comms.SerialReader;
import enginuity.logger.comms.SerialWriter;

public interface Query {

    byte[] execute(SerialWriter writer, SerialReader reader);

}
