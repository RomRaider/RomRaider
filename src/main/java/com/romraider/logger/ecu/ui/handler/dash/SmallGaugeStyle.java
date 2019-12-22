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

package com.romraider.logger.ecu.ui.handler.dash;

import com.romraider.logger.ecu.definition.LoggerData;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.awt.Font.PLAIN;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public final class SmallGaugeStyle extends PlainGaugeStyle {
    public SmallGaugeStyle(LoggerData loggerData) {
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
        JPanel data = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 1));
        data.setBackground(BLACK);
        liveValueLabel.setFont(panel.getFont().deriveFont(PLAIN, 20F));
        liveValueLabel.setForeground(WHITE);
        liveValuePanel.setBackground(LIGHT_GREY);
        liveValuePanel.setPreferredSize(new Dimension(85, 60));
        liveValuePanel.add(liveValueLabel, CENTER);
        data.add(liveValuePanel);

        // max/min panel
        JPanel maxMinPanel = new JPanel(new BorderLayout(1, 1));
        maxMinPanel.setBackground(BLACK);
        JPanel maxPanel = buildMaxMinPanel(rb.getString("LBLMAX"), maxLabel);
        JPanel minPanel = buildMaxMinPanel(rb.getString("LBLMIN"), minLabel);
        maxMinPanel.add(maxPanel, NORTH);
        maxMinPanel.add(minPanel, SOUTH);
        data.add(maxMinPanel);

        // progress bar
        progressBar.setStringPainted(false);
        progressBar.setIndeterminate(false);
        progressBar.setPreferredSize(new Dimension(10, 62));
        progressBar.setBackground(WHITE);
        progressBar.setForeground(GREEN);
        data.add(progressBar);

        // add panels
        panel.add(data, CENTER);
    }

    private JPanel buildMaxMinPanel(String title, JLabel label) {
        JPanel panel = new JPanel(new BorderLayout(1, 1));
        label.setFont(panel.getFont().deriveFont(PLAIN, 10F));
        label.setForeground(WHITE);
        panel.setPreferredSize(new Dimension(45, 28));
        panel.setBackground(LIGHT_GREY);
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(panel.getFont().deriveFont(PLAIN, 10F));
        titleLabel.setForeground(WHITE);
        JPanel dataPanel = new JPanel(new BorderLayout());
        dataPanel.setBackground(DARK_GREY);
        dataPanel.add(label, CENTER);
        panel.add(titleLabel, NORTH);
        panel.add(dataPanel, CENTER);
        return panel;
    }
}
