package enginuity.logger.definition.convertor;

import static enginuity.util.ByteUtil.asInt;

import java.text.DecimalFormat;

public final class AirFuelRatioLambdaConvertor implements EcuParameterConvertor {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");

    public double convert(byte[] bytes) {
        return (double) asInt(bytes) / 128.0;
    }

    public String getUnits() {
        return "Lambda";
    }

    public String format(double value) {
        return DECIMAL_FORMAT.format(value);
    }

}
