package enginuity.logger.comms.manager;

import enginuity.logger.comms.query.LoggerCallback;
import enginuity.logger.definition.EcuData;

public interface QueryManager extends Runnable {

    void addQuery(String callerId, EcuData ecuData, LoggerCallback callback);

    void removeQuery(String callerId, EcuData ecuData);

    void stop();

}
