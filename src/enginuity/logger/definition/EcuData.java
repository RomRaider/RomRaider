package enginuity.logger.definition;

public interface EcuData {

    String getId();

    String getName();

    String getDescription();

    String[] getAddresses();

    EcuDataConvertor getConvertor();

    EcuDataType getDataType();

}
