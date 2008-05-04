/*
 *
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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
 *
 */

package com.romraider.logger.ecu.definition;

import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

public final class EcuDefinitionImpl implements EcuDefinition {
    private final String ecuId;
    private final String calId;

    public EcuDefinitionImpl(String ecuId, String calId) {
        checkNotNullOrEmpty(ecuId, "ecuId");
        checkNotNullOrEmpty(calId, "calId");
        this.ecuId = ecuId;
        this.calId = calId;
    }

    public String getEcuId() {
        return ecuId;
    }

    public String getCalId() {
        return calId;
    }
}
