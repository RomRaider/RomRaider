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

import javax.swing.*;
import java.awt.*;

public class JProgressPane extends JPanel {

    JLabel label = new JLabel();
    JProgressBar progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);

    public JProgressPane() {

        this.setPreferredSize(new Dimension(500, 18));
        this.setLayout(new BorderLayout(1, 2));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setText(" Ready...");
        label.setFont(new Font("Tahoma", Font.PLAIN, 11));
        label.setHorizontalAlignment(JLabel.LEFT);
        progressBar.setMinimumSize(new Dimension(200, 50));

        this.add(progressBar, BorderLayout.WEST);
        this.add(label, BorderLayout.CENTER);

    }

    public void update(String status, int percent) {
        label.setText(" " + status);
        progressBar.setValue(percent);
        repaint();
        this.update(this.getGraphics());
    }
}