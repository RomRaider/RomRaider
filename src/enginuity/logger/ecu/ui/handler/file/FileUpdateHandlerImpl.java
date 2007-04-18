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

package enginuity.logger.ecu.ui.handler.file;

import enginuity.Settings;
import enginuity.logger.ecu.comms.query.Response;
import enginuity.logger.ecu.definition.ConvertorUpdateListener;
import enginuity.logger.ecu.definition.LoggerData;
import enginuity.logger.ecu.ui.MessageListener;
import enginuity.logger.ecu.ui.StatusChangeListener;
import static enginuity.util.ParamChecker.checkNotNull;

import java.util.ArrayList;
import static java.util.Collections.synchronizedList;
import static java.util.Collections.synchronizedMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FileUpdateHandlerImpl implements FileUpdateHandler, ConvertorUpdateListener {
    private final Map<LoggerData, Integer> loggerDatas = synchronizedMap(new LinkedHashMap<LoggerData, Integer>());
    private final List<StatusChangeListener> listeners = synchronizedList(new ArrayList<StatusChangeListener>());
    private final FileLogger fileLogger;
    private Line currentLine = new Line(loggerDatas.keySet());

    public FileUpdateHandlerImpl(Settings settings, MessageListener messageListener) {
        fileLogger = new FileLoggerImpl(settings, messageListener);
    }

    public synchronized void addListener(StatusChangeListener listener) {
        checkNotNull(listener, "listener");
        listeners.add(listener);
    }

    public synchronized void registerData(LoggerData loggerData) {
        if (loggerDatas.keySet().contains(loggerData)) {
            loggerDatas.put(loggerData, loggerDatas.get(loggerData) + 1);
        } else {
            loggerDatas.put(loggerData, 1);
            resetLine();
            writeHeaders();
        }
    }

    public synchronized void handleDataUpdate(Response response) {
        if (fileLogger.isStarted()) {
            for (LoggerData loggerData : response.getData()) {
                currentLine.updateParamValue(loggerData, loggerData.getSelectedConvertor().format(response.getDataValue(loggerData)));
            }
            if (currentLine.isFull()) {
                fileLogger.writeLine(currentLine.values(), response.getTimestamp());
                resetLine();
            }
        }
    }

    public synchronized void deregisterData(LoggerData loggerData) {
        if (loggerDatas.keySet().contains(loggerData) && loggerDatas.get(loggerData) > 1) {
            loggerDatas.put(loggerData, loggerDatas.get(loggerData) - 1);
        } else {
            loggerDatas.remove(loggerData);
            resetLine();
            writeHeaders();
        }
    }

    public synchronized void cleanUp() {
        if (fileLogger.isStarted()) {
            fileLogger.stop();
        }
    }

    public synchronized void reset() {
    }

    public synchronized void notifyConvertorUpdate(LoggerData updatedLoggerData) {
        resetLine();
        writeHeaders();
    }

    public synchronized void start() {
        if (!fileLogger.isStarted()) {
            fileLogger.start();
            notifyListeners(true);
            writeHeaders();
        }
    }

    public synchronized void stop() {
        if (fileLogger.isStarted()) {
            fileLogger.stop();
            notifyListeners(false);
        }
    }

    private void resetLine() {
        currentLine = new Line(loggerDatas.keySet());
    }

    private void writeHeaders() {
        if (fileLogger.isStarted()) {
            fileLogger.writeHeaders(currentLine.headers());
        }
    }

    private void notifyListeners(boolean loggingToFile) {
        for (StatusChangeListener listener : listeners) {
            if (loggingToFile) {
                listener.loggingData();
            } else {
                listener.readingData();
            }
        }
    }

    private final class Line {
        private static final char DELIMITER = ',';
        private final Map<LoggerData, String> loggerDataValues;

        public Line(Set<LoggerData> loggerDatas) {
            this.loggerDataValues = new LinkedHashMap<LoggerData, String>();
            for (LoggerData loggerData : loggerDatas) {
                loggerDataValues.put(loggerData, null);
            }
        }

        public synchronized void updateParamValue(LoggerData loggerData, String value) {
            if (loggerDataValues.containsKey(loggerData)) {
                loggerDataValues.put(loggerData, value);
            }
        }

        public synchronized boolean isFull() {
            for (LoggerData loggerData : loggerDataValues.keySet()) {
                if (loggerDataValues.get(loggerData) == null) {
                    return false;
                }
            }
            return true;
        }

        public synchronized String headers() {
            StringBuilder buffer = new StringBuilder();
            for (LoggerData loggerData : loggerDataValues.keySet()) {
                buffer.append(DELIMITER).append(loggerData.getName()).append(" (")
                        .append(loggerData.getSelectedConvertor().getUnits()).append(')');
            }
            return buffer.toString();
        }

        public synchronized String values() {
            StringBuilder buffer = new StringBuilder();
            for (LoggerData loggerData : loggerDataValues.keySet()) {
                String value = loggerDataValues.get(loggerData);
                buffer.append(DELIMITER).append(value);
            }
            return buffer.toString();
        }

    }

}
