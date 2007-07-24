/*
 * $Id:$
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2006 Klaus Bartz
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
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import com.izforge.izpack.panels.PathSelectionPanel;
import com.izforge.izpack.util.MultiLineLabel;

/**
 * This is a special layout manager for IzPanels.
 * 
 * @author Klaus Bartz
 * 
 */
public class IzPanelLayout implements LayoutManager, LayoutManager2, LayoutConstants
{

    /** holds all the components and layout constraints. */
    private ArrayList components = new ArrayList();

    /** Maximum rows to handle symbolic values like NEXT_ROW in constraints. */
    private int currentYPos = 0;

    /** Current column to handle symbolic values like NEXT_COLUMN in constraints. */
    private int currentXPos = -1;

    /** Dimension object with prefered size. Will be computed new if invalidateLayout will be called. */
    private Dimension prefLayoutDim;

    private Dimension oldParentSize;

    private Insets oldParentInsets;

    private int columnFillOutRule;

    private double[] overallYStretch = { -1.0, 0.0};

    protected static int[] DEFAULT_Y_GAPS = { -1, 0, 5, 5, 10, 5, 5, 5, 5, 5, 5, 5, 5, 5, 15, 12,
            9, 6, 3, 0};

    protected static int[] DEFAULT_X_GAPS = { -1, 0, 0, 0, 0, 0, 10, 10, 10, 10, 10, 10, 10, 15,
            12, 9, 6, 3, 0};

    protected static int[] DEFAULT_X_ALIGNMENT = { LEFT, LEFT, LEFT, LEFT};

    protected static int[] DEFAULT_Y_ALIGNMENT = { CENTER, CENTER, CENTER, CENTER};

    /** Array with some default constraints. */
    private static IzPanelConstraints DEFAULT_CONSTRAINTS[] = {
            // Default constraints for labels.
            new IzPanelConstraints(DEFAULT_LABEL_ALIGNMENT, DEFAULT_LABEL_ALIGNMENT, NEXT_COLUMN,
                    CURRENT_ROW, 1, 1, AUTOMATIC_GAP, AUTOMATIC_GAP, 0.0, 0.0),
            // Default constraints for text fields.
            new IzPanelConstraints(DEFAULT_TEXT_ALIGNMENT, DEFAULT_TEXT_ALIGNMENT, NEXT_COLUMN,
                    CURRENT_ROW, 1, 1, AUTOMATIC_GAP, AUTOMATIC_GAP, 0.0, 0.0),
            // Default constraints for other controls using two columns if possible.
            new IzPanelConstraints(DEFAULT_CONTROL_ALIGNMENT, DEFAULT_CONTROL_ALIGNMENT,
                    NEXT_COLUMN, CURRENT_ROW, 1, 1, AUTOMATIC_GAP, AUTOMATIC_GAP, 0.0, 0.0),
            // Default constraints for full line controls.
            new IzPanelConstraints(DEFAULT_LABEL_ALIGNMENT, DEFAULT_LABEL_ALIGNMENT, 0, NEXT_ROW,
                    Byte.MAX_VALUE, Byte.MAX_VALUE, AUTOMATIC_GAP, AUTOMATIC_GAP,
                    FULL_LINE_STRETCH, 0.0),
            // Default constraints for constraints for controls/container which are variable in x
            // and y dimension.
            new IzPanelConstraints(DEFAULT_LABEL_ALIGNMENT, DEFAULT_LABEL_ALIGNMENT, 0, NEXT_ROW,
                    Byte.MAX_VALUE, Byte.MAX_VALUE, AUTOMATIC_GAP, AUTOMATIC_GAP,
                    FULL_LINE_STRETCH, FULL_COLUMN_STRETCH),
            // Default constraints for x filler.
            new IzPanelConstraints(DEFAULT_LABEL_ALIGNMENT, DEFAULT_LABEL_ALIGNMENT, NEXT_COLUMN,
                    CURRENT_ROW, 1, 1, 0, 0, 0.0, 0.0),
            // Default constraints for y filler.
            new IzPanelConstraints(DEFAULT_LABEL_ALIGNMENT, DEFAULT_LABEL_ALIGNMENT, 0, NEXT_ROW,
                    1, 1, 0, 0, 0.0, 0.0),
            // Default constraints for other controls using the full line.
            new IzPanelConstraints(DEFAULT_CONTROL_ALIGNMENT, DEFAULT_CONTROL_ALIGNMENT, 0,
                    NEXT_ROW, Byte.MAX_VALUE, Byte.MAX_VALUE, AUTOMATIC_GAP, AUTOMATIC_GAP,
                    FULL_LINE_STRETCH, 0.0),

    };

    /** Anchor to be used for the controls in all panels. */
    private static int ANCHOR = CENTER;

    private static int X_STRETCH_TYPE = RELATIVE_STRETCH;

    private static int Y_STRETCH_TYPE = RELATIVE_STRETCH;

    private static double FULL_LINE_STRETCH_DEFAULT = 1.0;

    private static double FULL_COLUMN_STRETCH_DEFAULT = 1.0;

    private static int DEFAULT_TEXTFIELD_LENGTH = 12;

    private static final int[][] GAP_INTERMEDIAER_LOOKUP = {
            { LABEL_GAP, LABEL_TO_TEXT_GAP, LABEL_TO_CONTROL_GAP, LABEL_GAP, LABEL_TO_CONTROL_GAP,
                    LABEL_GAP, LABEL_GAP},
            { TEXT_TO_LABEL_GAP, TEXT_GAP, TEXT_TO_CONTROL_GAP, TEXT_TO_LABEL_GAP,
                    TEXT_TO_CONTROL_GAP, TEXT_GAP, TEXT_GAP},
            { CONTROL_TO_LABEL_GAP, CONTROL_TO_TEXT_GAP, CONTROL_GAP, CONTROL_TO_LABEL_GAP,
                    CONTROL_GAP, CONTROL_GAP, CONTROL_GAP},
            { LABEL_GAP, LABEL_TO_TEXT_GAP, LABEL_TO_CONTROL_GAP, LABEL_GAP, LABEL_TO_CONTROL_GAP,
                    LABEL_GAP, LABEL_GAP},
            { CONTROL_TO_LABEL_GAP, CONTROL_TO_TEXT_GAP, CONTROL_GAP, CONTROL_TO_LABEL_GAP,
                    CONTROL_GAP, CONTROL_GAP, CONTROL_GAP},
            { NO_GAP, NO_GAP, NO_GAP, NO_GAP, NO_GAP, NO_GAP, NO_GAP, NO_GAP},
            { NO_GAP, NO_GAP, NO_GAP, NO_GAP, NO_GAP, NO_GAP, NO_GAP, NO_GAP},
            { NO_GAP, NO_GAP, NO_GAP, NO_GAP, NO_GAP, NO_GAP, NO_GAP, NO_GAP}};

