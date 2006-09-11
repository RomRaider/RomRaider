package enginuity.logger.definition;

public interface EcuData {

    String getId();

    String getName();

    String getDescription();

    String[] getAddresses();

    EcuDataConvertor getSelectedConvertor();

    EcuDataConvertor[] getConvertors();

    void selectConvertor(int index);

    EcuDataType getDataType();

}
