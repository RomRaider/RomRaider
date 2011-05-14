/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2010 RomRaider.com
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

package com.romraider.logger.ecu.ui.swing.menubar.action;

import com.romraider.io.serial.port.SerialPortDiscoverer;
import com.romraider.io.serial.port.SerialPortDiscovererImpl;
import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.swing.menubar.action.AbstractAction;
import gnu.io.CommPortIdentifier;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;
import java.awt.event.ActionEvent;
import java.util.List;

public final class GenericPluginMenuAction extends AbstractAction {
    private final SerialPortDiscoverer portDiscoverer = new SerialPortDiscovererImpl();
    private final ExternalDataSource dataSource;

    public GenericPluginMenuAction(EcuLogger logger, ExternalDataSource dataSource) {
        super(logger);
        this.dataSource = dataSource;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        String port = (String) showInputDialog(
        		logger,
        		"Select COM port:",
        		dataSource.getName() + " Plugin Settings",
        		QUESTION_MESSAGE,
        		null,
                getPorts(),
                dataSource.getPort());
        if (port != null && port.length() > 0) dataSource.setPort(port);
    }

    private String[] getPorts() {
        List<CommPortIdentifier> portIdentifiers = portDiscoverer.listPorts();
        String[] ports = new String[portIdentifiers.size()];
        for (int i = 0; i < portIdentifiers.size(); i++) {
            CommPortIdentifier identifier = portIdentifiers.get(i);
            ports[i] = identifier.getName();
        }
        return ports;
    }
}
