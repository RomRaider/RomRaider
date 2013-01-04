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

package com.romraider.logger.ecu.comms.io.protocol;

import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import java.util.Collection;

public interface LoggerProtocol {

    byte[] constructEcuInitRequest(byte id);

    byte[] constructEcuResetRequest(byte id);

    byte[] constructReadAddressRequest(byte id, Collection<EcuQuery> queries);

    byte[] constructReadAddressResponse(Collection<EcuQuery> queries, PollingState pollState);

    byte[] preprocessResponse(byte[] request, byte[] response, PollingState pollState);

    void processEcuInitResponse(EcuInitCallback callback, byte[] response);

    void processEcuResetResponse(byte[] response);

    void processReadAddressResponses(Collection<EcuQuery> queries, byte[] response, PollingState pollState);
}
