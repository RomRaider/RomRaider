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

/**
 * The constraints class to use with <code>TwoColumnLayout</code>.
 * 
 * @see com.izforge.izpack.gui.TwoColumnLayout
 * 
 * @version 0.0.1 / 11/15/02
 * @author Elmar Grom
 */
public class TwoColumnConstraints implements Cloneable
{

    // these numbers are arbitrary - this way, there's a lower chance
    // of somebody using the number instead of the symbolic name
    public static final int NORTH = 9;

    public static final int WEST = 15;

    public static final int WESTONLY = 16;

    public static final int EAST = 26;

    public static final int EASTONLY = 27;

    public static final int BOTH = 29;

    public static final int LEFT = 31;

    public static final int CENTER = 35;

    public static final int RIGHT = 47;

    /**
     * Indicates where to place the associated component. <code>NORTH</code> will place the
     * component in the title margin. </code>WEST</code> will place the component in the left
     * column and <code>EAST</code> will place it in the right column. If <code>BOTH</code> is
     * used, the component will straddle both columns. <code>WESTONLY</code> and <code>EASTONLY</code>
     * will place the element accordingly but make sure that nothing is placed in the opposite
     * column.
     */
    public int position = WEST;

    /**
     * How to align the associated component, <code>LEFT</code>, <code>CENTER</code> or
     * <code>RIGHT</code>. Note that this setting only taks effect in the component is placed in
     * the title margin.
     */
    public int align = LEFT;

    /** If set to true, the indent setting in the layout manager will be applied. */
    public boolean indent = false;

    /**
     * If set to true the associated component will be allowed to stretch to the width of the entire
     * avaiable space.
     */
    public boolean stretch = false;

    /** for private use by the layout manager */
    Component component = null;

    /**
     * Creates a copy of this two column constraint.
     * 
     * @return a copy of this <code>TwoColumnConstraints</code>
     */
    public Object clone()
    {
        TwoColumnConstraints newObject = new TwoColumnConstraints();

        newObject.position = position;
        newObject.align = align;
        newObject.indent = indent;
        newObject.stretch = stretch;
        newObject.component = component;

        return (newObject);
    }
}
/*---------------------------------------------------------------------------*/
