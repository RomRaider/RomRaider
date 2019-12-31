/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2019 RomRaider.com
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

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Map;

import com.romraider.Settings;
import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.definition.Transport;
import com.romraider.swing.menubar.action.AbstractAction;
import com.romraider.util.SettingsManager;

public final class GlobalAdjustmentAction extends AbstractAction {
    final Settings settings = SettingsManager.getSettings();

    public GlobalAdjustmentAction(EcuLogger logger) {
        super(logger);
    }

    @Override
    public final void actionPerformed(ActionEvent actionEvent) {
        if (showConfirmation() == OK_OPTION) {
            final String transport = settings.getTransportProtocol();
            final Module module = settings.getDestinationTarget();
            if (settings.isCanBus()) {
                settings.setTransportProtocol("ISO9141");
                final Module ecuModule = getModule("ECU");
                settings.setDestinationTarget(ecuModule);
            }
            final boolean logging = logger.isLogging();
            if (logging) logger.stopLogging();
            adjustEcu();
            settings.setTransportProtocol(transport);
            settings.setDestinationTarget(module);
            if (logging) logger.startLogging();
        }
    }

    private final int showConfirmation() {
        return showConfirmDialog(logger,
                rb.getString("GAACONFIRM"),
                rb.getString("GAATITLE"),
                YES_NO_OPTION,
                QUESTION_MESSAGE);
    }

    private final void adjustEcu() {
        final int result = doAdjustEcu();
        if (result == 1) {
            showMessageDialog(logger,
                    rb.getString("GAASUCCESSMSG"),
                    rb.getString("GAATITLE"),
                    INFORMATION_MESSAGE);
        }
        else if (result == 0) {
            showMessageDialog(logger,
                    rb.getString("GAAERRORMSG"),
                    rb.getString("GAATITLE"),
                    ERROR_MESSAGE);
        }
        if (result == -1) {
            showMessageDialog(logger,
                    rb.getString("GAACANCELMSG"),
                    rb.getString("GAATITLE"),
                    INFORMATION_MESSAGE);
        }
    }

    private final int doAdjustEcu() {
        try {
            return logger.ecuGlobalAdjustment();
        } catch (Exception e) {
            logger.reportError("Error performing ECU global adjustments", e);
            return 0;
        }
    }

    private Transport getTransportById(String id) {
        for (Transport transport : getTransportMap().keySet()) {
            if (transport.getId().equalsIgnoreCase(id))
                return transport;
        }
        return null;
    }

    private Map<Transport, Collection<Module>> getTransportMap() {
        return logger.getProtocolList().get(settings.getLoggerProtocol());
    }

    private Module getModule(String name) {
        final Collection<Module> modules = getTransportMap().get(
                getTransportById(settings.getTransportProtocol()));
        for (Module module: modules) {
            if (module.getName().equalsIgnoreCase(name))
                return module;
        }
        return null;
    }
}
