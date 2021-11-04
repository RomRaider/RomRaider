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

package com.romraider.io.protocol;

import java.util.Map;

import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.definition.Module;


public interface ProtocolNCS extends Protocol {

    byte[] constructEcuFastInitRequest(Module module);

    byte[] constructReadSidPidRequest(Module module, byte sid, byte[][] pid);

    byte[] constructLoadAddressRequest(Map<byte[], Integer> queryMap);

    void validateLoadAddressResponse(byte[] response);

    byte[] checkValidSidPidResponse(byte[] response);

    byte[] constructReadAddressRequest(Module module, byte[][] bs,
            PollingState pollState);

    byte[] constructEcuIdRequest(Module module);

    byte[] constructEcuStopRequest(Module module);

    byte[] constructStartDiagRequest(Module module);

    byte[] constructElevatedDiagRequest(Module module);

    byte[] constructReadMemoryRequest(Module module, byte[][] address, int numBytes);
}
