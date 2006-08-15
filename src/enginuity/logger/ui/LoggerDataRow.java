package enginuity.logger.ui;

import enginuity.logger.definition.EcuParameter;
import static enginuity.util.ParamChecker.checkNotNull;

public final class LoggerDataRow {
    private final EcuParameter ecuParam;
    private double minValue;
    private double maxValue;
    private double currentValue;
    private boolean updated = false;

    public LoggerDataRow(EcuParameter ecuParam) {
        checkNotNull(ecuParam, "ecuParam");
        this.ecuParam = ecuParam;
    }

    public EcuParameter getEcuParam() {
        return ecuParam;
    }

    public String getName() {
        return ecuParam.getName();
    }

    public String getMinValue() {
        return ecuParam.getConvertor().format(minValue);
    }

    public String getMaxValue() {
        return ecuParam.getConvertor().format(maxValue);
    }

    public String getCurrentValue() {
        return ecuParam.getConvertor().format(currentValue);
    }

    public String getUnits() {
        return ecuParam.getConvertor().getUnits();
    }

    public void updateValue(byte[] bytes) {
        currentValue = ecuParam.getConvertor().convert(bytes);
        if (currentValue < minValue || !updated) {
            minValue = currentValue;
        }
        if (currentValue > maxValue || !updated) {
            maxValue = currentValue;
        }
        updated = true;
    }
}