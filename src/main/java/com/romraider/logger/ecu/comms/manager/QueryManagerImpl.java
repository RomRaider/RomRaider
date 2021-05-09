/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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

import static com.romraider.logger.ecu.comms.io.connection.LoggerConnectionFactory.getConnection;
import static com.romraider.logger.ecu.definition.EcuDataType.EXTERNAL;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ThreadUtil.sleep;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.synchronizedList;
import static java.util.Collections.synchronizedMap;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.logger.ecu.comms.io.connection.LoggerConnection;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.comms.query.EcuQueryImpl;
import com.romraider.logger.ecu.comms.query.ExternalQuery;
import com.romraider.logger.ecu.comms.query.ExternalQueryImpl;
import com.romraider.logger.ecu.comms.query.Query;
import com.romraider.logger.ecu.comms.query.Response;
import com.romraider.logger.ecu.comms.query.ResponseImpl;
import com.romraider.logger.ecu.definition.EcuData;
import com.romraider.logger.ecu.definition.ExternalData;
import com.romraider.logger.ecu.definition.LoggerData;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.ui.MessageListener;
import com.romraider.logger.ecu.ui.StatusChangeListener;
import com.romraider.logger.ecu.ui.handler.DataUpdateHandler;
import com.romraider.logger.ecu.ui.handler.file.FileLoggerControllerSwitchMonitor;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;

public final class QueryManagerImpl implements QueryManager {
    private static final Logger LOGGER = Logger.getLogger(QueryManagerImpl.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            QueryManagerImpl.class.getName());
    private final List<StatusChangeListener> listeners =
            synchronizedList(new ArrayList<StatusChangeListener>());
    private final Map<String, Query> queryMap =
            synchronizedMap(new HashMap<String, Query>());
    private final Map<String, Query> addList = new HashMap<String, Query>();
    private final List<String> removeList = new ArrayList<String>();
    private static final PollingState pollState = new PollingStateImpl();
    private static final Settings settings = SettingsManager.getSettings();
    private static final String EXT = "Externals";
    private final EcuInitCallback ecuInitCallback;
    private final MessageListener messageListener;
    private FileLoggerControllerSwitchMonitor monitor;
    private EcuQuery fileLoggerQuery;
    private Thread queryManagerThread;
    private static boolean started;
    private static boolean stop;
    private AsyncDataUpdateHandler dataUpdater;
    private DataUpdateHandler[] updateHandlers;
    private int queryCounter;
    private long queryStart;

    public QueryManagerImpl(EcuInitCallback ecuInitCallback,
            MessageListener messageListener,
            DataUpdateHandler... dataUpdateHandlers) {
        checkNotNull(ecuInitCallback,
                messageListener,
                dataUpdateHandlers);
        this.ecuInitCallback = ecuInitCallback;
        this.messageListener = messageListener;
        this.updateHandlers = dataUpdateHandlers;
        stop = true;       
    }

    @Override
    public synchronized void addListener(StatusChangeListener listener) {
        checkNotNull(listener, "listener");
        listeners.add(listener);
    }

    @Override
    public void setFileLoggerSwitchMonitor(FileLoggerControllerSwitchMonitor monitor) {
        checkNotNull(monitor);
        this.monitor = monitor;
        fileLoggerQuery = new EcuQueryImpl(monitor.getEcuSwitch());
    }

    @Override
    public synchronized void addQuery(String callerId, LoggerData loggerData) {
        checkNotNull(callerId, loggerData);
        
        //Reset stats
        queryCounter = 0;
        queryStart = System.currentTimeMillis();
        
        //FIXME: This is a hack!!
        String queryId = buildQueryId(callerId, loggerData);
        if (loggerData.getDataType() == EXTERNAL) {
            addList.put(queryId, new ExternalQueryImpl((ExternalData) loggerData));
        } else {
            addList.put(queryId, new EcuQueryImpl((EcuData) loggerData));
            pollState.setLastQuery(false);
            pollState.setNewQuery(true);
        }
    }

    @Override
    public synchronized void removeQuery(String callerId, LoggerData loggerData) {
        checkNotNull(callerId, loggerData);
        
        //Reset stats
        queryCounter = 0;
        queryStart = System.currentTimeMillis();
        
        removeList.add(buildQueryId(callerId, loggerData));
        if (loggerData.getDataType() != EXTERNAL) {
            pollState.setNewQuery(true);
        }
        
    }

