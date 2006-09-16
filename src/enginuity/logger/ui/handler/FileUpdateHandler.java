package enginuity.logger.ui.handler;

import enginuity.Settings;
import enginuity.logger.definition.ConvertorUpdateListener;
import enginuity.logger.definition.EcuData;
import enginuity.logger.definition.EcuDataConvertor;
import enginuity.logger.definition.EcuSwitch;
import enginuity.logger.io.file.FileLogger;
import enginuity.logger.io.file.FileLoggerImpl;
import static enginuity.util.ParamChecker.checkNotNull;

import static java.util.Collections.synchronizedList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class FileUpdateHandler implements DataUpdateHandler, ConvertorUpdateListener {
    private final FileLogger fileLogger;
    private final List<EcuData> ecuDatas = synchronizedList(new LinkedList<EcuData>());
    private Line currentLine = new Line(ecuDatas);

    public FileUpdateHandler(Settings settings) {
        checkNotNull(settings);
        fileLogger = new FileLoggerImpl(settings);
    }

    public void registerData(EcuData ecuData) {
        ecuDatas.add(ecuData);
        resetLine();
        writeHeaders();
    }

    public void handleDataUpdate(EcuData ecuData, byte[] bytes, long timestamp) {
        EcuDataConvertor convertor = ecuData.getSelectedConvertor();
        int value = (int) convertor.convert(bytes);
        checkStartStopFileLogging(ecuData, value);
        if (fileLogger.isStarted()) {
            currentLine.updateParamValue(ecuData, convertor.format(value), timestamp);
            if (currentLine.isFull()) {
                fileLogger.writeLine(currentLine.values());
                resetLine();
            }
        }
    }

    public void deregisterData(EcuData ecuData) {
        ecuDatas.remove(ecuData);
        resetLine();
        writeHeaders();
    }

    public void cleanUp() {
        fileLogger.stop();
    }

    public void notifyConvertorUpdate(EcuData updatedEcuData) {
        resetLine();
        writeHeaders();
    }

    private void resetLine() {
        currentLine = new Line(ecuDatas);
    }

    private void checkStartStopFileLogging(EcuData ecuData, int value) {
        if (ecuData instanceof EcuSwitch) {
            EcuSwitch ecuSwitch = (EcuSwitch) ecuData;
            if (ecuSwitch.isFileLogController()) {
                if (value == 1 && !fileLogger.isStarted()) {
                    fileLogger.start();
                    writeHeaders();
                } else if (value == 0 && fileLogger.isStarted()) {
                    fileLogger.stop();
                }
            }
        }
    }

    private void writeHeaders() {
        if (fileLogger.isStarted()) {
            fileLogger.writeLine(currentLine.headers());
        }
    }

    private static final class Line {
        private static final char DELIMITER = ',';
        private final Map<EcuData, String> ecuDataValues;
        private long lastTimestamp;

        public Line(List<EcuData> ecuParams) {
            this.ecuDataValues = new LinkedHashMap<EcuData, String>();
            for (EcuData ecuParam : ecuParams) {
                ecuDataValues.put(ecuParam, null);
            }
        }

        public void updateParamValue(EcuData ecuData, String value, long timestamp) {
            if (ecuDataValues.containsKey(ecuData)) {
                ecuDataValues.put(ecuData, value);
                lastTimestamp = timestamp;
            }
        }

        public boolean isFull() {
            for (EcuData ecuData : ecuDataValues.keySet()) {
                if (ecuDataValues.get(ecuData) == null) {
                    return false;
                }
            }
            return true;
        }

        public String values() {
            StringBuilder buffer = new StringBuilder();
            buffer.append(lastTimestamp);
            for (EcuData ecuData : ecuDataValues.keySet()) {
                String value = ecuDataValues.get(ecuData);
                buffer.append(DELIMITER).append(value);
            }
            return buffer.toString();
        }

        public String headers() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("Time");
            for (EcuData ecuData : ecuDataValues.keySet()) {
                buffer.append(DELIMITER).append(ecuData.getName()).append(" (").append(ecuData.getSelectedConvertor().getUnits()).append(')');
            }
            return buffer.toString();
        }
    }

}
