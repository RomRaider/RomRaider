/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2019 RomRaider.com
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

package com.romraider.logger.ecu.comms.globaladjust;

import static com.romraider.logger.ecu.comms.io.connection.LoggerConnectionFactory.getConnection;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.isNullOrEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.comms.io.connection.LoggerConnection;
import com.romraider.logger.ecu.comms.manager.PollingStateImpl;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.comms.query.EcuQueryImpl;
import com.romraider.logger.ecu.definition.EcuData;
import com.romraider.logger.ecu.ui.MessageListener;
import com.romraider.logger.ecu.ui.paramlist.ParameterListTableModel;
import com.romraider.logger.ecu.ui.paramlist.ParameterRow;
import com.romraider.logger.ecu.ui.swing.tools.GlobalAdjustmentsPanel;
import com.romraider.util.ResourceUtil;

public final class SSMGlobalAdjustManager implements GlobalAdjustManager {
    private static final Logger LOGGER = Logger.getLogger(SSMGlobalAdjustManager.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            SSMGlobalAdjustManager.class.getName());
    private static final String ID_P239 = "P239";
    private static final String ID_P240 = "P240";
    private static final String ID_P241 = "P241";
    private final EcuLogger logger;
    private final Settings settings;
    private final MessageListener messageListener;
    private final ParameterListTableModel parmeterList;

    public SSMGlobalAdjustManager(
            EcuLogger logger,
            ParameterListTableModel dataTabParamListTableModel) {

        checkNotNull(logger);
        this.logger = logger;
        this.settings = logger.getSettings();
        this.messageListener = logger;
        this.parmeterList = dataTabParamListTableModel;
    }

    @Override
    public final int ecuGlobalAdjustments() {
        try {
            LoggerConnection connection = getConnection(
                    settings.getLoggerProtocol(), settings.getLoggerPort(),
                    settings.getLoggerConnectionProperties());
            try {
                messageListener.reportMessage(rb.getString("GLOBALVALUES"));
                final Collection<EcuQuery> queries = buildGlobalAdjustQueries();
                connection.sendAddressReads(
                        queries,
                        settings.getDestinationTarget(),
                        new PollingStateImpl());
                messageListener.reportMessage(rb.getString("GLOBALDONE"));
                final GlobalAdjustmentsPanel gap =
                        new GlobalAdjustmentsPanel(logger, queries);
                gap.showGlobalAdjustPanel();
                final int[] results = gap.getResults();
                if (results == null) {
                    return -1;
                }
                else {
                    final Map<EcuQuery, byte[]> writes = new HashMap<EcuQuery, byte[]>();
                    for (EcuQuery query : queries) {
                        if (query.getLoggerData().getId().equals(ID_P239)) {
                            final byte[] timing =
                                    new byte[]{(byte) (results[0] & 0xff)};
                            writes.put(query, timing);
                        }
                        if (query.getLoggerData().getId().equals(ID_P240)) {
                            final byte[] rpmOff = convertRpm(results[1]);
                            writes.put(query, rpmOff);
                        }
                        if (query.getLoggerData().getId().equals(ID_P241)) {
                            final byte[] rpmOn = convertRpm(results[2]);
                            writes.put(query, rpmOn);
                        }
                    }
                    connection.sendAddressWrites(writes, settings.getDestinationTarget());
                }
                return 1;
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            messageListener.reportMessage(
                    rb.getString("NOCONNECTION"));
            LOGGER.error("Error retrieving current ECU global timing value", e);
            return 0;
        }
    }

    private final Collection<EcuQuery> buildGlobalAdjustQueries() {
        final Collection<EcuQuery> query = new ArrayList<EcuQuery>();
        final List<ParameterRow> parameterRows = parmeterList.getParameterRows();
        if (!isNullOrEmpty(parameterRows)) {
            for (ParameterRow parameterRow : parameterRows) {
                final String id = parameterRow.getLoggerData().getId();
                if (id.equals(ID_P239) ||
                        id.equals(ID_P240) ||
                        id.equals(ID_P241)) {

                    query.add(buildEcuQuery(parameterRow));
                }
            }
        }
        return query;
    }

    private final EcuQuery buildEcuQuery(ParameterRow parameterRow) {
        final EcuQuery ecuQuery =
                new EcuQueryImpl((EcuData) parameterRow.getLoggerData());
        return ecuQuery;
    }

    private final byte[] convertRpm(int result) {
        final int rpmConverted = (result + 3200) / 25;
        final byte[] rpmBytes = new byte[]{(byte) (rpmConverted & 0xff)};
        return rpmBytes;
    }
}
