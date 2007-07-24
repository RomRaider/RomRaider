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

import java.awt.*;
import java.util.Vector;

/**
 * This class implements a layout manager that generally lays out components in two columns. <BR>
 * <BR>
 * The design goal for this layout manager was to lay out forms for data entry, where there are
 * several rows of entry fields with associated labels. The goal was to have the beginning off all
 * labels line up, as well as the left edge of all the data entry fields. This leads to a situation
 * where all components are essentially laid out in two columns. The columns adjust to accommodate
 * components of various sizes. This means that components that are added are laid out top to
 * bottom, either in the left column, in the right column or straddling both columns. In addition to
 * this general behavior, the following additional layout capabilities are supported:<br>
 * <ul>
 * <li>Resizable margins are provided on the left and right side.
 * <li>A special region is provided at the top that is only affected by the side margins but not by
 * any other layout behavior.
 * <li>It is possible to specify the vertical positioning of the cluster of laid out components for
 * the case that they do not occupy the entire available real estate.
 * <li>Individual components can be indented.
 * </ul>
 * 
 * <b>The Layout Behavior</b> <br>
 * <br>
 * The left and right margin are absolute. This means that they can not be penetrated by components.
 * All layout happens between the limits established by these margins. The purpose of these margins
 * is to ensure that components are not laid out all the way to the edge of their container, without
 * the need to set matching borders for each component. <br>
 * <br>
 * The title margin at the top factors only into the layout behavior if there is a component set to
 * be laid out in that region, otherwise it is ignored. <br>
 * <br>
 * The vertical space available to each row of components depends on the space requirements of the
 * tallest component in that row. Both components are placed vertically centered in their row. <br>
 * <br>
 * All horizontal layout is based on the position of three vertical rules, the left rule, the right
 * rule and the center rule. <br>
 * <br>
 * <img src="doc-files/TwoColumnLayout.gif"/> <br>
 * <br>
 * The actual position of each rule depends on the alignment strategy, margin settings and component
 * sizes. Regardless of these factors, components placed in the left column are <i>always</i>
 * positioned with their left edge aligned with the left rule. Components placed in the right column
 * are <i>always</i> positioned with their left edge aligned with the center rule. If a component
 * straddles both columns, it is <i>always</i> positioned with the left edge aligned with the left
 * rule, but is allowed to extend all the way to the right rule. The only exception is a component
 * that is specified with an indent. In this case the component is moved to the right of the
 * respective rule by the indent amount. <br>
 * <br>
 * The location of the rules is determined based on the alignment strategy as follows:<br>
 * <ul>
 * <li>The right rule is always located at the edge of the right margin.
 * <li><b>Left Alignment:</b> The left rule is located the edge of the left margin. The center
 * rule is located far enough to the right to clear the widest component in the left column.
 * <li><b>Center Alignment:</b> The center rule is located at the center of the panel. The left
 * rule is located far enough to the left to make the widest component in the left column fit.
 * <li><b>Right Alignment</b> The center rule is located far enough to the left of the right rule
 * to make the widest component in the right column fit. The left rule is located far enough to the
 * left to make the widest component in the left column fit.
 * </ul>
 * All components clump together vertically and are positioned right beneath the title margin. This
 * is of course not a very appealing presentation. By setting how the remaining vertical space is
 * distributed above and below the cluster of components the cluster can be positioned more
 * favorably (see the shaded area in the illustration).
 * 
 * @see com.izforge.izpack.gui.TwoColumnConstraints
 * 
 * @version 0.0.1 / 11/14/02
 * @author Elmar Grom
 */
public class TwoColumnLayout implements LayoutManager2
{

    public static final int LEFT = 0;

    public static final int RIGHT = 1;

    public static final int CENTER = 2;

    /** holds all the components and layout constraints. */
    private Vector[] components = { new Vector(), new Vector()};

    /**
     * holds the component to be placed in the title region, including layout constraints.
     */
    private TwoColumnConstraints title = null;

    /** the margin setting in % of the container's width */
    private int margin = 0;

    /**
     * the setting for the buffer area on top of hte comonent cluster in % of the left over height.
     */
    private int topBuffer = 0;

    /** the indent setting in % of the conteiner's width */
    private int indent = 0;

    /** the gap between the two columns */
    private int gap = 5;

    private int alignment = LEFT;

    private int leftRule;

    private int rightRule;

