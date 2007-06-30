package enginuity.logger.aem.plugin;

import enginuity.util.HexUtil;
import org.apache.log4j.Logger;

public final class AemConvertorImpl implements AemConvertor {
    private static final Logger LOGGER = Logger.getLogger(AemConvertorImpl.class);
    public double convert(byte[] bytes) {
        // TODO: Finish me!!
        LOGGER.debug("AEM bytes = " + HexUtil.asHex(bytes));
        return 0;
    }
}
