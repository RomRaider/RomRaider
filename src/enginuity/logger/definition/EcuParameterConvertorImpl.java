package enginuity.logger.definition;

import static enginuity.util.ByteUtil.asInt;
import static enginuity.util.JEPUtil.evaluate;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

import java.text.DecimalFormat;

public final class EcuParameterConvertorImpl implements EcuDataConvertor {
    private final String units;
    private final String expression;
    private final DecimalFormat format;

    public EcuParameterConvertorImpl(String units, String expression, String format) {
        checkNotNullOrEmpty(units, "units");
        checkNotNullOrEmpty(expression, "expression");
        checkNotNullOrEmpty(format, "format");
        this.units = units;
        this.expression = expression;
        this.format = new DecimalFormat(format);
    }

    public double convert(byte[] bytes) {
        double value = (double) asInt(bytes);
        double result = evaluate(expression, value);
        return Double.isNaN(result) || Double.isInfinite(result) ? 0.0 : result;
    }

    public String getUnits() {
        return units;
    }

    public String format(double value) {
        return format.format(value);
    }

}
