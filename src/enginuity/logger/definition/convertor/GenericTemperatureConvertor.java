package enginuity.logger.definition.convertor;

import static enginuity.util.ByteUtil.asInt;

public final class GenericTemperatureConvertor implements EcuParameterConvertor {

    public String convert(byte[] bytes) {
        int degreesC = asInt(bytes) - 40;
        return String.valueOf(degreesC);
    }

    public String getUnits() {
        return "C";
    }

}
