package enginuity.logger.definition;

import static enginuity.logger.definition.EcuDataType.SWITCH;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class EcuSwitchImpl implements EcuSwitch {
    private final String id;
    private final String name;
    private final String description;
    private final String[] addresses;
    private final EcuDataConvertor[] convertors;
    private final boolean fileLogController;
    private int selectedConvertorIndex = 0;

    public EcuSwitchImpl(String id, String name, String description, String[] address, EcuDataConvertor[] convertors, boolean fileLogController) {
        checkNotNullOrEmpty(id, "id");
        checkNotNullOrEmpty(name, "name");
        checkNotNull(description, "description");
        checkNotNullOrEmpty(address, "addresses");
        checkNotNullOrEmpty(convertors, "convertors");
        this.id = id;
        this.name = name;
        this.description = description;
        this.addresses = address;
        this.convertors = convertors;
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
        return SWITCH;
    }

    public boolean isFileLogController() {
        return fileLogController;
    }
}
