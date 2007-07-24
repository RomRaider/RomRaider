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
package com.izforge.izpack.installer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager2;

import javax.swing.JComponent;

import com.izforge.izpack.gui.IzPanelConstraints;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LayoutConstants;
import com.izforge.izpack.installer.IzPanel.Filler;

/**
 * This class manages the layout for IzPanels. The layout related methods in IzPanel delegates the
 * work to this class. Use the layout helper directly because the delegating methods in IzPanel will
 * be removed in the future.<br>
 * This layout helper works with a GridBagLayout or a IzPanelLayout as layout manager. The layout
 * manager has to be set at calling the method <code>startLayout</code>. This method has to be
 * called before the first add of a component to the IzPanel.<br>
 * 
 * 
 * @author Klaus Bartz
 * 
 */
public class LayoutHelper implements LayoutConstants
{

    JComponent parent;

    /** Indicates whether grid bag layout was started or not */
    protected boolean layoutStarted = false;

    /** The default grid bag constraint. */
    protected Object defaultConstraints;

    /** Current x position of grid. */
    protected int gridxCounter = -1;

    /** Current y position of grid. */
    protected int gridyCounter = -1;

    /** internal layout */
    protected LayoutManager2 izPanelLayout;

    /**
     * Layout anchor declared in the xml file with the guiprefs modifier "layoutAnchor"
     */
    protected static int ANCHOR = -1;

    protected static int X_STRETCH_TYPE = -1;

    protected static int Y_STRETCH_TYPE = -1;

    protected static double FULL_LINE_STRETCH_DEFAULT = -1.0;

    protected static double FULL_COLUMN_STRETCH_DEFAULT = -1.0;

    protected static Double INITIAL_STRETCH_DEFAULT = new Double(1.0);

    protected static Double DOUBLE_ZERO = new Double(0.0);

    /**
     * Look-up table for gap identifier to gap names for the x direction. The gap names can be used
     * in the XML installation configuration file. Be aware that case sensitivity should be used.
     */
    public final static String[] X_GAP_NAME_LOOK_UP = { "INTERNAL_USED", "labelXGap", "textXGab",
            "controlXGap", "paragraphXGap", "labelToTextXGap", "labelToControlXGap",
            "textToLabelXGap", "controlToLabelXGap", "controlToTextXGap", "textToControlXGap",
            "firstXGap", "INTERNAL_USED", "INTERNAL_USED", "filler1XGap", "filler2XGap",
            "filler3XGap", "filler4XGap", "filler5XGap"};

    /**
     * Look-up table for gap identifier to gap names for the y direction. The gap names can be used
     * in the XML installation configuration file. Be aware that case sensitivity should be used.
     */
    public final static String[] Y_GAP_NAME_LOOK_UP = { "INTERNAL_USED", "labelYGap", "textYGab",
            "controlYGap", "paragraphYGap", "labelToTextYGap", "labelToControlYGap",
            "textToLabelYGap", "controlToLabelYGap", "controlToTextYGap", "textToControlYGap",
            "firstYGap", "INTERNAL_USED", "INTERNAL_USED", "filler1YGap", "filler2YGap",
            "filler3YGap", "filler4YGap", "filler5YGap"};

    /** Identifier of x gap for all default x gaps. */
    public final static String ALL_X_GAP = "allXGap";

    /** Identifier of x gap for all default y gaps. */
    public final static String ALL_Y_GAP = "allYGap";

    /**
     * Only useable constructor. Creates a layout manager for special purpose.
     * 
     * @param parent for which this layout manager will be used
     */
    public LayoutHelper(JComponent parent)
    {
        this();
        this.parent = parent;
        izPanelLayout = new GridBagLayout();
        parent.setLayout(izPanelLayout);
        gridyCounter++;
    }

    /**
     * The default constructor is only useable by derived classes.
     */
    protected LayoutHelper()
    {
        super();
    }

    /**
     * Returns whether the used layout is a GridBagLayout or not.
     * 
     * @return whether the used layout is a GridBagLayout or not
     */
    private boolean isGridBag()
    {
        return (izPanelLayout instanceof GridBagLayout);
    }

    /**
     * Returns whether the used layout is an IzPanelLayout or not.
     * 
     * @return whether the used layout is an IzPanelLayout or not
     */
    private boolean isIzPanel()
    {
        return (izPanelLayout instanceof IzPanelLayout);
    }

