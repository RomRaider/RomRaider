package enginuity.logger.innovate.lm1.plugin;

import enginuity.logger.innovate.generic.plugin.DataConvertor;
import static enginuity.util.ByteUtil.matchOnes;
import static enginuity.util.ByteUtil.matchZeroes;
import static enginuity.util.HexUtil.asBytes;
import static enginuity.util.HexUtil.asHex;
import org.apache.log4j.Logger;

//TODO: Remove dupe with Lc1DataConvertor
public final class Lm1DataConvertor implements DataConvertor {
    private static final Logger LOGGER = Logger.getLogger(Lm1DataConvertor.class);
    private static final double MAX_AFR = 20.33;

    public double convert(byte[] bytes) {
        LOGGER.trace("Converting LM-1 bytes: " + asHex(bytes));
        if (isLm1(bytes) && isHeaderValid(bytes)) {
            if (isError(bytes)) {
                int error = -1 * getLambda(bytes);
                LOGGER.error("LM-1 error: " + asHex(bytes) + " --> " + error);
                return error;
            }
            if (isOk(bytes)) {
                double afr = getAfr(bytes);
                LOGGER.trace("LM-1 AFR: " + afr);
                return afr > MAX_AFR ? MAX_AFR : afr;
            }
            // out of range value seen on overrun...
            LOGGER.trace("LM-1 response out of range (overrun?): " + asHex(bytes));
            return MAX_AFR;
        }
        LOGGER.error("LM-1 unrecognized response: " + asHex(bytes));
        return 0;
    }

    private double getAfr(byte[] bytes) {
        return (getLambda(bytes) + 500) * getAf(bytes) / 10000.0;
    }

    private int getAf(byte[] bytes) {
        return ((bytes[2] & 1) << 7) | bytes[3];
    }

    // 1x0xxx0x
    private boolean isLm1(byte[] bytes) {
        return bytes.length >= 6 && matchOnes(bytes[2], 128) && matchZeroes(bytes[2], 34);
    }

    // 1x11xx1x 1xxxxxxx
    private boolean isHeaderValid(byte[] bytes) {
        return matchOnes(bytes[0], 178) && matchOnes(bytes[1], 128);
    }

    // 1x00000x
    private boolean isOk(byte[] bytes) {
        return matchOnes(bytes[2], 128) && matchZeroes(bytes[2], 62);
    }

    // 1x01100x
    private boolean isError(byte[] bytes) {
        return matchOnes(bytes[2], 152) && matchZeroes(bytes[2], 38);
    }

    // 00xxxxxx 0xxxxxxx
    private int getLambda(byte[] bytes) {
        return (bytes[4] << 7) | bytes[5];
    }

    public static void main(String[] args) {
        byte[] bytes = asBytes("0xB2808113036F1E650124007000470039003A");
        DataConvertor convertor = new Lm1DataConvertor();
        double result = convertor.convert(bytes);
        System.out.println("result = " + result);
    }
}
