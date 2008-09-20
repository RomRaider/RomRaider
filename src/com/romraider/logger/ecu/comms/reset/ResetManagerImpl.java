/*
 *
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package com.romraider.logger.ecu.comms.reset;

import com.romraider.Settings;
import com.romraider.io.protocol.Protocol;
import com.romraider.io.protocol.ProtocolFactory;
import com.romraider.logger.ecu.comms.io.connection.EcuConnection;
import com.romraider.logger.ecu.comms.io.connection.EcuConnectionImpl;
import com.romraider.logger.ecu.ui.MessageListener;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
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
