package enginuity.logger.definition;

import static enginuity.logger.definition.EcuDataType.PARAMETER;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class EcuDerivedParameterImpl implements EcuParameter {
    private final String id;
    private final String name;
    private final String description;
    private final EcuDerivedParameterConvertor convertor;
    private final String[] addresses;

    public EcuDerivedParameterImpl(String id, String name, String description, EcuData[] ecuDatas, EcuDerivedParameterConvertor convertor) {
        checkNotNullOrEmpty(name, "id");
        checkNotNullOrEmpty(name, "name");
        checkNotNull(description, "description");
        checkNotNullOrEmpty(ecuDatas, "ecuDatas");
        checkNotNull(convertor, "convertor");
        this.id = id;
        this.name = name;
        this.description = description;
        this.convertor = convertor;
        this.convertor.setEcuDatas(ecuDatas);
        addresses = setAddresses(ecuDatas);
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
        return PARAMETER;
    }

    private String[] setAddresses(EcuData[] ecuDatas) {
        String[] addresses = new String[0];
        for (EcuData ecuData : ecuDatas) {
            String[] newAddresses = ecuData.getAddresses();
            String[] tmp = new String[addresses.length + newAddresses.length];
            System.arraycopy(addresses, 0, tmp, 0, addresses.length);
            System.arraycopy(newAddresses, 0, tmp, addresses.length, newAddresses.length);
            addresses = tmp;
        }
        return addresses;
    }
}
