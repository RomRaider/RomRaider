package enginuity.logger.definition;

public interface EcuSwitch extends EcuData {

    EcuDataConvertor getSelectedConvertor();

    boolean isFileLogController();

}
