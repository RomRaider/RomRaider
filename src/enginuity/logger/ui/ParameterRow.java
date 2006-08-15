package enginuity.logger.ui;

import enginuity.logger.definition.EcuParameter;
import static enginuity.util.ParamChecker.checkNotNull;

public final class ParameterRow {
    private final EcuParameter ecuParam;
    private boolean selected = false;

    public ParameterRow(EcuParameter ecuParam) {
        checkNotNull(ecuParam, "ecuParam");
        this.ecuParam = ecuParam;
    }

    public EcuParameter getEcuParam() {
        return ecuParam;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}
