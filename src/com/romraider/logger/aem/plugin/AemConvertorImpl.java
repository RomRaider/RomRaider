package com.romraider.logger.aem.plugin;

import static com.romraider.util.HexUtil.asHex;
import org.apache.log4j.Logger;

public final class AemConvertorImpl implements AemConvertor {
    private static final Logger LOGGER = Logger.getLogger(AemConvertorImpl.class);

    public double convert(byte[] bytes) {
        String value = new String(bytes);
        double result = convert(value);
        LOGGER.trace("Converting AEM response: " + asHex(bytes) + " --> \"" + value + "\" --> " + result);
        return result;
    }

    private double convert(String value) {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            LOGGER.error("Error converting AEM response to double: \"" + value + "\"", e);
            return -1.0;
        }
    }
}
