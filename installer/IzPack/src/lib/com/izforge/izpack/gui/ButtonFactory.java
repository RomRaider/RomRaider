/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Jan Blok
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

import java.awt.Color;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * This class makes it possible to use default buttons on macosx platform
 */
public class ButtonFactory
{

    private static boolean useHighlightButtons = false;

    private static boolean useButtonIcons = false;

    /**
     * Enable icons for buttons This setting has no effect on OSX
     */
    public static void useButtonIcons()
    {
        useButtonIcons(true);
    }

    /**
     * Enable or disable icons for buttons This setting has no effect on OSX
     * 
     * @param useit flag which determines the behavior
     */
    public static void useButtonIcons(boolean useit)
    {
        if (System.getProperty("mrj.version") == null)
        {
            useButtonIcons = useit;
        }
    }

    /**
     * Enable highlight buttons This setting has no effect on OSX
     */
    public static void useHighlightButtons()
    {
        useHighlightButtons(true);
    }

    /**
     * Enable or disable highlight buttons This setting has no effect on OSX
     * 
     * @param useit flag which determines the behavior
     */
    public static void useHighlightButtons(boolean useit)
    {
        if (System.getProperty("mrj.version") == null)
        {
            useHighlightButtons = useit;
        }
        useButtonIcons(useit);
    }

    public static JButton createButton(Icon icon, Color color)
    {
        if (useHighlightButtons)
        {
            if (useButtonIcons)
                return new HighlightJButton(icon, color);
            else
                return new HighlightJButton("", color);

        }
        else
        {
            if (useButtonIcons)
            {
                return new JButton(icon);
            }
            else
            {
                return new JButton();
            }
        }
    }

    public static JButton createButton(String text, Color color)
    {
        if (useHighlightButtons)
        {
            return new HighlightJButton(text, color);
        }
        else
        {
            return new JButton(text);
        }
    }

    public static JButton createButton(String text, Icon icon, Color color)
    {
        if (useHighlightButtons)
        {
            if (useButtonIcons)
                return new HighlightJButton(text, icon, color);
            else
                return new HighlightJButton(text, color);
        }
        else
        {
            if (useButtonIcons)
            {
                return new JButton(text, icon);
            }
            else
            {
                return new JButton(text);
            }
        }
    }

    public static JButton createButton(Action a, Color color)
    {
        if (useHighlightButtons)
        {
            return new HighlightJButton(a, color);
        }
        else
        {
            return new JButton(a);
        }
    }

}
