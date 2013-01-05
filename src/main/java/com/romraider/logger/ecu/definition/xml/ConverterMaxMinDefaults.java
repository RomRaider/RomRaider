/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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

package com.romraider.logger.ecu.definition.xml;

import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;
import java.util.HashMap;
import java.util.Map;

public final class ConverterMaxMinDefaults {
    private static final Map<String, GaugeMinMax> DEFAULTS = new HashMap<String, GaugeMinMax>();
    private static final double MIN_DEFAULT = 0.0;
    private static final double MAX_DEFAULT = 100.0;
    private static final double STEP_DEFAULT = 10.0;
    private static final GaugeMinMax DEFAULT = new GaugeMinMax(MIN_DEFAULT, MAX_DEFAULT, STEP_DEFAULT);

    static {
        add("%", 0.0, 100.0, 10.0);
        add("f", 0.0, 400.0, 40.0);
        add("c", -20.0, 200.0, 20.0);
        add("psi", -20.0, 40.0, 5.0);
        add("bar", -1.5, 3.0, 0.5);
        add("rpm", 0, 8000, 1000.0);
        add("mph", 0.0, 200.0, 20.0);
        add("km/h", 0.0, 300.0, 20.0);
        add("degrees", -15, 60.0, 5.0);
        add("g/s", 0.0, 400.0, 20.0);
        add("v", 0.0, 5.0, 0.5);
        add("ms", 0.0, 100.0, 10.0);
        add("a", 0.0, 20.0, 5.0);
        add("ma", 0.0, 100.0, 10.0);
        add("steps", 0.0, 100.0, 10.0);
        add("ohms", 0.0, 100.0, 10.0);
        add("afr", 10.0, 20.0, 1.0);
        add("lambda", 0.5, 1.5, 0.1);
        add("gear", 0.0, 6.0, 1.0);
        add("misfire count", 0.0, 20.0, 5.0);
        add("MPa", 0.0, 0.5, 0.1);
        add("2*g/rev", 0.0, 8.0, 1.0);
        add("g/rev", 0.0, 4.0, 0.5);
        add("g/cyl", 0.0, 2.0, 0.5);
        add("multiplier", 0.0, 1.0, 0.1);
        add("raw ecu value", 0.0, 16.0, 1.0);
        add("status", 0.0, 10.0, 1.0);
        add("mmHg", 0.0, 2000.0, 100.0);
    }

    public static double getMin(String units) {
        String key = units.toLowerCase();
        if (!DEFAULTS.containsKey(key)) return MIN_DEFAULT;
        return DEFAULTS.get(key).min;
    }

    public static double getMax(String units) {
        String key = units.toLowerCase();
        if (!DEFAULTS.containsKey(key)) return MAX_DEFAULT;
        return DEFAULTS.get(key).max;
    }

    public static double getStep(String units) {
        String key = units.toLowerCase();
        if (!DEFAULTS.containsKey(key)) return STEP_DEFAULT;
        return DEFAULTS.get(key).step;
    }

    public static GaugeMinMax getDefault() {
        return DEFAULT;
    }

    public static GaugeMinMax getMaxMin(String units) {
        double min = getMin(units);
        double max = getMax(units);
        double step = getStep(units);
        return new GaugeMinMax(min, max, step);
    }

    private static void add(String units, double min, double max, double step) {
        String key = units.toLowerCase();
        GaugeMinMax value = new GaugeMinMax(min, max, step);
        DEFAULTS.put(key, value);
    }
}
