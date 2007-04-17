/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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

package enginuity.logger.ecu.definition;

import static enginuity.logger.ecu.definition.EcuDataType.SWITCH;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class EcuSwitchImpl implements EcuSwitch {
    private final String id;
    private final String name;
    private final String description;
    private final EcuAddress address;
    private final EcuDataConvertor[] convertors;
    private int selectedConvertorIndex = 0;
    private boolean fileLogController;

    public EcuSwitchImpl(String id, String name, String description, EcuAddress address, EcuDataConvertor[] convertors) {
        checkNotNullOrEmpty(id, "id");
        checkNotNullOrEmpty(name, "name");
        checkNotNull(description, "description");
        checkNotNull(address, "address");
        checkNotNullOrEmpty(convertors, "convertors");
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.convertors = convertors;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EcuAddress getAddress() {
        return address;
    }

    public EcuDataConvertor getSelectedConvertor() {
        return convertors[selectedConvertorIndex];
    }

    public EcuDataConvertor[] getConvertors() {
        return convertors;
    }

    public void selectConvertor(EcuDataConvertor convertor) {
        for (int i = 0; i < convertors.length; i++) {
            EcuDataConvertor dataConvertor = convertors[i];
            if (convertor == dataConvertor) {
                selectedConvertorIndex = i;
            }
        }
    }

    public EcuDataType getDataType() {
        return SWITCH;
    }

    public void setFileLogController(boolean fileLogController) {
        this.fileLogController = fileLogController;
    }

    public boolean isFileLogController() {
        return fileLogController;
    }
}
