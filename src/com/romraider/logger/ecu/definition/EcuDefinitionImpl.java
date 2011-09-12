/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2010 RomRaider.com
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

import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import org.apache.log4j.Logger;


public final class EcuDefinitionImpl implements EcuDefinition {
    private static final Logger LOGGER = Logger.getLogger(EcuDefinitionImpl.class);
    private final String ecuId;
    private final String calId;
    private final String carString;

    public EcuDefinitionImpl(String ecuId, String calId, String carString) {
        checkNotNullOrEmpty(ecuId, "ecuId");
        checkNotNullOrEmpty(calId, "calId");
        checkNotNullOrEmpty(carString, "carString");
        this.ecuId = ecuId;
        this.calId = calId;
        this.carString = carString;
        LOGGER.trace(ecuId + "\t" + calId + "\t" + carString);
    }

    public String getEcuId() {
        return ecuId;
    }

    public String getCalId() {
        return calId;
    }

    public String getCarString() {
        return carString;
    }
}
