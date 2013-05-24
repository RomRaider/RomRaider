/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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

package com.romraider.logger.ecu.comms.readcodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.romraider.Settings;
import com.romraider.logger.ecu.comms.io.connection.LoggerConnection;
import com.romraider.logger.ecu.comms.manager.PollingStateImpl;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.comms.query.EcuQueryImpl;
import com.romraider.logger.ecu.definition.EcuData;
import com.romraider.logger.ecu.definition.EcuSwitch;

import static com.romraider.logger.ecu.comms.io.connection.LoggerConnectionFactory.getConnection;
import com.romraider.logger.ecu.ui.MessageListener;
import com.romraider.util.HexUtil;

import static com.romraider.util.ParamChecker.checkNotNull;
import org.apache.log4j.Logger;

public final class ReadCodesManagerImpl implements ReadCodesManager {
    private static final Logger LOGGER = Logger.getLogger(ReadCodesManagerImpl.class);
    private final Settings settings;
    private final MessageListener messageListener;
    private final List<EcuSwitch> dtcodes;
    private final int ecuInitLength;

    public ReadCodesManagerImpl(Settings settings, 
            MessageListener messageListener,
            List<EcuSwitch> dtcodes,
            int ecuInitLength) {
        checkNotNull(settings, messageListener);
        this.settings = settings;
        this.messageListener = messageListener;
        this.dtcodes = dtcodes;
        this.ecuInitLength = ecuInitLength;
    }

    public boolean readCodes() {
        String target = "ECU";
        final ArrayList<EcuQuery> queries = new ArrayList<EcuQuery>();
        String lastCode = dtcodes.get(dtcodes.size() - 1).getId();
        if (ecuInitLength < 104) {
            lastCode = "D168";
        }
        else if (ecuInitLength < 56) {
            lastCode = "D256";
        }
        LOGGER.debug("ECU init length:" + ecuInitLength + " Last code:" + lastCode);
        for (int i = 0; !dtcodes.get(i).getId().equals(lastCode); i++) {
            queries.add(new EcuQueryImpl((EcuData) dtcodes.get(i)));
            LOGGER.debug("Adding DTC:" + dtcodes.get(i).getName());
        }
        try {
            LoggerConnection connection = getConnection(Settings.getLoggerProtocol(), settings.getLoggerPort(),
                    settings.getLoggerConnectionProperties());
            try {
                if (Settings.getDestinationId() == 0x18) target = "TCU";
                messageListener.reportMessage("Sending " + target + " Read codes...");
                connection.sendAddressReads((Collection<EcuQuery>)queries, Settings.getDestinationId(),
                        new PollingStateImpl());
                messageListener.reportMessage("Sending " + target + " Read codes...complete.");
                for (EcuQuery query : queries) {
                    LOGGER.info(query.getLoggerData().getName() + " result:" + query.getResponse());
                }
                return true;
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            messageListener.reportMessage("Unable to read " + target + " codes - check correct serial port has been selected, cable is connected and ignition is on.");
            LOGGER.error("Error reading " + target + " codes", e);
            return false;
        }
    }
}
