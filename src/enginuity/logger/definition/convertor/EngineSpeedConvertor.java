package enginuity.logger.definition.convertor;

import static enginuity.util.ByteUtil.asInt;

import java.text.DecimalFormat;

public final class EngineSpeedConvertor implements EcuParameterConvertor {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0");

    public double convert(byte[] bytes) {
        return asInt(bytes) / 4;
    }

    public String getUnits() {
        return "rpm";
    }

    public String format(double value) {
        return DECIMAL_FORMAT.format(value);
    }
}
