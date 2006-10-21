package enginuity.logger.ui;

import enginuity.logger.definition.EcuData;
import enginuity.logger.definition.EcuDataConvertor;
import enginuity.logger.definition.EcuParameter;
import enginuity.logger.definition.EcuSwitch;
import enginuity.logger.exception.ConfigurationException;
import static enginuity.util.ParamChecker.checkNotNull;

import java.util.HashMap;
import java.util.Map;

public final class UserProfileImpl implements UserProfile {
    private final HashMap<String, UserProfileItem> params;
    private final HashMap<String, UserProfileItem> switches;

    public UserProfileImpl(HashMap<String, UserProfileItem> params, HashMap<String, UserProfileItem> switches) {
        checkNotNull(params, "params");
        checkNotNull(switches, "switches");
        this.params = params;
        this.switches = switches;
    }

    public boolean contains(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        return getMap(ecuData).keySet().contains(ecuData.getId());
    }

    public boolean isSelectedOnLiveDataTab(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        return getUserProfileItem(ecuData).isLiveDataSelected();
    }

    public boolean isSelectedOnGraphTab(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        return getUserProfileItem(ecuData).isGraphSelected();
    }

    public boolean isSelectedOnDashTab(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        return getUserProfileItem(ecuData).isDashSelected();
    }

    public EcuDataConvertor getSelectedConvertor(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        String defaultUnits = getUserProfileItem(ecuData).getUnits();
        if (defaultUnits != null && ecuData.getConvertors().length > 1) {
            for (EcuDataConvertor convertor : ecuData.getConvertors()) {
                if (defaultUnits.equals(convertor.getUnits())) {
                    return convertor;
                }
            }
            throw new ConfigurationException("Unknown default units, '" + defaultUnits + "', specified for " + ecuData.getName());
        } else {
            return ecuData.getSelectedConvertor();
        }
    }

    private UserProfileItem getUserProfileItem(EcuData ecuData) {
        return getMap(ecuData).get(ecuData.getId());
    }

    private Map<String, UserProfileItem> getMap(EcuData ecuData) {
        if (ecuData instanceof EcuParameter) {
            return params;
        } else if (ecuData instanceof EcuSwitch) {
            return switches;
        } else {
            throw new UnsupportedOperationException("Unknown EcuData type: " + ecuData.getClass());
        }
    }

}
