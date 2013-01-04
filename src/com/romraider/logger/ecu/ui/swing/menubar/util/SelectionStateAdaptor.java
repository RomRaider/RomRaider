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

package com.romraider.logger.ecu.ui.swing.menubar.util;

import static com.romraider.swing.menubar.action.AbstractAction.SELECTED_KEY;
import javax.swing.AbstractButton;
import javax.swing.Action;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public final class SelectionStateAdaptor implements PropertyChangeListener, ItemListener {
    private final Action action;
    private final AbstractButton button;

    public SelectionStateAdaptor(Action action, AbstractButton button) {
        this.action = action;
        this.button = button;
    }

    public void configure() {
        action.addPropertyChangeListener(this);
        button.addItemListener(this);
    }

    public void itemStateChanged(ItemEvent e) {
        action.putValue(SELECTED_KEY, e.getStateChange() == ItemEvent.SELECTED);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SELECTED_KEY)) {
            button.setSelected((Boolean) evt.getNewValue());
        }
    }
}