    // ------------------- Common Layout stuff -------------------- START ---

    /**
     * Start layout determining. If it is needed, a dummy component will be created as first row.
     * This will be done, if the IzPack guiprefs modifier with the key "layoutAnchor" has the value
     * "SOUTH" or "SOUTHWEST". The earlier used value "BOTTOM" and the declaration via the IzPack
     * variable <code>IzPanel.LayoutType</code> are also supported.
     * 
     * @param layout layout to be used by this layout helper
     */
    public void startLayout(LayoutManager2 layout)
    {
        if (layoutStarted) return;
        izPanelLayout = layout;
        if (isGridBag())
        {
            startGridBagLayout();
            return;
        }
        // TODO: impl for IzPanelLayout
        if (isIzPanel()) startIzPanelLayout();
    }

    /**
     * Special start method for IzPanelLayout. Called from <code>startLayout</code>.
     */
    private void startIzPanelLayout()
    {
        IzPanelLayout.setAnchor(getAnchor());
        IzPanelLayout.setXStretchType(getXStretchType());
        IzPanelLayout.setYStretchType(getYStretchType());
        IzPanelLayout.setFullLineStretch(getFullLineStretch());
        IzPanelLayout.setFullColumnStretch(getFullColumnStretch());
        getXGap(LABEL_GAP); // This call triggers resolving external setting if not already done.
        getYGap(LABEL_GAP); // This call triggers resolving external setting if not already done.
        parent.setLayout(izPanelLayout);
        // parent.add(IzPanelLayout.createGap(TOP_GAP, VERTICAL));
    }

    /**
     * Complete layout determining. If it is needed, a dummy component will be created as last row.
     * This will be done, if the IzPack guiprefs modifier with the key "layoutAnchor" has the value
     * "NORTH" or "NORTHWEST". The earlier used value "TOP" and the declaration via the IzPack
     * variable <code>IzPanel.LayoutType</code> are also supported.
     */
    public void completeLayout()
    {
        if (isGridBag())
        {
            completeGridBagLayout();
            return;
        }
        // TODO: impl for IzPanelLayout
    }

    /**
     * Returns the default constraints of this panel.
     * 
     * @return the default constraints of this panel
     */
    public Object getDefaultConstraints()
    {
        startLayout(izPanelLayout);
        return defaultConstraints;
    }

    /**
     * Sets the default constraints of this panel to the given object.
     * 
     * @param constraints which should be set as default for this object
     */
    public void setDefaultConstraints(Object constraints)
    {

        startLayout(izPanelLayout);
        if ((isGridBag() && !(constraints instanceof GridBagConstraints))
                || (isIzPanel() && !(constraints instanceof IzPanelConstraints)))
            throw new IllegalArgumentException(
                    "Layout and constraints have to be from the same type.");
        defaultConstraints = constraints;
    }

    /**
     * Resets the grid counters which are used at getNextXConstraints and getNextYConstraints.
     */
    public void resetGridCounter()
    {
        gridxCounter = -1;
        gridyCounter = -1;
    }

    /**
     * Returns a newly created constraints with the given values and the values from the default
     * constraints for the other parameters.
     * 
     * @param gridx value to be used for the new constraint
     * @param gridy value to be used for the new constraint
     * @return newly created constraints with the given values and the values from the default
     * constraints for the other parameters
     */
    public Object getNewConstraints(int gridx, int gridy)
    {
        if (isGridBag())
        {
            GridBagConstraints retval = (GridBagConstraints) ((GridBagConstraints) getDefaultConstraints())
                    .clone();
            retval.gridx = gridx;
            retval.gridy = gridy;
            return (retval);
        }
        if (isIzPanel())
        {
            IzPanelConstraints retval = (IzPanelConstraints) ((IzPanelConstraints) getDefaultConstraints())
                    .clone();
            retval.setXPos(gridx);
            retval.setYPos(gridy);
            return (retval);
        }
        return (null);
    }

