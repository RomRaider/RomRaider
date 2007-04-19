package enginuity.logger.ecu.definition;

import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class EcuDefinitionImpl implements EcuDefinition {
    private final String ecuId;
    private final String calId;

    public EcuDefinitionImpl(String ecuId, String calId) {
        checkNotNullOrEmpty(ecuId, "ecuId");
        checkNotNullOrEmpty(calId, "calId");
        this.ecuId = ecuId;
        this.calId = calId;
    }

    public String getEcuId() {
        return ecuId;
    }

    public String getCalId() {
        return calId;
    }
}