    /**
     * Default constructor
     */
    public IzPanelLayout()
    {
        this(NO_FILL_OUT_COLUMN);
    }

    /**
     * Creates an layout manager which consider the given column fill out rule. Valid rules are
     * NO_FILL_OUT_COLUMN, FILL_OUT_COLUMN_WIDTH, FILL_OUT_HEIGHT and FILL_OUT_COLUMN_SIZE
     * 
     * @param colFillOutRule
     */
    public IzPanelLayout(int colFillOutRule)
    {
        super();
        columnFillOutRule = colFillOutRule;
    }

    /**
     * Returns the y gap for the given constraint dependant on the next y constraint.
     * 
     * @param curConst constraint of the component for which the gap should be returnd
     * @param nextYConst constraint of the component which is the next in y direction
     * @return the y gap
     */
    private static int getYGap(IzPanelConstraints curConst, IzPanelConstraints nextYConst)
    {

        Class nextClass = (nextYConst != null) ? nextYConst.component.getClass()
                : FillerComponent.class;
        int interId = GAP_INTERMEDIAER_LOOKUP[getIntermediarId(curConst.component.getClass(), null)][getIntermediarId(
                nextClass, null)];
        if (interId < 0) interId = -interId;
        return (DEFAULT_Y_GAPS[interId]);

    }

    /**
     * Returns the x gap for the given constraint dependant on the next x constraint.
     * 
     * @param curConst constraint of the component for which the gap should be returnd
     * @param nextXConst constraint of the component which is the next in x direction
     * @return the x gap
     */
    private static int getXGap(IzPanelConstraints curConst, IzPanelConstraints nextXConst)
    {

        Class nextClass = (nextXConst != null) ? nextXConst.component.getClass()
                : FillerComponent.class;
        int interId = GAP_INTERMEDIAER_LOOKUP[getIntermediarId(curConst.component.getClass(), null)][getIntermediarId(
                nextClass, null)];
        if (interId < 0) interId = -interId;
        return (DEFAULT_X_GAPS[interId]);

    }

