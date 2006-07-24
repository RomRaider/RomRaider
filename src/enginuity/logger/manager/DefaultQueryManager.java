package enginuity.logger.manager;

import enginuity.Settings;
import enginuity.logger.query.RegisteredQuery;
import static enginuity.util.ParamChecker.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DefaultQueryManager implements QueryManager {
    private final TransmissionManager txManager = new DefaultTransmissionManager();
    private final Map<String, RegisteredQuery> queryMap = Collections.synchronizedMap(new HashMap<String, RegisteredQuery>());
    private final List<RegisteredQuery> addList = new ArrayList<RegisteredQuery>();
    private final List<String> removeList = new ArrayList<String>();
    private boolean stop = false;

    public synchronized void addQuery(RegisteredQuery registeredQuery) {
        checkNotNull(registeredQuery, "registeredQuery");
        System.out.println("Adding address: " + registeredQuery.getAddress());
        addList.add(registeredQuery);
    }

    public synchronized void removeQuery(String address) {
        removeList.add(address);
    }

    public void run() {
        try {
            // TODO: Pass in actual app settings object!! Move TxMgr construction/initialization out to LoggerController
            txManager.start(new Settings());
            while (!stop) {
                updateQueryList();
                for (String address : queryMap.keySet()) {
                    RegisteredQuery registeredQuery = queryMap.get(address);
                    byte[] response = txManager.queryAddress(address);
                    registeredQuery.setResponse(response);
                }
            }
        } finally {
            txManager.stop();
        }
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
            System.out.println("Adding address: " + registeredQuery.getAddress());
            queryMap.put(registeredQuery.getAddress(), registeredQuery);
        }
        addList.clear();
    }

    private void removeQueries() {
        for (String address : removeList) {
            System.out.println("Removing address: " + address);
            queryMap.remove(address);
        }
        removeList.clear();
    }

}
