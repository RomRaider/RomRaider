/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 1997,2002 Elmar Grom
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

package com.izforge.izpack.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Vector;

import javax.swing.JComponent;

/*---------------------------------------------------------------------------*/
/**
 * <BR>
 * <code>MultiLineLabel</code> may be used in place of javax.swing.JLabel. <BR>
 * <BR>
 * This class implements a component that is capable of displaying multiple lines of text. Line
 * breaks are inserted automatically whenever a line of text extends beyond the predefined maximum
 * line length. Line breaks will only be inserted between words, except where a single word is
 * longer than the maximum line length. Line breaks may be forced at any location in the text by
 * inserting a newline (\n). White space that is not valuable (i.e. is placed at the beginning of a
 * new line or at the very beginning or end of the text) is removed. <br>
 * <br>
 * <b>Note:</b> you can set the maximum width of the label either through one of the constructors
 * or you can call <code>setMaxWidth()</code> explicitly. If this is not set,
 * <code>MultiLineLabel</code> will derive its width from the parent component.
 * 
 * @version 0.0.1 / 05-15-97
 * @version 1.0 / 04-13-02
 * @author Elmar Grom
 */
/*---------------------------------------------------------------------------*
 * Reviving some old code here that was written before there was swing.
 * The original was written to work with awt. I had to do some masaging to
 * make it a JComponent and I hope it behaves like a reasonably good mannered
 * swing component.
 *---------------------------------------------------------------------------*/
public class MultiLineLabel extends JComponent
{

    /**
     * 
     */
    private static final long serialVersionUID = 4051045255031894837L;

    public static final int LEFT = 0; // alignment constants

    public static final int CENTER = 1;

    public static final int RIGHT = 2;

    public static final int DEFAULT_MARGIN = 10;

    public static final int DEFAULT_ALIGN = LEFT;

    public static final int LEAST_ALLOWED = 200; // default setting for

    // maxAllowed

    private static final int FOUND = 0; // constants for string search.

    private static final int NOT_FOUND = 1;

    private static final int NOT_DONE = 0;

    private static final int DONE = 1;

    private static final char[] WHITE_SPACE = { ' ', '\n', '\t'};

    private static final char[] SPACES = { ' ', '\t'};

    private static final char NEW_LINE = '\n';

    protected Vector line = new Vector();// text lines to display

    protected String labelText; // text lines to display

    protected int numLines; // the number of lines

    protected int marginHeight; // top and bottom margins

    protected int marginWidth; // left and right margins

    protected int lineHeight; // total height of the font

    protected int lineAscent; // font height above the baseline

    protected int lineDescent; // font hight below the baseline

    protected int[] lineWidth; // width of each line

    protected int maxWidth; // width of the widest line

    private int maxAllowed = LEAST_ALLOWED; // max width allowed to use

    private boolean maxAllowedSet = false; // signals if the max allowed width

    // has been explicitly set

    protected int alignment = LEFT; // default text alignment

    /*-------------------------------------------------------------------*/
    /**
     * Constructor
     * 
     * @param text the text to be displayed
     * @param horMargin the horizontal margin for the label
     * @param vertMargin the vertical margin for the label
     * @param maxWidth the maximum allowed width of the text
     * @param justify the text alignment for the label
     */
    /*-------------------------------------------------------------------*
     * <detailed description / implementation details if applicable>
     *-------------------------------------------------------------------*/
    public MultiLineLabel(String text, int horMargin, int vertMargin, int maxWidth, int justify)
    {
        this.labelText = text;
        this.marginWidth = horMargin;
        this.marginHeight = vertMargin;
        this.maxAllowed = maxWidth;
        this.maxAllowedSet = true;
        this.alignment = justify;
    }

    /*-------------------------------------------------------------------*/
    /**
     * Constructor using default max-width and alignment.
     * 
     * @param label the text to be displayed
     * @param marginWidth the horizontal margin for the label
     * @param marginHeight the vertical margin for the label
     */
    /*-------------------------------------------------------------------*
     * <detailed description / implementation details if applicable>
     *-------------------------------------------------------------------*/
    public MultiLineLabel(String label, int marginWidth, int marginHeight)
    {
        this.labelText = label;
        this.marginWidth = marginWidth;
        this.marginHeight = marginHeight;
    }

