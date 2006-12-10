package enginuity.logger.ui.handler.livedata;

import enginuity.logger.definition.ConvertorUpdateListener;
import enginuity.logger.definition.EcuData;
import enginuity.logger.ui.handler.DataUpdateHandler;

public final class LiveDataUpdateHandler implements DataUpdateHandler, ConvertorUpdateListener {
    private final LiveDataTableModel dataTableModel;

    public LiveDataUpdateHandler(LiveDataTableModel dataTableModel) {
        this.dataTableModel = dataTableModel;
    }

    public void registerData(EcuData ecuData) {
        // add to datatable
        dataTableModel.addParam(ecuData);
    }

    public void handleDataUpdate(EcuData ecuData, byte[] value, long timestamp) {
        // update data table
        dataTableModel.updateParam(ecuData, value);
    }

    public void deregisterData(EcuData ecuData) {
        // remove from datatable
        dataTableModel.removeParam(ecuData);
    }

    public void cleanUp() {
    }

    public void notifyConvertorUpdate(EcuData updatedEcuData) {
        dataTableModel.fireTableDataChanged();
    }
}