    /**
     * Returns an index depending on the class type. Only for internal use.
     * 
     * @param clazz class for which the index should be returned
     * @param comp component for which the index should be returned
     * @return an index depending on the class type
     */
    private static int getIntermediarId(Class clazz, Component comp)
    {

        if (comp != null)
        {
            if (MultiLineLabel.class.isAssignableFrom(clazz)
                    || LabelFactory.FullLineLabel.class.isAssignableFrom(clazz))
                return (FULL_LINE_COMPONENT_CONSTRAINT);
            if (PathSelectionPanel.class.isAssignableFrom(clazz)
                    || JCheckBox.class.isAssignableFrom(clazz)
                    || JRadioButton.class.isAssignableFrom(clazz))
                return (FULL_LINE_CONTROL_CONSTRAINT);
            if (FillerComponent.class.isAssignableFrom(clazz)
                    || javax.swing.Box.Filler.class.isAssignableFrom(clazz))
            {
                Dimension size = comp.getPreferredSize();
                if (size.height >= Short.MAX_VALUE || size.height <= 0)
                {
                    size.height = 0;
                    comp.setSize(size);
                    return (XDUMMY_CONSTRAINT);
                }
                else if (size.width >= Short.MAX_VALUE || size.width <= 0)
                {
                    size.width = 0;
                    comp.setSize(size);
                    return (YDUMMY_CONSTRAINT);
                }
            }
        }
        if (JScrollPane.class.isAssignableFrom(clazz)) return (XY_VARIABLE_CONSTRAINT);
        if (JLabel.class.isAssignableFrom(clazz)) return (LABEL_CONSTRAINT);
        if (JTextComponent.class.isAssignableFrom(clazz)) return (TEXT_CONSTRAINT);
        if (FillerComponent.class.isAssignableFrom(clazz)) return (XDUMMY_CONSTRAINT);
        if (javax.swing.Box.Filler.class.isAssignableFrom(clazz)) return (XDUMMY_CONSTRAINT);
        return (CONTROL_CONSTRAINT); // Other controls.
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
     */
    public void addLayoutComponent(String name, Component comp)
    {
        // Has to be implemented, but not supported in this class.
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
     */
    public void removeLayoutComponent(Component comp)
    {
        // Has to be implemented, but not supported in this class.
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
     */
    public Dimension minimumLayoutSize(Container parent)
    {
        return preferredLayoutSize(parent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
     */
    public Dimension preferredLayoutSize(Container parent)
    {
        return (determineSize());
    }

    /**
     * Method which determine minimum with and height of this layout. The size will be stored after
     * cumputing in a class member. With a call to invalidateLayout this will be deleted and at the
     * next call to this method the values are computed again.
     * 
     * @return current minimum size
     */
    private Dimension determineSize()
    {
        if (prefLayoutDim == null)
        {
            int width = minimumAllColumnsWidth();
            int height = minimumOverallHeight();
            prefLayoutDim = new Dimension(width, height);
        }
        return (Dimension) (prefLayoutDim.clone());
    }

    /**
     * Returns the number of rows that need to be laid out.
     * 
     * @return the number of rows that need to be laid out
     */
    private int rows()
    {
        int maxRows = 0;
        for (int i = 0; i < components.size(); ++i)
        {
            int curRows = ((ArrayList) components.get(i)).size();
            if (curRows > maxRows) maxRows = curRows;

        }
        return (maxRows);
    }

    /**
     * Returns the number of columns that need to be laid out.
     * 
     * @return the number of columns that need to be laid out
     */
    private int columns()
    {
        return (components.size());
    }

    /**
     * Minimum height of all rows.
     * 
     * @return minimum height of all rows
     */
    private int minimumOverallHeight()
    {
        int height = 0;

        for (int i = 0; i < rows(); i++)
        {
            height += rowHeight(i);
        }

        return (height);
    }

    /**
     * Measures and returns the minimum height required to render the components in the indicated
     * row.
     * 
     * @param row the index of the row to measure
     * @return minimum height of a row
     */
    private int rowHeight(int row)
    {
        int height = 0;
        for (int i = 0; i < components.size(); ++i)
        {
            int curHeight = getCellSize(i, row, null).height;
            if (curHeight > height) height = curHeight;
        }
        return (height);
    }

    /**
     * Measures and returns the minimum height required to render the components in the indicated
     * row.
     * 
     * @param row the index of the row to measure
     * @param maxOverallHeight
     * @param minOverallHeight
     * @return minimum height of a row
     */
    private int rowHeight(int row, int minOverallHeight, int maxOverallHeight)
    {
        // First determine minimum row height and whether there is a y stretch or not.
        int height = 0;
        double[] yStretch = getOverallYStretch();
        if (yStretch[0] <= 0.0) return (rowHeight(row));
        double maxStretch = 0.0;
        double[] stretchParts = new double[components.size()];
        for (int i = 0; i < components.size(); ++i)
        {
            IzPanelConstraints constraints = getConstraints(i, row);
            double stretch = constraints.getYStretch();
            stretchParts[i] = stretch;
            if (stretch > maxStretch) maxStretch = stretch;
            int curHeight = getCellSize(i, row, constraints).height;
            if (curHeight > height) height = curHeight;
        }
        if (maxOverallHeight <= minOverallHeight) return (height);
        // We have a y stretch and place. Let us disperse it.
        int pixels = (int) (maxOverallHeight - yStretch[1] - minOverallHeight);
        int stretchPart = (int) (pixels * maxStretch);
        if (stretchPart > 0)
        {
            for (int i = 0; i < components.size(); ++i)
            {
                if (stretchParts[i] < 0.00000001) continue;
                IzPanelConstraints constraints = getConstraints(i, row);
                Dimension size = constraints.component.getPreferredSize();
                if (size.height + stretchPart * stretchParts[i] < height)
                    size.height = (int) (height + stretchPart * stretchParts[i]);
                else
                    size.height = height + stretchPart;
                if (constraints.component instanceof JScrollPane)
                {   // This is a hack for text areas which uses word wrap. At tests
                    // they have a preferred width of 100 pixel which breaks the layout.

                    if (((JScrollPane) constraints.component).getViewport().getView() instanceof JTextArea)
                    {
                        if (((JTextArea) ((JScrollPane) constraints.component).getViewport()
                                .getView()).getLineWrap()) size.width = 1000;
                    }
                }
                ((JComponent) constraints.component).setPreferredSize(size);

            }
            height += stretchPart;

        }
        return (height);
    }

    /**
     * Returns the sum of the maximum y stretch of each row.
     * 
     * @return the sum of the maximum y stretch of each row
     */
    private double[] getOverallYStretch()
    {
        if (overallYStretch[0] >= 0.0) return (overallYStretch); // Stretch already computed.
        overallYStretch[0] = 0.0;
        for (int row = 0; row < rows(); ++row)
        {
            double maxStretch = 0.0;
            double maxGap = 0.0;
            for (int i = 0; i < components.size(); ++i)
            {
                IzPanelConstraints constraints = getConstraints(i, row);
                resolveDefaultSettings(i, row);
                if (constraints.getYStretch() == FULL_COLUMN_STRETCH)
                    constraints.setYStretch(IzPanelLayout.getFullColumnStretch());
                double curStretch = constraints.getYStretch();
                if (curStretch > maxStretch) maxStretch = curStretch;
                double curYGap = constraints.getYGap();
                if (curYGap > maxGap) maxGap = curYGap;
            }
            overallYStretch[0] += maxStretch;
            overallYStretch[1] += maxGap;
        }
        // Modify y stretch depending on the current Y-Stretch type.
        if (overallYStretch[0] > 0.0)
        {
            switch (IzPanelLayout.getYStretchType())
            {
            case RELATIVE_STRETCH:
                break;
            case ABSOLUTE_STRETCH:
                overallYStretch[0] = 1.0;
                break;
            case NO_STRETCH:
            default:
                overallYStretch[0] = 0.0;
                break;
            }
        }

        return (overallYStretch);
    }

    /**
     * Measures and returns the minimum size required to render the component in the indicated row
     * and column. The constraints can be null.
     * 
     * @param row the index of the row to measure
     * @param column the column of the component
     * @param constraints constraints of current component
     * @return size of the given cell
     */
    private Dimension getCellSize(int column, int row, IzPanelConstraints constraints)
    {
        Dimension retval = new Dimension();
        Component component;

        try
        {
            if (constraints == null) constraints = getConstraints(column, row);
            if (constraints != null)
            {
                component = constraints.component;
                // Some components returns the needed size with getPreferredSize
                // some with getMinimumSize. Therefore following code...
                Dimension dim = component.getPreferredSize();
                Dimension dim2 = component.getMinimumSize();
                retval.height = (dim.height > dim2.height) ? dim.height : dim2.height;
                retval.width = (dim.width > dim2.width) ? dim.width : dim2.width;
                if (component instanceof JScrollPane)
                { // But at a JScrollPane we have to use the minimum size because
                    // preferred size is the size of the view.
                    retval.height = dim2.height;
                    retval.width = dim2.width;
                }
                if (needsReEvaluation(component)) retval.width = 0;

            }
        }
        catch (Throwable exception)
        {
            // ----------------------------------------------------
            // we might get an exception if one of the array list is
            // shorter, because we index out of bounds. If there
            // is nothing there then the height is 0, nothing
            // further to worry about!
            // ----------------------------------------------------
        }

        return (retval);
    }

    /**
     * Returns the minimum width of the column requested. This contains not the gaps.
     * 
     * @param column the columns to measure
     * 
     * @return the minimum width required to fit the components in this column
     */
    private int minimumColumnWidth(int column)
    {
        int maxWidth = 0;
        Dimension[] cs = new Dimension[rows()];
        for (int i = 0; i < rows(); ++i)
        {
            IzPanelConstraints constraints = getConstraints(column, i);
            cs[i] = getCellSize(column, i, constraints);
            if (constraints.getXWeight() <= 1) if (maxWidth < cs[i].width) maxWidth = cs[i].width;
        }
        if (maxWidth == 0)
        {
            for (int i = 0; i < rows(); ++i)
            {
                if (maxWidth < cs[i].width) maxWidth = cs[i].width;
            }
        }
        return (maxWidth);
    }

    /**
     * Returns the minimum width needed by all columns.
     * 
     * @return the minimum width needed by all columns
     */
    private int minimumAllColumnsWidth()
    {
        int width = 0;
        for (int i = 0; i < this.components.size(); ++i)
            width += minimumColumnWidth(i);
        return (width);
    }

    /**
     * Returns the constraint object of the component at the given place.
     * 
     * @param col column of the component
     * @param row row of the component
     * @return the constraint object of the component at the given place
     */
    private IzPanelConstraints getConstraints(int col, int row)
    {
        if (col >= columns() || row >= rows()) return (null);
        Object obj = components.get(col);
        if (obj != null && obj instanceof ArrayList)
        {
            try
            {
                obj = ((ArrayList) components.get(col)).get(row);
            }
            catch (Throwable t)
            {
                obj = null;
            }
            if (obj != null) return ((IzPanelConstraints) obj);
            // no constraints is possible if no valid component
            // was added under this component.
            // Put dummy components into the array.

            ArrayList colA = (ArrayList) components.get(col);
            for (int curRow = colA.size(); row >= curRow; ++curRow)
            {

                IzPanelConstraints currentConst = IzPanelLayout
                        .getDefaultConstraint(XDUMMY_CONSTRAINT);
                currentConst.setXPos(col);
                currentConst.setYPos(curRow);
                currentConst.component = new FillerComponent();
                try
                {
                    ((ArrayList) components.get(col)).add(row, currentConst);
                }
                catch (Throwable t)
                {
                    return (null);
                }
            }
            return (getConstraints(col, row));
        }
        return (null);
    }

    private int getAdaptedXPos(int xpos, int curWidth, Dimension curDim,
            IzPanelConstraints currentConst)
    {
        int adaptedXPos = xpos;// currentConst.getXGap();
        if ((columnFillOutRule & FILL_OUT_COLUMN_WIDTH) > 0) return (adaptedXPos);
        switch (currentConst.getXCellAlignment())
        {
        case LEFT:
            break;
        case RIGHT:
            adaptedXPos += curWidth - curDim.width;
            break;
        case CENTER:
        default:
            adaptedXPos += (curWidth - curDim.width) / 2;
            break;

        }
        return (adaptedXPos);
    }

    private int getAdaptedYPos(int ypos, int curHeight, Dimension curDim,
            IzPanelConstraints currentConst)
    {
        int adaptedYPos = ypos;// + currentConst.getYGap();
        if ((columnFillOutRule & FILL_OUT_COLUMN_HEIGHT) > 0) return (adaptedYPos);
        int height = curDim.height;
        switch (currentConst.getYCellAlignment())
        {
        case TOP:
            break;
        case BOTTOM:
            adaptedYPos += curHeight - height;
            break;
        case CENTER:
        default:
            adaptedYPos += (curHeight - height) / 2;
            break;

        }
        if (adaptedYPos < ypos) return (ypos);
        return (adaptedYPos);
    }

    private void resolveDefaultSettings(int col, int row)
    {
        IzPanelConstraints currentConst = getConstraints(col, row);
        IzPanelConstraints nextYConst = getConstraints(col, row + 1);
        IzPanelConstraints nextXConst = getConstraints(col + 1, row);
        if (currentConst == null) return;
        int gap = currentConst.getYGap();
        if (gap == AUTOMATIC_GAP)
        { // Automatic gap; determine now.
            currentConst.setYGap(getYGap(currentConst, nextYConst));
        }
        else if (gap < 0)
        {
            currentConst.setYGap(DEFAULT_Y_GAPS[-gap]);
        }
        gap = currentConst.getXGap();
        if (gap == AUTOMATIC_GAP)
        { // Automatic gap; determine now.
            currentConst.setXGap(getXGap(currentConst, nextXConst));
        }
        else if (gap < 0)
        {
            currentConst.setXGap(DEFAULT_X_GAPS[-gap]);
        }

        if (currentConst.getXCellAlignment() < 0)
        {
            currentConst.setXCellAlignment(DEFAULT_X_ALIGNMENT[-currentConst.getXCellAlignment()]);
        }
        if (currentConst.getYCellAlignment() < 0)
        {
            currentConst.setYCellAlignment(DEFAULT_Y_ALIGNMENT[-currentConst.getYCellAlignment()]);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
     */
    public void layoutContainer(Container parent)
    {
        if (!needNewLayout(parent))
        {
            fastLayoutContainer(parent);
            return;
        }
        prefLayoutDim = null;
        preferredLayoutSize(parent);
        Dimension realSizeDim = parent.getSize();
        Insets insets = parent.getInsets();

        int rowHeight = 0;
        int onceAgain = 0;
        int[] generellOffset = new int[] { 0, 0};
        // int generellYOffset = 0;
        // int generellXOffset = 0;
        int maxWidth = 0;
        int overallHeight = 0;
        int anchorNeedsReEval = 0;
        Rectangle curRect = new Rectangle();
        int minOverallHeight = minimumOverallHeight();
        int maxOverallHeight = realSizeDim.height - insets.top - insets.bottom;
        while (anchorNeedsReEval < 2)
        {
            int ypos = insets.top;

            int row = 0;
            int minWidth = 0xffff;
            int minHeight = 0xffff;
            maxWidth = 0;
            overallHeight = 0;
            while (row < rows())
            {
                int outerRowHeight = 0;
                int xpos = insets.left;
                // rowHeight = rowHeight(row, minOverallHeight, maxOverallHeight);
                int col = 0;
                IzPanelConstraints[] colConstraints = new IzPanelConstraints[columns()];
                int usedWidth = 0;
                Dimension curDim;
                while (col < columns())
                {
                    if (col == 0) rowHeight = rowHeight(row, minOverallHeight, maxOverallHeight);
                    IzPanelConstraints currentConst = getConstraints(col, row);
                    colConstraints[col] = currentConst;
                    Component currentComp = currentConst.component;
                    curDim = currentComp.getPreferredSize();
                    int curWidth = minimumColumnWidth(col);
                    col++;
                    if (currentConst.getXWeight() > 1)
                    {
                        int weight = currentConst.getXWeight();
                        while (weight > 1 && col < columns())
                        {
                            colConstraints[col] = getConstraints(col, row);
                            if (!(colConstraints[col].component instanceof FillerComponent)) break;
                            curWidth += minimumColumnWidth(col);
                            col++;
                            weight--;
                        }
                    }
                    // width known
                    int adaptedXPos = getAdaptedXPos(xpos, curWidth, curDim, currentConst);
                    int adaptedYPos = getAdaptedYPos(ypos, rowHeight, curDim, currentConst);
                    // currentComp.setBounds(adaptedXPos + generellOffset[0], adaptedYPos
                    // + currentConst.getYGap() + generellOffset[1], curWidth, rowHeight);
                    // + generellOffset[1], curWidth, rowHeight);
                    int useWidth = curDim.width;
                    int useHeight = curDim.height;
                    if ((columnFillOutRule & FILL_OUT_COLUMN_WIDTH) > 0) useWidth = curWidth;
                    if ((columnFillOutRule & FILL_OUT_COLUMN_HEIGHT) > 0) useHeight = rowHeight;
                    if (curWidth < useWidth) useWidth = curWidth;
                    // First setBounds using computed offsets and the size which exist previous.
                    currentComp.setBounds(adaptedXPos + generellOffset[0], adaptedYPos
                            + generellOffset[1], useWidth, useHeight);
                    currentComp.getBounds(curRect);
                    if (!(currentComp instanceof FillerComponent))
                    {
                        if (curRect.x < minWidth) minWidth = curRect.x;
                        if (curRect.y < minHeight) minHeight = curRect.y;
                    }
                    int curMax = (int) curRect.getMaxX();
                    if (curMax - minWidth > maxWidth) maxWidth = curMax - minWidth;
                    curMax = (int) curRect.getMaxY();
                    currentConst.setBounds(curRect);
                    if (curMax - minHeight > overallHeight) overallHeight = curMax - minHeight;
                    // xpos += currentComp.getSize().width + currentConst.getXGap();
                    xpos += curWidth + currentConst.getXGap();
                    usedWidth += curWidth;
                    if (outerRowHeight < rowHeight + currentConst.getYGap())
                        outerRowHeight = rowHeight + currentConst.getYGap();
                }
                // Now we have made a row, but may be there are place across or/and a component
                // needs a reevaluation.
                double rowStretch = 0.0;
                int i;
                // Determine hole stretch of this row.
                for (i = 0; i < colConstraints.length; ++i)
                {
                    if (colConstraints[i].getXStretch() == FULL_LINE_STRETCH)
                        colConstraints[i].setXStretch(IzPanelLayout.getFullLineStretch());
                    rowStretch += colConstraints[i].getXStretch();
                }
                // Modify rowStretch depending on the current X-Stretch type.
                if (rowStretch > 0.0)
                {
                    switch (IzPanelLayout.getXStretchType())
                    {
                    case RELATIVE_STRETCH:
                        break;
                    case ABSOLUTE_STRETCH:
                        rowStretch = 1.0;
                        break;
                    case NO_STRETCH:
                    default:
                        rowStretch = 0.0;
                        break;
                    }
                }
                if (realSizeDim.width - insets.right != xpos && rowStretch > 0.0)
                { // Compute only if there is space to share and at least one control should be
                    // stretched.
                    int pixel = realSizeDim.width - insets.right - xpos; // How many pixel we
                    // can use for stretching.
                    int offset = 0;
                    int oldOnceAgain = onceAgain;
                    for (i = 0; i < colConstraints.length; ++i)
                    {
                        int curPixel = (int) ((colConstraints[i].getXStretch() / rowStretch) * pixel);

                        Rectangle curBounds = colConstraints[i].component.getBounds();
                        int newWidth = curPixel + curBounds.width;
                        colConstraints[i].component.setBounds(curBounds.x + offset, curBounds.y,
                                newWidth, curBounds.height);
                        colConstraints[i].component.getBounds(curRect);
                        if (curRect.x > 0 && curRect.x < minWidth) minWidth = curRect.x;
                        if (curRect.y > 0 && curRect.y < minHeight) minHeight = curRect.y;
                        int curMax = (int) curRect.getMaxX();
                        if (curMax - minWidth > maxWidth) maxWidth = curMax - minWidth;
                        curMax = (int) curRect.getMaxY();
                        colConstraints[i].setBounds(curRect);

                        if (curMax - minHeight > overallHeight) overallHeight = curMax - minHeight;

                        offset += curPixel;
                        if (needsReEvaluation(colConstraints[i].component))
                        {
                            if (oldOnceAgain == onceAgain) onceAgain++;
                        }
                    }

                }
                // Seems so that height has changed. Reevaluate only one time else it is possible
                // to go in a endless loop.

                if (onceAgain == 1) continue;
                onceAgain = 0;
                ypos += outerRowHeight;
                row++;
            }
            anchorNeedsReEval += resolveGenerellOffsets(generellOffset, realSizeDim, insets,
                    maxWidth, overallHeight);

        }
    }

    private void fastLayoutContainer(Container parent)
    {
        for (int row = 0; row < rows(); ++row)
        {
            for (int col = 0; col < columns(); ++col)
            {
                IzPanelConstraints currentConst = getConstraints(col, row);
                if (currentConst != null)
                    currentConst.component.setBounds(currentConst.getBounds());

            }

        }
    }

    private boolean needNewLayout(Container parent)
    {
        Dimension ops = oldParentSize;
        Insets opi = oldParentInsets;
        oldParentSize = parent.getSize();
        oldParentInsets = parent.getInsets();
        if (opi == null || opi == null) return (true);
        if (ops.equals(parent.getSize()) && opi.equals(parent.getInsets())) return (false);
        return (true);

    }

    private int resolveGenerellOffsets(int[] generellOffset, Dimension realSizeDim, Insets insets,
            int maxWidth, int overallHeight)
    {
        int retval = 1;
        switch (getAnchor())
        {
        case CENTER:
            generellOffset[0] = (realSizeDim.width - insets.left - insets.right - maxWidth) / 2;
            generellOffset[1] = (realSizeDim.height - insets.top - insets.bottom - overallHeight) / 2;
            break;
        case WEST:
            generellOffset[0] = 0;
            generellOffset[1] = (realSizeDim.height - insets.top - insets.bottom - overallHeight) / 2;
            break;
        case EAST:
            generellOffset[0] = realSizeDim.width - insets.left - insets.right - maxWidth;
            generellOffset[1] = (realSizeDim.height - insets.top - insets.bottom - overallHeight) / 2;
            break;
        case NORTH:
            generellOffset[0] = (realSizeDim.width - insets.left - insets.right - maxWidth) / 2;
            generellOffset[1] = 0;
            break;
        case SOUTH:
            generellOffset[0] = (realSizeDim.width - insets.left - insets.right - maxWidth) / 2;
            generellOffset[1] = realSizeDim.height - insets.top - insets.bottom - overallHeight;
            break;
        case NORTH_WEST:
            generellOffset[0] = 0;
            generellOffset[1] = 0;
            retval = 2;
            break;
        case NORTH_EAST:
            generellOffset[0] = realSizeDim.width - insets.left - insets.right - maxWidth;
            generellOffset[1] = 0;
            break;
        case SOUTH_WEST:
            generellOffset[0] = 0;
            generellOffset[1] = realSizeDim.height - insets.top - insets.bottom - overallHeight;
            break;
        case SOUTH_EAST:
            generellOffset[0] = realSizeDim.width - insets.left - insets.right - maxWidth;
            generellOffset[1] = realSizeDim.height - insets.top - insets.bottom - overallHeight;
            break;

        }
        if(generellOffset[0] < 0 ) generellOffset[0] = 0;
        if(generellOffset[1] < 0 ) generellOffset[1] = 0;
        return (retval);
    }

    /**
     * Returns whether the type of component needs potential a reevaluation or not.
     * 
     * @param comp component to check
     * @return whether the type of component needs potential a reevaluation or not
     */
    private boolean needsReEvaluation(Component comp)
    {
        if ((comp instanceof com.izforge.izpack.util.MultiLineLabel)
                || (comp instanceof com.izforge.izpack.panels.PathSelectionPanel)) return (true);
        return (false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager2#getLayoutAlignmentX(java.awt.Container)
     */
    public float getLayoutAlignmentX(Container target)
    {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager2#getLayoutAlignmentY(java.awt.Container)
     */
    public float getLayoutAlignmentY(Container target)
    {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager2#invalidateLayout(java.awt.Container)
     */
    public void invalidateLayout(Container target)
    {
        // prefLayoutDim = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
     */
    public Dimension maximumLayoutSize(Container target)
    {
        return (minimumLayoutSize(target));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.LayoutManager2#addLayoutComponent(java.awt.Component, java.lang.Object)
     */
    public void addLayoutComponent(Component comp, Object constraints)
    {
        if (comp == null) throw new NullPointerException("component has to be not null");
        IzPanelConstraints cc;
        if (!(constraints instanceof IzPanelConstraints))
        {
            if ((comp instanceof FillerComponent)
                    && ((FillerComponent) comp).getConstraints() != null)
                cc = (IzPanelConstraints) ((FillerComponent) comp).getConstraints().clone();
            else
                cc = (IzPanelConstraints) IzPanelLayout.DEFAULT_CONSTRAINTS[getIntermediarId(comp
                        .getClass(), comp)].clone();
            if (NEXT_LINE.equals(constraints))
            {
                cc.setXPos(0);
                cc.setYPos(NEXT_ROW);
            }
        }
        else
            cc = (IzPanelConstraints) ((IzPanelConstraints) constraints).clone();
        cc.component = comp;
        int i;
        // Modify positions if constraint value is one of the symbolic ints.
        int yPos = cc.getYPos();
        if (yPos == LayoutConstants.NEXT_ROW) yPos = currentYPos + 1;
        if (yPos == LayoutConstants.CURRENT_ROW) yPos = currentYPos;
        cc.setYPos(yPos);
        int xPos = cc.getXPos();
        if (xPos == LayoutConstants.NEXT_COLUMN) xPos = currentXPos + 1;
        if (xPos == LayoutConstants.CURRENT_COLUMN) xPos = currentXPos;
        cc.setXPos(xPos);
        // Now we know real x and y position. If needed, expand array or
        // array of array.
        int perfCol = cc.getXWeight() < Byte.MAX_VALUE ? cc.getXWeight() : 1;
        if (components.size() < cc.getXPos() + perfCol)
        {
            for (i = components.size() - 1; i < cc.getXPos() + perfCol - 1; ++i)
                components.add(new ArrayList());
        }
        IzPanelConstraints curConst = cc;
        for (int j = 0; j < perfCol; ++j)
        {
            ArrayList xComp = (ArrayList) components.get(xPos);
            if (xComp.size() < yPos)
            {
                for (i = xComp.size() - 1; i < yPos - 1; ++i)
                {
                    IzPanelConstraints dc = getDefaultConstraint(XDUMMY_CONSTRAINT);
                    dc.component = new FillerComponent();
                    xComp.add(dc);

                }
            }

            xComp.add(yPos, curConst);
            if (j < perfCol - 1)
            {
                curConst = getDefaultConstraint(XDUMMY_CONSTRAINT);
                curConst.component = new FillerComponent();
                xPos++;
            }
        }
        int xcsize = ((ArrayList) components.get(xPos)).size() - 1;
        if (currentYPos < xcsize) currentYPos = xcsize;
        currentXPos = xPos;

    }

    /**
     * Creates an invisible, component with a defined width. This component will be placed in the
     * given cell of an IzPackLayout. If no constraint will be set (the default) a default
     * constraint with NEXT_COLUMN and CURRENT_ROW will be used. This component has the height 0.
     * The height of the row will be determined by other components in the same row.
     * 
     * @param width the width of the invisible component
     * @return the component
     */
    public static Component createHorizontalStrut(int width)
    {
        return (new FillerComponent(new Dimension(width, 0)));
    }

    /**
     * Creates an invisible, component with a defined height. This component will be placed in the
     * given cell of an IzPackLayout. If no constraint will be set (the default) a default
     * constraint with column 0 and NEXT_ROW will be used. If the next component also uses NEXT_ROW,
     * this strut goes over the hole width with the declared height. If more components are in the
     * row, the highest of them determines the height of the row. This component has the width 0.
     * The width of a row will be determined by other rows.
     * 
     * @param height the height of the invisible component, in pixels >= 0
     * @return the component
     */
    public static Component createVerticalStrut(int height)
    {
        return (new FillerComponent(new Dimension(0, height)));
    }

    /**
     * Returns a filler component which has self the size 0|0. Additional there is a constraints
     * which has the x position 0,y position NEXT_ROW, x and y weight 10, x-stretch 1.0 and the y
     * gap PARAGRAPH_GAP. Add the returned component to the IzPanel. Use NEXT_LINE (or NEXT_ROW in
     * the constraints) for the next added control, else the layout will be confused.
     * 
     * @return a filler component with the height of the defined paragraph gap
     */
    public static Component createParagraphGap()
    {
        return (createGap(PARAGRAPH_GAP, VERTICAL));
    }

    /**
     * Returns a filler component which has self the size 0|0. Additional there is a constraints
     * which has the x position 0,y position NEXT_ROW, x and y weight 10, x-stretch 1.0 and the y
     * gap FILLER&lt;given filler number&gt;_GAP. Add the returned component to the IzPanel. Use
     * NEXT_LINE (or NEXT_ROW in the constraints) for the next added control, else the layout will
     * be confused. Attention! fillerNumber determines not directly the size of filler else the
     * filler type. The size can be determined in the config file as modifier in the info section
     * defining from filler1YGap to filler5YGap.
     * 
     * @param fillerNumber number of the filler which should be used
     * @return a filler component with the height of the defined paragraph gap
     */
    public static Component createVerticalFiller(int fillerNumber)
    {
        // The symbolic numbers all are negative. Therefore a higher filler needs
        // a less number.
        return (createGap(FILLER1_GAP + 1 - fillerNumber, VERTICAL));
    }

    /**
     * Returns a filler component which has self the size 0|0. Additional there is a constraints
     * which has the position NEXT_COLUMN ,y position CURRENT_ROW, x and y weight 1, stretch 0.0 and
     * the gap FILLER&lt;given filler number&gt;_GAP. Add the returned component to the IzPanel.
     * Attention! fillerNumber determines not directly the size of filler else the filler type. The
     * size can be determined in the config file as modifier in the info section defining from
     * filler1XGap to filler5XGap.
     * 
     * @param fillerNumber number of the filler which should be used
     * @return a filler component with the width of the defined paragraph gap
     */
    public static Component createHorizontalFiller(int fillerNumber)
    {
        // The symbolic numbers all are negative. Therefore a higher filler needs
        // a less number.
        return (createGap(FILLER1_GAP + 1 - fillerNumber, HORIZONTAL));
    }

    /**
     * Returns a filler component which has self the size 0|0. Additional there is a constraints
     * which has the gap defined for the given gap type. If direction is HORIZONTAL, the x position
     * is NEXT_COLUMN ,y position CURRENT_ROW, x and y weight 1, stretch 0.0. If direction is
     * VERTICAL, the x position will by 0,y position NEXT_ROW, x and y weight 10, x-stretch 1.0. Add
     * the returned component to the IzPanel. The result will be that a gap will be inserted into
     * the layout at the current place with the width equal to the defined paragraph gap.
     * 
     * @param gapType type of gap to be used
     * @param direction direction to be used
     * 
     * @return a filler component with the width of the defined paragraph gap
     */

    public static Component createGap(int gapType, int direction)
    {
        if (direction == HORIZONTAL)
            return (new FillerComponent(new Dimension(0, 0), new IzPanelConstraints(
                    DEFAULT_CONTROL_ALIGNMENT, DEFAULT_CONTROL_ALIGNMENT, NEXT_COLUMN, CURRENT_ROW,
                    1, 1, gapType, 0, 0.0, 0.0)));
        return (new FillerComponent(new Dimension(0, 0), new IzPanelConstraints(
                DEFAULT_CONTROL_ALIGNMENT, DEFAULT_CONTROL_ALIGNMENT, 0, NEXT_ROW, 10, 10, 0,
                gapType, 1.0, 0.0)));
    }

    /**
     * Returns the constraint for the given type. Valid types are declared in the interface
     * <code>LayoutConstraints</code>. Possible are LABEL_CONSTRAINT, TEXT_CONSTRAINT and
     * CONTROL_CONSTRAINT.
     * 
     * @param type for which the constraint should be returned
     * @return a copy of the default constraint for the given type
     */
    public static IzPanelConstraints getDefaultConstraint(int type)
    {
        return ((IzPanelConstraints) DEFAULT_CONSTRAINTS[type].clone());
    }

    /**
     * Component which will be used as placeholder if not extern component will be set or as filler
     * for struts.
     * 
     * @author Klaus Bartz
     * 
     */
    public static class FillerComponent extends Component
    {

        private Dimension size;

        private IzPanelConstraints constraints;

        /**
         * Default constructor creating an filler with the size 0|0.
         */
        public FillerComponent()
        {
            this(new Dimension(0, 0));
        }

        /**
         * Constructor with giving the filler a size.
         * 
         * @param size dimension to be used as size for this filler.
         */
        public FillerComponent(Dimension size)
        {
            this(size, null);
        }

        /**
         * Constructor with giving the filler a size and set the constraints.
         * 
         * @param size
         * @param constraints
         */
        public FillerComponent(Dimension size, IzPanelConstraints constraints)
        {
            super();
            this.size = size;
            this.constraints = constraints;
        }

        public Dimension getMinimumSize()
        {
            return (Dimension) (size.clone());
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.Component#getPreferredSize()
         */
        public Dimension getPreferredSize()
        {
            return getMinimumSize();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.Component#getMaximumSize()
         */
        public Dimension getMaximumSize()
        {
            return getMinimumSize();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.Component#getBounds()
         */
        public Rectangle getBounds()
        {
            return (getBounds(new Rectangle()));
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.Component#getBounds(java.awt.Rectangle)
         */
        public Rectangle getBounds(Rectangle rect)
        {
            Rectangle rv = (rect != null) ? rect : new Rectangle();
            rv.setBounds(0, 0, size.width, size.height);
            return (rv);
        }

        /**
         * Returns the constraints defined for this component. Often this will be null.
         * 
         * @return the constraints defined for this component
         */
        public IzPanelConstraints getConstraints()
        {
            return constraints;
        }

        /**
         * Sets the constraints which should be used by this component.
         * 
         * @param constraints constraints to be used
         */
        public void setConstraints(IzPanelConstraints constraints)
        {
            this.constraints = constraints;
        }
    }

    /**
     * Returns the anchor constant.
     * 
     * @return the anchor constant
     */
    public static int getAnchor()
    {
        return ANCHOR;
    }

    /**
     * Sets the anchor constant.
     * 
     * @param anchor symbolic constant to be used
     */
    public static void setAnchor(int anchor)
    {
        ANCHOR = anchor;
    }

    /**
     * Returns the current used type of stretching for the X-direction. Possible values are NO,
     * RELATIVE and ABSOLUTE.
     * 
     * @return the current used type of stretching for the X-direction
     */
    public static int getXStretchType()
    {
        return X_STRETCH_TYPE;
    }

    /**
     * Sets the type of stretching to be used for the X-Direction. Possible values are NO, RELATIVE
     * and ABSOLUTE.
     * 
     * @param x_stretch constant to be used for stretch type
     */
    public static void setXStretchType(int x_stretch)
    {
        X_STRETCH_TYPE = x_stretch;
    }

    /**
     * Returns the current used type of stretching for the Y-direction. Possible values are NO,
     * RELATIVE and ABSOLUTE.
     * 
     * @return the current used type of stretching for the Y-direction
     */
    public static int getYStretchType()
    {
        return Y_STRETCH_TYPE;
    }

    /**
     * Sets the type of stretching to be used for the Y-Direction. Possible values are NO, RELATIVE
     * and ABSOLUTE.
     * 
     * @param y_stretch constant to be used for stretch type
     */
    public static void setYStretchType(int y_stretch)
    {
        Y_STRETCH_TYPE = y_stretch;
    }

    /**
     * Returns the value which should be used stretching to a full line.
     * 
     * @return the value which should be used stretching to a full line
     */
    public static double getFullLineStretch()
    {
        return FULL_LINE_STRETCH_DEFAULT;
    }

    /**
     * Sets the value which should be used as default for stretching to a full line.
     * 
     * @param fullLineStretch value to be used as full line stretching default
     */
    public static void setFullLineStretch(double fullLineStretch)
    {
        FULL_LINE_STRETCH_DEFAULT = fullLineStretch;

    }

    /**
     * Returns the value which should be used stretching to a full column.
     * 
     * @return the value which should be used stretching to a full column
     */
    public static double getFullColumnStretch()
    {
        return FULL_COLUMN_STRETCH_DEFAULT;
    }

    /**
     * Sets the value which should be used as default for stretching to a full column.
     * 
     * @param fullStretch value to be used as full column stretching default
     */
    public static void setFullColumnStretch(double fullStretch)
    {
        FULL_COLUMN_STRETCH_DEFAULT = fullStretch;

    }

    /**
     * Verifies whether a gap id is valid or not. If the id is less than zero, the sign will be
     * removed. If the id is out of range, an IndexOutOfBoundsException will be thrown. The return
     * value is the verified unsigned id.
     * 
     * @param gapId to be verified
     * @return the verified gap id
     */
    public static int verifyGapId(int gapId)
    {
        if (gapId < 0) gapId = -gapId;
        if (gapId >= DEFAULT_X_GAPS.length)
            throw new IndexOutOfBoundsException("gapId is not in the default gap container.");
        return (gapId);
    }

    /**
     * Returns the default x gap for the given gap id.
     * 
     * @param gapId for which the default x gap should be returned
     * @return the default x gap for the given gap id
     */
    public static int getDefaultXGap(int gapId)
    {
        gapId = verifyGapId(gapId);
        return DEFAULT_X_GAPS[gapId];
    }

    /**
     * Set the gap for the given gap id for the x default gaps.
     * 
     * @param gap to be used as default
     * @param gapId for which the default should be set
     */
    public static void setDefaultXGap(int gap, int gapId)
    {
        gapId = verifyGapId(gapId);
        DEFAULT_X_GAPS[gapId] = gap;
    }

    /**
     * Returns the default y gap for the given gap id.
     * 
     * @param gapId for which the default y gap should be returned
     * @return the default x gap for the given gap id
     */
    public static int getDefaultYGap(int gapId)
    {
        gapId = verifyGapId(gapId);
        return DEFAULT_Y_GAPS[gapId];
    }

    /**
     * Set the gap for the given gap id for the y default gaps.
     * 
     * @param gap to be used as default
     * @param gapId for which the default should be set
     */
    public static void setDefaultYGap(int gap, int gapId)
    {
        gapId = verifyGapId(gapId);
        DEFAULT_Y_GAPS[gapId] = gap;
    }

    /**
     * Returns the default length used by textfields.
     * 
     * @return the default length used by textfields
     */
    public static int getDefaultTextfieldLength()
    {
        return DEFAULT_TEXTFIELD_LENGTH;
    }

    /**
     * Sets the value for the default length of textfields.
     * 
     * @param val to be set as default length for textfields
     */
    public static void setDefaultTextfieldLength(int val)
    {
        DEFAULT_TEXTFIELD_LENGTH = val;
    }

}
