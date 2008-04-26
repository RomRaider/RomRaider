package enginuity.logger.ecu.definition;

import static enginuity.logger.ecu.definition.EcuDataType.EXTERNAL;
import enginuity.logger.ecu.external.ExternalDataItem;
import enginuity.logger.ecu.external.ExternalDataSource;
import static enginuity.util.ParamChecker.checkNotNull;
import java.text.DecimalFormat;
import java.text.Format;

public final class ExternalDataImpl implements ExternalData {
    private final EcuDataConvertor[] convertors = new EcuDataConvertor[1];
    private final ExternalDataItem dataItem;
    private final ExternalDataSource dataSource;
    private final String id;
    private boolean selected;

    public ExternalDataImpl(final ExternalDataItem dataItem, ExternalDataSource dataSource) {
        checkNotNull(dataItem, dataSource);
        this.dataItem = dataItem;
        this.dataSource = dataSource;
        id = createId(dataItem);
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
        return id;
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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateConnection(selected);
    }

    private String createId(ExternalDataItem dataItem) {
        return "X_" + dataItem.getName().replaceAll(" ", "_");
    }

    private void updateConnection(boolean connect) {
        if (connect) {
            dataSource.connect();
        } else {
            dataSource.disconnect();
        }
    }
}