    /*-------------------------------------------------------------------*/
    /**
     * Constructor using default max-width, and margin.
     * 
     * @param label the text to be displayed
     * @param alignment the text alignment for the label
     */
    /*-------------------------------------------------------------------*
     * <detailed description / implementation details if applicable>
     *-------------------------------------------------------------------*/
    public MultiLineLabel(String label, int alignment)
    {
        this.labelText = label;
        this.alignment = alignment;
    }

    /*-------------------------------------------------------------------*/
    /**
     * Constructor using default max-width, alignment, and margin.
     * 
     * @param label the text to be displayed
     */
    /*-------------------------------------------------------------------*
     * <detailed description / implementation details if applicable>
     *-------------------------------------------------------------------*/
    public MultiLineLabel(String label)
    {
        this.labelText = label;
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method searches the target string for occurences of any of the characters in the source
     * string. The return value is the position of the first hit. Based on the mode parameter the
     * hit position is either the position where any of the source characters first was found or the
     * first position where none of the source characters where found.
     * 
     * 
     * @return position of the first occurence
     * @param target the text to be searched
     * @param start the start position for the search
     * @param source the list of characters to be searched for
     * @param mode the search mode FOUND = reports first found NOT_FOUND = reports first not found
     */
    /*-------------------------------------------------------------------*
     * <detailed description / implementation details if applicable>
     *-------------------------------------------------------------------*/
    int getPosition(String target, int start, char[] source, int mode)
    {
        int status;
        int position;
        int scan;
        int targetEnd;
        int sourceLength;
        char temp;

        targetEnd = (target.length() - 1);
        sourceLength = source.length;
        position = start;

        if (mode == FOUND)
        {
            status = NOT_DONE;
            while (status != DONE)
            {
                position++;
                if (!(position < targetEnd)) // end of string reached, the
                // next
                { // statement would cause a runtime error
                    return (targetEnd);
                }
                temp = target.charAt(position);
                for (scan = 0; scan < sourceLength; scan++) // walk through the
                // source
                { // string and compare each char
                    if (source[scan] == temp)
                    {
                        status = DONE;
                    }
                }
            }
            return (position);
        }
        else if (mode == NOT_FOUND)
        {
            status = NOT_DONE;
            while (status != DONE)
            {
                position++;
                if (!(position < targetEnd)) // end of string reached, the
                // next
                { // statement would cause a runtime error
                    return (targetEnd);
                }
                temp = target.charAt(position);
                status = DONE;
                for (scan = 0; scan < sourceLength; scan++) // walk through the
                // source
                { // string and compare each char
                    if (source[scan] == temp)
                    {
                        status = NOT_DONE;
                    }
                }
            }
            return (position);
        }
        return (0);
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method scans the input string until the max allowed width is reached. The return value
     * indicates the position just before this happens.
     * 
     * 
     * @return position character position just before the string is too long
     * @param word word to break
     */
    /*-------------------------------------------------------------------*
     * <detailed description / implementation details if applicable>
     *-------------------------------------------------------------------*/
    int breakWord(String word, FontMetrics fm)
    {
        int width;
        int currentPos;
        int endPos;

        width = 0;
        currentPos = 0;
        endPos = word.length() - 1;

        // make sure we don't end up with a negative position
        if (endPos <= 0) { return (currentPos); }
        // seek the position where the word first is longer than allowed
        while ((width < maxAllowed) && (currentPos < endPos))
        {
            currentPos++;
            width = fm.stringWidth(labelText.substring(0, currentPos));
        }
        // adjust to get the chatacter just before (this should make it a bit
        // shorter than allowed!)
        if (currentPos != endPos)
        {
            currentPos--;
        }
        return (currentPos);
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method breaks the label text up into multiple lines of text. Line breaks are established
     * based on the maximum available space. A new line is started whenever a line break is
     * encountered, even if the permissible length is not yet reached. Words are broken only if a
     * single word happens to be longer than one line.
     */
    /*-------------------------------------------------------------------*/
    private void divideLabel()
    {
        int width;
        int startPos;
        int currentPos;
        int lastPos;
        int endPos;

        line.clear();
        FontMetrics fm = this.getFontMetrics(this.getFont());

        startPos = 0;
        currentPos = startPos;
        lastPos = currentPos;
        endPos = (labelText.length() - 1);

        while (currentPos < endPos)
        {
            width = 0;
            // ----------------------------------------------------------------
            // find the first substring that occupies more than the granted
            // space.
            // Break at the end of the string or a line break
            // ----------------------------------------------------------------
            while ((width < maxAllowed) && (currentPos < endPos)
                    && (labelText.charAt(currentPos) != NEW_LINE))
            {
                lastPos = currentPos;
                currentPos = getPosition(labelText, currentPos, WHITE_SPACE, FOUND);
                width = fm.stringWidth(labelText.substring(startPos, currentPos));
            }
            // ----------------------------------------------------------------
            // if we have a line break we want to copy everything up to
            // currentPos
            // ----------------------------------------------------------------
            if (labelText.charAt(currentPos) == NEW_LINE)
            {
                lastPos = currentPos;
            }
            // ----------------------------------------------------------------
            // if we are at the end of the string we want to copy everything up
            // to
            // the last character. Since there seems to be a problem to get the
            // last
            // character if the substring definition ends at the very last
            // character
            // we have to call a different substring function than normal.
            // ----------------------------------------------------------------
            if (currentPos == endPos && width <= maxAllowed)
            {
                lastPos = currentPos;
                String s = labelText.substring(startPos);
                line.addElement(s);
            }
            // ----------------------------------------------------------------
            // in all other cases copy the substring that we have found to fit
            // and
            // add it as a new line of text to the line vector.
            // ----------------------------------------------------------------
            else
            {
                // ------------------------------------------------------------
                // make sure it's not a single word. If so we must break it at
                // the
                // proper location.
                // ------------------------------------------------------------
                if (lastPos == startPos)
                {
                    lastPos = startPos + breakWord(labelText.substring(startPos, currentPos), fm);
                }
                String s = labelText.substring(startPos, lastPos);
                line.addElement(s);
            }

            // ----------------------------------------------------------------
            // seek for the end of the white space to cut out any unnecessary
            // spaces
            // and tabs and set the new start condition.
            // ----------------------------------------------------------------
            startPos = getPosition(labelText, lastPos, SPACES, NOT_FOUND);
            currentPos = startPos;
        }

        numLines = line.size();
        lineWidth = new int[numLines];
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method finds the font size, each line width and the widest line.
     * 
     */
    /*-------------------------------------------------------------------*/
    protected void measure()
    {
        if (!maxAllowedSet)
        {
            maxAllowed = getParent().getSize().width;
        }

        // return if width is too small
        if (maxAllowed < (20)) { return; }

        FontMetrics fm = this.getFontMetrics(this.getFont());

        // return if no font metrics available
        if (fm == null) { return; }

        divideLabel();

        this.lineHeight = fm.getHeight();
        this.lineDescent = fm.getDescent();
        this.maxWidth = 0;

        for (int i = 0; i < numLines; i++)
        {
            this.lineWidth[i] = fm.stringWidth((String) this.line.elementAt(i));
            if (this.lineWidth[i] > this.maxWidth)
            {
                this.maxWidth = this.lineWidth[i];
            }
        }
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method draws the label.
     * 
     * @param graphics the device context
     */
    /*-------------------------------------------------------------------*/
    public void paint(Graphics graphics)
    {
        int x;
        int y;

        measure();
        Dimension d = this.getSize();

        y = lineAscent + (d.height - (numLines * lineHeight)) / 2;

        for (int i = 0; i < numLines; i++)
        {
            y += lineHeight;
            switch (alignment)
            {
            case LEFT:
                x = marginWidth;
                break;
            case CENTER:
                x = (d.width - lineWidth[i]) / 2;
                break;
            case RIGHT:
                x = d.width - marginWidth - lineWidth[i];
                break;
            default:
                x = (d.width - lineWidth[i]) / 2;
            }
            graphics.drawString((String) line.elementAt(i), x, y);
        }
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method may be used to set the label text
     * 
     * @param labelText the text to be displayed
     */
    /*-------------------------------------------------------------------*/
    public void setText(String labelText)
    {
        this.labelText = labelText;
        repaint();
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method may be used to set the font that should be used to draw the label
     * 
     * @param font font to be used within the label
     */
    /*-------------------------------------------------------------------*/
    public void setFont(Font font)
    {
        super.setFont(font);
        repaint();
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method may be used to set the color in which the text should be drawn
     * 
     * @param color the text color
     */
    /*-------------------------------------------------------------------*/
    public void setColor(Color color)
    {
        super.setForeground(color);
        repaint();
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method may be used to set the text alignment for the label
     * 
     * @param alignment the alignment, possible values are LEFT, CENTER, RIGHT
     */
    /*-------------------------------------------------------------------*/
    public void setJustify(int alignment)
    {
        this.alignment = alignment;
        repaint();
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method may be used to set the max allowed line width
     * 
     * @param width the max allowed line width in pixels
     */
    /*-------------------------------------------------------------------*/
    public void setMaxWidth(int width)
    {
        this.maxAllowed = width;
        this.maxAllowedSet = true;
        repaint();
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method may be used to set the horizontal margin
     * 
     * @param margin the margin to the left and to the right of the label
     */
    /*-------------------------------------------------------------------*/
    public void setMarginWidth(int margin)
    {
        this.marginWidth = margin;
        repaint();
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method may be used to set the vertical margin for the label
     * 
     * @param margin the margin on the top and bottom of the label
     */
    /*-------------------------------------------------------------------*/
    public void setMarginHeight(int margin)
    {
        this.marginHeight = margin;
        repaint();
    }

    /*-------------------------------------------------------------------*/
    /**
     * Moves and resizes this component. The new location of the top-left corner is specified by
     * <code>x</code> and <code>y</code>, and the new size is specified by <code>width</code>
     * and <code>height</code>.
     * 
     * @param x The new x-coordinate of this component.
     * @param y The new y-coordinate of this component.
     * @param width The new width of this component.
     * @param height The new height of this component.
     */
    /*-------------------------------------------------------------------*/
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);
        this.maxAllowed = width;
        this.maxAllowedSet = true;
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method may be used to retrieve the text alignment for the label
     * 
     * @return alignment the text alignment currently in use for the label
     */
    /*-------------------------------------------------------------------*/
    public int getAlignment()
    {
        return (this.alignment);
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method may be used to retrieve the horizontal margin for the label
     * 
     * @return marginWidth the margin currently in use to the left and right of the label
     */
    /*-------------------------------------------------------------------*/
    public int getMarginWidth()
    {
        return (this.marginWidth);
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method may be used to retrieve the vertical margin for the label
     * 
     * @return marginHeight the margin currently in use on the top and bottom of the label
     */
    /*-------------------------------------------------------------------*/
    public int getMarginHeight()
    {
        return (this.marginHeight);
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method is typically used by the layout manager, it reports the necessary space to
     * display the label comfortably.
     */
    /*-------------------------------------------------------------------*/
    public Dimension getPreferredSize()
    {
        measure();
        return (new Dimension(maxAllowed, (numLines * (lineHeight + lineAscent + lineDescent))
                + (2 * marginHeight)));
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method is typically used by the layout manager, it reports the absolute minimum space
     * required to display the entire label.
     * 
     */
    /*-------------------------------------------------------------------*/
    public Dimension getMinimumSize()
    {
        measure();
        return (new Dimension(maxAllowed, (numLines * (lineHeight + lineAscent + lineDescent))
                + (2 * marginHeight)));
    }

    /*-------------------------------------------------------------------*/
    /**
     * This method is called by the system after this object is first created.
     * 
     */
    /*-------------------------------------------------------------------*/
    public void addNotify()
    {
        super.addNotify(); // invoke the superclass
    }
}
/*---------------------------------------------------------------------------*/
