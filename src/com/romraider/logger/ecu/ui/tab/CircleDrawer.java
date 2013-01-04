/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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
package com.romraider.logger.ecu.ui.tab;

import org.jfree.ui.Drawable;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * An implementation of the {Drawable} interface.
 *
 * @author David Gilbert
 */
public class CircleDrawer implements Drawable {

    /**
     * The outline paint.
     */
    private Paint outlinePaint;

    /**
     * The outline stroke.
     */
    private Stroke outlineStroke;

    /**
     * The fill paint.
     */
    private Paint fillPaint;

    /**
     * Creates a new instance.
     *
     * @param outlinePaint  the outline paint.
     * @param outlineStroke the outline stroke.
     * @param fillPaint     the fill paint.
     */
    public CircleDrawer(Paint outlinePaint, Stroke outlineStroke, Paint fillPaint) {
        this.outlinePaint = outlinePaint;
        this.outlineStroke = outlineStroke;
        this.fillPaint = fillPaint;
    }

    /**
     * Draws the circle.
     *
     * @param g2   the graphics device.
     * @param area the area in which to draw.
     */
    public void draw(Graphics2D g2, Rectangle2D area) {
        Ellipse2D ellipse = new Ellipse2D.Double(area.getX(), area.getY(),
                area.getWidth(), area.getHeight());
        if (this.fillPaint != null) {
            g2.setPaint(this.fillPaint);
            g2.fill(ellipse);
        }
        if (this.outlinePaint != null && this.outlineStroke != null) {
            g2.setPaint(this.outlinePaint);
            g2.setStroke(this.outlineStroke);
            g2.draw(ellipse);
        }

        g2.setPaint(Color.black);
        g2.setStroke(new BasicStroke(1.0f));
        Line2D line1 = new Line2D.Double(area.getCenterX(), area.getMinY(),
                area.getCenterX(), area.getMaxY());
        Line2D line2 = new Line2D.Double(area.getMinX(), area.getCenterY(),
                area.getMaxX(), area.getCenterY());
        g2.draw(line1);
        g2.draw(line2);
    }
}

