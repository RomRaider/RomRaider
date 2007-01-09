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

package enginuity.logger.ui.handler.dash;

import enginuity.logger.definition.EcuData;
import static enginuity.util.ParamChecker.checkNotNull;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import static javax.swing.border.BevelBorder.LOWERED;
import java.awt.*;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.Color.GREEN;
import static java.awt.Font.PLAIN;

public final class PlainGauge extends Gauge {
    private static final double ZERO = 0.0;
    private static final String BLANK = "";
    private final EcuData ecuData;
    private final JLabel data = new JLabel(BLANK, JLabel.CENTER);
    private final JLabel title = new JLabel(BLANK, JLabel.CENTER);

    public PlainGauge(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        this.ecuData = ecuData;
        initTitleLayout();
        initDataLayout();
        initGaugeLayout();
    }

    public void refreshTitle() {
        title.setText(ecuData.getName() + " (" + ecuData.getSelectedConvertor().getUnits() + ')');
    }

    public void updateValue(byte[] value) {
        refreshValue(ecuData.getSelectedConvertor().convert(value));
    }

    public void resetValue() {
        refreshValue(ZERO);
    }

    private void initTitleLayout() {
    }

    private void initDataLayout() {
        data.setBorder(new BevelBorder(LOWERED));
        data.setFont(data.getFont().deriveFont(PLAIN, 50F));
    }

    private void initGaugeLayout() {
        refreshValue(ZERO);
        refreshTitle();
        setLayout(new BorderLayout());
        setBackground(GREEN);
        add(data, CENTER);
        add(title, NORTH);
    }

    private void refreshValue(double value) {
        data.setText(ecuData.getSelectedConvertor().format(value));
    }

}
