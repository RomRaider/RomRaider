/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Klaus Bartz
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

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * <p>
 * A label factory which can handle modified look like to present icons or present it not.
 * </p>
 * 
 * @author Klaus Bartz
 * 
 */
public class LabelFactory implements SwingConstants
{

    private static boolean useLabelIcons = true;

    /**
     * Returns whether the factory creates labels with icons or without icons.
     * 
     * @return whether the factory creates labels with icons or without icons
     */
    public static boolean isUseLabelIcons()
    {
        return useLabelIcons;
    }

    /**
     * Sets the use icon state.
     * 
     * @param b flag for the icon state
     */
    public static void setUseLabelIcons(boolean b)
    {
        useLabelIcons = b;
    }

    /**
     * Returns a new JLabel with the horizontal alignment CENTER. If isUseLabelIcons is true, the
     * given image will be set to the label, else an empty label returns.
     * 
     * @param image the image to be used as label icon
     * @return new JLabel with the given parameters
     */
    public static JLabel create(Icon image)
    {
        return (create(image, CENTER));

    }

    /**
     * Returns a new JLabel with the given horizontal alignment. If isUseLabelIcons is true, the
     * given image will be set to the label, else an empty label returns.
     * 
     * @param image the image to be used as label icon
     * @param horizontalAlignment horizontal alignment of the label
     * @return new JLabel with the given parameters
     */
    public static JLabel create(Icon image, int horizontalAlignment)
    {
        return (create(null, image, horizontalAlignment));

    }

    /**
     * Returns a new JLabel with the horizontal alignment CENTER.
     * 
     * @param text the text to be set
     * @return new JLabel with the given parameters
     */
    public static JLabel create(String text)
    {
        return (create(text, CENTER));

    }

    /**
     * Returns a new JLabel or FullLineLabel with the horizontal alignment CENTER.
     * 
     * @param text the text to be set
     * @param isFullLine determines whether a FullLineLabel or a JLabel should be created
     * @return new JLabel or FullLineLabel with the given parameters
     */
    public static JLabel create(String text, boolean isFullLine)
    {
        return (create(text, CENTER, isFullLine));

    }

    /**
     * Returns a new JLabel with the given horizontal alignment.
     * 
     * @param text the text to be set
     * @param horizontalAlignment horizontal alignment of the label
     * @return new JLabel with the given parameters
     */
    public static JLabel create(String text, int horizontalAlignment)
    {
        return (create(text, null, horizontalAlignment));

    }

    /**
     * Returns a new JLabel or FullLineLabel with the given horizontal alignment.
     * 
     * @param text the text to be set
     * @param horizontalAlignment horizontal alignment of the label
     * @param isFullLine determines whether a FullLineLabel or a JLabel should be created
     * @return new JLabel or FullLineLabel with the given parameters
     */
    public static JLabel create(String text, int horizontalAlignment, boolean isFullLine)
    {
        return (create(text, null, horizontalAlignment, isFullLine));

    }

    /**
     * Returns a new JLabel with the given horizontal alignment. If isUseLabelIcons is true, the
     * given image will be set to the label. The given text will be set allways to the label. It is
     * allowed, that image and/or text are null.
     * 
     * @param text the text to be set
     * @param image the image to be used as label icon
     * @param horizontalAlignment horizontal alignment of the label
     * @return new JLabel with the given parameters
     */
    public static JLabel create(String text, Icon image, int horizontalAlignment)
    {
        return( create(text, image, horizontalAlignment, false));
    }
    
    /**
     * Returns a new JLabel or FullLineLabel with the given horizontal alignment. If isUseLabelIcons
     * is true, the given image will be set to the label. The given text will be set allways to the
     * label. It is allowed, that image and/or text are null.
     * 
     * @param text the text to be set
     * @param image the image to be used as label icon
     * @param horizontalAlignment horizontal alignment of the label
     * @param isFullLine determines whether a FullLineLabel or a JLabel should be created
     * @return new JLabel or FullLineLabel with the given parameters
     */
    public static JLabel create(String text, Icon image, int horizontalAlignment, boolean isFullLine)
    {
        JLabel retval = null;
        if (image != null && isUseLabelIcons())
        {
            if (isFullLine)
                retval = new FullLineLabel(image);
            else
                retval = new JLabel(image);
        }
        else
        {
            if (isFullLine)
                retval = new FullLineLabel();
            else
                retval = new JLabel();
        }
        if (text != null) retval.setText(text);
        retval.setHorizontalAlignment(horizontalAlignment);
        return (retval);
    }

    /**
     * This class is only needed to signal a different layout handling. There is no additonal
     * functionality related to a JLabel. Only the needed constructors are implemented.
     * A FullLineLabel gets from the IzPanelLayout as default a constraints for a full line.
     * Therefore the width of this label do not determine the width of a column as a JLable
     * it do.
     * 
     * @author Klaus Bartz
     * 
     */
    public static class FullLineLabel extends JLabel
    {

        /**
         * Creates a <code>JLabel</code> instance with the specified image.
         * The label is centered vertically and horizontally
         * in its display area.
         *
         * @param image  The image to be displayed by the label.
         */
        public FullLineLabel(Icon image)
        {
            super(image);
        }

        /**
         * Default constructor.
         */
        public FullLineLabel()
        {
            super();
        }
    }

}
