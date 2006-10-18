package enginuity.logger.ui;

import enginuity.logger.definition.EcuParameter;
import enginuity.logger.definition.EcuSwitch;
import static enginuity.util.ParamChecker.checkNotNull;

import java.util.Set;

public final class UserProfileImpl implements UserProfile {
    private final Set<String> params;
    private final Set<String> switches;

    public UserProfileImpl(Set<String> params, Set<String> switches) {
        checkNotNull(params, "params");
        checkNotNull(switches, "switches");
        this.params = params;
        this.switches = switches;
    }

    public boolean contains(EcuParameter ecuParam) {
        return params.contains(ecuParam.getId());
    }

    public boolean contains(EcuSwitch ecuSwitch) {
        return switches.contains(ecuSwitch.getId());
    }
}
