package enginuity.logger.ui.handler;

import enginuity.logger.definition.EcuData;
import enginuity.logger.definition.EcuDataConvertor;
import enginuity.maps.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public final class TableUpdateHandler implements DataUpdateHandler {
    private static final TableUpdateHandler INSTANCE = new TableUpdateHandler();
    private final Map<String, List<TableWrapper>> tables = Collections.synchronizedMap(new HashMap<String, List<TableWrapper>>());

    private TableUpdateHandler() {
    }

    public void registerData(EcuData ecuData) {
    }

    public void handleDataUpdate(EcuData ecuData, byte[] value, long timestamp) {
        List<TableWrapper> wrappers = tables.get(ecuData.getId());
        if (wrappers != null && !wrappers.isEmpty()) {
            EcuDataConvertor[] convertors = ecuData.getConvertors();
            for (TableWrapper wrapper : wrappers) {
                try {
                    EcuDataConvertor convertor = convertors[wrapper.getLogParamConvertorIndex()];
                    wrapper.getTable().setLiveValue(convertor.convert(value));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void deregisterData(EcuData ecuData) {
    }

    public void cleanUp() {
    }

    public void registerTable(Table table) {
        TableWrapper wrapper = new TableWrapper(table);
        String logParamId = wrapper.getLogParamId();
        if (!tables.containsKey(logParamId)) {
            tables.put(logParamId, new ArrayList<TableWrapper>());
        }
        tables.get(logParamId).add(wrapper);
    }

    public static TableUpdateHandler getInstance() {
        return INSTANCE;
    }

    private static final class TableWrapper {
        private final Table table;
        private String logParamId;
        private int logParamConvertorIndex;

        public TableWrapper(Table table) {
            this.table = table;
            if (table.getLogParam() != null) {
                StringTokenizer st = new StringTokenizer(table.getLogParam(), ":");
                if (st.hasMoreTokens()) {
                    logParamId = st.nextToken();
                    if (st.hasMoreTokens()) {
                        try {
                            logParamConvertorIndex = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException e) {
                            logParamConvertorIndex = 0;
                        }
                    }
                }
            }
        }

        public Table getTable() {
            return table;
        }

        public String getLogParamId() {
            return logParamId;
        }

        public int getLogParamConvertorIndex() {
            return logParamConvertorIndex;
        }

    }

}
