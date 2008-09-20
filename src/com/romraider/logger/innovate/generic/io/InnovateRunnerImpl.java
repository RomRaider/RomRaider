/*
 *
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
 *
 */

package com.romraider.logger.innovate.generic.io;

import com.romraider.logger.innovate.generic.plugin.DataListener;
import com.romraider.logger.innovate.generic.plugin.InnovateSettings;

public final class InnovateRunnerImpl implements InnovateRunner {
    private final InnovateConnection connection;
    private final DataListener listener;
    private boolean stop;

    public InnovateRunnerImpl(String device, InnovateSettings innovateSettings, DataListener listener, int responseLength) {
        connection = new InnovateConnectionImpl(device, innovateSettings.getPort(), new InnovateConnectionProperties(), responseLength);
        this.listener = listener;
    }

    public void run() {
        try {
            while (!stop) {
                byte[] bytes = connection.read();
                listener.setBytes(bytes);
            }
        } finally {
            connection.close();
        }
    }

    public void stop() {
        stop = true;
    }
}
