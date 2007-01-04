/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.logger.ui;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.WEST;

public final class StatusIndicator extends JPanel implements StatusChangeListener {
    private final JLabel statusLabel = new JLabel();
    private static final String TEXT_CONNECTING = "Connecting to ECU...";
    private static final String TEXT_READING = "Reading data...";
    private static final String TEXT_LOGGING = "Logging data to file...";
    private static final String TEXT_STOPPED = "Stopped.";
    private static final ImageIcon ICON_CONNECTING = new ImageIcon("./graphics/logger_red.png");
    private static final ImageIcon ICON_READING = new ImageIcon("./graphics/logger_blue.png");
    private static final ImageIcon ICON_LOGGING = new ImageIcon("./graphics/logger_green.png");
    private static final ImageIcon ICON_STOPPED = new ImageIcon("./graphics/logger_red.png");

    public StatusIndicator() {
        setLayout(new BorderLayout());
        add(statusLabel, WEST);
        stopped();
    }

    public void connecting() {
        statusLabel.setText(TEXT_CONNECTING);
        statusLabel.setIcon(ICON_CONNECTING);
    }

    public void readingData() {
        statusLabel.setText(TEXT_READING);
        statusLabel.setIcon(ICON_READING);
    }

    public void loggingData() {
        statusLabel.setText(TEXT_LOGGING);
        statusLabel.setIcon(ICON_LOGGING);
    }

    public void stopped() {
        statusLabel.setText(TEXT_STOPPED);
        statusLabel.setIcon(ICON_STOPPED);
    }
}
