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

package com.romraider.logger.utec.gui.mapTabs;

import com.ecm.graphics.tools.ColorTable;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.vecmath.Color3f;
import java.awt.Color;
import java.awt.Component;

public class UtecTableCellRenderer extends DefaultTableCellRenderer {
    private double min;
    private double max;
    private Object[] ignoredValues;
    private boolean isInvertedColoring;

    public UtecTableCellRenderer(double min, double max, Object[] ignoredValues, boolean isInvertedColoring) {
        this.min = min;
        this.max = max;
        this.ignoredValues = ignoredValues;
        this.isInvertedColoring = isInvertedColoring;
    }

    /**
     * Called when table needs cell rendering information. Cell logic on color values goes here.
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

        if (isSelected) {
            cell.setBackground(Color.BLUE);
        } else {
            if (value instanceof Double) {
                ColorTable.initColorTable(min, max);
                if (this.isInvertedColoring) {
                    ColorTable.initColorTable(max, min);
                }
                Color3f theColor = ColorTable.getColor((Double) value);
                cell.setBackground(new Color(theColor.x, theColor.y, theColor.z));

                // If out of range color cell red
                if ((Double) value < min || (Double) value > max) {
                    cell.setBackground(Color.RED);
                }
            }

            // Iterate through the ignored values, paint them gray
            for (int i = 0; i < ignoredValues.length; i++) {

                // Double ignored values
                if ((value instanceof Double) && (ignoredValues[i] instanceof Double)) {
                    Double doubleValue = (Double) value;
                    Double ignoredValue = (Double) ignoredValues[i];

                    if ((doubleValue - ignoredValue) == 0) {
                        cell.setBackground(Color.GRAY);
                    }
                }

                // Maybe add string value detection as needed
            }
        }


        return cell;
    }
}