    /**
     * Returns a newly created constraints with the given values and the values from the
     * defaultGridBagConstraints for the other parameters.
     * 
     * @param gridx value to be used for the new constraint
     * @param gridy value to be used for the new constraint
     * @param gridwidth value to be used for the new constraint
     * @param gridheight value to be used for the new constraint
     * @return newly created constraints with the given values and the values from the default
     * constraints for the other parameters
     */
    public Object getNewConstraints(int gridx, int gridy, int gridwidth, int gridheight)
    {
        Object retval = getNewConstraints(gridx, gridy);
        if (isGridBag())
        {
            GridBagConstraints gbc = (GridBagConstraints) retval;
            gbc.gridwidth = gridwidth;
            gbc.gridheight = gridheight;
        }
        if (isIzPanel())
        {
            IzPanelConstraints gbc = (IzPanelConstraints) retval;
            gbc.setXWeight(gridwidth);
            gbc.setYWeight(gridheight);
        }
        return (retval);
    }

    /**
     * Returns a newly created constraints for the next column of the current layout row.
     * 
     * @return a newly created constraints for the next column of the current layout row
     * 
     */
    public Object getNextXConstraints()
    {
        gridxCounter++;
        return (getNewConstraints(gridxCounter, gridyCounter));
    }

    /**
     * Returns a newly created constraints with column 0 for the next row.
     * 
     * @return a newly created constraints with column 0 for the next row
     * 
     */
    public Object getNextYConstraints()
    {
        gridyCounter++;
        gridxCounter = 0;
        return (getNewConstraints(0, gridyCounter));
    }

    /**
     * Returns a newly created constraints with column 0 for the next row using the given
     * parameters.
     * 
     * @param gridwidth width for this constraint
     * @param gridheight height for this constraint
     * @return a newly created constraints with column 0 for the next row using the given parameters
     */
    public Object getNextYConstraints(int gridwidth, int gridheight)
    {
        gridyCounter++;
        gridxCounter = 0;
        return (getNewConstraints(0, gridyCounter, gridwidth, gridheight));
    }

    // ------------------- Common Layout stuff -------------------- END ---

    // ------------------- GridBag Layout stuff -------------------- START ---
    /**
     * Start layout determining. If it is needed, a dummy component will be created as first row.
     * This will be done, if the IzPack guiprefs modifier with the key "layoutAnchor" has the value
     * "SOUTH" or "SOUTHWEST". The earlier used value "BOTTOM" and the declaration via the IzPack
     * variable <code>IzPanel.LayoutType</code> are also supported.
     */
    private void startGridBagLayout()
    {
        if (layoutStarted) return;
        layoutStarted = true;
        if (izPanelLayout == null || !(izPanelLayout instanceof GridBagLayout))
            izPanelLayout = new GridBagLayout();
        GridBagConstraints dgbc = new GridBagConstraints();
        dgbc.insets = new Insets(0, 0, getYGap(LABEL_GAP), 0);
        dgbc.anchor = GridBagConstraints.WEST;
        defaultConstraints = dgbc;
        parent.setLayout(izPanelLayout);
        switch (getAnchor())
        {
        case SOUTH:
        case SOUTH_WEST:
            // Make a header to push the rest to the bottom.
            Filler dummy = new Filler();
            GridBagConstraints gbConstraint = (GridBagConstraints) getNextYConstraints();
            gbConstraint.weighty = 1.0;
            gbConstraint.fill = GridBagConstraints.BOTH;
            gbConstraint.anchor = GridBagConstraints.WEST;
            parent.add(dummy, gbConstraint);
            break;
        default:
            break;
        }
        // TODO: impl for layout type CENTER, ...
    }

    /**
     * Complete layout determining. If it is needed, a dummy component will be created as last row.
     * This will be done, if the IzPack guiprefs modifier with the key "layoutAnchor" has the value
     * "NORTH" or "NORTHWEST". The earlier used value "TOP" and the declaration via the IzPack
     * variable <code>IzPanel.LayoutType</code> are also supported.
     */
    private void completeGridBagLayout()
    {
        switch (getAnchor())
        {
        case NORTH:
        case NORTH_WEST:
            // Make a footer to push the rest to the top.
            Filler dummy = new Filler();
            GridBagConstraints gbConstraint = (GridBagConstraints) getNextYConstraints();
            gbConstraint.weighty = 1.0;
            gbConstraint.fill = GridBagConstraints.BOTH;
            gbConstraint.anchor = GridBagConstraints.WEST;
            parent.add(dummy, gbConstraint);
            break;
        default:
            break;
        }
    }

