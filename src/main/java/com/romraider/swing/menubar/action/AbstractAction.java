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

package com.romraider.swing.menubar.action;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.util.ResourceUtil;

import javax.swing.Action;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public abstract class AbstractAction implements Action {
    protected static final ResourceBundle rb = new ResourceUtil().getBundle(
            AbstractAction.class.getName());
    public static final String SELECTED_KEY = "selected";
    private final Map<String, Object> valueMap = new HashMap<String, Object>();
    private boolean enabled = true;
    protected EcuLogger logger;

    public AbstractAction(EcuLogger logger) {
        this.logger = logger;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }

    public Object getValue(String key) {
        return valueMap.get(key);
    }

    public void putValue(String key, Object value) {
        valueMap.put(key, value);
    }
}
