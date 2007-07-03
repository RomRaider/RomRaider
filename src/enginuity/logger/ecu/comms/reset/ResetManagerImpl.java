package enginuity.logger.ecu.comms.reset;

import enginuity.Settings;
import enginuity.io.connection.EcuConnection;
import enginuity.io.connection.EcuConnectionImpl;
import enginuity.io.protocol.Protocol;
import enginuity.io.protocol.ProtocolFactory;
import enginuity.logger.ecu.ui.MessageListener;
import static enginuity.util.HexUtil.asHex;
import static enginuity.util.ParamChecker.checkNotNull;
import org.apache.log4j.Logger;

public final class ResetManagerImpl implements ResetManager {
    private static final Logger LOGGER = Logger.getLogger(ResetManagerImpl.class);
    private final Settings settings;
    private final MessageListener messageListener;

    public ResetManagerImpl(Settings settings, MessageListener messageListener) {
        checkNotNull(settings, messageListener);
        this.settings = settings;
        this.messageListener = messageListener;
    }

    public boolean resetEcu() {
        try {
            Protocol protocol = ProtocolFactory.getInstance().getProtocol(settings.getLoggerProtocol());
            EcuConnection ecuConnection = new EcuConnectionImpl(settings.getLoggerConnectionProperties(), settings.getLoggerPort());
            try {
                messageListener.reportMessage("Sending ECU Reset...");
                byte[] request = protocol.constructEcuResetRequest();
                LOGGER.debug("Ecu Reset Request  ---> " + asHex(request));
                byte[] response = ecuConnection.send(request);
                byte[] processedResponse = protocol.preprocessResponse(request, response);
                protocol.checkValidEcuResetResponse(processedResponse);
                LOGGER.debug("Ecu Reset Response <--- " + asHex(processedResponse));
                messageListener.reportMessage("Sending ECU Reset...done.");
                return true;
            } finally {
                ecuConnection.close();
            }
        } catch (Exception e) {
            messageListener.reportMessage("Unable to reset ecu - check correct serial port has been selected, cable is connected and ignition is on.");
            logError(e);
            return false;
        }
    }

    private void logError(Exception e) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Error sending ecu reset", e);
        } else {
            LOGGER.info("Error sending ecu reset: " + e.getMessage());
        }
    }
}
