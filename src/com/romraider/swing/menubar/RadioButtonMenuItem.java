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

package com.romraider.swing.menubar;

import com.romraider.logger.ecu.ui.swing.menubar.util.SelectionStateAdaptor;
import javax.swing.Action;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

public final class RadioButtonMenuItem extends JRadioButtonMenuItem {

    private static final long serialVersionUID = -7872873697564909545L;

    public RadioButtonMenuItem(String text, int mnemonic, KeyStroke accelerator, Action action, boolean selected) {
        super(action);
        initSelectionStateAdaptor(action);
        setText(text);
        setMnemonic(mnemonic);
        setAccelerator(accelerator);
        setSelected(selected);
    }

    private void initSelectionStateAdaptor(Action action) {
        new SelectionStateAdaptor(action, this).configure();
    }

}
