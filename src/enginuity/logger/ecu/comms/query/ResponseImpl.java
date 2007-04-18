package enginuity.logger.ecu.comms.query;

import enginuity.logger.ecu.definition.LoggerData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ResponseImpl implements Response {
    private static final double ZERO = 0.0;
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
        Double value = dataValues.get(data);
        return value == null ? ZERO : value;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
