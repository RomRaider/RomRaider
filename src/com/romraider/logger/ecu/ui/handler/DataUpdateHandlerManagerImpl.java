/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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

package com.romraider.logger.ecu.ui.handler;

import com.romraider.logger.ecu.definition.LoggerData;
import java.util.ArrayList;
import java.util.List;

public final class DataUpdateHandlerManagerImpl implements DataUpdateHandlerManager {
    private final List<DataUpdateHandler> handlers = new ArrayList<DataUpdateHandler>();

    public synchronized void addHandler(DataUpdateHandler handler) {
        handlers.add(handler);
    }

    public synchronized void registerData(LoggerData loggerData) {
        for (DataUpdateHandler handler : handlers) {
            handler.registerData(loggerData);
        }
    }

    public synchronized void deregisterData(LoggerData loggerData) {
        for (DataUpdateHandler handler : handlers) {
            handler.deregisterData(loggerData);
        }
    }

    public synchronized void cleanUp() {
        for (DataUpdateHandler handler : handlers) {
            handler.cleanUp();
        }
    }

    public synchronized void reset() {
        for (DataUpdateHandler handler : handlers) {
            handler.reset();
        }
    }

}
