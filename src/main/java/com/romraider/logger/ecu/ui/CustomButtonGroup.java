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

package com.romraider.logger.ecu.ui;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

/**
 * Custom ButtonGroup allows none or only one button in the group to be selected.
 */
public class CustomButtonGroup extends ButtonGroup {
    private static final long serialVersionUID = 3271988244802054464L;

    /**
     * Sets the selected value for the ButtonModel.
     * Only one button in the group may be selected at a time but this
     * override allows a selected button to be un-selected so none in the group
     * are selected.
     */
    @Override
    public void setSelected(ButtonModel m, boolean b) {
        if (b && m != null && m != getSelection()) {
            super.setSelected(m, b);
            }
        else if (!b && m == getSelection()) {
            clearSelection();
        }
    }
}
