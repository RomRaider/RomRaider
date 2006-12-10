package enginuity.logger.ui.handler.table;

import enginuity.logger.definition.EcuData;
import enginuity.logger.ui.handler.DataUpdateHandler;
import enginuity.maps.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TableUpdateHandler implements DataUpdateHandler {
    private static final TableUpdateHandler INSTANCE = new TableUpdateHandler();
    private final Map<String, List<Table>> tableMap = Collections.synchronizedMap(new HashMap<String, List<Table>>());

    private TableUpdateHandler() {
    }

    public void registerData(EcuData ecuData) {
    }

    public void handleDataUpdate(EcuData ecuData, byte[] value, long timestamp) {
        List<Table> tables = tableMap.get(ecuData.getId());
        if (tables != null && !tables.isEmpty()) {
            double liveValue = ecuData.getSelectedConvertor().convert(value);
            for (Table table : tables) {
                table.setLiveValue(liveValue);
            }
        }
    }

    public void deregisterData(EcuData ecuData) {
    }

    public void cleanUp() {
    }

    public void registerTable(Table table) {
        String logParam = table.getLogParam();
        if (!tableMap.containsKey(logParam)) {
            tableMap.put(logParam, new ArrayList<Table>());
        }
        List<Table> tables = tableMap.get(logParam);
        if (!tables.contains(table)) {
            tables.add(table);
        }
    }

    public static TableUpdateHandler getInstance() {
        return INSTANCE;
    }

}