    private int centerRule;

    private int titleHeight;

    /**
     * Constructs a <code>TwoColumnLayout</code> layout manager. To add components use the
     * container's <code>add(comp, constraints)</code> method with a TwoColumnConstraints object.
     * 
     * @param margin the margin width to use on the left and right side in % of the total container
     * width. Values less than 0% and greater than 50% are not accepted.
     * @param gap the gap between the two columns.
     * @param indent the indent to use for components that have that constraint set. This is a value
     * in pixels.
     * @param topBuffer the percentage of left over vertical space to place on top of the component
     * cluster. Values between 0% and 100% are accepted.
     * @param alignment how to align the overall layout. Legal values are LEFT, CENTER, RIGHT.
     */
    public TwoColumnLayout(int margin, int gap, int indent, int topBuffer, int alignment)
    {
        this.indent = indent;
        this.gap = gap;

        if ((margin >= 0) && (margin <= 50))
        {
            this.margin = margin;
        }
        if ((topBuffer >= 0) && (topBuffer <= 100))
        {
            this.topBuffer = topBuffer;
        }
        if ((alignment == LEFT) || (alignment == CENTER) || (alignment == RIGHT))
        {
            this.alignment = alignment;
        }
    }

    /**
     * Sets the constraints for the specified component in this layout. <code>null</code> is a
     * legal value for a component, but not for a constraints object.
     * 
     * @param comp the component to be modified.
     * @param constraints the constraints to be applied.
     */
    public void addLayoutComponent(Component comp, Object constraints)
    {
        if (constraints == null) { return; }

        TwoColumnConstraints component = null;
        try
        {
            component = (TwoColumnConstraints) constraints;
            component = (TwoColumnConstraints) component.clone();
        }
        catch (Throwable exception)
        {
            return;
        }

        component.component = comp;

        // ----------------------------------------------------
        // the title component is recorded in a separate
        // variable, displacing any component that might have
        // been previously recorded for that location.
        // ----------------------------------------------------
        if (component.position == TwoColumnConstraints.NORTH)
        {
            title = component;
            if (title.stretch)
            {
                title.align = LEFT;
            }
        }

        // ----------------------------------------------------
        // components that straddle both columns are a bit
        // tricky because these components are recorded in the
        // left column and the same row cannot contain a
        // component in the right column.
        //
        // If there are fewer components in the left column
        // than in the right one, a null is inserted at this
        // place in the right column. This allows the component
        // to use both columns. The component that previously
        // occupied this position and any that were placed
        // below will be pushed down by one row due to this
        // action.
        //
        // If there are the same number of components in both
        // columns or if there are fewer in the right column
        // then the component is added to the left column and
        // then the right column filled with null until both
        // contain the same number of components. this means
        // that any components that will now be placed in the
        // right column are positioned beneath this component.
        // Unoccupied spots higher in the right column become
        // inaccessible.
        // ----------------------------------------------------
        else if (component.position == TwoColumnConstraints.BOTH)
        {
            // first make sure that both columns have the same number of entries
            while (components[RIGHT].size() > components[LEFT].size())
            {
                components[LEFT].add(null);
            }

            while (components[LEFT].size() > components[RIGHT].size())
            {
                components[RIGHT].add(null);
            }

            components[LEFT].add(component);
            components[RIGHT].add(null);
        }

        // ----------------------------------------------------
        // WEST components are added to the left column
        // ----------------------------------------------------
        else if (component.position == TwoColumnConstraints.WEST)
        {
            components[LEFT].add(component);
        }

        // ----------------------------------------------------
        // WESTONLY components are added to the left column
        // the right column has to be kept free
        // ----------------------------------------------------
        else if (component.position == TwoColumnConstraints.WESTONLY)
        {
            components[LEFT].add(component);

            // fill right column to make sure nothing is placed there
            while (components[RIGHT].size() < components[LEFT].size())
            {
                components[RIGHT].add(null);
            }

        }

        // ----------------------------------------------------
        // EAST components are added to the right column
        // ----------------------------------------------------
        else if (component.position == TwoColumnConstraints.EAST)
        {
            components[RIGHT].add(component);
        }

        // ----------------------------------------------------
        // EASTONLY components are added to the left column
        // the right column has to be kept free
        // ----------------------------------------------------
        else if (component.position == TwoColumnConstraints.EASTONLY)
        {
            components[RIGHT].add(component);

            // fill left column to make sure nothing is placed there
            while (components[LEFT].size() < components[RIGHT].size())
            {
                components[LEFT].add(null);
            }

        }

        // ----------------------------------------------------
        // If the position did not match any of the above
        // criteria then the component is not added and
        // consequently will not be laid out.
        // ----------------------------------------------------
    }

