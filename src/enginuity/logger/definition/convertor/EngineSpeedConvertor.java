package enginuity.logger.definition.convertor;

import static enginuity.util.ByteUtil.asInt;

public final class EngineSpeedConvertor implements EcuParameterConvertor {

    public String convert(byte[] bytes) {
        int rpm = asInt(bytes) / 4;
        return String.valueOf(rpm);
    }

    public String getUnits() {
        return "rpm";
    }
}
