package enginuity.logger.ui;

import enginuity.logger.definition.EcuParameter;
import enginuity.logger.definition.EcuSwitch;

public interface UserProfile {

    boolean contains(EcuParameter ecuParam);

    boolean contains(EcuSwitch ecuSwitch);

}
