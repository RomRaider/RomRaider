package enginuity.logger.ui;

import enginuity.logger.definition.EcuData;
import enginuity.logger.definition.EcuParameter;
import enginuity.logger.definition.EcuSwitch;
import static enginuity.util.ParamChecker.checkNotNull;

import java.util.HashMap;
import java.util.Map;

public final class UserProfileImpl implements UserProfile {
    private final HashMap<String, Boolean[]> params;
    private final HashMap<String, Boolean[]> switches;

    public UserProfileImpl(HashMap<String, Boolean[]> params, HashMap<String, Boolean[]> switches) {
        checkNotNull(params, "params");
        checkNotNull(switches, "switches");
        this.params = params;
        this.switches = switches;
    }

    public boolean contains(EcuData ecuData) {
        return getMap(ecuData).keySet().contains(ecuData.getId());
    }

    public boolean isSelectedOnLiveDataTab(EcuData ecuData) {
        return getSelectedArray(ecuData)[0];
    }

    public boolean isSelectedOnGraphTab(EcuData ecuData) {
        return getSelectedArray(ecuData)[1];
    }

    public boolean isSelectedOnDashTab(EcuData ecuData) {
        return getSelectedArray(ecuData)[2];
    }

    private Boolean[] getSelectedArray(EcuData ecuData) {
        return getMap(ecuData).get(ecuData.getId());
    }

    private Map<String, Boolean[]> getMap(EcuData ecuData) {
        if (ecuData instanceof EcuParameter) {
            return params;
        } else if (ecuData instanceof EcuSwitch) {
            return switches;
        } else {
            throw new UnsupportedOperationException("Unknown EcuData type: " + ecuData.getClass());
        }
    }

}