    /**
     * Returns the anchor as value declared in GridBagConstraints. Possible are NORTH, NORTHWEST,
     * SOUTH, SOUTHWEST and CENTER. The values can be configured in the xml description file with
     * the variable "IzPanel.LayoutType". The old values "TOP" and "BOTTOM" from the xml file are
     * mapped to NORTH and SOUTH.
     * 
     * @return the anchor defined in the IzPanel.LayoutType variable.
     */
    public static int getAnchor()
    {
        if (ANCHOR >= 0) return (ANCHOR);
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String todo;
        if (idata instanceof InstallData
                && ((InstallData) idata).guiPrefs.modifier.containsKey("layoutAnchor"))
            todo = (String) ((InstallData) idata).guiPrefs.modifier.get("layoutAnchor");
        else
            todo = idata.getVariable("IzPanel.LayoutType");
        if (todo == null) // No command, no work.
            ANCHOR = CENTER;
        else if ("EAST".equalsIgnoreCase(todo))
            ANCHOR = EAST;
        else if ("WEST".equalsIgnoreCase(todo))
            ANCHOR = WEST;
        else if ("TOP".equalsIgnoreCase(todo) || "NORTH".equalsIgnoreCase(todo))
            ANCHOR = NORTH;
        else if ("BOTTOM".equalsIgnoreCase(todo) || "SOUTH".equalsIgnoreCase(todo))
            ANCHOR = SOUTH;
        else if ("SOUTHWEST".equalsIgnoreCase(todo) || "SOUTH_WEST".equalsIgnoreCase(todo))
            ANCHOR = SOUTH_WEST;
        else if ("SOUTHEAST".equalsIgnoreCase(todo) || "SOUTH_EAST".equalsIgnoreCase(todo))
            ANCHOR = SOUTH_EAST;
        else if ("NORTHWEST".equalsIgnoreCase(todo) || "NORTH_WEST".equalsIgnoreCase(todo))
            ANCHOR = NORTH_WEST;
        else if ("NORTHEAST".equalsIgnoreCase(todo) || "NORTH_EAST".equalsIgnoreCase(todo))
            ANCHOR = NORTH_EAST;
        else if ("CENTER".equalsIgnoreCase(todo)) ANCHOR = CENTER;
        return (ANCHOR);
    }

    /**
     * Returns the gap which should be used between the given gui objects for the x direction. The
     * value will be configurable by guiprefs modifiers. Valid values are all entries in the static
     * String array X_GAP_NAME_LOOK_UP of this class. There are constant ints for the indexes of
     * this array.
     * 
     * @param gapId index in array GAP_NAME_LOOK_UP for the needed gap
     * 
     * @return the gap depend on the xml-configurable guiprefs modifier
     */
    public static int getXGap(int gapId)
    {
        gapId = IzPanelLayout.verifyGapId(gapId);
        if (IzPanelLayout.getDefaultXGap(GAP_LOAD_MARKER) >= 0)
            return (IzPanelLayout.getDefaultXGap(gapId));
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        if (!(idata instanceof InstallData)) return (IzPanelLayout.getDefaultXGap(gapId));
        String var = null;
        InstallData id = (InstallData) idata;
        int commonDefault = -1;
        if (id.guiPrefs.modifier.containsKey(ALL_X_GAP))
        {
            try
            {
                commonDefault = Integer.parseInt((String) id.guiPrefs.modifier.get(ALL_X_GAP));
            }
            catch (NumberFormatException nfe)
            {
                // Do nothing else use the default value.
                // Need to set it again at this position??
            }

        }
        for (int i = 0; i < X_GAP_NAME_LOOK_UP.length; ++i)
        {
            int currentDefault = 0;
            if (commonDefault >= 0)
            {
                currentDefault = commonDefault;
            }
            else
            {
                var = (String) id.guiPrefs.modifier.get(X_GAP_NAME_LOOK_UP[i]);
                if (var != null)
                {
                    try
                    {
                        currentDefault = Integer.parseInt(var);
                    }
                    catch (NumberFormatException nfe)
                    {
                        // Do nothing else use the default value.
                        // Need to set it again at this position??
                    }
                }
            }
            IzPanelLayout.setDefaultXGap(currentDefault, i);
        }
        IzPanelLayout.setDefaultXGap(0, GAP_LOAD_MARKER); // Mark external settings allready
        // loaded.
        return (IzPanelLayout.getDefaultXGap(gapId));
    }

