package enginuity.logger.definition;

import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class EcuSwitchImpl implements EcuSwitch {
    private String name;
    private String description;
    private String[] addresses;
    private EcuDataConvertor convertor;

    public EcuSwitchImpl(String name, String description, String[] address, EcuDataConvertor convertor) {
        checkNotNullOrEmpty(name, "name");
        checkNotNull(description, "description");
        checkNotNullOrEmpty(address, "addresses");
        checkNotNull(convertor, "convertor");
        this.name = name;
        this.description = description;
        this.addresses = address;
        this.convertor = convertor;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getAddresses() {
        return addresses;
    }

    public EcuDataConvertor getConvertor() {
        return convertor;
    }
}
