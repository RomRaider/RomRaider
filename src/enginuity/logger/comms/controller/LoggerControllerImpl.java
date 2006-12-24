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

package enginuity.logger.comms.controller;

import enginuity.Settings;
import enginuity.logger.comms.manager.QueryManager;
import enginuity.logger.comms.manager.QueryManagerImpl;
import enginuity.logger.comms.query.EcuInitCallback;
import enginuity.logger.comms.query.LoggerCallback;
import enginuity.logger.definition.EcuData;
import enginuity.logger.ui.ControllerListener;
import enginuity.logger.ui.MessageListener;
import static enginuity.util.ParamChecker.checkNotNull;

import java.util.ArrayList;
import static java.util.Collections.synchronizedList;
import java.util.List;

public final class LoggerControllerImpl implements LoggerController {
    private final QueryManager queryManager;
    private final List<ControllerListener> listeners = synchronizedList(new ArrayList<ControllerListener>());
    private boolean started = false;

    public LoggerControllerImpl(Settings settings, EcuInitCallback ecuInitCallback, MessageListener messageListener) {
        checkNotNull(settings, ecuInitCallback, messageListener);
        queryManager = new QueryManagerImpl(settings, ecuInitCallback, messageListener);
    }

    public synchronized void addListener(ControllerListener listener) {
        checkNotNull(listener, "listener");
        listeners.add(listener);
    }

    public void addLogger(String callerId, EcuData ecuData, LoggerCallback callback) {
        checkNotNull(ecuData, callback);
        System.out.println("Adding logger:   " + ecuData.getName());
        queryManager.addQuery(callerId, ecuData, callback);
    }

    public void removeLogger(String callerId, EcuData ecuData) {
        checkNotNull(ecuData, "ecuParam");
        System.out.println("Removing logger: " + ecuData.getName());
        queryManager.removeQuery(callerId, ecuData);
    }

    public synchronized void start() {
        if (!started) {
            Thread queryManagerThread = new Thread(queryManager);
            queryManagerThread.setDaemon(true);
            queryManagerThread.start();
            started = true;
            startListeners();
        }
    }

    public synchronized void stop() {
        if (started) {
            queryManager.stop();
            started = false;
            stopListeners();
        }
    }

    private void startListeners() {
        for (ControllerListener listener : listeners) {
            listener.start();
        }
    }

    private void stopListeners() {
        for (ControllerListener listener : listeners) {
            listener.stop();
        }
    }

}
