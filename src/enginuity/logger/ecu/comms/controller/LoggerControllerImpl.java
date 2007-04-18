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

package enginuity.logger.ecu.comms.controller;

import enginuity.Settings;
import enginuity.logger.ecu.comms.manager.QueryManager;
import enginuity.logger.ecu.comms.manager.QueryManagerImpl;
import enginuity.logger.ecu.comms.query.EcuInitCallback;
import enginuity.logger.ecu.definition.EcuSwitch;
import enginuity.logger.ecu.definition.LoggerData;
import enginuity.logger.ecu.ui.MessageListener;
import enginuity.logger.ecu.ui.StatusChangeListener;
import enginuity.logger.ecu.ui.handler.DataUpdateHandlerManager;
import static enginuity.util.ParamChecker.checkNotNull;

public final class LoggerControllerImpl implements LoggerController {
    private final QueryManager queryManager;

    public LoggerControllerImpl(Settings settings, EcuInitCallback ecuInitCallback, MessageListener messageListener,
                                DataUpdateHandlerManager dataUpdateHandlerManager) {
        checkNotNull(settings, ecuInitCallback, messageListener, dataUpdateHandlerManager);
        queryManager = new QueryManagerImpl(settings, ecuInitCallback, messageListener, dataUpdateHandlerManager);
    }

    public synchronized void addListener(StatusChangeListener listener) {
        checkNotNull(listener, "listener");
        queryManager.addListener(listener);
    }

    public void setFileLoggerSwitch(EcuSwitch ecuSwitch) {
        checkNotNull(ecuSwitch);
        System.out.println("Setting file logger switch:   " + ecuSwitch.getName());
        queryManager.setFileLoggerQuery(ecuSwitch);
    }

    public void addLogger(String callerId, LoggerData loggerData) {
        checkNotNull(loggerData);
        System.out.println("Adding logger:   " + loggerData.getName());
        queryManager.addQuery(callerId, loggerData);
    }

    public void removeLogger(String callerId, LoggerData loggerData) {
        checkNotNull(loggerData, "ecuParam");
        System.out.println("Removing logger: " + loggerData.getName());
        queryManager.removeQuery(callerId, loggerData);
    }

    public synchronized boolean isStarted() {
        return queryManager.isRunning();
    }

    public synchronized void start() {
        if (!isStarted()) {
            Thread queryManagerThread = new Thread(queryManager);
            queryManagerThread.setDaemon(true);
            queryManagerThread.start();
        }
    }

    public synchronized void stop() {
        if (isStarted()) {
            queryManager.stop();
        }
    }

}
