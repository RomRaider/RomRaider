/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.logger.ecu.comms.manager;

import enginuity.Settings;
import enginuity.io.connection.EcuConnection;
import enginuity.io.connection.EcuConnectionImpl;
import enginuity.io.protocol.Protocol;
import enginuity.io.protocol.ProtocolFactory;
import enginuity.logger.ecu.comms.query.EcuInitCallback;
import enginuity.logger.ecu.comms.query.LoggerCallback;
import enginuity.logger.ecu.comms.query.RegisteredQuery;
import enginuity.logger.ecu.comms.query.RegisteredQueryImpl;
import enginuity.logger.ecu.definition.EcuSwitch;
import enginuity.logger.ecu.definition.LoggerData;
import enginuity.logger.ecu.ui.MessageListener;
import enginuity.logger.ecu.ui.StatusChangeListener;
import static enginuity.util.HexUtil.asHex;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ThreadUtil.sleep;

import javax.swing.SwingUtilities;
import java.text.DecimalFormat;
import java.util.ArrayList;
import static java.util.Collections.synchronizedList;
import static java.util.Collections.synchronizedMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class QueryManagerImpl implements QueryManager {
    private final DecimalFormat format = new DecimalFormat("0.00");
    private final List<StatusChangeListener> listeners = synchronizedList(new ArrayList<StatusChangeListener>());
    private final Map<String, RegisteredQuery> queryMap = synchronizedMap(new HashMap<String, RegisteredQuery>());
    private final Map<String, RegisteredQuery> addList = new HashMap<String, RegisteredQuery>();
    private final List<String> removeList = new ArrayList<String>();
    private final Settings settings;
    private final EcuInitCallback ecuInitCallback;
    private final MessageListener messageListener;
    private RegisteredQuery fileLoggerQuery;
    private Thread queryManagerThread;
    private boolean started;
    private boolean stop;

    public QueryManagerImpl(Settings settings, EcuInitCallback ecuInitCallback, MessageListener messageListener) {
        checkNotNull(settings, ecuInitCallback, messageListener);
        this.settings = settings;
        this.ecuInitCallback = ecuInitCallback;
        this.messageListener = messageListener;
    }

    public synchronized void addListener(StatusChangeListener listener) {
        checkNotNull(listener, "listener");
        listeners.add(listener);
    }

    public void setFileLoggerQuery(EcuSwitch ecuSwitch, LoggerCallback callback) {
        checkNotNull(ecuSwitch, callback);
        fileLoggerQuery = new RegisteredQueryImpl(ecuSwitch, callback);
    }

    public synchronized void addQuery(String callerId, LoggerData loggerData, LoggerCallback callback) {
        checkNotNull(callerId, loggerData, callback);
        //FIXME: Integrate LoggerData here!!! Cater for ecu and external data items
        //addList.put(buildQueryId(callerId, loggerData), new RegisteredQueryImpl(loggerData, callback));
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
        System.out.println("QueryManager started.");
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
            System.out.println("QueryManager stopped.");
        }
    }

    private boolean doEcuInit() {
        try {
            Protocol protocol = ProtocolFactory.getInstance().getProtocol(settings.getLoggerProtocol());
            EcuConnection ecuConnection = new EcuConnectionImpl(settings.getLoggerConnectionProperties(), settings.getLoggerPort());
            try {
                messageListener.reportMessage("Sending ECU Init...");
                byte[] request = protocol.constructEcuInitRequest();
                System.out.println("Ecu Init Request  ---> " + asHex(request));
                byte[] response = ecuConnection.send(request);
                byte[] processedResponse = protocol.preprocessResponse(request, response);
                protocol.checkValidEcuInitResponse(processedResponse);
                System.out.println("Ecu Init Response <--- " + asHex(processedResponse));
                ecuInitCallback.callback(protocol.parseEcuInitResponse(processedResponse));
                messageListener.reportMessage("Sending ECU Init...done.");
                return true;
            } finally {
                ecuConnection.close();
            }
        } catch (Exception e) {
            messageListener.reportMessage("Unable to send ECU init - check correct serial port has been selected, cable is connected and ignition is on.");
            e.printStackTrace();
            return false;
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
                    List<RegisteredQuery> queries = new ArrayList<RegisteredQuery>(queryMap.values());
                    if (fileLoggerQuery != null) {
                        queries.add(fileLoggerQuery);
                    }
                    txManager.sendQueries(queries);
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
