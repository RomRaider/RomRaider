/*
 *
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
 *
 */

package com.romraider.logger.ecu.ui.tab.injector;

import com.romraider.logger.ecu.definition.EcuParameter;
import com.romraider.logger.ecu.definition.EcuSwitch;
import com.romraider.logger.ecu.definition.ExternalData;
import com.romraider.logger.ecu.ui.tab.Tab;
import java.util.List;

public interface InjectorTab extends Tab {

    double getFuelStoichAfr();

    double getFuelDensity();

    boolean isRecordData();

    boolean isValidClOl(double value);

    boolean isValidAfr(double value);

    boolean isValidRpm(double value);

    boolean isValidMaf(double value);

    boolean isValidCoolantTemp(double value);

    boolean isValidIntakeAirTemp(double value);

    boolean isValidTipInThrottle(double value);

    void addData(double mafv, double correction);

    void setEcuParams(List<EcuParameter> params);

    void setEcuSwitches(List<EcuSwitch> switches);

    void setExternalDatas(List<ExternalData> external);
}