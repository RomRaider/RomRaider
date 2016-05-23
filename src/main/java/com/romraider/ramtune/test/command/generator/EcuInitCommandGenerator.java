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

import static java.util.Arrays.asList;

import java.util.List;

import com.romraider.io.protocol.Protocol;
import com.romraider.logger.ecu.definition.Module;

public final class EcuInitCommandGenerator extends AbstractCommandGenerator {

    public EcuInitCommandGenerator(Protocol protocol) {
        super(protocol);
    }

    public List<byte[]> createCommands(Module module, byte[] data, byte[] address,
            int length, boolean blockRead, int blocksize) {
        return asList(protocol.constructEcuInitRequest(module));
    }

    public String toString() {
        return "Init";
    }
}
