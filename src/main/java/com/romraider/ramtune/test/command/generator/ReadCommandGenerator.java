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

package com.romraider.ramtune.test.command.generator;

import static com.romraider.util.ParamChecker.checkGreaterThanZero;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static java.util.Arrays.asList;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.romraider.io.protocol.Protocol;

public final class ReadCommandGenerator extends AbstractCommandGenerator {
    private static final int INCREMENT_SIZE = 32;
    
    public ReadCommandGenerator(Protocol protocol) {
        super(protocol);
    }

    public List<byte[]> createCommands(byte id, byte[] data, byte[] address, int length) {
        checkGreaterThanZero(id, "Target ID");
        checkNotNullOrEmpty(address, "address");
        checkGreaterThanZero(length, "length");
        if (length == 1) {
            return asList(createCommandForAddress(id, address));
        } else {
            return createCommandsForRange(id, address, length);
        }
    }

    private byte[] createCommandForAddress(byte id, byte[] address) {
        return protocol.constructReadAddressRequest(id, new byte[][]{address});
    }

    private List<byte[]> createCommandsForRange(byte id, byte[] address, int length) {
        List<byte[]> commands = new ArrayList<byte[]>();
        byte[] readAddress = copy(address);
        int i = 0;
        while (i < length) {
            int readLength = (length - i) > INCREMENT_SIZE ? INCREMENT_SIZE : length - i;
            if (readLength == 1) {
                commands.add(createCommandForAddress(id, readAddress));
            } else {
                commands.add(protocol.constructReadMemoryRequest(id, readAddress, readLength));
            }
            i += INCREMENT_SIZE;
            System.arraycopy(incrementAddress(readAddress, readLength), 0, readAddress, 0, readAddress.length);
        }
        return commands;
    }

    private byte[] copy(byte[] bytes) {
        byte[] bytes2 = new byte[bytes.length];
        System.arraycopy(bytes, 0, bytes2, 0, bytes2.length);
        return bytes2;
    }

    private byte[] incrementAddress(byte[] address, int increment) {
        BigInteger currentAddr = new BigInteger(1, address);
        String strIncrement = String.valueOf(increment);
        BigInteger bintIncrement = new BigInteger(strIncrement);
        BigInteger newAddress = currentAddr.add(bintIncrement);
        byte[] incAddr = newAddress.toByteArray();
        if (incAddr.length == 1){
            address[0] = 0;
            address[1] = 0;
            address[2] = incAddr[0];
            return address;
        }
        if (incAddr.length == 2){
            address[0] = 0;
            address[1] = incAddr[0];
            address[2] = incAddr[1];
            return address;
        }
        if (incAddr.length == 4){
            System.arraycopy(incAddr, 1, address, 0, 3);
            return address;
        }
        else {
            return incAddr;
        }
    }

    public String toString() {
        return "Read";
    }
}
