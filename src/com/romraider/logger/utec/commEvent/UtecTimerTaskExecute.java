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

package com.romraider.logger.utec.commEvent;

import com.romraider.logger.utec.properties.UtecProperties;
import java.util.Timer;

public class UtecTimerTaskExecute implements UtecTimerTaskListener {
    private final Timer timer = new Timer();
    private int delay = Integer.parseInt(UtecProperties.getProperties("utec.commandTransmissionPauseMS")[0]);
    private int period = Integer.parseInt(UtecProperties.getProperties("utec.dataTransmissionPauseMS")[0]);
    private UtecTimerTask utecTimerTask = new UtecTimerTask(this);

    public UtecTimerTaskExecute(String data) {
        utecTimerTask.setData(data);
        timer.schedule(utecTimerTask, delay, period);
    }

    public void utecCommTimerCompleted() {
        // Ensure that the manager knows we are done
        UtecTimerTaskManager.operationComplete();

        timer.cancel();
    }
}
