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

package com.romraider.logger.ecu.ui.handler.dash;

import static java.awt.BorderLayout.CENTER;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public final class Gauge extends JPanel {
    private static final long serialVersionUID = 7354117571944547043L;
    private GaugeStyle style;

    public Gauge(GaugeStyle style) {
        setLayout(new BorderLayout(0, 0));
        setGaugeStyle(style);
    }

    public void refreshTitle() {
        style.refreshTitle();
    }

    public void updateValue(double value) {
        style.updateValue(value);
    }

    public void resetValue() {
        style.resetValue();
    }

    public void setGaugeStyle(final GaugeStyle style) {
        this.style = style;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                removeAll();
                JPanel child = new JPanel();
                style.apply(child);
                add(child, CENTER);
            }
        });
    }

}
