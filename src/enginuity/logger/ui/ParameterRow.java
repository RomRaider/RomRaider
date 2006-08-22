package enginuity.logger.ui;

import enginuity.logger.definition.EcuData;
import static enginuity.util.ParamChecker.checkNotNull;

public final class ParameterRow {
    private final EcuData ecuData;
    private boolean selected = false;

    public ParameterRow(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        this.ecuData = ecuData;
    }

    public EcuData getEcuData() {
        return ecuData;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}
