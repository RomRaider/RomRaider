package enginuity.logger.manager;

import enginuity.logger.LoggerController;
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
    private final Map<String, RegisteredQuery> queryMap = Collections.synchronizedMap(new HashMap<String, RegisteredQuery>());
    private final Map<String, RegisteredQuery> addList = new HashMap<String, RegisteredQuery>();
    private final List<String> removeList = new ArrayList<String>();
    private final TransmissionManager txManager;
    private final MessageListener messageListener;
    private final LoggerController controller;
    private boolean stop = false;

    public QueryManagerImpl(LoggerController controller, TransmissionManager txManager, MessageListener messageListener) {
        checkNotNull(controller, txManager, messageListener);
        this.controller = controller;
        this.txManager = txManager;
        this.messageListener = messageListener;
    }

    public synchronized void addQuery(String callerId, EcuData ecuData, LoggerCallback callback) {
        checkNotNull(ecuData, callback);
        addList.put(buildQueryId(callerId, ecuData), new RegisteredQueryImpl(ecuData, callback));
    }

    public synchronized void removeQuery(String callerId, EcuData ecuData) {
        removeList.add(buildQueryId(callerId, ecuData));
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
            controller.stop();
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

    private String buildQueryId(String callerId, EcuData ecuData) {
        return callerId + "_" + ecuData.getName();
    }

    private synchronized void updateQueryList() {
        addQueries();
        removeQueries();
    }

    private void addQueries() {
        for (String queryId : addList.keySet()) {
            queryMap.put(queryId, addList.get(queryId));
        }
        addList.clear();
    }

    private void removeQueries() {
        for (String queryId : removeList) {
            queryMap.remove(queryId);
        }
        removeList.clear();
    }

    private String buildStatsMessage(long start, int count) {
        double duration = ((double) (System.currentTimeMillis() - start)) / 1000D;
        return "Logging [Total queries sent: " + count + ", Queries per second: " + format.format(((double) count) / duration)
                + ", Avg. Query Time: " + format.format(duration / ((double) count)) + "s]";
    }

}
