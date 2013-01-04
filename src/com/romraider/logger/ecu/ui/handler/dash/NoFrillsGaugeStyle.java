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

import com.romraider.logger.ecu.definition.LoggerData;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.awt.Font.PLAIN;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public final class NoFrillsGaugeStyle extends PlainGaugeStyle {
    public NoFrillsGaugeStyle(LoggerData loggerData) {
        super(loggerData);
    }

    protected void doApply(JPanel panel) {
        refreshTitle();
        resetValue();
        panel.setPreferredSize(new Dimension(150, 78));
        panel.setBackground(LIGHT_GREY);
        panel.setLayout(new BorderLayout(1, 0));

        // title
        title.setFont(panel.getFont().deriveFont(PLAIN, 10F));
        title.setForeground(WHITE);
        panel.add(title, NORTH);

        // data panel
        JPanel data = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        data.setBackground(BLACK);
        liveValueLabel.setFont(panel.getFont().deriveFont(PLAIN, 35F));
        liveValueLabel.setForeground(WHITE);
        liveValuePanel.setBackground(LIGHT_GREY);
        liveValuePanel.setPreferredSize(new Dimension(144, 60));
        liveValuePanel.add(liveValueLabel, CENTER);
        data.add(liveValuePanel);

        // add panels
        panel.add(data, CENTER);
    }
}