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

import java.awt.Font;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

/**
 * The IzPack metal theme.
 * 
 * @author Julien Ponge
 */
public class IzPackMetalTheme extends DefaultMetalTheme
{

    /** The fonts color. */
    private ColorUIResource color;

    private FontUIResource controlFont;

    private FontUIResource menuFont;

    private FontUIResource windowTitleFont;

    /** The constructor. */
    public IzPackMetalTheme()
    {
        color = new ColorUIResource(0, 0, 0);

        Font font1 = createFont("Tahoma", Font.PLAIN, 11);
        Font font2 = createFont("Tahoma", Font.BOLD, 11);

        menuFont = new FontUIResource(font1);
        controlFont = new FontUIResource(font1);
        windowTitleFont = new FontUIResource(font2);
    }

    private Font createFont(String name, int style, int size)
    {
        Font font = new Font(name, style, size);
        return ((font == null) ? new Font("Dialog", style, size) : font);
    }

    /**
     * Returns the color.
     * 
     * @return The color.
     */
    public ColorUIResource getControlTextColor()
    {
        return color;
    }

    /**
     * Returns the color.
     * 
     * @return The color.
     */
    public ColorUIResource getMenuTextColor()
    {
        return color;
    }

    /**
     * Returns the color.
     * 
     * @return The color.
     */
    public ColorUIResource getSystemTextColor()
    {
        return color;
    }

    /**
     * Returns the color.
     * 
     * @return The color.
     */
    public ColorUIResource getUserTextColor()
    {
        return color;
    }

    /**
     * The Font of Labels in many cases
     */
    public FontUIResource getControlTextFont()
    {
        return controlFont;
    }

    /**
     * The Font of Menus and MenuItems
     */
    public FontUIResource getMenuTextFont()
    {
        return menuFont;
    }

    /**
     * The Font of Nodes in JTrees
     */
    public FontUIResource getSystemTextFont()
    {
        return controlFont;
    }

    /**
     * The Font in TextFields, EditorPanes, etc.
     */
    public FontUIResource getUserTextFont()
    {
        return controlFont;
    }

    /**
     * The Font of the Title of JInternalFrames
     */
    public FontUIResource getWindowTitleFont()
    {
        return windowTitleFont;
    }

}