    /**
     * Lays out the container in the specified panel.
     * 
     * @param parent the component which needs to be laid out.
     */
    public void layoutContainer(Container parent)
    {
        positionRules(parent);
        positionTitle(parent);
        positionComponents(parent);
    }

    /**
     * Positions the three rules in preparation for layout. Sets the variables:<br>
     * <ul>
     * <li><code>leftRule</code>
     * <li><code>rightRule</code>
     * <li><code>centerRule</code>
     * </ul>
     * 
     * @param parent the component which needs to be laid out.
     */
    private void positionRules(Container parent)
    {
        int margin = margin(parent);

        if (alignment == LEFT)
        {
            leftRule = margin;
            centerRule = leftRule + minimumColumnWidth(LEFT, parent) + gap;
            rightRule = parent.getWidth() - margin;
        }

        else if (alignment == CENTER)
        {
            centerRule = (int) (parent.getMinimumSize().getWidth() / 2);
            leftRule = centerRule - minimumColumnWidth(LEFT, parent) - gap;
            rightRule = parent.getWidth() - margin;
        }

        else if (alignment == RIGHT)
        {
            rightRule = parent.getWidth() - margin;
            centerRule = rightRule - minimumColumnWidth(RIGHT, parent);
            leftRule = centerRule - minimumColumnWidth(LEFT, parent) - gap;
        }
    }

    /**
     * Positions the title component and sets the variable <code>titleHeight</code>. <b>Note:</b>
     * this method depends on the fact that the rules are set to their correct layout position.
     * 
     * @param parent the component which needs to be laid out.
     */
    private void positionTitle(Container parent)
    {
        if (title != null)
        {
            Component component = title.component;
            int width = (int) component.getMinimumSize().getWidth();
            titleHeight = (int) component.getMinimumSize().getHeight();

            if (component != null)
            {
                if (title.stretch)
                {
                    width = rightRule - leftRule;
                    component.setBounds(leftRule, 0, width, titleHeight);
                }

                else if (title.align == TwoColumnConstraints.LEFT)
                {
                    component.setBounds(leftRule, 0, width, titleHeight);
                }

                else if (title.align == TwoColumnConstraints.CENTER)
                {
                    int left = centerRule - (width / 2);
                    component.setBounds(left, 0, width, titleHeight);
                }

                else if (title.align == TwoColumnConstraints.RIGHT)
                {
                    int left = rightRule - width;
                    component.setBounds(left, 0, width, titleHeight);
                }
            }
        }
    }

    /**
     * Positions all components in the container.
     * 
     * @param parent the component which needs to be laid out.
     */
    private void positionComponents(Container parent)
    {
        int usedHeight = titleHeight + minimumClusterHeight();
        int topBuffer = topBuffer(usedHeight, parent);
        int leftHeight = 0;
        int rightHeight = 0;

        if (topBuffer < 0)
        {
            topBuffer = 0;
        }

        int y = titleHeight + topBuffer;

        for (int i = 0; i < rows(); i++)
        {
            leftHeight = height(i, LEFT);
            rightHeight = height(i, RIGHT);

            if (leftHeight > rightHeight)
            {
                int offset = (leftHeight - rightHeight) / 2;

                positionComponent(y, i, LEFT, parent);
                positionComponent((y + offset), i, RIGHT, parent);

                y += leftHeight;
            }
            else if (leftHeight < rightHeight)
            {
                int offset = (rightHeight - leftHeight) / 2;

                positionComponent((y + offset), i, LEFT, parent);
                positionComponent(y, i, RIGHT, parent);

                y += rightHeight;
            }
            else
            {
                positionComponent(y, i, LEFT, parent);
                positionComponent(y, i, RIGHT, parent);

                y += leftHeight;
            }
        }
    }

