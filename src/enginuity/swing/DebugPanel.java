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

package enginuity.swing;

import enginuity.net.URL;

import javax.swing.*;
import java.awt.*;

public class DebugPanel extends JPanel {

    public DebugPanel(Exception ex, String url) {
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(7, 1));
        top.add(new JLabel("Enginuity has encountered an exception. Please review the details below."));
        top.add(new JLabel("If you are unable to fix this problem please visit the following website"));
        top.add(new JLabel("and provide these details and the steps that lead to this error."));
        top.add(new JLabel());
        top.add(new URL(url));
        top.add(new JLabel());
        top.add(new JLabel("Details:"));
        add(top, BorderLayout.NORTH);

        JTextArea output = new JTextArea(ex.getMessage());
        add(output, BorderLayout.CENTER);
        output.setAutoscrolls(true);
        output.setRows(10);
        output.setColumns(40);
        ex.printStackTrace();
    }
}