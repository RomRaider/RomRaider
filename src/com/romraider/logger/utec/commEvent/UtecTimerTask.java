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

import com.romraider.logger.utec.comm.UtecSerialConnectionManager;
import java.util.TimerTask;

public class UtecTimerTask extends TimerTask {
    private UtecTimerTaskListener listener = null;
    private String data = "";
    private int counter = 0;
    private StringBuffer stringBuffer = null;

    public UtecTimerTask(UtecTimerTaskListener listener) {
        this.listener = listener;
    }

    public void setData(String data) {
        this.data = data;
        this.stringBuffer = new StringBuffer(data);
        //JutecGUI.getInstance().getJProgressBar().setMinimum(0);
        //JutecGUI.getInstance().getJProgressBar().setMaximum(data.length());
    }

    public void run() {
        char theChar = stringBuffer.charAt(counter);
        System.out.println("->" + theChar + "<-  :" + (int) theChar + "");

        //Send the data to the Utec
        UtecSerialConnectionManager.sendCommandToUtec((int) theChar);

        counter++;
        //JutecGUI.getInstance().getJProgressBar().setValue(counter);

        // Kill the timer after a at the end of the string
        if (counter == data.length()) {
            this.cancel();
            listener.utecCommTimerCompleted();
        }
    }
}
