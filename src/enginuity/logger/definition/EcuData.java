package enginuity.logger.definition;

//TODO: add addListener() method so parameters can notify listeners when the selected convertor is updated

public interface EcuData {

    String getId();

    String getName();

    String getDescription();

    String[] getAddresses();

    EcuDataConvertor getSelectedConvertor();

    EcuDataConvertor[] getConvertors();

    void selectConvertor(EcuDataConvertor convertor);

    EcuDataType getDataType();

}
