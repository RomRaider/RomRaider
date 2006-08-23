package enginuity.logger.definition;

public interface EcuSwitch extends EcuData {

    EcuDataConvertor getConvertor();

    boolean isFileLogController();

}
