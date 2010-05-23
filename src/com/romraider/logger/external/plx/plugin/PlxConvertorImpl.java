/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2010 RomRaider.com
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

package com.romraider.logger.external.plx.plugin;

import com.romraider.logger.external.plx.io.PlxSensorType;
import static com.romraider.logger.external.plx.io.PlxSensorType.EXHAUST_GAS_TEMPERATURE;
import static com.romraider.logger.external.plx.io.PlxSensorType.WIDEBAND_AFR;
import com.romraider.logger.external.plx.io.PlxSensorUnits;
import static com.romraider.logger.external.plx.io.PlxSensorUnits.EXHAUST_GAS_TEMPERATURE_CELSIUS;
import static com.romraider.logger.external.plx.io.PlxSensorUnits.EXHAUST_GAS_TEMPERATURE_FAHRENHEIT;
import static com.romraider.logger.external.plx.io.PlxSensorUnits.WIDEBAND_AFR_CNG172;
import static com.romraider.logger.external.plx.io.PlxSensorUnits.WIDEBAND_AFR_DIESEL146;
import static com.romraider.logger.external.plx.io.PlxSensorUnits.WIDEBAND_AFR_ETHANOL90;
import static com.romraider.logger.external.plx.io.PlxSensorUnits.WIDEBAND_AFR_GASOLINE147;
import static com.romraider.logger.external.plx.io.PlxSensorUnits.WIDEBAND_AFR_LAMBDA;
import static com.romraider.logger.external.plx.io.PlxSensorUnits.WIDEBAND_AFR_LPG155;
import static com.romraider.logger.external.plx.io.PlxSensorUnits.WIDEBAND_AFR_METHANOL64;

public final class PlxConvertorImpl implements PlxConvertor {
    public double convert(int raw, PlxSensorType sensorType, PlxSensorUnits units) {
        if (sensorType == WIDEBAND_AFR) {
            if (units == WIDEBAND_AFR_LAMBDA) return (raw / 3.75 + 68) / 100;
            if (units == WIDEBAND_AFR_GASOLINE147) return (raw / 2.55 + 100) / 10;
            if (units == WIDEBAND_AFR_DIESEL146) return (raw / 2.58 + 100) / 10;
            if (units == WIDEBAND_AFR_METHANOL64) return (raw / 5.856 + 43.5) / 10;
            if (units == WIDEBAND_AFR_ETHANOL90) return (raw / 4.167 + 61.7) / 10;
            if (units == WIDEBAND_AFR_LPG155) return (raw / 2.417 + 105.6) / 10;
            if (units == WIDEBAND_AFR_CNG172) return (raw / 2.18 + 117) / 10;
        }
        if (sensorType == EXHAUST_GAS_TEMPERATURE) {
            if (units == EXHAUST_GAS_TEMPERATURE_CELSIUS) return raw;
            if (units == EXHAUST_GAS_TEMPERATURE_FAHRENHEIT) return (raw / .555 + 32);
        }
        return 0.0;

/* To be supported in the future... maybe..

else if (sensorType == PlxSensorType.FLUID_TEMPERATURE)
{
    if (units == 0) //Degrees Celsius Water
        return raw;
    else if (units == 1) //Degrees Fahrenheit Water
        return (raw / .555 + 32);
    else if (units == 2) //Degrees Celsius Oil
        return raw;
    else if (units == 3) //Degrees Fahrenheit Oil
        return (raw / .555 + 32);
}
else if (sensorType == PlxSensorType.VACUUM) //Vac
{
    if (units == 0) //in/Hg (inch Mercury)
        return -(raw / 11.39 - 29.93);
    else if (units == 1) //mm/Hg (millimeters Mercury)
        return -(raw * 2.23 + 760.4);
}
else if (sensorType == PlxSensorType.BOOST) //Boost
{
    if (units == 0) //0-30 PSI
        return raw / 22.73;
    else if (units == 1) //0-2 kg/cm^2
        return raw / 329.47;
    else if (units == 2) //0-15 PSI
        return raw / 22.73;
    else if (units == 3) //0-1 kg/cm^2
        return raw / 329.47;
    else if (units == 4) //0-60 PSI
        return raw / 22.73;
    else if (units == 5) //0-4 kg/cm^2
        return raw / 329.47;
}
else if (sensorType == PlxSensorType.AIR_INTAKE_TEMPERATURE) //AIT
{
    if (units == 0) //Celsius
        return raw;
    else if (units == 1) //Fahrenheit
        return (raw / .555 + 32);
}
else if (sensorType == PlxSensorType.RPM) //RPM
{
    return raw * 19.55; //RPM
}
else if (sensorType == PlxSensorType.VEHICLE_SPEED) //Speed
{
    if (units == 0) //MPH
        return raw / 6.39;
    else if (units == 1) //KMH
        return raw / 3.97;
}
else if (sensorType == PlxSensorType.THROTTLE_POSITION) //TPS
{
    return raw; //Throttle Position %
}
else if (sensorType == PlxSensorType.ENGINE_LOAD) //Engine Load
{
    return raw; //Engine Load %
}
else if (sensorType == PlxSensorType.FLUID_PRESSURE) //Fluid Pressure
{
    if (units == 0) //PSI Fuel
        return raw / 5.115;
    else if (units == 1) //kg/cm^2 Fuel
        return raw / 72.73;
    else if (units == 2) //Bar Fuel
        return raw / 74.22;
    else if (units == 3) //PSI Oil
        return raw / 5.115;
    else if (units == 4) //kg/cm^2 Oil
        return raw / 72.73;
    else if (units == 5) //Bar Oil
        return raw / 74.22;
}
else if (sensorType == PlxSensorType.TIMING) //Engine timing
{
    return raw - 64; //Degree Timing
}
else if (sensorType == PlxSensorType.MANIFOLD_ABSOLUTE_PRESSURE) //MAP
{
    if (units == 0) //kPa
        return raw;
    else if (units == 1) //inHg
        return raw / 3.386;
}
else if (sensorType == PlxSensorType.MASS_AIR_FLOW) //MAF
{
    if (units == 0) //g/s (grams per second)
        return raw;
    else if (units == 1) //lb/min (pounds per minute)
        return raw / 7.54;
}
else if (sensorType == PlxSensorType.SHORT_TERM_FUEL_TRIM) //Short term fuel trim
{
    return raw - 100; //Fuel trim %
}
else if (sensorType == PlxSensorType.LONG_TERM_FUEL_TRIM) //Long term fuel trim
{
    return raw - 100; //Fuel trim %
}
else if (sensorType == PlxSensorType.NARROWBAND_AFR) //Narrowband O2 sensor
{
    if (units == 0) //Percent
        return raw;
    else if (units == 1) //Volts
        return raw / 78.43;
}
else if (sensorType == PlxSensorType.FUEL_LEVEL) //Fuel level
{
    return raw; //Fuel Level %
}
else if (sensorType == PlxSensorType.VOLTAGE) //Volts
{
    return raw / 51.15; //Volt Meter Volts
}
else if (sensorType == PlxSensorType.KNOCK) //Knock
{
    return raw / 204.6; //Knock volts 0-5
}
else if (sensorType == PlxSensorType.DUTY_CYCLE) //Duty cycle
{
    if (units == 0) //Positive Duty
        return raw / 10.23;
    else if (units == 1) //Negative Duty
        return 100 - (raw / 10.23);
}
return 0.0;
*/
    }
}