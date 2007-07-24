/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.gui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.border.EtchedBorder;

/**
 * Draws an etched line border.
 * 
 * @author Julien Ponge
 */
public class EtchedLineBorder extends EtchedBorder
{

    private static final long serialVersionUID = 3256999956257649201L;

    /**
     * Paints the etched line.
     * 
     * @param c The component to draw the border on.
     * @param g The graphics object.
     * @param x The top-left x.
     * @param y The top-left y.
     * @param width The border width.
     * @param height The border height.
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
    {
        g.translate(x, y);

        g.setColor(etchType == LOWERED ? getShadowColor(c) : getHighlightColor(c));
        g.drawLine(10, 0, width - 2, 0);

        g.setColor(etchType == LOWERED ? getHighlightColor(c) : getShadowColor(c));
        g.drawLine(10, 1, width - 2, 1);

        g.translate(0 - x, 0 - y);
    }
}
