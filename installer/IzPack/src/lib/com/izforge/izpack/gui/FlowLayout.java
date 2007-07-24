/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Elmar Grom
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/*---------------------------------------------------------------------------*/
/**
 * A flow layout arranges components in a left-to-right flow, much like lines of text in a
 * paragraph. Flow layouts are typically used to arrange buttons in a panel. It will arrange buttons
 * left to right until no more buttons fit on the same line. Each line is centered.
 * <p>
 * For example, the following picture shows an applet using the flow layout manager (its default
 * layout manager) to position three buttons:
 * <p>
 * <img src="doc-files/FlowLayout-1.gif" ALT="Graphic of Layout for Three Buttons" ALIGN=center
 * HSPACE=10 VSPACE=7>
 * <p>
 * Here is the code for this applet:
 * <p>
 * <hr>
 * <blockquote>
 * 
 * <pre>
 * import java.awt.*;
 * import java.applet.Applet;
 * 
 * public class myButtons extends Applet
 * {
 * 
 *     Button button1, button2, button3;
 * 
 *     public void init()
 *     {
 *         button1 = new Button(&quot;Ok&quot;);
 *         button2 = new Button(&quot;Open&quot;);
 *         button3 = new Button(&quot;Close&quot;);
 *         add(button1);
 *         add(button2);
 *         add(button3);
 *     }
 * }
 * 
 * </pre>
 * 
 * </blockquote>
 * <hr>
 * <p>
 * A flow layout lets each component assume its natural (preferred) size.
 * 
 * This class is a bit different from java.awt.FlowLayout. <blockquote> java.awt.FlowLayout has a
 * minor problem that was bugging me when I wrote the UserInputPanel. FlowLayout puts some amount of
 * space in between each component that it lays out. In addition it adds that same amount of space
 * to the left and to the right of the entire group. Therefore items such as the RuleInputfield that
 * are laid out with a FlowLayout would never line up properly with the other components (it would
 * appear to be slightly indented). Because there is no way to circumvent this behavior in
 * FlowLayout (it's hard coded) I copied the source and modified it so that it does not add the
 * space to the left and to the right. Now my stuff lines up properly. (Elmar Grom)</blockquote>
 * 
 * @version 1.39, 11/29/02
 * @author Arthur van Hoff
 * @author Sami Shaio
 * @author Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class FlowLayout implements LayoutManager
{

    /**
     * This value indicates that each row of components should be left-justified.
     */
    public static final int LEFT = 0;

    /**
     * This value indicates that each row of components should be centered.
     */
    public static final int CENTER = 1;

    /**
     * This value indicates that each row of components should be right-justified.
     */
    public static final int RIGHT = 2;

    /**
     * This value indicates that each row of components should be justified to the leading edge of
     * the container's orientation, for example, to the left in left-to-right orientations.
     * 
     * @see java.awt.Component#getComponentOrientation
     * @see java.awt.ComponentOrientation
     * @since 1.2 Package-private pending API change approval
     */
    public static final int LEADING = 3;

    /**
     * This value indicates that each row of components should be justified to the leading edge of
     * the container's orientation, for example, to the right in left-to-right orientations.
     * 
     * @see java.awt.Component#getComponentOrientation
     * @see java.awt.ComponentOrientation
     * @since 1.2 Package-private pending API change approval
     */
    public static final int TRAILING = 4;

    /**
     * <code>align</code> is the proprty that determines how each row distributes empty space. It
     * can be one of the following three values : <code>LEFT</code>
     * <code>RIGHT</code>
     * <code>CENTER</code>
     * 
     * @serial
     * @see #getAlignment
     * @see #setAlignment
     */
    int align; // This is for 1.1 serialization compatibilitys

    /**
     * <code>newAlign</code> is the property that determines how each row distributes empty space
     * for the Java 2 platform, v1.2 and greater. It can be one of the following three values :
     * <code>LEFT</code>
     * <code>RIGHT</code>
     * <code>CENTER</code>
     * 
     * @serial
     * @since 1.2
     * @see #getAlignment
     * @see #setAlignment
     */
    int newAlign; // This is the one we actually use

    /**
     * The flow layout manager allows a seperation of components with gaps. The horizontal gap will
     * specify the space between components.
     * 
     * @serial
     * @see #getHgap
     * @see #setHgap
     */
    int hgap;

    /**
     * The flow layout manager allows a seperation of components with gaps. The vertical gap will
     * specify the space between rows.
     * 
     * @serial
     * @see #getVgap
     * @see #setVgap
     */
    int vgap;

    /*--------------------------------------------------------------------------*/
    /**
     * Constructs a new Flow Layout with a centered alignment and a default 5-unit horizontal and
     * vertical gap.
     */
    /*--------------------------------------------------------------------------*/
    public FlowLayout()
    {
        this(CENTER, 5, 5);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Constructs a new Flow Layout with the specified alignment and a default 5-unit horizontal and
     * vertical gap. The value of the alignment argument must be one of <code>FlowLayout.LEFT</code>,
     * <code>FlowLayout.RIGHT</code>, or <code>FlowLayout.CENTER</code>.
     * 
     * @param align the alignment value
     */
    /*--------------------------------------------------------------------------*/
    public FlowLayout(int align)
    {
        this(align, 5, 5);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Creates a new flow layout manager with the indicated alignment and the indicated horizontal
     * and vertical gaps.
     * <p>
     * The value of the alignment argument must be one of <code>FlowLayout.LEFT</code>,
     * <code>FlowLayout.RIGHT</code>, or <code>FlowLayout.CENTER</code>.
     * 
     * @param align the alignment value.
     * @param hgap the horizontal gap between components.
     * @param vgap the vertical gap between components.
     */
    /*--------------------------------------------------------------------------*/
    public FlowLayout(int align, int hgap, int vgap)
    {
        this.hgap = hgap;
        this.vgap = vgap;
        setAlignment(align);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Gets the alignment for this layout. Possible values are <code>FlowLayout.LEFT</code>,
     * <code>FlowLayout.RIGHT</code>, or <code>FlowLayout.CENTER</code>.
     * 
     * @return the alignment value for this layout.
     * 
     * @see java.awt.FlowLayout#setAlignment
     */
    /*--------------------------------------------------------------------------*/
    public int getAlignment()
    {
        return (newAlign);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets the alignment for this layout. Possible values are <code>FlowLayout.LEFT</code>,
     * <code>FlowLayout.RIGHT</code>, and <code>FlowLayout.CENTER</code>.
     * 
     * @param align the alignment value.
     * 
     * @see #getAlignment()
     */
    /*--------------------------------------------------------------------------*/
    public void setAlignment(int align)
    {
        this.newAlign = align;

        // this.align is used only for serialization compatibility,
        // so set it to a value compatible with the 1.1 version
        // of the class

        switch (align)
        {
        case LEADING:
            this.align = LEFT;
            break;
        case TRAILING:
            this.align = RIGHT;
            break;
        default:
            this.align = align;
            break;
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Gets the horizontal gap between components.
     * 
     * @return the horizontal gap between components.
     * 
     * @see #setHgap(int)
     */
    /*--------------------------------------------------------------------------*/
    public int getHgap()
    {
        return (hgap);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets the horizontal gap between components.
     * 
     * @param hgap the horizontal gap between components
     * 
     * @see #getHgap()
     */
    /*--------------------------------------------------------------------------*/
    public void setHgap(int hgap)
    {
        this.hgap = hgap;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Gets the vertical gap between components.
     * 
     * @return the vertical gap between components.\
     * 
     * @see #setVgap(int)
     */
    /*--------------------------------------------------------------------------*/
    public int getVgap()
    {
        return (vgap);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets the vertical gap between components.
     * 
     * @param vgap the vertical gap between components
     * 
     * @see #getVgap()
     */
    /*--------------------------------------------------------------------------*/
    public void setVgap(int vgap)
    {
        this.vgap = vgap;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds the specified component to the layout. Not used by this class.
     * 
     * @param name the name of the component
     * @param comp the component to be added
     */
    /*--------------------------------------------------------------------------*/
    public void addLayoutComponent(String name, Component comp)
    {
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Removes the specified component from the layout. Not used by this class.
     * 
     * @param comp the component to remove
     * 
     */
    /*--------------------------------------------------------------------------*/
    public void removeLayoutComponent(Component comp)
    {
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the preferred dimensions for this layout given the components in the specified target
     * container.
     * 
     * @param target the component which needs to be laid out
     * 
     * @return the preferred dimensions to lay out the subcomponents of the specified container.
     * @see #minimumLayoutSize(Container)
     */
    /*--------------------------------------------------------------------------*/
    public Dimension preferredLayoutSize(Container target)
    {
        synchronized (target.getTreeLock())
        {
            Dimension dim = new Dimension(0, 0);
            int nmembers = target.getComponentCount();
            boolean firstVisibleComponent = true;

            for (int i = 0; i < nmembers; i++)
            {
                Component m = target.getComponent(i);
                if (m.isVisible())
                {
                    Dimension d = m.getPreferredSize();
                    dim.height = Math.max(dim.height, d.height);
                    if (firstVisibleComponent)
                    {
                        firstVisibleComponent = false;
                    }
                    else
                    {
                        dim.width += hgap;
                    }
                    dim.width += d.width;
                }
            }

            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right + hgap * 2;
            dim.height += insets.top + insets.bottom + vgap * 2;

            return (dim);
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the minimum dimensions needed to layout the components contained in the specified
     * target container.
     * 
     * @param target the component which needs to be laid out
     * 
     * @return the minimum dimensions to lay out the subcomponents of the specified container.
     * 
     * @see #preferredLayoutSize(Container)
     */
    /*--------------------------------------------------------------------------*/
    public Dimension minimumLayoutSize(Container target)
    {
        synchronized (target.getTreeLock())
        {
            Dimension dim = new Dimension(0, 0);
            int nmembers = target.getComponentCount();

            for (int i = 0; i < nmembers; i++)
            {
                Component m = target.getComponent(i);
                if (m.isVisible())
                {
                    Dimension d = m.getMinimumSize();
                    dim.height = Math.max(dim.height, d.height);
                    if (i > 0)
                    {
                        dim.width += hgap;
                    }
                    dim.width += d.width;
                }
            }

            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right + hgap * 2;
            dim.height += insets.top + insets.bottom + vgap * 2;

            return (dim);
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Centers the elements in the specified row, if there is any slack.
     * 
     * @param target the component which needs to be moved
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the width dimensions
     * @param height the height dimensions
     * @param rowStart the beginning of the row
     * @param rowEnd the the ending of the row
     */
    /*--------------------------------------------------------------------------*/
    private void moveComponents(Container target, int x, int y, int width, int height,
            int rowStart, int rowEnd, boolean ltr)
    {
        synchronized (target.getTreeLock())
        {
            switch (newAlign)
            {
            case LEFT:
                x += ltr ? 0 : width;
                break;
            case CENTER:
                x += width / 2;
                break;
            case RIGHT:
                x += ltr ? width : 0;
                break;
            case LEADING:
                break;
            case TRAILING:
                x += width;
                break;
            }

            for (int i = rowStart; i < rowEnd; i++)
            {
                Component m = target.getComponent(i);

                if (m.isVisible())
                {
                    if (ltr)
                    {
                        m.setLocation(x, y + (height - m.getSize().height) / 2);
                    }
                    else
                    {
                        m.setLocation(target.getSize().width - x - m.getSize().width, y
                                + (height - m.getSize().height) / 2);
                    }

                    x += m.getSize().width + hgap;
                }
            }
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Lays out the container. This method lets each component take its preferred size by reshaping
     * the components in the target container in order to satisfy the constraints of this
     * <code>FlowLayout</code> object.
     * 
     * @param target the specified component being laid out.
     * 
     */
    /*--------------------------------------------------------------------------*/
    public void layoutContainer(Container target)
    {
        synchronized (target.getTreeLock())
        {
            Insets insets = target.getInsets();
            int maxWidth = target.getSize().width - (insets.left + insets.right + hgap * 2);
            int nMembers = target.getComponentCount();
            int x = 0;
            int y = insets.top + vgap;
            int rowh = 0;
            int start = 0;

            boolean ltr = target.getComponentOrientation().isLeftToRight();

            for (int i = 0; i < nMembers; i++)
            {
                Component m = target.getComponent(i);

                if (m.isVisible())
                {
                    Dimension d = m.getPreferredSize();
                    m.setSize(d.width, d.height);

                    if ((x == 0) || ((x + d.width) <= maxWidth))
                    {
                        if (x > 0)
                        {
                            x += hgap;
                        }
                        x += d.width;
                        rowh = Math.max(rowh, d.height);
                    }
                    else
                    {
                        moveComponents(target, insets.left, y, maxWidth - x, rowh, start, i, ltr);
                        x = d.width;
                        y += vgap + rowh;
                        rowh = d.height;
                        start = i;
                    }
                }
            }

            moveComponents(target, insets.left, y, maxWidth - x, rowh, start, nMembers, ltr);
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns a string representation of this <code>FlowLayout</code> object and its values.
     * 
     * @return a string representation of this layout.
     */
    /*--------------------------------------------------------------------------*/
    public String toString()
    {
        String str = "";

        switch (align)
        {
        case LEFT:
            str = ",align=left";
            break;
        case CENTER:
            str = ",align=center";
            break;
        case RIGHT:
            str = ",align=right";
            break;
        case LEADING:
            str = ",align=leading";
            break;
        case TRAILING:
            str = ",align=trailing";
            break;
        }

        return (getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + str + "]");
    }
}
/*---------------------------------------------------------------------------*/
