package enginuity.logger.definition.convertor;

public interface EcuParameterConvertor {

    double convert(byte[] bytes);

    String getUnits();

    String format(double value);

}
