package enginuity.logger.ui;

import enginuity.logger.definition.EcuData;
import enginuity.logger.definition.EcuDataConvertor;

public interface UserProfile {

    boolean contains(EcuData ecuData);

    boolean isSelectedOnLiveDataTab(EcuData ecuData);

    boolean isSelectedOnGraphTab(EcuData ecuData);

    boolean isSelectedOnDashTab(EcuData ecuData);

    EcuDataConvertor getSelectedConvertor(EcuData ecuData);

    byte[] getBytes();
}