    /**
     * Positiones one component as instructed. Constraints for each component, such as
     * <code>stretch</code>, <code>BOTH</code> and <code>indent</code> are taken into
     * account. In addition, empty comonents are handled properly.
     * 
     * @param y the y location within the continer, where the component should be positioned.
     * @param row the row of the component
     * @param column the column of the component
     * @param parent the container which needs to be laid out.
     */
    private void positionComponent(int y, int row, int column, Container parent)
    {
        TwoColumnConstraints constraints = null;

        try
        {
            constraints = (TwoColumnConstraints) (components[column].elementAt(row));
        }
        catch (Throwable exception)
        {
            return;
        }

        int x = 0;

        if (constraints != null)
        {
            Component component = constraints.component;
            int width = (int) component.getPreferredSize().getWidth();
            int height = (int) component.getPreferredSize().getHeight();

            // --------------------------------------------------
            // set x to the appropriate rule. The only need to
            // modify this is for indent
            // --------------------------------------------------
            if (column == LEFT)
            {
                x = leftRule;
            }
            else
            {
                x = centerRule;
            }

            if (component != null)
            {
                // --------------------------------------------------
                // set the width for stretch based on BOTH, LEFT and
                // RIGHT positionsing
                // --------------------------------------------------
                if ((constraints.stretch) && (constraints.position == TwoColumnConstraints.BOTH))
                {
                    width = rightRule - leftRule;
                    x = leftRule;
                }
                else if ((constraints.stretch) && (column == LEFT))
                {
                    width = centerRule - leftRule;
                }
                else if ((constraints.stretch) && (column == RIGHT))
                {
                    width = rightRule - centerRule;
                }

                // --------------------------------------------------
                // if we straddle both columns but are not stretching
                // use the preferred width as long as it is less then
                // the width of both columns combined. Also set the x
                // position to left, just to be sure.
                // --------------------------------------------------
                else if (constraints.position == TwoColumnConstraints.BOTH)
                {
                    if (width > (rightRule - leftRule))
                    {
                        width = rightRule - leftRule;
                    }
                    x = leftRule;
                }

                // --------------------------------------------------
                // correct for indent if this option is set
                // --------------------------------------------------
                if (constraints.indent)
                {
                    width -= indent;
                    x += indent;
                }

                component.setBounds(x, y, width, height);
            }
        }
    }

    /**
     * Returns the minimum width of the column requested.
     * 
     * @param column the columns to measure (LEFT / RIGHT)
     * @param parent the component which needs to be laid out.
     * 
     * @return the minimum width required to fis the components in this column
     */
    private int minimumColumnWidth(int column, Container parent)
    {
        Component component = null;
        TwoColumnConstraints constraints = null;
        int width = 0;
        int temp = 0;

        for (int i = 0; i < components[column].size(); i++)
        {
            constraints = (TwoColumnConstraints) components[column].elementAt(i);

            if ((constraints != null) && (constraints.position != TwoColumnConstraints.BOTH))
            {
                component = constraints.component;
                temp = (int) component.getMinimumSize().getWidth();

                if (constraints.indent)
                {
                    temp += indent;
                }

                if (temp > width)
                {
                    width = temp;
                }
            }
        }

        return (width);
    }

    /**
     * Retrunds the minimum width both columns together should have based on the minimum widths of
     * all the components that straddle both columns and the minimum width of the title component.
     * 
     * @param parent the component which needs to be laid out.
     * 
     * @return the minimum width required to fis the components in this column
     */
    private int minimumBothColumnsWidth(Container parent)
    {
        Component component = null;
        TwoColumnConstraints constraints = null;
        int width = 0;
        int temp = 0;

        if (title != null)
        {
            component = title.component;
            width = (int) component.getMinimumSize().getWidth();
        }

        for (int i = 0; i < components[LEFT].size(); i++)
        {
            constraints = (TwoColumnConstraints) components[LEFT].elementAt(i);

            if ((constraints != null) && (constraints.position == TwoColumnConstraints.BOTH))
            {
                component = constraints.component;
                temp = (int) component.getMinimumSize().getWidth();

                if (constraints.indent)
                {
                    temp += indent;
                }

                if (temp > width)
                {
                    width = temp;
                }
            }
        }

        return (width);
    }

    private int minimumClusterHeight()
    {
        int height = 0;

        for (int i = 0; i < rows(); i++)
        {
            height += rowHeight(i);
        }

        return (height);
    }

