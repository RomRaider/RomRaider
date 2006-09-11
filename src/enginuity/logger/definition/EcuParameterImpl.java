package enginuity.logger.definition;

import static enginuity.logger.definition.EcuDataType.PARAMETER;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class EcuParameterImpl implements EcuParameter {
    private final String id;
    private final String name;
    private final String description;
    private final String[] addresses;
    private final EcuDataConvertor[] convertors;
    private int selectedConvertorIndex = 0;

    public EcuParameterImpl(String id, String name, String description, String[] address, EcuDataConvertor[] convertors) {
        checkNotNullOrEmpty(name, "id");
        checkNotNullOrEmpty(name, "name");
        checkNotNull(description, "description");
        checkNotNullOrEmpty(address, "addresses");
        checkNotNullOrEmpty(convertors, "convertors");
        this.id = id;
        this.name = name;
        this.description = description;
        this.addresses = address;
        this.convertors = convertors;
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

    public EcuDataConvertor getSelectedConvertor() {
        return convertors[selectedConvertorIndex];
    }

    public EcuDataConvertor[] getConvertors() {
        return convertors;
    }

    public void selectConvertor(int index) {
        if (index < 0) {
            selectedConvertorIndex = 0;
        } else if (index >= convertors.length) {
            selectedConvertorIndex = convertors.length - 1;
        } else {
            selectedConvertorIndex = index;
        }
    }

    public EcuDataType getDataType() {
        return PARAMETER;
    }
}
