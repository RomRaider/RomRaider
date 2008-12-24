/*
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
 */

package com.romraider.logger.ecu.ui.swing.menubar;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public final class EcuLoggerMenuItem extends JMenuItem {

    public EcuLoggerMenuItem(String text, Action action) {
        super(action);
        setText(text);
    }

    public EcuLoggerMenuItem(String text, Action action, int mnemonic) {
        super(action);
        setText(text);
        setMnemonic(mnemonic);
    }

    public EcuLoggerMenuItem(String text, Action action, int mnemonic, KeyStroke accelerator) {
        this(text, action, mnemonic);
        setAccelerator(accelerator);
    }

}
