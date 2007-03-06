package enginuity.logger.ecu.definition;

public interface LoggerData {

    String getId();

    String getName();

    String getDescription();

    EcuDataConvertor getSelectedConvertor();

    EcuDataConvertor[] getConvertors();

    void selectConvertor(EcuDataConvertor convertor);

    EcuDataType getDataType();
}
