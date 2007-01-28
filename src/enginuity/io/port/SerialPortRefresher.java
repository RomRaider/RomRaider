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

package enginuity.io.port;

import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ThreadUtil.sleep;
import gnu.io.CommPortIdentifier;

import javax.swing.*;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class SerialPortRefresher implements Runnable {
    private static final long PORT_REFRESH_INTERVAL = 15000L;
    private final SerialPortDiscoverer serialPortDiscoverer = new SerialPortDiscovererImpl();
    private final SerialPortRefreshListener listener;
    private final String defaultLoggerPort;
    private boolean started;

    public SerialPortRefresher(SerialPortRefreshListener listener, String defaultLoggerPort) {
        checkNotNull(listener);
        this.listener = listener;
        this.defaultLoggerPort = defaultLoggerPort;
    }

    public void run() {
        refreshPortList();
        started = true;
        while (true) {
            sleep(PORT_REFRESH_INTERVAL);
            refreshPortList();
        }
    }

    public boolean isStarted() {
        return started;
    }

    private void refreshPortList() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                listener.refreshPortList(listSerialPorts(), defaultLoggerPort);
            }
        });
    }

    private Set<String> listSerialPorts() {
        List<CommPortIdentifier> portIdentifiers = serialPortDiscoverer.listPorts();
        Set<String> portNames = new TreeSet<String>();
        for (CommPortIdentifier portIdentifier : portIdentifiers) {
            String portName = portIdentifier.getName();
            if (!portNames.contains(portName)) {
                portNames.add(portName);
            }
        }
        return portNames;
    }

}