    /**
     * Returns the gap which should be used between the given gui objects for the y direction. The
     * value will be configurable by guiprefs modifiers. Valid values are all entries in the static
     * String array Y_GAP_NAME_LOOK_UP of this class. There are constant ints for the indexes of
     * this array.
     * 
     * @param gapId index in array GAP_NAME_LOOK_UP for the needed gap
     * 
     * @return the gap depend on the xml-configurable guiprefs modifier
     */
    public static int getYGap(int gapId)
    {
        gapId = IzPanelLayout.verifyGapId(gapId);
        if (IzPanelLayout.getDefaultYGap(GAP_LOAD_MARKER) >= 0)
            return (IzPanelLayout.getDefaultYGap(gapId));
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        if (!(idata instanceof InstallData)) return (IzPanelLayout.getDefaultYGap(gapId));
        String var = null;
        InstallData id = (InstallData) idata;
        int commonDefault = -1;
        if (id.guiPrefs.modifier.containsKey(ALL_Y_GAP))
        {
            try
            {
                commonDefault = Integer.parseInt((String) id.guiPrefs.modifier.get(ALL_Y_GAP));
            }
            catch (NumberFormatException nfe)
            {
                // Do nothing else use the default value.
                // Need to set it again at this position??
            }

        }
        for (int i = 0; i < Y_GAP_NAME_LOOK_UP.length; ++i)
        {
            int currentDefault = 0;
            if (commonDefault >= 0)
            {
                currentDefault = commonDefault;
            }
            else
            {
                var = (String) id.guiPrefs.modifier.get(Y_GAP_NAME_LOOK_UP[i]);
                if (var != null)
                {
                    try
                    {
                        currentDefault = Integer.parseInt(var);
                    }
                    catch (NumberFormatException nfe)
                    {
                        // Do nothing else use the default value.
                        // Need to set it again at this position??
                    }
                }
            }
            IzPanelLayout.setDefaultYGap(currentDefault, i);
        }
        IzPanelLayout.setDefaultYGap(0, GAP_LOAD_MARKER); // Mark external settings allready
        // loaded.
        return (IzPanelLayout.getDefaultYGap(gapId));
    }

    /**
     * Returns the used stretch type for the x direction. Possible are NO_STRETCH, RELATIVE_STRETCH
     * and ABSOLUTE_STRETCH. The stretch type will be used at rows where one or more components has
     * a stretch value greater than 0.0 in the constraints. If NO_STRETCH is used, no stretch will
     * be performed. If ABSOLUTE_STRETCH is used, parts of the unused area are given to the
     * components depending on the unmodified stretch value. At RELATIVE_STRETCH first the hole
     * stretch for a row will be computed. Relative to this value the unused area will be splited.<br>
     * The default type is ABSOLUTE_STRETCH. With the modifier "layoutXStretchType" of the "info"
     * section of the installation configuration file this can be changed.
     * 
     * @return used stretch type
     */
    public static int getXStretchType()
    {
        if (X_STRETCH_TYPE > -1) return (X_STRETCH_TYPE);
        X_STRETCH_TYPE = ABSOLUTE_STRETCH;
        String var = ((String) getModifierValue(null, "RELATIVE_STRETCH", null,
                "layoutXStretchType"));
        if (var != null)
        {
            if ("RELATIVE_STRETCH".equalsIgnoreCase(var) || "RELATIVE".equalsIgnoreCase(var))
                X_STRETCH_TYPE = RELATIVE_STRETCH;
            else if ("ABSOLUTE_STRETCH".equalsIgnoreCase(var) || "ABSOLUTE".equalsIgnoreCase(var))
                X_STRETCH_TYPE = ABSOLUTE_STRETCH;
            else if ("NO_STRETCH".equalsIgnoreCase(var) || "NO".equalsIgnoreCase(var))
                X_STRETCH_TYPE = NO_STRETCH;
        }
        return (X_STRETCH_TYPE);
    }

