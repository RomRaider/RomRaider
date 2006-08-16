package enginuity.logger.definition;

public interface EcuParameterConvertor {

    double convert(byte[] bytes);

    String getUnits();

    String format(double value);

}
