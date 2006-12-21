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
import enginuity.io.port.SerialPortDiscoverer;
import enginuity.io.port.SerialPortDiscovererImpl;
import enginuity.logger.comms.manager.QueryManager;
import enginuity.logger.comms.manager.QueryManagerImpl;
import enginuity.logger.comms.manager.TransmissionManager;
import enginuity.logger.comms.manager.TransmissionManagerImpl;
import enginuity.logger.comms.query.LoggerCallback;
import enginuity.logger.definition.EcuData;
import enginuity.logger.ui.ControllerListener;
import enginuity.logger.ui.MessageListener;
import static enginuity.util.ParamChecker.checkNotNull;
import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class LoggerControllerImpl implements LoggerController {
    private final QueryManager queryManager;
    private final List<ControllerListener> listeners = Collections.synchronizedList(new ArrayList<ControllerListener>());
    private boolean started = false;

    public LoggerControllerImpl(Settings settings, MessageListener messageListener) {
        TransmissionManager txManager = new TransmissionManagerImpl(settings);
        queryManager = new QueryManagerImpl(this, txManager, messageListener);
    }

    public Set<String> listSerialPorts() {
        SerialPortDiscoverer serialPortDiscoverer = new SerialPortDiscovererImpl();
        List<CommPortIdentifier> portIdentifiers = serialPortDiscoverer.listPorts();
        Set<String> portNames = new TreeSet<String>();
        for (CommPortIdentifier portIdentifier : portIdentifiers) {
            String portName = portIdentifier.getName();
            if (!portNames.contains(portName)) {
                portNames.add(portName);
            }
        }
        return portNames;
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
            startListeners();
            started = true;
        }
    }

    public synchronized void stop() {
        stopListeners();
        queryManager.stop();
        started = false;
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
