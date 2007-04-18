package enginuity.logger.ecu.comms.query;

import enginuity.logger.ecu.definition.LoggerData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ResponseImpl implements Response {
    private final Map<LoggerData, Double> dataValues = new LinkedHashMap<LoggerData, Double>();
    private final long timestamp;

    public ResponseImpl() {
        timestamp = System.currentTimeMillis();
    }

    public void setDataValue(LoggerData data, double value) {
        dataValues.put(data, value);
    }

    public Set<LoggerData> getData() {
        return dataValues.keySet();
    }

    public double getDataValue(LoggerData data) {
        return dataValues.get(data);
    }

    public long getTimestamp() {
        return timestamp;
    }
}
