/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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

import com.romraider.logger.ecu.EcuLogger;
import javax.swing.Action;
import java.util.List;
import java.util.Properties;

public interface ExternalDataSource {
    String getId();

    String getName();

    String getVersion();

    List<? extends ExternalDataItem> getDataItems();

    Action getMenuAction(EcuLogger logger);

    void setPort(String port);

    String getPort();

    void setProperties(Properties properties);
    
    public void connect();

    public void disconnect();
}
