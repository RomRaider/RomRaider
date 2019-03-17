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

package com.romraider.swing.util;

import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;


/**
 * SelectionVerifier checks the field value to be true or false allowing
 * focus transfer or not.
 */
public class SelectionVerifier extends InputVerifier {
    private boolean selectionValid;

    /**
     * The set method allows an external validity check to change the field
     * value to allowing focus transfer or not.
     * @param valid boolean value
     */
    public void set(final boolean valid) {
        selectionValid = valid;
    }

    @Override
    public boolean verify(final JComponent input) {
        if (!(input instanceof JComboBox)) {
            return true;
        }
        return selectionValid;
    }
}