    /**
     * Returns the number of rows that need to be laid out.
     */
    private int rows()
    {
        int rows = 0;
        int leftRows = components[LEFT].size();
        int rightRows = components[RIGHT].size();

        if (leftRows > rightRows)
        {
            rows = leftRows;
        }
        else
        {
            rows = rightRows;
        }

        return (rows);
    }

    /**
     * Measures and returns the minimum height required to render the components in the indicated
     * row.
     * 
     * @param row the index of the row to measure
     */
    private int rowHeight(int row)
    {
        int height = 0;
        int height1 = height(row, LEFT);
        int height2 = height(row, RIGHT);

        // ----------------------------------------------------
        // take the higher one
        // ----------------------------------------------------
        if (height1 > height2)
        {
            height = height1;
        }
        else
        {
            height = height2;
        }

        return (height);
    }

    /**
     * Measures and returns the minimum height required to render the component in the indicated row
     * and column.
     * 
     * @param row the index of the row to measure
     * @param column the column of the component to measure (<code>LEFT</code> or
     * <code>RIGHT</code>)
     */
    private int height(int row, int column)
    {
        int height = 0;
        int width = 0;
        Component component;
        TwoColumnConstraints constraints;

        try
        {
            constraints = (TwoColumnConstraints) components[column].elementAt(row);
            if (constraints != null)
            {
                component = constraints.component;
                width = (int) component.getMinimumSize().getWidth();
                height = (int) component.getMinimumSize().getHeight();

                if (constraints.position == TwoColumnConstraints.WEST)
                {
                    if (width > (centerRule - leftRule))
                    {
                        component.setBounds(0, 0, (centerRule - leftRule), height);
                    }
                }
                else if (constraints.position == TwoColumnConstraints.EAST)
                {
                    if (width > (rightRule - centerRule))
                    {
                        component.setBounds(0, 0, (rightRule - centerRule), height);
                    }
                }
                else if (constraints.position == TwoColumnConstraints.BOTH)
                {
                    if (width > (rightRule - leftRule))
                    {
                        component.setBounds(0, 0, (rightRule - leftRule), height);
                    }
                }

                height = (int) component.getMinimumSize().getHeight();
            }
        }
        // ----------------------------------------------------
        // we might get an exception if one of the vectors is
        // shorter, because we index out of bounds. If there
        // is nothing there then the height is 0, nothing
        // further to worry about!
        // ----------------------------------------------------
        catch (Throwable exception)
        {}

        return (height);
    }

    /**
     * Computes the margin value based on the container width and the margin setting.
     * 
     * @param parent the component which needs to be laid out.
     */
    private int margin(Container parent)
    {
        int amount = (int) (((parent.getSize().getWidth()) * margin) / 100);

        return (amount);
    }

