/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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

package com.romraider.logger.external.core;

import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ThreadUtil.sleep;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class GenericDataSourceConnector implements Stoppable {
    private static final Logger LOGGER = getLogger(GenericDataSourceConnector.class);
    private final ExternalDataSource dataSource;
    private boolean stop;

    public GenericDataSourceConnector(ExternalDataSource dataSource) {
        checkNotNull(dataSource);
        this.dataSource = dataSource;
    }

    public void run() {
        LOGGER.info(dataSource.getName() + ": connecting...");
        Thread.currentThread().setName(dataSource.getName());
        
        while (!stop) {
            try {
                dataSource.connect();
                LOGGER.info(dataSource.getName() + ": connected.");
                break;
            } catch (Exception e) {
                LOGGER.error(dataSource.getName() + ": connect error", e);
                sleep(1000L);
            }
        }
    }

    public void stop() {
        stop = true;
    }
}
