package enginuity.logger.definition;

import static enginuity.logger.definition.EcuDataType.PARAMETER;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

import java.util.HashSet;
import java.util.Set;

public final class EcuParameterImpl implements EcuParameter {
    private final String id;
    private final String name;
    private final String description;
    private final String[] addresses;
    private final EcuDataConvertor[] convertors;
    private final Set<ConvertorUpdateListener> listeners = new HashSet<ConvertorUpdateListener>();
    private int selectedConvertorIndex = 0;

    public EcuParameterImpl(String id, String name, String description, String[] address, EcuDataConvertor[] convertors) {
        checkNotNullOrEmpty(name, "id");
        checkNotNullOrEmpty(name, "name");
        checkNotNull(description, "description");
        checkNotNullOrEmpty(address, "addresses");
        checkNotNullOrEmpty(convertors, "convertors");
        this.id = id;
        this.name = name;
        this.description = description;
        this.addresses = address;
        this.convertors = convertors;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getAddresses() {
        return addresses;
    }

    public EcuDataConvertor getSelectedConvertor() {
        return convertors[selectedConvertorIndex];
    }

    public EcuDataConvertor[] getConvertors() {
        return convertors;
    }

    public void selectConvertor(EcuDataConvertor convertor) {
        if (convertor != getSelectedConvertor()) {
            for (int i = 0; i < convertors.length; i++) {
                EcuDataConvertor dataConvertor = convertors[i];
                if (convertor == dataConvertor) {
                    selectedConvertorIndex = i;
                }
            }
            notifyUpdateListeners();
        }
    }

    public EcuDataType getDataType() {
        return PARAMETER;
    }

    public void addConvertorUpdateListener(ConvertorUpdateListener listener) {
        checkNotNull(listener, "listener");
        listeners.add(listener);
    }

    private void notifyUpdateListeners() {
        for (ConvertorUpdateListener listener : listeners) {
            listener.notifyConvertorUpdate(this);
        }
    }

}
