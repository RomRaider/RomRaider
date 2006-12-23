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

import enginuity.logger.ui.handler.file.FileLoggerListener;

import javax.swing.*;
import java.awt.*;
import static java.awt.BorderLayout.WEST;

public final class StatusIndicator extends JPanel implements ControllerListener, FileLoggerListener {
    private final JLabel statusLabel = new JLabel();
    private static final String TEXT_LOGGING = "Reading data...";
    private static final String TEXT_STOPPED = "Stopped.";
    private static final String TEXT_FILE_LOGGING = "Logging to file...";
    private static final ImageIcon ICON_FILE_LOGGING = new ImageIcon("./graphics/logger_green.png");
    private static final ImageIcon ICON_STOPPED = new ImageIcon("./graphics/logger_red.png");
    private static final ImageIcon ICON_LOGGING = new ImageIcon("./graphics/logger_blue.png");

    public StatusIndicator() {
        setLayout(new BorderLayout());
        add(statusLabel, WEST);
        stop();
    }

    public void start() {
        statusLabel.setText(TEXT_LOGGING);
        statusLabel.setIcon(ICON_LOGGING);
    }

    public void stop() {
        statusLabel.setText(TEXT_STOPPED);
        statusLabel.setIcon(ICON_STOPPED);
    }

    public void setLoggingToFile(boolean loggingToFile) {
        if (loggingToFile) {
            fileLogging();
        } else {
            start();
        }
    }

    private void fileLogging() {
        statusLabel.setText(TEXT_FILE_LOGGING);
        statusLabel.setIcon(ICON_FILE_LOGGING);
    }
}
