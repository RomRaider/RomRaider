package enginuity.logger.definition.convertor;

import static enginuity.util.HexUtil.asHex;

public final class HexConvertor implements EcuParameterConvertor {

    public String convert(byte[] bytes) {
        return asHex(bytes);
    }

    public String getUnits() {
        return "hex";
    }
}
