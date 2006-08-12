package enginuity.logger.definition;

import enginuity.logger.definition.convertor.EcuParameterConvertor;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class EcuParameterImpl implements EcuParameter {
    private String name;
    private String description;
    private String address;
    private EcuParameterConvertor convertor;

    public EcuParameterImpl(String name, String description, String address, EcuParameterConvertor convertor) {
        checkNotNullOrEmpty(name, "name");
        checkNotNullOrEmpty(description, "description");
        checkNotNullOrEmpty(address, "address");
        checkNotNull(convertor, "convertor");
        this.name = name;
        this.description = description;
        this.address = address;
        this.convertor = convertor;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public EcuParameterConvertor getConvertor() {
        return convertor;
    }
}
