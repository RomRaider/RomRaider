package enginuity.logger.definition.convertor;

public interface EcuParameterConvertor {

    String convert(byte[] bytes);

    String getUnits();

}