    /**
     * Computes the top buffer value based on the container width and the setting for the top buffer
     * 
     * @param usedHeight the amount of the parent component's height that is already in use (height
     * of the title and the combined height of all rows).
     * @param parent the component which needs to be laid out.
     */
    private int topBuffer(int usedHeight, Container parent)
    {
        int amount = ((int) parent.getSize().getHeight()) - usedHeight;
        amount = (int) (amount * topBuffer) / 100;

        return (amount);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Computes the indent value based on the container width and the indent setting.
     * 
     * @param parent the component which needs to be laid out.
     */
    /*--------------------------------------------------------------------------*/
    /*
     * private int indent (Container parent) { int amount = (int)(((parent.getMinimumSize
     * ().getWidth ()) * indent) / 100);
     * 
     * return (amount); }
     */
    /**
     * Calculates the preferred size dimensions for the specified panel given the components in the
     * specified parent container.
     * 
     * @param parent the component to be laid out
     */
    public Dimension preferredLayoutSize(Container parent)
    {
        return (minimumLayoutSize(parent));
    }

    /**
     * Calculates the minimum size dimensions for the specified panel given the components in the
     * specified parent container.
     * 
     * @param parent the component to be laid out
     */
    public Dimension minimumLayoutSize(Container parent)
    {
        positionTitle(parent);

        int width = minimumBothColumnsWidth(parent);
        int height = minimumClusterHeight() + titleHeight;

        return (new Dimension(width, height));
    }

    /**
     * Calculates the maximum size dimensions for the specified panel given the components in the
     * specified parent container.
     * 
     * @param parent the component to be laid out
     */
    public Dimension maximumLayoutSize(Container parent)
    {
        return (minimumLayoutSize(parent));
    }

    /**
     * Returns the alignment along the x axis. This specifies how the component would like to be
     * aligned relative to other components. The value should be a number between 0 and 1 where 0
     * represents alignment along the origin, 1 is aligned the furthest away from the origin, 0.5 is
     * centered, etc.
     * 
     * @param parent the component to be laid out
     */
    public float getLayoutAlignmentX(Container parent)
    {
        return (0);
    }

    /**
     * Returns the alignment along the y axis. This specifies how the component would like to be
     * aligned relative to other components. The value should be a number between 0 and 1 where 0
     * represents alignment along the origin, 1 is aligned the furthest away from the origin, 0.5 is
     * centered, etc.
     * 
     * @param parent the component to be laid out
     */
    public float getLayoutAlignmentY(Container parent)
    {
        return (0);
    }

    /**
     * Invalidates the layout, indicating that if the layout manager has cached information it
     * should be discarded.
     * 
     * @param parent the component to be laid out
     */
    public void invalidateLayout(Container parent)
    {
        leftRule = 0;
        rightRule = 0;
        centerRule = 0;
        titleHeight = 0;
    }

    /**
     * Adds the specified component with the specified name to the layout. This version is not
     * supported, use <code>addLayoutComponent</code> with layout contsraints.
     * 
     * @param name the component name
     * @param comp the component to be added
     */
    public void addLayoutComponent(String name, Component comp)
    {
    }

    /**
     * This functionality removes the TwoColumnConstraints from Vectors
     * so that alignment of components on UserInputPanel doesn't get
     * dirty
     * 
     * @param comp the component to be removed
     */
    public void removeLayoutComponent(Component comp)
    {
        Vector left = components[LEFT];
        Vector right = components[RIGHT];

        for (int i = 0; i < left.size(); i++)
        {
            TwoColumnConstraints constraints = (TwoColumnConstraints) left.get(i);
            if (constraints == null)
            {
                continue;
            }
            Component ctemp = constraints.component;
            if (ctemp != null && ctemp.equals(comp))
            {
                if (constraints.position == TwoColumnConstraints.BOTH || constraints.position == TwoColumnConstraints.WESTONLY)
                {
                    right.remove(i);
                }
                break;
            }
        }

        for (int j = 0; j < right.size(); j++)
        {
            TwoColumnConstraints constraints = (TwoColumnConstraints) right.get(j);
            if (constraints == null)
            {
                continue;
            }
            Component ctemp = constraints.component;
            if (ctemp != null && ctemp.equals(comp))
            {
                if (constraints.position == TwoColumnConstraints.BOTH || constraints.position == TwoColumnConstraints.EASTONLY)
                {
                    left.remove(j);
                }
                break;
            }
        }
    }

    /**
     * This method is provided for conveninence of debugging layout problems. It renders the three
     * rules and the limit of the title marign visible after these positions have been computed. In
     * addition, the indent locations are shown as dashed lines. To use this functionality do the
     * following:<br>
     * <br>
     * <ul>
     * <li>in the container using this layout manager override the <code>paint()</code> method.
     * <li>in that method, first call <code>super.paint()</code>
     * <li>then call this method
     * </ul>
     * <br>
     * <b>Note:</b> cast the graphics object received in the <code>paint()</code> method to
     * <code>Graphics2D</code> when making the call.<br>
     * <br>
     * 
     * @param graphics the graphics context used for drawing.
     * @param color the color to use for rendering the layout grid
     */
    public void showRules(Graphics2D graphics, Color color)
    {
        int height = graphics.getClipBounds().height;

        Stroke currentStroke = graphics.getStroke();
        Color currentColor = graphics.getColor();

        Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.5f,
                new float[] { 10, 5}, 5);
        graphics.setColor(color);

        graphics.drawLine(leftRule, 0, leftRule, height);
        graphics.drawLine(centerRule, titleHeight, centerRule, height);
        graphics.drawLine(rightRule, 0, rightRule, height);
        graphics.drawLine(leftRule, titleHeight, rightRule, titleHeight);

        graphics.setStroke(stroke);
        graphics.drawLine((leftRule + indent), titleHeight, (leftRule + indent), height);
        graphics.drawLine((centerRule + indent), titleHeight, (centerRule + indent), height);

        graphics.setStroke(currentStroke);
        graphics.setColor(currentColor);
    }
}
/*---------------------------------------------------------------------------*/
