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

package com.romraider.logger.car.util;

import static com.romraider.logger.car.util.Constants.IMPERIAL_UNIT;
import static com.romraider.logger.car.util.Constants.KPH_2_MPH;
import static com.romraider.logger.car.util.Constants.METRIC_UNIT;

public class SpeedCalculator {
    public static double calculateMph(double rpm, double ratio) {
        return (rpm / ratio);
    }

    public static double calculateKph(double rpm, double ratio) {
        return calculateMph(rpm, ratio) * KPH_2_MPH;
    }

    public static double calculateRpm(double vs, double ratio, String units) {
        double rpm = 0;
        if (units.equalsIgnoreCase(IMPERIAL_UNIT)) rpm = (vs * ratio);
        if (units.equalsIgnoreCase(METRIC_UNIT)) rpm = (vs * ratio / KPH_2_MPH);
        return rpm;
    }

}
