package enginuity.logger.ecu.definition;

import static enginuity.logger.ecu.definition.EcuDataType.EXTERNAL;
import enginuity.logger.ecu.external.ExternalDataItem;
import static enginuity.util.ParamChecker.checkNotNull;

import java.text.DecimalFormat;
import java.text.Format;

public final class ExternalDataImpl implements ExternalData {
    private final EcuDataConvertor[] convertors = new EcuDataConvertor[1];
    private final ExternalDataItem dataItem;

    public ExternalDataImpl(final ExternalDataItem dataItem) {
        checkNotNull(dataItem);
        this.dataItem = dataItem;
        convertors[0] = new EcuDataConvertor() {
            Format format = new DecimalFormat("0.##");

            public double convert(byte[] bytes) {
                return dataItem.getData();
            }

            public String format(double value) {
                return format.format(value);
            }

            public String getUnits() {
                return dataItem.getUnits();
            }
        };
    }

    public String getId() {
        return "X_" + dataItem.getName();
    }

    public String getName() {
        return dataItem.getName();
    }

    public String getDescription() {
        return dataItem.getDescription();
    }

    public EcuDataConvertor getSelectedConvertor() {
        return convertors[0];
    }

    public EcuDataConvertor[] getConvertors() {
        return convertors;
    }

    public void selectConvertor(EcuDataConvertor convertor) {
    }

    public EcuDataType getDataType() {
        return EXTERNAL;
    }
}
