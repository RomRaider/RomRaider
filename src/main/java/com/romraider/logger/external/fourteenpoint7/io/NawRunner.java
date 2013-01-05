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

package com.romraider.logger.external.fourteenpoint7.io;

import com.romraider.logger.external.core.RawDataListener;
import com.romraider.logger.external.core.Stoppable;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class NawRunner implements Stoppable {
    private static final Logger LOGGER = getLogger(NawRunner.class);
    private static final byte[] NAW_PROMPT = {0x07};
    private final NawConnection connection;
    private final RawDataListener listener;
    private boolean stop;

    public NawRunner(String port, RawDataListener listener) {
        connection = new NawConnectionImpl(port);
        this.listener = listener;
    }

    public void run() {
        try {
            while (!stop) {
                connection.write(NAW_PROMPT);
                final byte[] buffer = connection.readBytes();
                listener.setBytes(buffer);
            }
            connection.close();
        } catch (Throwable t) {
            LOGGER.error("Error occurred", t);
        } finally {
            connection.close();
        }
    }

    public void stop() {
        stop = true;
    }
}
