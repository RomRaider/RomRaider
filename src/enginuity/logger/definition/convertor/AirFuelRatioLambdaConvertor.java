package enginuity.logger.definition.convertor;

import static enginuity.util.ByteUtil.asInt;

import java.text.DecimalFormat;

public final class AirFuelRatioLambdaConvertor implements EcuParameterConvertor {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");

    public String convert(byte[] bytes) {
        double afr = (double) asInt(bytes) / 128.0;
        return DECIMAL_FORMAT.format(afr);
    }

    public String getUnits() {
        return "Lambda";
    }

}
