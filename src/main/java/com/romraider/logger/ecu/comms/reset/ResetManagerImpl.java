/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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

import static com.romraider.logger.ecu.comms.io.connection.LoggerConnectionFactory.getConnection;
import static com.romraider.util.ParamChecker.checkNotNull;

import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.logger.ecu.comms.io.connection.LoggerConnection;
import com.romraider.logger.ecu.ui.MessageListener;
import com.romraider.util.SettingsManager;

public final class ResetManagerImpl implements ResetManager {
    private static final Logger LOGGER = Logger.getLogger(ResetManagerImpl.class);
    private final MessageListener messageListener;

    public ResetManagerImpl(MessageListener messageListener) {
        checkNotNull(messageListener);
        this.messageListener = messageListener;
    }

    @Override
    public boolean resetEcu(int resetCode) {
        final Settings settings = SettingsManager.getSettings();
        final String target = settings.getDestinationTarget().getName();
        try {
            LoggerConnection connection = getConnection(settings.getLoggerProtocol(), settings.getLoggerPort(),
                    settings.getLoggerConnectionProperties());
            try {
                messageListener.reportMessage("Sending " + target + " Reset...");
                connection.ecuReset(settings.getDestinationTarget(), resetCode);
                messageListener.reportMessage("Sending " + target + " Reset...done.");
                return true;
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            messageListener.reportMessage("Unable to reset " + target + " - check correct serial port has been selected, cable is connected and ignition is on.");
            LOGGER.error("Error sending " + target + " reset", e);
            return false;
        }
    }
}
