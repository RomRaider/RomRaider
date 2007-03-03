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

package enginuity.logger.ecu.ui;

import enginuity.logger.ecu.comms.controller.LoggerController;
import enginuity.logger.ecu.comms.query.LoggerCallback;
import enginuity.logger.ecu.definition.EcuData;
import enginuity.logger.ecu.ui.handler.DataUpdateHandlerManager;
import static enginuity.util.ParamChecker.checkNotNull;

import java.util.ArrayList;
import static java.util.Collections.synchronizedList;
import java.util.List;

public final class DataRegistrationBrokerImpl implements DataRegistrationBroker {
    private final List<EcuData> registeredEcuData = synchronizedList(new ArrayList<EcuData>());
    private final LoggerController controller;
    private final DataUpdateHandlerManager handlerManager;
    private final String id;

    public DataRegistrationBrokerImpl(LoggerController controller, DataUpdateHandlerManager handlerManager) {
        checkNotNull(controller, handlerManager);
        this.controller = controller;
        this.handlerManager = handlerManager;
        id = System.currentTimeMillis() + "_" + hashCode();
    }

    public synchronized void registerEcuDataForLogging(final EcuData ecuData) {
        if (!registeredEcuData.contains(ecuData)) {
            // register param with handlers
            handlerManager.registerData(ecuData);

            // add logger and setup callback
            controller.addLogger(id, ecuData, new LoggerCallback() {
                public void callback(byte[] bytes) {
                    // update handlers
                    double value = ecuData.getSelectedConvertor().convert(bytes);
                    handlerManager.handleDataUpdate(ecuData, value, System.currentTimeMillis());
                }
            });

            // add to registered parameters list
            registeredEcuData.add(ecuData);
        }
    }

    public synchronized void deregisterEcuDataFromLogging(EcuData ecuData) {
        if (registeredEcuData.contains(ecuData)) {
            // deregister from dependant objects
            deregisterEcuDataFromDependants(ecuData);

            // remove from registered list
            registeredEcuData.remove(ecuData);
        }

    }

    public synchronized void clear() {
        for (EcuData ecuData : registeredEcuData) {
            deregisterEcuDataFromDependants(ecuData);
        }
        registeredEcuData.clear();
    }

    public synchronized void connecting() {
    }

    public synchronized void readingData() {
    }

    public synchronized void loggingData() {
    }

    public synchronized void stopped() {
    }

    private void deregisterEcuDataFromDependants(EcuData ecuData) {
        // remove logger
        controller.removeLogger(id, ecuData);

        // deregister param from handlers
        handlerManager.deregisterData(ecuData);
    }

}
