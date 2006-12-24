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

package enginuity.logger.comms.manager;

import enginuity.Settings;
import enginuity.io.connection.EcuConnection;
import enginuity.io.connection.EcuConnectionImpl;
import enginuity.io.protocol.Protocol;
import enginuity.io.protocol.ProtocolFactory;
import enginuity.logger.comms.query.EcuInitCallback;
import enginuity.logger.comms.query.LoggerCallback;
import enginuity.logger.comms.query.RegisteredQuery;
import enginuity.logger.comms.query.RegisteredQueryImpl;
import enginuity.logger.definition.EcuData;
import enginuity.logger.ui.MessageListener;
import static enginuity.util.HexUtil.asHex;
import static enginuity.util.ParamChecker.checkNotNull;
import enginuity.util.ThreadUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class QueryManagerImpl implements QueryManager {
    private final DecimalFormat format = new DecimalFormat("0.00");
    private final Map<String, RegisteredQuery> queryMap = Collections.synchronizedMap(new HashMap<String, RegisteredQuery>());
    private final Map<String, RegisteredQuery> addList = new HashMap<String, RegisteredQuery>();
    private final List<String> removeList = new ArrayList<String>();
    private final Settings settings;
    private final EcuInitCallback ecuInitCallback;
    private final MessageListener messageListener;
    private boolean stop = false;
    private Thread queryManagerThread = null;

    public QueryManagerImpl(Settings settings, EcuInitCallback ecuInitCallback, MessageListener messageListener) {
        checkNotNull(settings, ecuInitCallback, messageListener);
        this.settings = settings;
        this.ecuInitCallback = ecuInitCallback;
        this.messageListener = messageListener;
    }

    public synchronized void addQuery(String callerId, EcuData ecuData, LoggerCallback callback) {
        checkNotNull(callerId, ecuData, callback);
        addList.put(buildQueryId(callerId, ecuData), new RegisteredQueryImpl(ecuData, callback));
    }

    public synchronized void removeQuery(String callerId, EcuData ecuData) {
        checkNotNull(callerId, ecuData);
        removeList.add(buildQueryId(callerId, ecuData));
    }

    public void run() {
        queryManagerThread = Thread.currentThread();
        System.out.println("QueryManager started.");
        stop = false;
        while (!stop) {
            if (doEcuInit()) {
                runLogger();
            } else {
                ThreadUtil.sleep(5000L);
            }
        }
        System.out.println("QueryManager stopped.");
    }

    private boolean doEcuInit() {
        Protocol protocol = ProtocolFactory.getInstance().getProtocol(settings.getLoggerProtocol());
        EcuConnection ecuConnection = new EcuConnectionImpl(protocol.getConnectionProperties(), settings.getLoggerPort());
        try {
            messageListener.reportMessage("Sending ECU Init...");
            byte[] response = ecuConnection.send(protocol.constructEcuInitRequest());
            System.out.println("Ecu Init response = " + asHex(response));
            if (protocol.isValidEcuInitResponse(response)) {
                ecuInitCallback.callback(protocol.parseEcuInitResponse(response));
                messageListener.reportMessage("Sending ECU Init...done.");
                return true;
            } else {
                messageListener.reportMessage("Waiting for ECU connection...");
                return false;
            }
        } catch (Exception e) {
            messageListener.reportMessage("Error sending ECU init - check correct serial port has been selected.");
            e.printStackTrace();
            return false;
        } finally {
            ecuConnection.close();
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
                    messageListener.reportMessage("Select parameters to be logged...");
                    ThreadUtil.sleep(2000L);
                } else {
                    txManager.sendQueries(queryMap.values());
                    count++;
                    messageListener.reportMessage(buildStatsMessage(start, count));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        double duration = ((double) (System.currentTimeMillis() - start)) / 1000.0;
        return "Logging [Total queries sent: " + count + ", Queries per second: " + format.format(((double) count) / duration)
                + ", Avg. Query Time: " + format.format(duration / ((double) count)) + "s]";
    }

}
