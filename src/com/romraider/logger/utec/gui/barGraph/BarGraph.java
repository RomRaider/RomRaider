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

package com.romraider.logger.utec.gui.barGraph;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

/**
 * @author emorgan
 *         <p/>
 */
public class BarGraph extends JComponent {

    public BarGraph() {

    }

    private void Init() {

    }

    public void paint(Graphics g) {

        //Dimensions of bounding entity
        Dimension theSize = this.getSize();
        int width = theSize.width;
        int height = theSize.height;


        Graphics2D g2 = (Graphics2D) g;
        GradientPaint gp = new GradientPaint((float) 0, (float) 0, Color.RED, (float) 0, (float) height - 10, Color.GREEN);

        RoundRectangle2D rect1 = new RoundRectangle2D.Double(30, 5, 90, 390, 10, 10);
        g2.setPaint(gp);
        g2.fill(rect1);
        g2.setPaint(Color.BLACK);
        g2.draw(rect1);
    }
}