    /**
     * Returns the used stretch type for the y direction. Possible are NO_STRETCH, RELATIVE_STRETCH
     * and ABSOLUTE_STRETCH. The stretch type will be used at rows where one or more components has
     * a stretch value greater than 0.0 in the constraints. If NO_STRETCH is used, no stretch will
     * be performed. If ABSOLUTE_STRETCH is used, parts of the unused area are given to the
     * components depending on the unmodified stretch value. At RELATIVE_STRETCH first the hole
     * stretch for a row will be computed. Relative to this value the unused area will be splited.<br>
     * The default type is ABSOLUTE_STRETCH. With the modifier "layoutYStretchType" of the "info"
     * section of the installation configuration file this can be changed.
     * 
     * @return used stretch type
     */
    public static int getYStretchType()
    {
        if (Y_STRETCH_TYPE > -1) return (Y_STRETCH_TYPE);
        Y_STRETCH_TYPE = ABSOLUTE_STRETCH;
        String var = ((String) getModifierValue(null, "RELATIVE_STRETCH", null,
                "layoutYStretchType"));
        if (var != null)
        {
            if ("RELATIVE_STRETCH".equalsIgnoreCase(var) || "RELATIVE".equalsIgnoreCase(var))
                Y_STRETCH_TYPE = RELATIVE_STRETCH;
            else if ("ABSOLUTE_STRETCH".equalsIgnoreCase(var) || "ABSOLUTE".equalsIgnoreCase(var))
                Y_STRETCH_TYPE = ABSOLUTE_STRETCH;
            else if ("NO_STRETCH".equalsIgnoreCase(var) || "NO".equalsIgnoreCase(var))
                Y_STRETCH_TYPE = NO_STRETCH;
        }
        return (Y_STRETCH_TYPE);
    }

    /**
     * Returns the default value for stretching to a full line. With the modifier
     * "layoutFullLineStretch" of the "info" section of the installation configuration file this can
     * be changed. Valid are doubles for the value. This setting is possible to give panels a chance
     * to center the controls in x direction also a control uses stretching.
     * 
     * @return the default value for stretching to a full line
     */

    public static double getFullLineStretch()
    {
        FULL_LINE_STRETCH_DEFAULT = ((Double) getModifierValue(
                new Double(FULL_LINE_STRETCH_DEFAULT), INITIAL_STRETCH_DEFAULT, DOUBLE_ZERO,
                "layoutFullLineStretch")).doubleValue();
        return (FULL_LINE_STRETCH_DEFAULT);
    }

    /**
     * Returns the default value for stretching to a full column. With the modifier
     * "layoutFullColumnStretch" of the "info" section of the installation configuration file this
     * can be changed. Valid are doubles for the value. This setting is possible to give panels a
     * chance to center the controls in y direction also a control uses stretching.
     * 
     * @return the default value for stretching to a full column
     */

    public static double getFullColumnStretch()
    {
        FULL_COLUMN_STRETCH_DEFAULT = ((Double) getModifierValue(new Double(
                FULL_COLUMN_STRETCH_DEFAULT), INITIAL_STRETCH_DEFAULT, DOUBLE_ZERO,
                "layoutFullColumnStretch")).doubleValue();
        return (FULL_COLUMN_STRETCH_DEFAULT);
    }

    private static Object getModifierValue(Object currentVal, Object defaultVal, Object readLimit,
            String key)
    {
        if (defaultVal instanceof Integer)
            if (((Integer) currentVal).intValue() >= ((Integer) readLimit).intValue())
                return (currentVal);
        if (defaultVal instanceof Double)
        {
            if (((Double) currentVal).doubleValue() >= ((Double) readLimit).doubleValue())
                return (currentVal);
        }
        Object retval = defaultVal;
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        if (!(idata instanceof InstallData)) return (retval);
        String var = null;
        if (((InstallData) idata).guiPrefs.modifier.containsKey(key))
        {
            var = (String) ((InstallData) idata).guiPrefs.modifier.get(key);
            if (var != null)
            {
                try
                {
                    if (defaultVal instanceof Integer) return (new Integer(Integer.parseInt(var)));
                    if (defaultVal instanceof Double) { return (new Double(Double.parseDouble(var))); }
                    return (var);
                }
                catch (NumberFormatException nfe)
                {
                    // Do nothing else use the default value.
                    // Need to set it again at this position??
                }
            }
        }
        return (retval);
    }

    /**
     * Returns the layout manager which current used by this layout helper. The layout manager
     * implements LayoutManager2. It can be a GridBagLayout or a IzPanelLayout.
     * 
     * @return current used layout manager
     */
    public LayoutManager2 getLayout()
    {
        return izPanelLayout;
    }

    /**
     * Sets the given layout manager for this layout helper to be used.
     * 
     * @param izPanelLayout layout manager to be used
     */
    public void setLayout(LayoutManager2 izPanelLayout)
    {
        this.izPanelLayout = izPanelLayout;
    }

}
