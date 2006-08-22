package enginuity.logger.manager;

import enginuity.logger.definition.EcuData;
import enginuity.logger.query.LoggerCallback;

public interface QueryManager extends Runnable {

    void addQuery(EcuData ecuData, LoggerCallback callback);

    void removeQuery(EcuData ecuData);

    void stop();

}
