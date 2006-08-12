package enginuity.logger.definition.convertor;

import static enginuity.util.ByteUtil.asInt;

public final class ExhaustGasTemperatureConvertor implements EcuParameterConvertor {

    public String convert(byte[] bytes) {
        int degreesC = (asInt(bytes) + 40) * 5;
        return String.valueOf(degreesC);
    }

    public String getUnits() {
        return "C";
    }
}