    @Override
    public Thread getThread() {
        return queryManagerThread;
    }

    @Override
    public boolean isRunning() {
        return started && !stop;
    }

    @Override
    public void run() {
        started = true;
        queryManagerThread = Thread.currentThread();
        queryManagerThread.setName("Query Manager");
        LOGGER.debug("QueryManager started.");

        try {
            stop = false;
            
            while (!stop) {
                Module target = settings.getDestinationTarget();
                
                if(target == null) {
                	notifyStopped();
                }
                else {
                	notifyConnecting();
                }
                
                if (!settings.isLogExternalsOnly() &&  (target != null && doEcuInit(target))) {
                    notifyReading();
                    runLogger(target);
                } else if (settings.isLogExternalsOnly()) {
                    notifyReading();
                    runLogger(null);
                } else {
                    sleep(1000L);
                }
            }
        } catch (Exception e) {
            messageListener.reportError(e);
        } finally {
            notifyStopped();
            messageListener.reportMessage(rb.getString("DISCONNECTED"));
            LOGGER.debug("QueryManager stopped.");
            
            if (dataUpdater != null) {
	            dataUpdater.stopUpdater();
	            try {
					dataUpdater.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
        }
    }

    private boolean doEcuInit(Module module) {    	
        try {
            LoggerConnection connection =
                    getConnection(settings.getLoggerProtocol(),
                            settings.getLoggerPort(),
                            settings.getLoggerConnectionProperties());
            try {
                messageListener.reportMessage(MessageFormat.format(
                        rb.getString("SENDINIT"), module.getName()));
                connection.ecuInit(ecuInitCallback, module);
                messageListener.reportMessage(MessageFormat.format(
                        rb.getString("INITDONE"), module.getName()));
                return true;
            } finally {
                connection.close();
            }
        } catch (Exception e) {       	
            messageListener.reportMessage(MessageFormat.format(rb.getString("INITFAIL"), module.getName()));
            logError(e);
            return false;
        }
    }

    private void logError(Exception e) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Error sending init", e);
        } else {
            LOGGER.info("Error sending init: " + e.getMessage());
        }
    }

    private void runLogger(Module module) {
        String moduleName = null;
        if (module == null){
            moduleName = EXT;
        }
        else {
            moduleName = module.getName();
        }
        TransmissionManager txManager = new TransmissionManagerImpl();
        queryStart = currentTimeMillis();
        long end = currentTimeMillis();

        try {
            txManager.start();
            if(dataUpdater == null || !dataUpdater.isRunning()) {
            	if(dataUpdater == null) {
            		dataUpdater = new AsyncDataUpdateHandler(updateHandlers);
            	}
            	dataUpdater.start();
            }
            
            boolean lastPollState = settings.isFastPoll();
            while (!stop) {
                pollState.setFastPoll(settings.isFastPoll());
                updateQueryList();
                if (queryMap.isEmpty()) {
                    if (pollState.isLastQuery() &&
                            pollState.getCurrentState() == PollingState.State.STATE_0) {
                        endEcuQueries(txManager);
                        pollState.setLastState(PollingState.State.STATE_0);
                    }
                    
                    queryStart = System.currentTimeMillis();
                    queryCounter = 0;
                    messageListener.reportMessage(rb.getString("SELECTPARAMS"));
                    sleep(100L);
                } else {
                    end = currentTimeMillis() + 1L; // update once every 1msec
                    final List<EcuQuery> ecuQueries =
                            filterEcuQueries(queryMap.values());

                    if (!settings.isLogExternalsOnly()) {
                        if (!ecuQueries.isEmpty()) {
                            sendEcuQueries(txManager);
                            if (!pollState.isFastPoll() && lastPollState) {
                                endEcuQueries(txManager);
                            }
                            if (pollState.isFastPoll()) {
                                if (pollState.getCurrentState() == PollingState.State.STATE_0 &&
                                        pollState.isNewQuery()) {
                                    pollState.setCurrentState(PollingState.State.STATE_1);
                                    pollState.setNewQuery(false);
                                }
                                if (pollState.getCurrentState() == PollingState.State.STATE_0 &&
                                        !pollState.isNewQuery()) {
                                    pollState.setCurrentState(PollingState.State.STATE_1);
                                }
                                if (pollState.getCurrentState() == PollingState.State.STATE_1 &&
                                        pollState.isNewQuery()) {
                                    pollState.setCurrentState(PollingState.State.STATE_0);
                                    pollState.setLastState(PollingState.State.STATE_1);
                                    pollState.setNewQuery(false);
                                }
                                if (pollState.getCurrentState() == PollingState.State.STATE_1 &&
                                        !pollState.isNewQuery()) {
                                    pollState.setLastState(PollingState.State.STATE_1);
                                }
                                pollState.setLastQuery(true);
                            }
                            else {
                                pollState.setCurrentState(PollingState.State.STATE_0);
                                pollState.setLastState(PollingState.State.STATE_0);
                                pollState.setNewQuery(false);
                            }
                            lastPollState = pollState.isFastPoll();
                        }
                        else {
                            if (pollState.isLastQuery() &&
                                    pollState.getLastState() == PollingState.State.STATE_1) {
                                endEcuQueries(txManager);
                                pollState.setLastState(PollingState.State.STATE_0);
                                pollState.setCurrentState(PollingState.State.STATE_0);
                                pollState.setNewQuery(true);
                            }
                        }
                    }
                    sendExternalQueries();
                    // waiting until at least 1msec has passed since last query set
                    while (currentTimeMillis() < end) {
                        sleep(1L);
                    }
                    
                    handleQueryResponse();
                    queryCounter++;
                    messageListener.reportMessage(MessageFormat.format(
                            rb.getString("QUERYING"), moduleName));
                    messageListener.reportStats(buildStatsMessage(queryStart, queryCounter));
                }
            }
        } catch (Exception e) {
            messageListener.reportError(e);
        } finally {
            txManager.stop();
            pollState.setCurrentState(PollingState.State.STATE_0);
            pollState.setNewQuery(true);
        }
    }

