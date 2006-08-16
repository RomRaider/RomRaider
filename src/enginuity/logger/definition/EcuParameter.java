package enginuity.logger.definition;

public interface EcuParameter {

    String getName();

    String getDescription();

    String[] getAddresses();

    EcuParameterConvertor getConvertor();

}
