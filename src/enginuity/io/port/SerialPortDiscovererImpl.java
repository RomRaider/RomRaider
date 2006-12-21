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

import gnu.io.CommPortIdentifier;
import static gnu.io.CommPortIdentifier.PORT_SERIAL;
import static gnu.io.CommPortIdentifier.getPortIdentifiers;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class SerialPortDiscovererImpl implements SerialPortDiscoverer {

    @SuppressWarnings({"unchecked"})
    public List<CommPortIdentifier> listPorts() {
        List<CommPortIdentifier> serialPortIdentifiers = new ArrayList<CommPortIdentifier>();
        Enumeration<CommPortIdentifier> portEnum = getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            if (portIdentifier.getPortType() == PORT_SERIAL) {
                serialPortIdentifiers.add(portIdentifier);
            }
        }
        return serialPortIdentifiers;
    }
}
