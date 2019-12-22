/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2019 RomRaider.com
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

package com.romraider.logger.ecu.profile;

import com.romraider.swing.GenericFileFilter;
import com.romraider.util.ResourceUtil;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.ResourceBundle;

public final class UserProfileFileFilter extends FileFilter {
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            UserProfileFileFilter.class.getName());
    private final FileFilter filter = new GenericFileFilter(
            rb.getString("FILTER"), "xml");

    public boolean accept(File file) {
        return filter.accept(file);
    }

    public String getDescription() {
        return filter.getDescription();
    }

}
