package enginuity.logger.ui.handler;

import enginuity.logger.definition.EcuParameter;
import enginuity.logger.ui.LoggerDataTableModel;

public final class LiveDataUpdateHandler implements ParameterUpdateHandler {
    private final LoggerDataTableModel dataTableModel;

    public LiveDataUpdateHandler(LoggerDataTableModel dataTableModel) {
        this.dataTableModel = dataTableModel;
    }

    public void registerParam(EcuParameter ecuParam) {
        // add to datatable
        dataTableModel.addParam(ecuParam);
    }

    public void handleParamUpdate(EcuParameter ecuParam, byte[] value, long timestamp) {
        // update data table
        dataTableModel.updateParam(ecuParam, value);
    }

    public void deregisterParam(EcuParameter ecuParam) {
        // remove from datatable
        dataTableModel.removeParam(ecuParam);
    }

}
