package enginuity.logger.manager;

import enginuity.logger.definition.EcuData;
import enginuity.logger.query.LoggerCallback;

public interface QueryManager extends Runnable {

    void addQuery(String callerId, EcuData ecuData, LoggerCallback callback);

    void removeQuery(String callerId, EcuData ecuData);

    void stop();

}
