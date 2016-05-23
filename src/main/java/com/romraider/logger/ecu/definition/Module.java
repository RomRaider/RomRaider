/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2014 RomRaider.com
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

package com.romraider.logger.ecu.definition;

import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

public final class Module {
    private final String name;
    private final byte[] address;
    private final String description;
    private final byte[] tester;
    private final boolean fastpoll;

    public Module(
            String name, byte[] address, String description,
            byte[] tester, boolean fastpoll) {
        
        checkNotNull(name, "name");
        checkNotNullOrEmpty(address, "address");
        checkNotNull(description, "description");
        checkNotNull(tester, "tester");
        this.name = name;
        this.address = address;
        this.description = description;
        this.tester = tester;
        this.fastpoll = fastpoll;
    }

    public String getName() {
        return name;
    }

    public byte[] getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public byte[] getTester() {
        return tester;
    }

    public boolean getFastPoll() {
        return fastpoll;
    }

    @Override
    public String toString() {
        return name.toUpperCase();
    }
}
