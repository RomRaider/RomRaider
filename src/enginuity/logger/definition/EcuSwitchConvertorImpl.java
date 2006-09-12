package enginuity.logger.definition;

public final class EcuSwitchConvertorImpl implements EcuDataConvertor {
    private final int bit;

    public EcuSwitchConvertorImpl(int bit) {
        checkBit(bit);
        this.bit = bit;
    }

    public double convert(byte[] bytes) {
        return (bytes[0] & (1 << bit)) > 0 ? 1 : 0;
    }

    public String getUnits() {
        return "On/Off";
    }

    public String format(double value) {
        //return value > 0 ? "On" : "Off";
        return value > 0 ? "1" : "0";
    }

    public String toString() {
        return getUnits();
    }

    private void checkBit(int bit) {
        if (bit < 0 || bit > 7) {
            throw new IllegalArgumentException("Bit must be between 0 and 7 inclusive.");
        }
    }

}
