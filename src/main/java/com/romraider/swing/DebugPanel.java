/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

package com.romraider.swing;

import static com.romraider.Version.PRODUCT_NAME;

import com.romraider.net.URL;
import com.romraider.util.ResourceUtil;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class DebugPanel extends JPanel {

    private static final long serialVersionUID = -7159385694793030962L;
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            DebugPanel.class.getName());

    public DebugPanel(Exception ex, String url) {
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(7, 1));
        top.add(new JLabel(MessageFormat.format(
                rb.getString("LABEL1"),
                PRODUCT_NAME)));
        top.add(new JLabel(rb.getString("LABEL2")));
        top.add(new JLabel(rb.getString("LABEL3")));
        top.add(new JLabel());
        top.add(new URL(url));
        top.add(new JLabel());
        top.add(new JLabel(rb.getString("LABEL4")));
        add(top, BorderLayout.NORTH);

        JTextArea output = new JTextArea(ex.toString());
        add(output, BorderLayout.CENTER);
        output.setAutoscrolls(true);
        output.setRows(10);
        output.setColumns(40);
        ex.printStackTrace();
    }
}
