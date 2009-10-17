/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
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
 */

package com.romraider.logger.ecu.comms.reset;

import com.romraider.Settings;
import com.romraider.logger.ecu.comms.io.connection.LoggerConnection;
import static com.romraider.logger.ecu.comms.io.connection.LoggerConnectionFactory.getConnection;
import com.romraider.logger.ecu.ui.MessageListener;
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
            LoggerConnection connection = getConnection(settings.getLoggerProtocol(), settings.getLoggerPort(),
                    settings.getLoggerConnectionProperties());
            try {
                messageListener.reportMessage("Sending ECU Reset...");
                connection.ecuReset();
                messageListener.reportMessage("Sending ECU Reset...done.");
                return true;
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            messageListener.reportMessage("Unable to reset ecu - check correct serial port has been selected, cable is connected and ignition is on.");
            LOGGER.error("Error sending ecu reset", e);
            return false;
        }
    }
}
