/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2020 RomRaider.com
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

package com.romraider.logger.ecu.ui.tab;

import com.romraider.maps.Rom;
import com.romraider.maps.Table;
import com.romraider.util.ResourceUtil;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

public final class TableFinder {
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            TableFinder.class.getName());
    public static Table findTableStartsWith(Rom rom, String name) {
        List<Table> tables = rom.findTables("^" + name + ".*$");
        if (tables.isEmpty()) throw new IllegalStateException(
                MessageFormat.format(rb.getString("NOTABLENAME"), name));
        if (tables.size() > 1) throw new IllegalStateException(
                MessageFormat.format(rb.getString("MULTITABLENAME"), name));
        return tables.get(0);
    }
}
