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

package com.romraider.ramtune.test.command.generator;

import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import com.romraider.io.protocol.Protocol;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.util.ByteUtil;

public final class WriteCommandGenerator extends AbstractCommandGenerator {

    public WriteCommandGenerator(Protocol protocol) {
        super(protocol);
    }

    public List<byte[]> createCommands(Module module, byte[] data, byte[] address,
            int length, boolean blockRead, int blocksize) {
        checkNotNull(module, "module");
        checkNotNullOrEmpty(address, "address");
        checkNotNullOrEmpty(data, "data");
        final List<byte[]> commands = new ArrayList<byte[]>();
        if (blockRead) {
            commands.add(
                    protocol.constructWriteMemoryRequest(module, address, data));
        }
        else {
            for (int i = 0; i < length; i++) {
                int singleAddress = ByteUtil.asUnsignedInt(address) + i;
                byte[] singleAddrBytes = intToByteArray(singleAddress);
                commands.add(
                        protocol.constructWriteAddressRequest(module, singleAddrBytes, data[i]));
            }
        }
        return commands;
    }

    public String toString() {
        return "Write";
    }

    private final byte[] intToByteArray(int address) {
        final ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(address);
        final byte[] result = new byte[3];
        System.arraycopy(bb.array(), 1, result, 0, result.length);
        return result;
    }
}
