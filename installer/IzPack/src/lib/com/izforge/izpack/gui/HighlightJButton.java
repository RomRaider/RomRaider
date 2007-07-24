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

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * A button that highlights when the button passes over.
 * 
 * @author Julien Ponge
 */
public class HighlightJButton extends JButton
{

    private static final long serialVersionUID = 3833184718324969525L;

    /**
     * The constructor (use ButtonFactory to create button).
     * 
     * @param icon The icon to display.
     * @param color The highlight color.
     */
    HighlightJButton(Icon icon, Color color)
    {
        super(icon);
        initButton(color);
    }

    /**
     * The constructor (use ButtonFactory to create button).
     * 
     * @param text The text to display.
     * @param color The highlight color.
     */
    HighlightJButton(String text, Color color)
    {
        super(text);
        initButton(color);
    }

    /**
     * The constructor (use ButtonFactory to create button).
     * 
     * @param text The text to display.
     * @param icon The icon to display.
     * @param color The highlight color.
     */
    HighlightJButton(String text, Icon icon, Color color)
    {
        super(text, icon);
        initButton(color);
    }

    /**
     * The constructor (use ButtonFactory to create button).
     * 
     * @param a The action.
     * @param color The highlight color.
     */
    HighlightJButton(Action a, Color color)
    {
        super(a);
        initButton(color);
    }

    /**
     * Does the extra initialisations.
     * 
     * @param highlightColor The highlight color.
     */
    protected void initButton(Color highlightColor)
    {
        this.highlightColor = highlightColor;
        defaultColor = getBackground();

        addMouseListener(new MouseHandler());
    }

    /**
     * Overriden to ensure that the button won't stay highlighted if it had the mouse over it.
     * 
     * @param b Button state.
     */
    public void setEnabled(boolean b)
    {
        reset();
        super.setEnabled(b);
    }

    /** Forces the button to unhighlight. */
    protected void reset()
    {
        setBackground(defaultColor);
    }

    /** The highlighted color. */
    protected Color highlightColor;

    /** The default color. */
    protected Color defaultColor;

    /**
     * The mouse handler which makes the highlighting.
     * 
     * @author Julien Ponge
     */
    private class MouseHandler extends MouseAdapter
    {

        /**
         * When the mouse passes over the button.
         * 
         * @param e The event.
         */
        public void mouseEntered(MouseEvent e)
        {
            if (isEnabled()) setBackground(highlightColor);
        }

        /**
         * When the mouse passes out of the button.
         * 
         * @param e The event.
         */
        public void mouseExited(MouseEvent e)
        {
            if (isEnabled()) setBackground(defaultColor);
        }
    }
}
