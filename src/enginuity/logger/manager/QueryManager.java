package enginuity.logger.manager;

import enginuity.logger.definition.EcuParameter;
import enginuity.logger.query.LoggerCallback;

public interface QueryManager extends Runnable {

    void addQuery(EcuParameter ecuParam, LoggerCallback callback);

    void removeQuery(EcuParameter ecuParam);

    void stop();

}
