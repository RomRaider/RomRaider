package enginuity.logger.definition;

public interface EcuDataConvertor {

    double convert(byte[] bytes);

    String format(double value);

    String getUnits();

}
