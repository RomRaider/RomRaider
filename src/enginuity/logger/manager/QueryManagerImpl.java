package enginuity.logger.manager;

import enginuity.logger.definition.EcuData;
import enginuity.logger.query.LoggerCallback;
import enginuity.logger.query.RegisteredQuery;
import enginuity.logger.query.RegisteredQueryImpl;
import enginuity.logger.ui.MessageListener;
import static enginuity.util.ParamChecker.checkNotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"FieldCanBeLocal"})
public final class QueryManagerImpl implements QueryManager {
    private final DecimalFormat format = new DecimalFormat("0.00");
    private final Map<EcuData, RegisteredQuery> queryMap = Collections.synchronizedMap(new HashMap<EcuData, RegisteredQuery>());
    private final List<RegisteredQuery> addList = new ArrayList<RegisteredQuery>();
    private final List<EcuData> removeList = new ArrayList<EcuData>();
    private final TransmissionManager txManager;
    private final MessageListener messageListener;
    private boolean stop = false;

    public QueryManagerImpl(TransmissionManager txManager, MessageListener messageListener) {
        checkNotNull(txManager, messageListener);
        this.txManager = txManager;
        this.messageListener = messageListener;
    }

    public synchronized void addQuery(EcuData ecuData, LoggerCallback callback) {
        checkNotNull(ecuData, callback);
        addList.add(new RegisteredQueryImpl(ecuData, callback));
    }

    public synchronized void removeQuery(EcuData ecuData) {
        removeList.add(ecuData);
    }

    public void run() {
        System.out.println("QueryManager started.");

        long start = System.currentTimeMillis();
        int count = 0;

        try {
            txManager.start();
            stop = false;
            while (!stop) {
                updateQueryList();
                txManager.sendQueries(queryMap.values());
                count++;
                messageListener.reportMessage(buildStatsMessage(start, count));
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageListener.reportError(e);
        } finally {
            txManager.stop();
            messageListener.reportMessage("Logging stopped.");
        }
        System.out.println("QueryManager stopped.");
    }

    public void stop() {
        stop = true;
    }

    private synchronized void updateQueryList() {
        addQueries();
        removeQueries();
    }

    private void addQueries() {
        for (RegisteredQuery registeredQuery : addList) {
            queryMap.put(registeredQuery.getEcuData(), registeredQuery);
        }
        addList.clear();
    }

    private void removeQueries() {
        for (EcuData ecuData : removeList) {
            queryMap.remove(ecuData);
        }
        removeList.clear();
    }

    private String buildStatsMessage(long start, int count) {
        double duration = ((double) (System.currentTimeMillis() - start)) / 1000D;
        return "Logging [Total queries sent: " + count + ", Queries per second: " + format.format(((double) count) / duration)
                + ", Avg. Query Time: " + format.format(duration / ((double) count)) + "s]";
    }

}
