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

package com.romraider.logger.external.phidget.interfacekit.plugin;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.ExternalDataConvertorImpl;
import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.logger.external.phidget.interfacekit.io.IntfKitSensor;
import com.romraider.swing.menubar.action.AbstractAction;
import com.romraider.util.SettingsManager;

/**
 * IntfKitPluginMenuAction is used to populate the Phidgets Plugins menu
 * of the Logger. It will report the device type and serial number of each
 * PhidgetInterfaceKit found and allow the user to custom define each sensor's
 * field values.
 */
public final class IntfKitPluginMenuAction extends AbstractAction {
    private static final String PHIDGET_IK = "Phidget InterfaceKit";
    private List<? extends ExternalDataItem> dataItems;

    /**
     * Initialise the Phidgets Plugins menu item.
     * @param logger - the parent frame to bind the dialog message to
     */
    public IntfKitPluginMenuAction(final EcuLogger logger) {
        super(logger);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        final IntfKitConvertorPanel intfKitPanel =
                new IntfKitConvertorPanel(logger, getDataItems());
        intfKitPanel.displayPanel();
        final JTable table = intfKitPanel.getTable();
        if (table != null) {
            saveChanges(table);
        }
    }

    private List<List<String>> getDataItems() {
        final List<List<String>> ikDataItems = new ArrayList<List<String>>();
        final List<ExternalDataSource> externalSources = logger.getExternalDataSources();
        dataItems = new ArrayList<ExternalDataItem>();
        for (ExternalDataSource source : externalSources) {
            if (source.getName().equals(PHIDGET_IK)) {
                dataItems = source.getDataItems();
                break;
            }
        }
        for (ExternalDataItem item : dataItems) {
            ikDataItems.add(Arrays.asList(
                    item.getName(),
                    item.getConvertors()[0].getExpression(),
                    item.getConvertors()[0].getFormat(),
                    item.getConvertors()[0].getUnits(),
                    String.valueOf(item.getConvertors()[0].getGaugeMinMax().min),
                    String.valueOf(item.getConvertors()[0].getGaugeMinMax().max),
                    String.valueOf(item.getConvertors()[0].getGaugeMinMax().step)
                    ));
        }
        return ikDataItems;
    }

    private final void saveChanges(JTable table) {
        final TableModel tm = table.getModel();
        final Map<String, IntfKitSensor> phidgets = SettingsManager.getSettings().getPhidgetSensors();

        for (int i = 0; i < tm.getRowCount(); i++) {
            String column0 = (String) tm.getValueAt(i, 0);
            String key = column0.replaceAll("Phidget IK Sensor ", "");
            if (phidgets.containsKey(key)) {
                for (int j = 1; j < tm.getColumnCount(); j++) {
                    String value = (String) tm.getValueAt(i, j);
                    switch (j) {
                    case 1:
                        phidgets.get(key).setExpression(value);
                        break;
                    case 2:
                        phidgets.get(key).setFormat(value);
                        break;
                    case 3:
                        phidgets.get(key).setUnits(value);
                        break;
                    case 4:
                        phidgets.get(key).setMinValue(Float.parseFloat(value));
                        break;
                    case 5:
                        phidgets.get(key).setMaxValue(Float.parseFloat(value));
                        break;
                    case 6:
                        phidgets.get(key).setStepValue(Float.parseFloat(value));
                        break;
                    default:
                        break;
                    }
                }
            }
            final IntfKitDataItem ikDataItem = (IntfKitDataItem) dataItems.get(i);
            final GaugeMinMax gaugeMinMax = new GaugeMinMax(
                    Float.parseFloat((String) tm.getValueAt(i, 4)),
                    Float.parseFloat((String) tm.getValueAt(i, 5)),
                    Float.parseFloat((String) tm.getValueAt(i, 6)));

            final EcuDataConvertor[] convertors = ikDataItem.getConvertors();
            convertors[0] = new ExternalDataConvertorImpl(
                    ikDataItem,
                    (String) tm.getValueAt(i, 3),
                    (String) tm.getValueAt(i, 1),
                    (String) tm.getValueAt(i, 2),
                    gaugeMinMax);

            ikDataItem.setConvertors(convertors);
        }
        SettingsManager.getSettings().setPhidgetSensors(phidgets);
        JOptionPane.showMessageDialog(
                logger,
                rb.getString("IPMAMSG"),
                rb.getString("IPMAMSGTITLE"),
                JOptionPane.INFORMATION_MESSAGE);
    }
}
