package enginuity.logger.ui.handler;

import enginuity.Settings;
import enginuity.logger.definition.EcuData;
import enginuity.logger.definition.EcuDataConvertor;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//TODO: finish log to file: detect start file-logging switch; add header line to log when params change; only process updates when file logging has been started

public final class FileUpdateHandler implements DataUpdateHandler {
    private final Settings settings;
    private final List<EcuData> ecuDatas = new LinkedList<EcuData>();
    private Line currentLine = new Line(ecuDatas);

    public FileUpdateHandler(Settings settings) {
        this.settings = settings;
    }

    public void registerData(EcuData ecuData) {
        ecuDatas.add(ecuData);
        currentLine = new Line(ecuDatas);
    }

    public void handleDataUpdate(EcuData ecuData, byte[] value, long timestamp) {
        currentLine.updateParamValue(ecuData, value, timestamp);
        if (currentLine.isFull()) {
            System.out.println(currentLine.toString());
            currentLine = new Line(ecuDatas);
        }
    }

    public void deregisterData(EcuData ecuData) {
        ecuDatas.remove(ecuData);
        currentLine = new Line(ecuDatas);
    }

    private static final class Line {
        private static final char DELIMITER = ',';
        private final Map<EcuData, byte[]> ecuDataValues;
        private long lastTimestamp;

        public Line(List<EcuData> ecuParams) {
            this.ecuDataValues = new LinkedHashMap<EcuData, byte[]>();
            for (EcuData ecuParam : ecuParams) {
                ecuDataValues.put(ecuParam, null);
            }
        }

        public void updateParamValue(EcuData ecuData, byte[] value, long timestamp) {
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

        public String toString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append(lastTimestamp);
            for (EcuData ecuData : ecuDataValues.keySet()) {
                byte[] value = ecuDataValues.get(ecuData);
                EcuDataConvertor convertor = ecuData.getConvertor();
                buffer.append(DELIMITER).append(convertor.format(convertor.convert(value)));
            }
            return buffer.toString();
        }
    }

}
