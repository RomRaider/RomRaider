package enginuity.logger.definition;

//TODO: add addListener() method so parameters can notify listeners when the selected convertor is updated
//TODO: create corresponding ConvertorUpdateListener interface (to be implemented by handlers?)

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
