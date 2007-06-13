package enginuity.logger.innovate.plugin;

public final class Lc1ConvertorImpl implements Lc1Convertor {
    public double convert(byte[] bytes) {
        /*
       Example bytes: 10110010 10000010 01000011 00010011 00010111 00101111
        */
        if (isLc1(bytes) && isHeaderValid(bytes)) {
            if (isError(bytes)) {
                return -1 * getLambda(bytes);
            }
            if (isOk(bytes)) {
                return getAfr(bytes);
            }
        }
        return 0;
    }

    private double getAfr(byte[] bytes) {
        return (getLambda(bytes) + 500) * getAF(bytes) / 10000.0;
    }

    private int getAF(byte[] bytes) {
        return (((bytes[2] | 254) & 1) << 7) | bytes[3];
    }

    private int getLambda(byte[] bytes) {
        return ((bytes[4] & 63) << 7) | bytes[5];
    }

    private boolean isOk(byte[] bytes) {
        return ((bytes[2] >> 2) | 56) == 56;
    }

    private boolean isError(byte[] bytes) {
        return matches(bytes[2], 24);
    }

    private boolean isHeaderValid(byte[] bytes) {
        return matches(bytes[0], 178) && matches(bytes[1], 128);
    }

    private boolean isLc1(byte[] bytes) {
        return bytes.length == 6 && matches(bytes[2], 66);
    }

    private boolean matches(byte b, int mask) {
        return (b & mask) == mask;
    }
}