    private void sendEcuQueries(TransmissionManager txManager) {
        final List<EcuQuery> ecuQueries = filterEcuQueries(queryMap.values());
        if (fileLoggerQuery != null
                && settings.isFileLoggingControllerSwitchActive())
            ecuQueries.add(fileLoggerQuery);
        	txManager.sendQueries(ecuQueries, pollState);
    }

    private void sendExternalQueries() {
        final List<ExternalQuery> externalQueries =
                filterExternalQueries(queryMap.values());
        for (ExternalQuery externalQuery : externalQueries) {
            //FIXME: This is a hack!!
            externalQuery.setResponse(
                    externalQuery.getLoggerData().getSelectedConvertor().convert(null));
        }
    }

    private void endEcuQueries(TransmissionManager txManager) {
        txManager.endQueries();
        pollState.setLastQuery(false);
    }

    private void handleQueryResponse() {
        if (settings.isFileLoggingControllerSwitchActive())
            monitor.monitorFileLoggerSwitch(fileLoggerQuery.getResponse());
        final Response response = buildResponse(queryMap.values());
        
        
        dataUpdater.addResponse(response);
    }

    private Response buildResponse(Collection<Query> queries) {
        final Response response = new ResponseImpl();
        for (final Query query : queries) {
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

    @Override
    public void stop() {
        stop = true;
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
        String state = MessageFormat.format(
                    rb.getString("SLOWK"), settings.getLoggerProtocol());
        if (pollState.isFastPoll()) {
            state = MessageFormat.format(
                    rb.getString("FASTK"), settings.getLoggerProtocol());
        }
        if (settings.getTransportProtocol().equals("ISO15765")) {
            state = MessageFormat.format(
                    rb.getString("CANBUS"), settings.getLoggerProtocol());
        }
        if (settings.isLogExternalsOnly()) {
            state = MessageFormat.format(
                    rb.getString("EXTERNALS"), settings.getLoggerProtocol());
        }
        double duration = (System.currentTimeMillis() - start) / 1000.0;
        String result = MessageFormat.format(
                rb.getString("QUERYSTATS"),
                state,
                (count) / duration,
                duration / (count)
                );
        return result;
    }

    private void notifyConnecting() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (StatusChangeListener listener : listeners) {
                    listener.connecting();
                }
            }
        });
    }

    private void notifyReading() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (StatusChangeListener listener : listeners) {
                    listener.readingData();
                }
            }
        });
    }

    private void notifyStopped() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (StatusChangeListener listener : listeners) {
                    listener.stopped();
                }
            }
        });
    }

}