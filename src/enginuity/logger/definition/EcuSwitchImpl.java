package enginuity.logger.definition;

import static enginuity.logger.definition.EcuDataType.SWITCH;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class EcuSwitchImpl implements EcuSwitch {
    private final String id;
    private final String name;
    private final String description;
    private final String[] addresses;
    private final EcuDataConvertor convertor;
    private final boolean fileLogController;

    public EcuSwitchImpl(String id, String name, String description, String[] address, EcuDataConvertor convertor, boolean fileLogController) {
        checkNotNullOrEmpty(id, "id");
        checkNotNullOrEmpty(name, "name");
        checkNotNull(description, "description");
        checkNotNullOrEmpty(address, "addresses");
        checkNotNull(convertor, "convertor");
        this.id = id;
        this.name = name;
        this.description = description;
        this.addresses = address;
        this.convertor = convertor;
        this.fileLogController = fileLogController;
    }

    public String getId() {
        return id;
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

    public EcuDataType getDataType() {
        return SWITCH;
    }

    public boolean isFileLogController() {
        return fileLogController;
    }
}
