package enginuity.logger.definition;

import static enginuity.logger.definition.EcuDataType.PARAMETER;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class EcuDerivedParameterImpl implements EcuParameter {
    private final String id;
    private final String name;
    private final String description;
    private final EcuDerivedParameterConvertor[] convertors;
    private final String[] addresses;
    private int selectedConvertorIndex = 0;

    public EcuDerivedParameterImpl(String id, String name, String description, EcuData[] ecuDatas, EcuDerivedParameterConvertor[] convertors) {
        checkNotNullOrEmpty(name, "id");
        checkNotNullOrEmpty(name, "name");
        checkNotNull(description, "description");
        checkNotNullOrEmpty(ecuDatas, "ecuDatas");
        checkNotNullOrEmpty(convertors, "convertors");
        this.id = id;
        this.name = name;
        this.description = description;
        this.convertors = convertors;
        addresses = setAddresses(ecuDatas);
        setEcuDatas(ecuDatas);
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

    public void selectConvertor(EcuDataConvertor convertor) {
        for (int i = 0; i < convertors.length; i++) {
            EcuDerivedParameterConvertor parameterConvertor = convertors[i];
            if (convertor == parameterConvertor) {
                selectedConvertorIndex = i;
            }
        }
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

    private void setEcuDatas(EcuData[] ecuDatas) {
        for (EcuDerivedParameterConvertor convertor : convertors) {
            convertor.setEcuDatas(ecuDatas);
        }
    }
}
