/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.romraider.logger.ecu.comms.manager;

import com.romraider.Settings;
import com.romraider.logger.ecu.comms.io.connection.LoggerConnection;
import static com.romraider.logger.ecu.comms.io.connection.LoggerConnectionFactory.getConnection;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.comms.query.EcuQueryImpl;
import com.romraider.logger.ecu.comms.query.ExternalQuery;
import com.romraider.logger.ecu.comms.query.ExternalQueryImpl;
import com.romraider.logger.ecu.comms.query.Query;
import com.romraider.logger.ecu.comms.query.Response;
import com.romraider.logger.ecu.comms.query.ResponseImpl;
import com.romraider.logger.ecu.definition.EcuData;
import static com.romraider.logger.ecu.definition.EcuDataType.EXTERNAL;
import com.romraider.logger.ecu.definition.ExternalData;
import com.romraider.logger.ecu.definition.LoggerData;
import com.romraider.logger.ecu.ui.MessageListener;
import com.romraider.logger.ecu.ui.StatusChangeListener;
import com.romraider.logger.ecu.ui.handler.DataUpdateHandler;
import com.romraider.logger.ecu.ui.handler.file.FileLoggerControllerSwitchMonitor;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static com.romraider.util.ThreadUtil.sleep;
import org.apache.log4j.Logger;
import javax.swing.SwingUtilities;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import static java.util.Collections.synchronizedList;
import static java.util.Collections.synchronizedMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class QueryManagerImpl implements QueryManager {
    private static final Logger LOGGER = Logger.getLogger(QueryManagerImpl.class);
    private final DecimalFormat format = new DecimalFormat("0.00");
    private final List<StatusChangeListener> listeners = synchronizedList(new ArrayList<StatusChangeListener>());
    private final Map<String, Query> queryMap = synchronizedMap(new HashMap<String, Query>());
    private final Map<String, Query> addList = new HashMap<String, Query>();
    private final List<String> removeList = new ArrayList<String>();
    private final Settings settings;
    private final EcuInitCallback ecuInitCallback;
    private final MessageListener messageListener;
    private final DataUpdateHandler[] dataUpdateHandlers;
    private FileLoggerControllerSwitchMonitor monitor;
    private EcuQuery fileLoggerQuery;
    private Thread queryManagerThread;
    private boolean started;
    private boolean stop;

    public QueryManagerImpl(Settings settings, EcuInitCallback ecuInitCallback, MessageListener messageListener,
                            DataUpdateHandler... dataUpdateHandlers) {
        checkNotNull(settings, ecuInitCallback, messageListener, dataUpdateHandlers);
        this.settings = settings;
        this.ecuInitCallback = ecuInitCallback;
        this.messageListener = messageListener;
        this.dataUpdateHandlers = dataUpdateHandlers;
    }

    public synchronized void addListener(StatusChangeListener listener) {
        checkNotNull(listener, "listener");
        listeners.add(listener);
    }

    public void setFileLoggerSwitchMonitor(FileLoggerControllerSwitchMonitor monitor) {
        checkNotNull(monitor);
        this.monitor = monitor;
        fileLoggerQuery = new EcuQueryImpl(monitor.getEcuSwitch());
    }

    public synchronized void addQuery(String callerId, LoggerData loggerData) {
        checkNotNull(callerId, loggerData);
        //FIXME: This is a hack!!
        String queryId = buildQueryId(callerId, loggerData);
        if (loggerData.getDataType() == EXTERNAL) {
            addList.put(queryId, new ExternalQueryImpl((ExternalData) loggerData));
        } else {
            addList.put(queryId, new EcuQueryImpl((EcuData) loggerData));
        }
    }

    public synchronized void removeQuery(String callerId, LoggerData loggerData) {
        checkNotNull(callerId, loggerData);
        removeList.add(buildQueryId(callerId, loggerData));
    }

    public boolean isRunning() {
        return started && !stop;
    }

    public void run() {
        started = true;
        queryManagerThread = Thread.currentThread();
        LOGGER.debug("QueryManager started.");
        try {
            stop = false;
            while (!stop) {
                notifyConnecting();
                if (doEcuInit()) {
                    notifyReading();
                    runLogger();
                } else {
                    sleep(1000L);
                }
            }
        } catch (Exception e) {
            messageListener.reportError(e);
        } finally {
            notifyStopped();
            messageListener.reportMessage("Disconnected.");
            LOGGER.debug("QueryManager stopped.");
        }
    }

    private boolean doEcuInit() {
        try {
            LoggerConnection connection = getConnection(settings.getLoggerProtocol(), settings.getLoggerPort(),
                    settings.getLoggerConnectionProperties());
            try {
                messageListener.reportMessage("Sending ECU Init...");
                connection.ecuInit(ecuInitCallback);
                messageListener.reportMessage("Sending ECU Init...done.");
                return true;
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            messageListener.reportMessage("Unable to send ECU init - check cable is connected and ignition is on.");
            logError(e);
            return false;
        }
    }

    private void logError(Exception e) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Error sending ecu init", e);
        } else {
            LOGGER.info("Error sending ecu init: " + e.getMessage());
        }
    }

    private void runLogger() {
        TransmissionManager txManager = new TransmissionManagerImpl(settings);
        long start = System.currentTimeMillis();
        int count = 0;
        try {
            txManager.start();
            while (!stop) {
                updateQueryList();
                if (queryMap.isEmpty()) {
                    start = System.currentTimeMillis();
                    count = 0;
                    messageListener.reportMessage("Select parameters to be logged...");
                    sleep(1000L);
                } else {
                    sendEcuQueries(txManager);
                    sendExternalQueries();
                    handleQueryResponse();
                    count++;
                    messageListener.reportMessage("Querying ECU...");
                    messageListener.reportStats(buildStatsMessage(start, count));
                }
            }
        } catch (Exception e) {
            messageListener.reportError(e);
        } finally {
            txManager.stop();
        }
    }

    private void sendEcuQueries(TransmissionManager txManager) {
        List<EcuQuery> ecuQueries = filterEcuQueries(queryMap.values());
        if (fileLoggerQuery != null) {
            ecuQueries.add(fileLoggerQuery);
        }
        txManager.sendQueries(ecuQueries);
    }

    private void sendExternalQueries() {
        List<ExternalQuery> externalQueries = filterExternalQueries(queryMap.values());
        for (ExternalQuery externalQuery : externalQueries) {
            //FIXME: This is a hack!!
            externalQuery.setResponse(externalQuery.getLoggerData().getSelectedConvertor().convert(null));
        }
    }

    private void handleQueryResponse() {
        monitor.monitorFileLoggerSwitch(fileLoggerQuery.getResponse());
        final Response response = buildResponse(queryMap.values());
        for (final DataUpdateHandler dataUpdateHandler : dataUpdateHandlers) {
            runAsDaemon(new Runnable() {
                public void run() {
                    dataUpdateHandler.handleDataUpdate(response);
                }
            });
        }
    }

    private Response buildResponse(Collection<Query> queries) {
        Response response = new ResponseImpl();
        for (Query query : queries) {
            response.setDataValue(query.getLoggerData(), query.getResponse());
        }
        return response;
    }

    //FIXME: This is a hack!!
    private List<EcuQuery> filterEcuQueries(Collection<Query> queries) {
        List<EcuQuery> filtered = new ArrayList<EcuQuery>();
        for (Query query : queries) {
            if (EcuQuery.class.isAssignableFrom(query.getClass())) {
                filtered.add((EcuQuery) query);
            }
        }
        return filtered;
    }

    //FIXME: This is a hack!!
    private List<ExternalQuery> filterExternalQueries(Collection<Query> queries) {
        List<ExternalQuery> filtered = new ArrayList<ExternalQuery>();
        for (Query query : queries) {
            if (ExternalQuery.class.isAssignableFrom(query.getClass())) {
                filtered.add((ExternalQuery) query);
            }
        }
        return filtered;
    }

    public void stop() {
        stop = true;
        if (queryManagerThread != null) {
            queryManagerThread.interrupt();
        }
    }

    private String buildQueryId(String callerId, LoggerData loggerData) {
        return callerId + "_" + loggerData.getName();
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
        double duration = ((double) (System.currentTimeMillis() - start)) / 1000.0;
        return "[ " + format.format(((double) count) / duration) + " queries/sec, " + format.format(duration / ((double) count)) + " sec/query ]";
    }

    private void notifyConnecting() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (StatusChangeListener listener : listeners) {
                    listener.connecting();
                }
            }
        });
    }

    private void notifyReading() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (StatusChangeListener listener : listeners) {
                    listener.readingData();
                }
            }
        });
    }

    private void notifyStopped() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (StatusChangeListener listener : listeners) {
                    listener.stopped();
                }
            }
        });
    }

}
