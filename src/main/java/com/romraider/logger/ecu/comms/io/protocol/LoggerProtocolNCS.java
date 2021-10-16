/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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

import java.util.Collection;

import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.definition.Module;


public interface LoggerProtocolNCS extends LoggerProtocol {

    byte[] constructEcuFastInitRequest(Module module);

    byte[] constructReadSidPidRequest(Module module, byte sid, byte[] pid);

    byte[] constructLoadAddressRequest(Collection<EcuQuery> queries);

    void validateLoadAddressResponse(byte[] response);

    byte[] processReadSidPidResponse(byte[] response);

    byte[] constructReadAddressRequest(Module module,
            Collection<EcuQuery> queries, PollingState pollState);

    byte[] constructEcuIdRequest(Module module);

    byte[] processEcuIdResponse(byte[] response);

    byte[] constructEcuStopRequest(Module module);

    byte[] constructStartDiagRequest(Module module);

    byte[] constructElevatedDiagRequest(Module module);

    Collection<EcuQuery> filterDuplicates(Collection<EcuQuery> queries);

    byte[] constructReadMemoryRequest(Module module, Collection<EcuQuery> queries, int length);

    byte[] constructReadMemoryResponse(int requestSize, int length);

    void processReadMemoryResponses(Collection<EcuQuery> queries, byte[] response);
}
