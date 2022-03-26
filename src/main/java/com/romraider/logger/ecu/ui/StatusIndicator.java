/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

import static java.awt.BorderLayout.WEST;
import static java.awt.Font.BOLD;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.romraider.util.ResourceUtil;

import java.awt.BorderLayout;
import java.util.ResourceBundle;

public final class StatusIndicator extends JPanel implements StatusChangeListener {
    private static final long serialVersionUID = -3244690866698807677L;
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            StatusIndicator.class.getName());
    private final JLabel statusLabel = new JLabel();
    private static final String TEXT_CONNECTING = rb.getString("CONNECTING");
    private static final String TEXT_READING = rb.getString("READING");
    private static final String TEXT_READING_EXTERNAL = rb.getString("READING_EXTERNAL");
    private static final String TEXT_LOGGING = rb.getString("LOGGING");
    private static final String TEXT_STOPPED = rb.getString("STOPPED");
    private static final ImageIcon ICON_CONNECTING = new ImageIcon(StatusIndicator.class.getResource("/graphics/logger_blue.png"));
    private static final ImageIcon ICON_READING = new ImageIcon(StatusIndicator.class.getResource("/graphics/logger_green.png"));
    private static final ImageIcon ICON_LOGGING = new ImageIcon(StatusIndicator.class.getResource("/graphics/logger_recording.png"));
    private static final ImageIcon ICON_STOPPED = new ImageIcon(StatusIndicator.class.getResource("/graphics/logger_stop.png"));

    public StatusIndicator() {
        setLayout(new BorderLayout());
        statusLabel.setFont(getFont().deriveFont(BOLD));
        add(statusLabel, WEST);
        stopped();
    }

    @Override
    public void connecting() {
        updateStatusLabel(TEXT_CONNECTING, ICON_CONNECTING);
    }

    @Override
    public void readingData() {
        updateStatusLabel(TEXT_READING, ICON_READING);
    }

    @Override
    public void readingDataExternal() {
        updateStatusLabel(TEXT_READING_EXTERNAL, ICON_READING);
    }

    @Override
    public void loggingData() {
        updateStatusLabel(TEXT_LOGGING, ICON_LOGGING);
    }

    @Override
    public void stopped() {
        updateStatusLabel(TEXT_STOPPED, ICON_STOPPED);
    }

    private void updateStatusLabel(final String text, final ImageIcon icon) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                statusLabel.setText(text);
                statusLabel.setIcon(icon);
            }
        });
    }
}
