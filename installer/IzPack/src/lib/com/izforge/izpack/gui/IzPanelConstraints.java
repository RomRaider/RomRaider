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
import java.awt.Rectangle;

/**
 * Constraints class for the layout manager <code>IzPanelLayout</code>.
 * 
 */
public class IzPanelConstraints implements Cloneable, LayoutConstants
{

    /**
     * Current defined gaps. Here are the defaults which can be overwritten at the first call to
     * method getGap. The gap type will be determined by the array index and has to be synchron to
     * the gap identifier and the indices of array GAP_NAME_LOOK_UP
     */
    private int xCellAlignment = IzPanelLayout.DEFAULT_X_ALIGNMENT[0];

    private int yCellAlignment = IzPanelLayout.DEFAULT_Y_ALIGNMENT[0];

    private int xPos = 0;

    private int yPos = NEXT_ROW;

    private int xWeight = 1;

    private int yWeight = 1;

    private int xGap = IzPanelLayout.DEFAULT_X_GAPS[-LABEL_GAP];

    private int yGap = IzPanelLayout.DEFAULT_Y_GAPS[-LABEL_GAP];

    private double xStretch = 0.0;

    private double yStretch = 0.0;

    private Rectangle bounds;

    /** for private use by the layout manager */
    Component component = null;

    /** for private use by the layout manager */
    int preferredHeight = 0;
    
    /**
     * Returns the declared stretch value in x direction.
     * 
     * @return the declared stretch value in x direction
     */
    public double getXStretch()
    {
        return xStretch;
    }

    /**
     * Sets the given value as stretch value for x direction.
     * 
     * @param stretch value to be set
     */
    public void setXStretch(double stretch)
    {
        this.xStretch = stretch;
    }

    /**
     * Returns the declared stretch value in y direction.
     * 
     * @return the declared stretch value in y direction
     */
    public double getYStretch()
    {
        return yStretch;
    }

    /**
     * Sets the given value as stretch value for y direction.
     * 
     * @param stretch value to be set
     */
    public void setYStretch(double stretch)
    {
        this.yStretch = stretch;
    }

    /**
     * Returns the declared x gap value.
     * 
     * @return the declared x gap value
     */
    public int getXGap()
    {
        return xGap;
    }

    /**
     * Sets the given value as x gap.
     * 
     * @param gap value to be set
     */
    public void setXGap(int gap)
    {
        xGap = gap;
    }

    /**
     * Returns the declared y gap value.
     * 
     * @return the declared y gap value
     */
    public int getYGap()
    {
        return yGap;
    }

    /**
     * Sets the given value as y gap.
     * 
     * @param gap value to be set
     */
    public void setYGap(int gap)
    {
        yGap = gap;
    }

    /**
     * Constructor with all existent parameters.
     * 
     * @param xCellAlignment value to be used as x alignment
     * @param yCellAlignment value to be used as y alignment
     * @param xPos x position to be used
     * @param yPos y position to be used
     * @param xWeight weight at x direction
     * @param yWeight weight at y direction
     * @param xGap gap for x direction
     * @param yGap gap for y direction
     * @param xStretch stretch value for the x direction
     * @param yStretch stretch value for the y direction
     */
    public IzPanelConstraints(int xCellAlignment, int yCellAlignment, int xPos, int yPos,
            int xWeight, int yWeight, int xGap, int yGap, double xStretch, double yStretch)
    {
        this.xCellAlignment = xCellAlignment;
        this.yCellAlignment = yCellAlignment;
        this.xPos = xPos;
        this.yPos = yPos;
        this.xWeight = xWeight;
        this.yWeight = yWeight;
        setXGap(xGap);
        setYGap(yGap);
        setXStretch(xStretch);
        setYStretch(yStretch);
    }

    /**
     * Default constructor
     */
    public IzPanelConstraints()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        try
        {
            IzPanelConstraints c = (IzPanelConstraints) super.clone();
            return c;
        }
        catch (CloneNotSupportedException e)
        {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    /**
     * Returns the alignment for the x direction.
     * 
     * @return the alignment for the x direction
     */
    public int getXCellAlignment()
    {
        return xCellAlignment;
    }

    /**
     * Sets the alignment for the x direction. Possible values are LEFT, RIGHT and CENTER.
     * 
     * @param cellAlignment to be used
     */
    public void setXCellAlignment(int cellAlignment)
    {
        xCellAlignment = cellAlignment;
    }

    /**
     * Returns the x position (column number).
     * 
     * @return the x position (column number)
     */
    public int getXPos()
    {
        return xPos;
    }

    /**
     * Sets the x position to be used.
     * 
     * @param pos position to be used
     */
    public void setXPos(int pos)
    {
        xPos = pos;
    }

    /**
     * Returns the weight for the x direction. The weight determines how many cells are occupied by
     * the component.
     * 
     * @return the weight for the x direction
     */
    public int getXWeight()
    {
        return xWeight;
    }

    /**
     * Sets the weight value for the x direction.
     * 
     * @param weight to be used for the x direction
     */
    public void setXWeight(int weight)
    {
        xWeight = weight;
    }

    /**
     * Returns the alignment for the y direction.
     * 
     * @return the alignment for the y direction
     */
    public int getYCellAlignment()
    {
        return yCellAlignment;
    }

    /**
     * Sets the alignment for the y direction. Possible values are TOP, BOTTOM and CENTER.
     * 
     * @param cellAlignment to be used
     */
    public void setYCellAlignment(int cellAlignment)
    {
        yCellAlignment = cellAlignment;
    }

    /**
     * Returns the y position (row number).
     * 
     * @return the y position (row number)
     */
    public int getYPos()
    {
        return yPos;
    }

    /**
     * Sets the y position to be used.
     * 
     * @param pos position to be used
     */
    public void setYPos(int pos)
    {
        yPos = pos;
    }

    /**
     * Returns the weight for the y direction. The weight determines how many cells are occupied by
     * the component.
     * 
     * @return the weight for the y direction
     */
    public int getYWeight()
    {
        return yWeight;
    }

    /**
     * Sets the weight value for the y direction.
     * 
     * @param weight to be used for the y direction
     */
    public void setYWeight(int weight)
    {
        yWeight = weight;
    }

    /**
     * Returns the bounds which should be used by the corresponding component. This will be used by
     * the layout manager at a fast layouting.
     * 
     * @return used bounds
     */
    public Rectangle getBounds()
    {
        if (bounds != null) return (Rectangle) (bounds.clone());
        return (new Rectangle());
    }

    /**
     * Sets the bounds which should be used for the component.
     * 
     * @param bounds bounds to be used
     */
    public void setBounds(Rectangle bounds)
    {
        this.bounds = (Rectangle) bounds.clone();
    }
}
