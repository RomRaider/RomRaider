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

import javax.swing.SwingConstants;

/**
 * Interface with some constants used by or for the IzPanelLayout.
 * 
 * @author Klaus Bartz
 * 
 */
public interface LayoutConstants extends SwingConstants
{

    /** Identifier for gaps between labels. */
    final static int LABEL_GAP = -1;

    /** Identifier for gaps between text fields. */
    final static int TEXT_GAP = -2;

    /** Identifier for gaps between controls like radio buttons/groups. */
    final static int CONTROL_GAP = -3;

    /** Identifier for gaps between paragraphs. */
    final static int PARAGRAPH_GAP = -4;

    /** Identifier for gaps between labels and text fields. */
    final static int LABEL_TO_TEXT_GAP = -5;

    /** Identifier for gaps between labels and controls like radio buttons/groups. */
    final static int LABEL_TO_CONTROL_GAP = -6;

    /** Identifier for gaps between text fields and labels. */
    final static int TEXT_TO_LABEL_GAP = -7;

    /** Identifier for gaps between controls like radio buttons/groups and labels. */
    final static int CONTROL_TO_LABEL_GAP = -8;

    /** Identifier for gaps between controls like radio buttons/groups and text components. */
    final static int CONTROL_TO_TEXT_GAP = -9;

    /** Identifier for gaps between text components and controls like radio buttons/groups and labels */
    final static int TEXT_TO_CONTROL_GAP = -10;

    /** Identifier for gaps between panel top and the first control. */
    final static int TOP_GAP = -11;

    /** Identifier for all gaps. */
    final static int ALL_GAP = -12;

    /** Identifier for gaps for filler. */
    final static int NO_GAP = -13;

    /** Identifier for gaps for filler. */
    final static int FILLER1_GAP = -14;

    /** Identifier for gaps for filler. */
    final static int FILLER2_GAP = -15;

    /** Identifier for gaps for filler. */
    final static int FILLER13_GAP = -16;

    /** Identifier for gaps for filler. */
    final static int FILLER4_GAP = -17;

    /** Identifier for gaps for filler. */
    final static int FILLER5_GAP = -18;

    /** Identifier for gaps to be evaluated automatically at a late time. */
    final static int AUTOMATIC_GAP = -19;

    /** Identifier for gaps load marker. */
    final static int GAP_LOAD_MARKER = 0;

    /** Identifier for relative row positioning (next). */
    public static final int NEXT_ROW = -1;

    /** Identifier for relative row positioning (current). */
    public static final int CURRENT_ROW = -2;

    /** Identifier for relative column positioning (next). */
    public static final int NEXT_COLUMN = -1;

    /** Identifier for relative column positioning (current). */
    public static final int CURRENT_COLUMN = -2;

    /**
     * Identifier for using the default alignment defined for labels. The value will be resolved at
     * layouting, therefore it is possible to change the default values in </code>IzPanelConstraints</code>.
     */
    public static final int DEFAULT_LABEL_ALIGNMENT = -1;

    /**
     * Identifier for using the default alignment defined for text fields. The value will be
     * resolved at layouting, therefore it is possible to change the default values in </code>IzPanelConstraints</code>.
     */
    public static final int DEFAULT_TEXT_ALIGNMENT = -2;

    /**
     * Identifier for using the default alignment defined for other controls. The value will be
     * resolved at layouting, therefore it is possible to change the default values in </code>IzPanelConstraints</code>.
     */
    public static final int DEFAULT_CONTROL_ALIGNMENT = -3;

    /** Identifier for the default label constraints. */
    public static final int LABEL_CONSTRAINT = 0;

    /** Identifier for the default text component constraints. */
    public static final int TEXT_CONSTRAINT = 1;

    /** Identifier for the default for other control constraints. */
    public static final int CONTROL_CONSTRAINT = 2;

    /** Identifier for the default multi line label constraints. */
    public static final int FULL_LINE_COMPONENT_CONSTRAINT = 3;

    /**
     * Identifier for the default constraints for controls/container which are variable in x and y
     * dimension.
     */
    public static final int XY_VARIABLE_CONSTRAINT = 4;

    /** Identifier for the default constraint used by a filler with x direction. */
    public static final int XDUMMY_CONSTRAINT = 5;

    /** Identifier for the default constraint used by a filler with y direction. */
    public static final int YDUMMY_CONSTRAINT = 6;

    /** Identifier for the default constraint used by a filler with y direction. */
    public static final int FULL_LINE_CONTROL_CONSTRAINT = 7;

    /** Constant used to specify that no action should be done. Useable for X- and Y_STRETCH. */
    public static final int NO_STRETCH = 0;

    /** X- and Y_STRETCH constant used to specify relative weighting of stretch factors. */
    public static final int RELATIVE_STRETCH = 1;

    /** X- and Y_STRETCH constant used to specify absolute weighting of stretch factors. */
    public static final int ABSOLUTE_STRETCH = 2;

    /**
     * X_STRETCH "symbolic" value to signal that the configurable default stretch value should be
     * used.
     */
    public static final double FULL_LINE_STRETCH = -1.0;

    /**
     * Y_STRETCH "symbolic" value to signal that the configurable default stretch value should be
     * used.
     */
    public static final double FULL_COLUMN_STRETCH = -2.0;

    /**
     * Object constant used as "constraint" at add a component to the IzPanel to signal that this
     * component should be place in the next line.
     */
    public static final String NEXT_LINE = "nextLine";

    /**
     * Flag used in the IzPanelLayout manager to trigger expansion of all components to the netto
     * width of a column.
     */
    public static final int NO_FILL_OUT_COLUMN = 0;

    /**
     * Flag used in the IzPanelLayout manager to trigger expansion of all components to the netto
     * width of a column.
     */
    public static final int FILL_OUT_COLUMN_WIDTH = 1;

    /**
     * Flag used in the IzPanelLayout manager to trigger expansion of all components to the netto
     * width of a column.
     */
    public static final int FILL_OUT_COLUMN_HEIGHT = 2;

    /**
     * Flag used in the IzPanelLayout manager to trigger expansion of all components to the netto
     * width of a column.
     */
    public static final int FILL_OUT_COLUMN_SIZE = 3;

}
