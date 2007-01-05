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

package enginuity.logger.comms.manager;

import enginuity.logger.comms.query.LoggerCallback;
import enginuity.logger.definition.EcuData;
import enginuity.logger.ui.StatusChangeListener;

public interface QueryManager extends Runnable {

    void addQuery(String callerId, EcuData ecuData, LoggerCallback callback);

    void removeQuery(String callerId, EcuData ecuData);

    boolean isRunning();

    void stop();

    void addListener(StatusChangeListener listener);

}
