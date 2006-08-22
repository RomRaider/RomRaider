package enginuity.logger.definition;

public interface EcuData {

    String getName();

    String getDescription();

    String[] getAddresses();

    EcuDataConvertor getConvertor();

}
