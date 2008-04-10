package enginuity.logger.innovate.lc1.plugin;

import enginuity.logger.innovate.generic.plugin.DataConvertor;
import static enginuity.util.ByteUtil.matchOnes;
import static enginuity.util.ByteUtil.matchZeroes;
import static enginuity.util.HexUtil.asHex;
import org.apache.log4j.Logger;

public final class Lc1DataConvertor implements DataConvertor {
    private static final Logger LOGGER = Logger.getLogger(Lc1DataConvertor.class);
    private static final double MAX_AFR = 20.33;

    public double convert(byte[] bytes) {
        /*
       Example bytes: 10110010 10000010 01000011 00010011 00010111 00101111
        */
        LOGGER.trace("Converting LC-1 bytes: " + asHex(bytes));
        if (isLc1(bytes) && isHeaderValid(bytes)) {
            if (isError(bytes)) {
                int error = -1 * getLambda(bytes);
                LOGGER.error("LC-1 error: " + asHex(bytes) + " --> " + error);
                return error;
            }
            if (isOk(bytes)) {
                double afr = getAfr(bytes);
                LOGGER.trace("LC-1 AFR: " + afr);
                return afr > MAX_AFR ? MAX_AFR : afr;
            }
            // out of range value seen on overrun...
            LOGGER.trace("LC-1 response out of range (overrun?): " + asHex(bytes));
            return MAX_AFR;
        }
        LOGGER.error("LC-1 unrecognized response: " + asHex(bytes));
        return 0;
    }

    private double getAfr(byte[] bytes) {
        return (getLambda(bytes) + 500) * getAf(bytes) / 10000.0;
    }

    private int getAf(byte[] bytes) {
        return ((bytes[2] & 1) << 7) | bytes[3];
    }

    // 010xxx1x
    private boolean isLc1(byte[] bytes) {
        return bytes.length >= 6 && matchOnes(bytes[2], 66) && matchZeroes(bytes[2], 160);
    }

    // 1x11xx1x 1xxxxxxx
    private boolean isHeaderValid(byte[] bytes) {
        return matchOnes(bytes[0], 178) && matchOnes(bytes[1], 128);
    }

    // 0100001x
    private boolean isOk(byte[] bytes) {
        return matchOnes(bytes[2], 66) && matchZeroes(bytes[2], 188);
    }

    // 0101101x
    private boolean isError(byte[] bytes) {
        return matchOnes(bytes[2], 90) && matchZeroes(bytes[2], 164);
    }

    // 01xxxxxx 0xxxxxxx
    private int getLambda(byte[] bytes) {
        return ((bytes[4] & 63) << 7) | bytes[5];
    }
}
