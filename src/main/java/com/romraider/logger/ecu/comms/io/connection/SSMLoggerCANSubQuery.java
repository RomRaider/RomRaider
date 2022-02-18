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

package com.romraider.logger.ecu.comms.io.connection;

import static com.romraider.io.protocol.ssm.iso15765.SSMProtocol.ADDRESS_SIZE;
import static com.romraider.util.HexUtil.asHex;
import static java.lang.System.arraycopy;
import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.romraider.io.connection.ConnectionManager;
import com.romraider.logger.ecu.comms.io.protocol.LoggerProtocol;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.comms.query.EcuQueryImpl;
import com.romraider.logger.ecu.definition.EcuAddress;
import com.romraider.logger.ecu.definition.EcuAddressImpl;
import com.romraider.logger.ecu.definition.EcuData;
import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.EcuParameterImpl;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.util.HexUtil;

public class SSMLoggerCANSubQuery {
    private static final Logger LOGGER = getLogger(SSMLoggerCANSubQuery.class);
    private static final int CAN_HEADER_LENGTH = 5;
    private static final ArrayList<EcuQuery> subQuery = new ArrayList<EcuQuery>();

    public static final byte[] doSubQuery(
            ArrayList<EcuQuery> tcuSubQuery,
            ConnectionManager manager,
            LoggerProtocol protocol,
            Module module,
            PollingState pollState) {

        final byte[][] addresses = convertToByteAddresses(tcuSubQuery);
        final byte[] responses = new byte[CAN_HEADER_LENGTH + addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            final EcuAddress ea =
                    new EcuAddressImpl("0x" + HexUtil.asHex(addresses[i]), 1, 1);
            final EcuParameterImpl epi =
                    new EcuParameterImpl(tcuSubQuery.get(0).getLoggerData().getId(),
                            tcuSubQuery.get(0).getLoggerData().getName(),
                            tcuSubQuery.get(0).getLoggerData().getDescription(),
                            ea, null, null, null,
                            new EcuDataConvertor[] {
                                tcuSubQuery.get(0).getLoggerData().getSelectedConvertor()
                            }
                    );
            subQuery.clear();
            subQuery.add(new EcuQueryImpl(epi));
            final byte[] request = protocol.constructReadAddressRequest(
                    module, subQuery);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(module + " CAN Sub Request " + i + " ---> " + asHex(request));
            final byte[] response = protocol.constructReadAddressResponse(
                    subQuery, pollState);
            manager.send(request, response, pollState);
            if (i == 0) {
                arraycopy(response, 0, responses, 0, response.length);
            }
            else {
                final byte b = response[response.length - 1 ];
                responses[CAN_HEADER_LENGTH + i] = b;
            }
        }
        return responses;
    }

    private static final byte[][] convertToByteAddresses(Collection<EcuQuery> queries) {
        int byteCount = 0;
        for (EcuQuery query : queries) {
            byteCount += query.getAddresses().length;
        }
        final byte[][] addresses = new byte[byteCount][ADDRESS_SIZE];
        int i = 0;
        for (EcuQuery query : queries) {
            final byte[] bytes = query.getBytes();
            for (int j = 0; j < bytes.length / ADDRESS_SIZE; j++) {
                arraycopy(bytes, j * ADDRESS_SIZE, addresses[i++], 0, ADDRESS_SIZE);
            }
        }
        return addresses;
    }
}
