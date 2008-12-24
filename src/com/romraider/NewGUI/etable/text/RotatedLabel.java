/*
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
 */

package com.romraider.NewGUI.etable.text;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class RotatedLabel extends JPanel implements Icon {

    private static final double NINETY_DEGREES = Math.toRadians(90.0);
    private JLabel label = new JLabel();


    public RotatedLabel(String value) {
        label.setText(value);
    }


    public RotatedLabel(String value, Icon icon) {
        label.setText(value);
        label.setIcon(icon);
    }

    // Implementation of Icon interface
    // (especially useful with side tabs on a JTabbedPane)

    public int getIconHeight() {
        return getPreferredSize().height;
    }

    public int getIconWidth() {
        return getPreferredSize().width;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Dimension d = this.getPreferredSize();
        paintHere(c, g, x, y, d.width, d.height);
    }

    // Delegate these methods to the JLabel...

    public void setText(String value) {
        label.setText(value);
    }

    public String getText() {
        return label.getText();
    }

    public void setIcon(Icon icon) {
        label.setIcon(icon);
    }

    public Icon getIcon() {
        return label.getIcon();
    }

    public void setHorizontalAlignment(int alignment) {
        label.setHorizontalAlignment(alignment);
    }

    public void setVerticalAlignment(int alignment) {
        label.setVerticalAlignment(alignment);
    }


    public Dimension getPreferredSize() {
        Dimension d = label.getPreferredSize();
        return new Dimension(d.height, d.width);
    }


    public void paintComponent(Graphics g) {
        Dimension d = this.getSize();
        paintHere(this, g, 0, 0, d.width, d.height);
    }


    public void paintHere(Component c, Graphics g, int x, int y, int width, int height) {
        if (height <= 0 || width <= 0) return;

        label.updateUI();
        // Paint the JLabel into an image buffer...
        BufferedImage buffer = new BufferedImage(height, width,
                BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g2 = buffer.createGraphics();
        label.setSize(new Dimension(height, width));
        label.paint(g2);

        // ...then apply a transform while painting the buffer into the component
        AffineTransform af = AffineTransform.getTranslateInstance(x, y + height);
        AffineTransform af2 = AffineTransform.getRotateInstance(-NINETY_DEGREES);
        af.concatenate(af2);

        ((Graphics2D) g).drawImage(buffer, af, this);
    }

}
