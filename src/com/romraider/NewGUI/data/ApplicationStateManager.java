/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.romraider.NewGUI.data;

import com.romraider.NewGUI.NewGUI;
import com.romraider.NewGUI.interfaces.TuningEntity;
import com.romraider.NewGUI.interfaces.TuningEntityListener;
import java.util.Iterator;
import java.util.Vector;

public class ApplicationStateManager {

    public static final int USER_LEVEL_1 = 1;
    public static final int USER_LEVEL_2 = 2;
    public static final int USER_LEVEL_3 = 3;
    public static final int USER_LEVEL_4 = 4;
    public static final int USER_LEVEL_5 = 5;

    private static Vector<TuningEntity> tuningEntities = new Vector<TuningEntity>();
    private static TuningEntity currentTuningEntity;
    private static int currentUserLevel = ApplicationStateManager.USER_LEVEL_1;

    private static NewGUI romRaiderInstance = null;

    private static String selectedTuningGroup = "";


    public static Vector<TuningEntity> getTuningEntities() {
        return tuningEntities;
    }

    public static void addTuningEntity(TuningEntity tuningEntity) {
        tuningEntities.add(tuningEntity);
    }

    public static void setCurrentTuningEntity(String entityName, TuningEntityListener listener) {
        // Do nothing if the same entity is specified
        if (currentTuningEntity != null && currentTuningEntity.getName().endsWith(entityName)) {
            return;
        }

        Iterator iterator = tuningEntities.iterator();
        while (iterator.hasNext()) {
            TuningEntity tuningEntity = (TuningEntity) iterator.next();
            if (tuningEntity.getName().equalsIgnoreCase(entityName)) {
                currentTuningEntity = tuningEntity;
                currentTuningEntity.init(listener);
                listener.rebuildJMenuBar(currentTuningEntity.getMenuItems());
                listener.setNewToolBar(currentTuningEntity.getToolBar());
            }
        }
    }

    public static int getCurrentUserLevel() {
        return currentUserLevel;
    }

    public static void setCurrentUserLevel(int currentUserLevel) {
        ApplicationStateManager.currentUserLevel = currentUserLevel;
    }

    public static TuningEntity getCurrentTuningEntity() {
        return currentTuningEntity;
    }

    public static NewGUI getRomRaiderInstance() {
        return romRaiderInstance;
    }

    public static void setRomRaiderInstance(NewGUI romRaiderInstance) {
        ApplicationStateManager.romRaiderInstance = romRaiderInstance;
    }

    public static String getSelectedTuningGroup() {
        return selectedTuningGroup;
    }

    public static void setSelectedTuningGroup(String selectedTuningGroup) {
        ApplicationStateManager.selectedTuningGroup = selectedTuningGroup;
    }
}
