package enginuity.logger.manager;

import enginuity.logger.query.RegisteredQuery;
import static enginuity.util.ParamChecker.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DefaultQueryManager implements QueryManager {
    private final Map<String, RegisteredQuery> queryMap = Collections.synchronizedMap(new HashMap<String, RegisteredQuery>());
    private final List<RegisteredQuery> addList = new ArrayList<RegisteredQuery>();
    private final List<String> removeList = new ArrayList<String>();
    private final TransmissionManager txManager;
    private boolean stop = false;

    public DefaultQueryManager(TransmissionManager txManager) {
        checkNotNull(txManager, "txManager");
        this.txManager = txManager;
    }

    public synchronized void addQuery(RegisteredQuery registeredQuery) {
        checkNotNull(registeredQuery, "registeredQuery");
        System.out.println("Adding address: " + registeredQuery.getAddress());
        addList.add(registeredQuery);
    }

    public synchronized void removeQuery(String address) {
        System.out.println("Removing address: " + address);
        removeList.add(address);
    }

    public void run() {
        System.out.println("QueryManager started.");
        try {
            txManager.start();
            while (!stop) {
                updateQueryList();
                for (String address : queryMap.keySet()) {
                    RegisteredQuery registeredQuery = queryMap.get(address);
                    byte[] response = txManager.queryAddress(registeredQuery.getBytes());
                    registeredQuery.setResponse(response);
                }
            }
        } finally {
            txManager.stop();
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
            queryMap.put(registeredQuery.getAddress(), registeredQuery);
        }
        addList.clear();
    }

    private void removeQueries() {
        for (String address : removeList) {
            queryMap.remove(address);
        }
        removeList.clear();
    }

}
