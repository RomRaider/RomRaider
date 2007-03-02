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

package enginuity.logger.ui.handler.file;

import enginuity.Settings;
import enginuity.logger.definition.ConvertorUpdateListener;
import enginuity.logger.definition.EcuData;
import enginuity.logger.ui.MessageListener;
import enginuity.logger.ui.StatusChangeListener;
import static enginuity.util.ParamChecker.checkNotNull;

import java.util.ArrayList;
import static java.util.Collections.synchronizedList;
import static java.util.Collections.synchronizedMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FileUpdateHandlerImpl implements FileUpdateHandler, ConvertorUpdateListener {
    private final Map<EcuData, Integer> ecuDatas = synchronizedMap(new LinkedHashMap<EcuData, Integer>());
    private final List<StatusChangeListener> listeners = synchronizedList(new ArrayList<StatusChangeListener>());
    private final Settings settings;
    private final FileLogger fileLogger;
    private Line currentLine = new Line(ecuDatas.keySet());

    public FileUpdateHandlerImpl(Settings settings, MessageListener messageListener) {
        checkNotNull(settings);
        this.settings = settings;
        fileLogger = new FileLoggerImpl(settings, messageListener);
    }

    public synchronized void addListener(StatusChangeListener listener) {
        checkNotNull(listener, "listener");
        listeners.add(listener);
    }

    public synchronized void registerData(EcuData ecuData) {
        if (ecuDatas.keySet().contains(ecuData)) {
            ecuDatas.put(ecuData, ecuDatas.get(ecuData) + 1);
        } else {
            ecuDatas.put(ecuData, 1);
            resetLine();
            writeHeaders();
        }
    }

    public synchronized void handleDataUpdate(EcuData ecuData, double value, long timestamp) {
        if (fileLogger.isStarted()) {
            currentLine.updateParamValue(ecuData, ecuData.getSelectedConvertor().format(value));
            if (currentLine.isFull()) {
                fileLogger.writeLine(currentLine.values(), timestamp);
                resetLine();
            }
        }
    }

    public synchronized void deregisterData(EcuData ecuData) {
        if (ecuDatas.keySet().contains(ecuData) && ecuDatas.get(ecuData) > 1) {
            ecuDatas.put(ecuData, ecuDatas.get(ecuData) - 1);
        } else {
            ecuDatas.remove(ecuData);
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

    public synchronized void notifyConvertorUpdate(EcuData updatedEcuData) {
        resetLine();
        writeHeaders();
    }

    public synchronized void start() {
        if (settings.isFileLoggingControllerSwitchActive() && !fileLogger.isStarted()) {
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
        currentLine = new Line(ecuDatas.keySet());
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
        private final Map<EcuData, String> ecuDataValues;

        public Line(Set<EcuData> ecuParams) {
            this.ecuDataValues = new LinkedHashMap<EcuData, String>();
            for (EcuData ecuParam : ecuParams) {
                ecuDataValues.put(ecuParam, null);
            }
        }

        public synchronized void updateParamValue(EcuData ecuData, String value) {
            if (ecuDataValues.containsKey(ecuData)) {
                ecuDataValues.put(ecuData, value);
            }
        }

        public synchronized boolean isFull() {
            for (EcuData ecuData : ecuDataValues.keySet()) {
                if (ecuDataValues.get(ecuData) == null) {
                    return false;
                }
            }
            return true;
        }

        public synchronized String headers() {
            StringBuilder buffer = new StringBuilder();
            for (EcuData ecuData : ecuDataValues.keySet()) {
                buffer.append(DELIMITER).append(ecuData.getName()).append(" (")
                        .append(ecuData.getSelectedConvertor().getUnits()).append(')');
            }
            return buffer.toString();
        }

        public synchronized String values() {
            StringBuilder buffer = new StringBuilder();
            for (EcuData ecuData : ecuDataValues.keySet()) {
                String value = ecuDataValues.get(ecuData);
                buffer.append(DELIMITER).append(value);
            }
            return buffer.toString();
        }

    }

}
