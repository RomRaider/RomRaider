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

import javax.swing.plaf.ColorUIResource;

/**
 * The IzPack Kunststoff L&F theme.
 * 
 * @author Julien Ponge
 */
public class IzPackKMetalTheme extends IzPackMetalTheme
{

    /** Primary color. */
    private final ColorUIResource primary1 = new ColorUIResource(32, 32, 64);

    /** Primary color. */
    private final ColorUIResource primary2 = new ColorUIResource(160, 160, 180);

    /** Primary color. */
    private final ColorUIResource primary3 = new ColorUIResource(200, 200, 224);

    /** Secondary color. */
    private final ColorUIResource secondary1 = new ColorUIResource(130, 130, 130);

    /** Secondary color. */
    private final ColorUIResource secondary2 = new ColorUIResource(180, 180, 180);

    /** Secondary color. */
    private final ColorUIResource secondary3 = new ColorUIResource(224, 224, 224);

    /** The constructor. */
    public IzPackKMetalTheme()
    {
        super();
    }

    /**
     * Returns the wished color.
     * 
     * @return The wished color.
     */
    public ColorUIResource getPrimary1()
    {
        return primary1;
    }

    /**
     * Returns the wished color.
     * 
     * @return The wished color.
     */
    public ColorUIResource getPrimary2()
    {
        return primary2;
    }

    /**
     * Returns the wished color.
     * 
     * @return The wished color.
     */
    public ColorUIResource getPrimary3()
    {
        return primary3;
    }

    /**
     * Returns the wished color.
     * 
     * @return The wished color.
     */
    public ColorUIResource getSecondary1()
    {
        return secondary1;
    }

    /**
     * Returns the wished color.
     * 
     * @return The wished color.
     */
    public ColorUIResource getSecondary2()
    {
        return secondary2;
    }

    /**
     * Returns the wished color.
     * 
     * @return The wished color.
     */
    public ColorUIResource getSecondary3()
    {
        return secondary3;
    }
}
