/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

import static com.romraider.logger.ecu.comms.io.connection.LoggerConnectionFactory.getConnection;
import static com.romraider.util.ParamChecker.checkNotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.comms.io.connection.LoggerConnection;
import com.romraider.logger.ecu.comms.manager.PollingStateImpl;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.comms.query.EcuQueryImpl;
import com.romraider.logger.ecu.definition.EcuSwitch;
import com.romraider.logger.ecu.ui.MessageListener;
import com.romraider.logger.ecu.ui.swing.tools.ReadCodesResultsPanel;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;

public final class ReadCodesManagerImpl implements ReadCodesManager {
    private static final Logger LOGGER =
            Logger.getLogger(ReadCodesManagerImpl.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            ReadCodesManagerImpl.class.getName());
    private final MessageListener messageListener;
    private final EcuLogger logger;
    private final List<EcuSwitch> dtcodes;
    private final int ecuInitLength;

    public ReadCodesManagerImpl(EcuLogger logger,
            List<EcuSwitch> dtcodes,
            int ecuInitLength) {
        checkNotNull(logger, dtcodes);
        this.logger = logger;
        this.messageListener = logger;
        this.dtcodes = dtcodes;
        this.ecuInitLength = ecuInitLength;
    }

    @Override
    public final int readCodes() {
        final ArrayList<EcuQuery> queries = new ArrayList<EcuQuery>();
        String lastCode = dtcodes.get(dtcodes.size() - 1).getId();
        if (ecuInitLength < 104) {
            lastCode = "D488";
        }
        else if (ecuInitLength < 56) {
            lastCode = "D256";
        }
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(
                "DT codes ECU init length: " + ecuInitLength +
                ", Last code: " + lastCode);

        for (int i = 0; !dtcodes.get(i).getId().equals(lastCode); i++) {
            queries.add(new EcuQueryImpl(dtcodes.get(i)));
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Adding query for DTC: " + dtcodes.get(i).getName());
        }

        final Settings settings = SettingsManager.getSettings();
        final String target = settings.getDestinationTarget().getName().toUpperCase();
        try {
            final LoggerConnection connection = getConnection(
                    settings.getLoggerProtocol(),
                    settings.getLoggerPort(),
                    settings.getLoggerConnectionProperties());
            try {
                messageListener.reportMessage(MessageFormat.format(
                        rb.getString("READCODES"), target));
                final Collection<EcuQuery> querySet = new ArrayList<EcuQuery>();
                for (int i = 0; i < queries.size(); i += 150) {
                    for (int j = i; (j < i + 150) && (j < queries.size()); j++) {
                        querySet.add(queries.get(j));
                    }
                    connection.sendAddressReads(
                            querySet,
                            settings.getDestinationTarget(),
                            new PollingStateImpl());
                    querySet.clear();
                }
                messageListener.reportMessage(MessageFormat.format(
                        rb.getString("COMPLETE"), target));

                double result = 0;
                final ArrayList<EcuQuery> dtcSet = new ArrayList<EcuQuery>();
                for (EcuQuery query : queries) {
                    result = query.getResponse();
                    if (!(result == -1 || result == 0)) {
                        int tmp = 0;
                        int mem = 0;
                        if (result == 1 || result == 3) tmp = 1;
                        if (result == 2 || result == 3) mem = 1;
                        if (LOGGER.isDebugEnabled())
                            LOGGER.debug("DTC: " +
                                query.getLoggerData().getName() +
                                " tmp:" + tmp + " mem:" + mem);
                        dtcSet.add(query);
                    }
                }
                if (dtcSet.isEmpty()) {
                    LOGGER.info("Success reading " + target +
                            " DTC codes, none set");
                    return -1;
                }
                else {
                    ReadCodesResultsPanel.displayResultsPane(logger, dtcSet);
                }
                return 1;
            }
            finally {
                connection.close();
            }
        }
        catch (Exception e) {
            messageListener.reportMessage(MessageFormat.format(
                    rb.getString("FAILED"), target));
            LOGGER.error("Error reading " + target + " DTC codes", e);

            return 0;
        }
    }
}
